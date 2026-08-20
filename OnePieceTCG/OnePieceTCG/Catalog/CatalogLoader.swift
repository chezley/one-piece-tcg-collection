import Foundation
import SwiftData
import os

/// Decodable shape of a bundled catalog-set JSON file (see `OP01.json`).
struct CatalogCardDTO: Decodable {
    let id: String
    let name: String
    let cardNumber: String
    let rarity: String
    let cost: Int?
    let power: Int?
    let attribute: String?
    let type: String?
    let imageURL: String?
}

struct CatalogSetDTO: Decodable {
    let code: String
    let name: String
}

struct CatalogDataset: Decodable {
    let set: CatalogSetDTO
    let cards: [CatalogCardDTO]
}

enum CatalogLoaderError: Error {
    case resourceNotFound(String)
}

/// Discovers every bundled catalog-set JSON file and seeds the local
/// `Card`/`CardSet` catalog from all of them, so shipping an additional set
/// is just adding another JSON file to the bundle — no loader changes.
///
/// Seeding is idempotent per set: cards already present (matched by `id`)
/// are left untouched, so relaunching, or adding a new set file in a future
/// build, only inserts what's missing and never duplicates or resets
/// existing owned-card data. A set file that fails to load or decode is
/// logged and skipped rather than blocking the other sets or crashing.
enum CatalogLoader {
    private static let logger = Logger(subsystem: "com.chezley.onepiecetcg", category: "CatalogLoader")

    /// Base names (without extension) of every catalog-set JSON file bundled
    /// in `bundle`, sorted for a deterministic seeding order.
    static func discoverDatasetResourceNames(bundle: Bundle) -> [String] {
        let urls = bundle.urls(forResourcesWithExtension: "json", subdirectory: nil) ?? []
        return urls.map { $0.deletingPathExtension().lastPathComponent }.sorted()
    }

    static func loadDataset(resourceName: String, bundle: Bundle) throws -> CatalogDataset {
        guard let url = bundle.url(forResource: resourceName, withExtension: "json") else {
            throw CatalogLoaderError.resourceNotFound(resourceName)
        }
        let data = try Data(contentsOf: url)
        return try JSONDecoder().decode(CatalogDataset.self, from: data)
    }

    /// Seeds the catalog from every set file discovered in `bundle`. Returns
    /// the total number of new cards inserted across all sets.
    ///
    /// Fetches the existing `CardSet`/`Card` IDs from `context` once up
    /// front and keeps them updated in memory across sets, rather than
    /// re-fetching both full tables from the store for every set file — with
    /// N bundled sets that turned an O(N) seed pass into an O(N x
    /// total-cards) one.
    @discardableResult
    static func seedCatalog(into context: ModelContext, bundle: Bundle = .main) throws -> Int {
        var existingSetCodes = Set(try context.fetch(FetchDescriptor<CardSet>()).map(\.code))
        var existingCardIDs = Set(try context.fetch(FetchDescriptor<Card>()).map(\.id))

        var totalInserted = 0
        for resourceName in discoverDatasetResourceNames(bundle: bundle) {
            do {
                totalInserted += try seedDataset(
                    resourceName: resourceName,
                    into: context,
                    bundle: bundle,
                    existingSetCodes: &existingSetCodes,
                    existingCardIDs: &existingCardIDs
                )
            } catch {
                logger.error("Skipping catalog set '\(resourceName, privacy: .public)': \(String(describing: error), privacy: .public)")
            }
        }
        return totalInserted
    }

    /// Seeds the catalog from a single named set file. Exposed separately
    /// from `seedCatalog` so a specific known set can be (re-)seeded, and so
    /// tests can exercise one dataset without going through discovery.
    @discardableResult
    static func seedDataset(resourceName: String, into context: ModelContext, bundle: Bundle = .main) throws -> Int {
        var existingSetCodes = Set(try context.fetch(FetchDescriptor<CardSet>()).map(\.code))
        var existingCardIDs = Set(try context.fetch(FetchDescriptor<Card>()).map(\.id))
        return try seedDataset(
            resourceName: resourceName,
            into: context,
            bundle: bundle,
            existingSetCodes: &existingSetCodes,
            existingCardIDs: &existingCardIDs
        )
    }

    /// Core seed step shared by `seedCatalog`/`seedDataset`. Takes the
    /// existing set-code/card-id membership as inout sets so a caller
    /// seeding multiple files (`seedCatalog`) can fetch them once and keep
    /// this step's inserts reflected in memory for the next file, instead of
    /// re-querying the store every time.
    private static func seedDataset(
        resourceName: String,
        into context: ModelContext,
        bundle: Bundle,
        existingSetCodes: inout Set<String>,
        existingCardIDs: inout Set<String>
    ) throws -> Int {
        let dataset = try loadDataset(resourceName: resourceName, bundle: bundle)

        let isNewSet = !existingSetCodes.contains(dataset.set.code)
        if isNewSet {
            context.insert(CardSet(code: dataset.set.code, name: dataset.set.name))
            existingSetCodes.insert(dataset.set.code)
        }

        let newCards = dataset.cards.filter { !existingCardIDs.contains($0.id) }
        for dto in newCards {
            context.insert(
                Card(
                    id: dto.id,
                    name: dto.name,
                    setCode: dataset.set.code,
                    cardNumber: dto.cardNumber,
                    rarity: dto.rarity,
                    cost: dto.cost,
                    power: dto.power,
                    attribute: dto.attribute,
                    type: dto.type,
                    imageURL: dto.imageURL
                )
            )
            existingCardIDs.insert(dto.id)
        }

        if !newCards.isEmpty || isNewSet {
            try context.save()
        }
        return newCards.count
    }
}

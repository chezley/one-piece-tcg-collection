import XCTest
import SwiftData
@testable import OnePieceTCG

final class CatalogLoaderTests: XCTestCase {
    private var container: ModelContainer!
    private var context: ModelContext!

    override func setUpWithError() throws {
        container = PersistenceController.makeContainer(inMemory: true)
        context = ModelContext(container)
    }

    override func tearDownWithError() throws {
        container = nil
        context = nil
    }

    func testDatasetParsesWithoutError() throws {
        let dataset = try CatalogLoader.loadDataset(resourceName: "OP01", bundle: .main)
        XCTAssertFalse(dataset.cards.isEmpty)
    }

    func testDatasetCardCountMatchesSourceData() throws {
        let dataset = try CatalogLoader.loadDataset(resourceName: "OP01", bundle: .main)
        XCTAssertEqual(dataset.cards.count, 121)
        XCTAssertEqual(dataset.set.code, "OP01")
    }

    func testKnownCardsHaveExpectedFields() throws {
        let dataset = try CatalogLoader.loadDataset(resourceName: "OP01", bundle: .main)
        let cardsByID = Dictionary(uniqueKeysWithValues: dataset.cards.map { ($0.id, $0) })

        let zoro = try XCTUnwrap(cardsByID["OP01-001"])
        XCTAssertEqual(zoro.name, "Roronoa Zoro")
        XCTAssertEqual(zoro.rarity, "L")
        XCTAssertEqual(zoro.type, "Leader")
        XCTAssertEqual(zoro.cost, 5)
        XCTAssertEqual(zoro.power, 5000)

        let luffy = try XCTUnwrap(cardsByID["OP01-003"])
        XCTAssertEqual(luffy.name, "Monkey.D.Luffy")
        XCTAssertEqual(luffy.type, "Leader")
    }

    func testSeedingSingleDatasetInsertsCardsAndSet() throws {
        let insertedCount = try CatalogLoader.seedDataset(resourceName: "OP01", into: context, bundle: .main)

        XCTAssertEqual(insertedCount, 121)
        let cards = try context.fetch(FetchDescriptor<Card>())
        XCTAssertEqual(cards.count, 121)
        let sets = try context.fetch(FetchDescriptor<CardSet>())
        XCTAssertEqual(sets.map(\.code), ["OP01"])
    }

    func testSeedingSingleDatasetTwiceDoesNotDuplicate() throws {
        try CatalogLoader.seedDataset(resourceName: "OP01", into: context, bundle: .main)
        let secondRunInsertedCount = try CatalogLoader.seedDataset(resourceName: "OP01", into: context, bundle: .main)

        XCTAssertEqual(secondRunInsertedCount, 0)
        let cards = try context.fetch(FetchDescriptor<Card>())
        XCTAssertEqual(cards.count, 121)
        let sets = try context.fetch(FetchDescriptor<CardSet>())
        XCTAssertEqual(sets.count, 1)
    }

    // MARK: - Multi-set discovery (synthetic bundles)
    //
    // These build throwaway bundles from fixture JSON on disk (rather than
    // adding fixture files to the app target) so multi-set/failure scenarios
    // can be exercised without touching the Xcode project's resource list.

    func testDiscoveryFindsEverySetFileInABundle() throws {
        let bundle = try makeSyntheticBundle(datasets: [
            fixture(setCode: "TS1", setName: "Test Set One", cardIDs: ["TS1-001", "TS1-002"]),
            fixture(setCode: "TS2", setName: "Test Set Two", cardIDs: ["TS2-001"])
        ])

        XCTAssertEqual(CatalogLoader.discoverDatasetResourceNames(bundle: bundle), ["TS1", "TS2"])
    }

    func testSeedingCatalogAcrossMultipleSetFilesCombinesCardCounts() throws {
        let bundle = try makeSyntheticBundle(datasets: [
            fixture(setCode: "TS1", setName: "Test Set One", cardIDs: ["TS1-001", "TS1-002"]),
            fixture(setCode: "TS2", setName: "Test Set Two", cardIDs: ["TS2-001"])
        ])

        let insertedCount = try CatalogLoader.seedCatalog(into: context, bundle: bundle)

        XCTAssertEqual(insertedCount, 3)
        let cards = try context.fetch(FetchDescriptor<Card>())
        XCTAssertEqual(Set(cards.map(\.id)), ["TS1-001", "TS1-002", "TS2-001"])
        let sets = try context.fetch(FetchDescriptor<CardSet>())
        XCTAssertEqual(Set(sets.map(\.code)), ["TS1", "TS2"])
    }

    func testAddingANewSetFileOnASimulatedSecondLaunchOnlyAddsTheNewCards() throws {
        let firstLaunchBundle = try makeSyntheticBundle(datasets: [
            fixture(setCode: "TS1", setName: "Test Set One", cardIDs: ["TS1-001", "TS1-002"])
        ])
        try CatalogLoader.seedCatalog(into: context, bundle: firstLaunchBundle)

        let secondLaunchBundle = try makeSyntheticBundle(datasets: [
            fixture(setCode: "TS1", setName: "Test Set One", cardIDs: ["TS1-001", "TS1-002"]),
            fixture(setCode: "TS2", setName: "Test Set Two", cardIDs: ["TS2-001"])
        ])
        let secondLaunchInsertedCount = try CatalogLoader.seedCatalog(into: context, bundle: secondLaunchBundle)

        XCTAssertEqual(secondLaunchInsertedCount, 1)
        let cards = try context.fetch(FetchDescriptor<Card>())
        XCTAssertEqual(Set(cards.map(\.id)), ["TS1-001", "TS1-002", "TS2-001"])
        let sets = try context.fetch(FetchDescriptor<CardSet>())
        XCTAssertEqual(Set(sets.map(\.code)), ["TS1", "TS2"])
    }

    func testAMalformedSetFileIsLoggedAndSkippedWithoutBlockingTheOthers() throws {
        let bundle = try makeSyntheticBundle(
            datasets: [fixture(setCode: "TS1", setName: "Test Set One", cardIDs: ["TS1-001"])],
            extraFiles: ["Broken.json": Data("{ this is not valid json".utf8)]
        )

        let insertedCount = try CatalogLoader.seedCatalog(into: context, bundle: bundle)

        XCTAssertEqual(insertedCount, 1)
        let cards = try context.fetch(FetchDescriptor<Card>())
        XCTAssertEqual(cards.map(\.id), ["TS1-001"])
        let sets = try context.fetch(FetchDescriptor<CardSet>())
        XCTAssertEqual(sets.map(\.code), ["TS1"])
    }

    // MARK: - Fixture helpers

    private func fixture(setCode: String, setName: String, cardIDs: [String]) -> (name: String, data: Data) {
        let cards = cardIDs
            .map { "{ \"id\": \"\($0)\", \"name\": \"\($0)\", \"cardNumber\": \"\($0)\", \"rarity\": \"C\" }" }
            .joined(separator: ",")
        let json = "{ \"set\": { \"code\": \"\(setCode)\", \"name\": \"\(setName)\" }, \"cards\": [\(cards)] }"
        return (setCode, Data(json.utf8))
    }

    private func makeSyntheticBundle(
        datasets: [(name: String, data: Data)],
        extraFiles: [String: Data] = [:]
    ) throws -> Bundle {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        for dataset in datasets {
            try dataset.data.write(to: directory.appendingPathComponent("\(dataset.name).json"))
        }
        for (fileName, data) in extraFiles {
            try data.write(to: directory.appendingPathComponent(fileName))
        }
        return try XCTUnwrap(Bundle(url: directory))
    }
}

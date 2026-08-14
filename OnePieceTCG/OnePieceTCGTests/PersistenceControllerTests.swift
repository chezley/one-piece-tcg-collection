import XCTest
import SwiftData
@testable import OnePieceTCG

final class PersistenceControllerTests: XCTestCase {
    private func removeStoreFiles(at url: URL) {
        let fileManager = FileManager.default
        for suffix in ["", "-wal", "-shm"] {
            try? fileManager.removeItem(at: URL(fileURLWithPath: url.path + suffix))
        }
    }

    /// Regression test for #18: a corrupted/unopenable store must not crash
    /// `makeContainer` — it should recover by recreating a fresh store.
    func testMakeContainerRecoversFromCorruptedStoreInsteadOfCrashing() throws {
        let storeURL = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString)
            .appendingPathExtension("store")
        defer { removeStoreFiles(at: storeURL) }

        // Not a valid SQLite store, so ModelContainer's first open attempt fails.
        try Data("not a valid sqlite store".utf8).write(to: storeURL)

        let container = PersistenceController.makeContainer(url: storeURL)
        let context = ModelContext(container)
        let repository = SwiftDataCardRepository(modelContext: context)

        // The corrupted store was replaced with a fresh, empty one.
        XCTAssertTrue(try repository.fetchOwnedCards().isEmpty)

        // The recovered container is fully usable afterwards.
        let card = Card(id: "OP01-001", name: "Monkey D. Luffy", setCode: "OP01", cardNumber: "OP01-001", rarity: "L")
        context.insert(card)
        try repository.addOwnedCard(card, quantity: 1, condition: .nearMint)

        XCTAssertEqual(try repository.fetchOwnedCards().count, 1)
    }

    func testMakeContainerStillOpensAHealthyExistingStore() throws {
        let storeURL = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString)
            .appendingPathExtension("store")
        defer { removeStoreFiles(at: storeURL) }

        do {
            let firstContainer = PersistenceController.makeContainer(url: storeURL)
            let firstContext = ModelContext(firstContainer)
            let firstRepository = SwiftDataCardRepository(modelContext: firstContext)
            let card = Card(id: "OP01-001", name: "Monkey D. Luffy", setCode: "OP01", cardNumber: "OP01-001", rarity: "L")
            firstContext.insert(card)
            try firstRepository.addOwnedCard(card, quantity: 2, condition: .nearMint)
        }

        // A second, healthy open of the same store must not be treated as
        // corrupted and wiped — the data from above must still be there.
        let secondContainer = PersistenceController.makeContainer(url: storeURL)
        let secondRepository = SwiftDataCardRepository(modelContext: ModelContext(secondContainer))

        let owned = try secondRepository.fetchOwnedCards()
        XCTAssertEqual(owned.count, 1)
        XCTAssertEqual(owned.first?.quantity, 2)
    }
}

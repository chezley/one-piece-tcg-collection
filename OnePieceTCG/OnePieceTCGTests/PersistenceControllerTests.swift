import XCTest
import SwiftData
@testable import OnePieceTCG

final class PersistenceControllerTests: XCTestCase {
    private func makeStoreURL() -> URL {
        FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString)
            .appendingPathExtension("store")
    }

    func testMakeContainerRecoversFromCorruptedStoreInsteadOfCrashing() throws {
        let storeURL = makeStoreURL()
        defer { try? FileManager.default.removeItem(at: storeURL) }

        // Simulate a corrupted/unreadable store file already sitting on disk.
        try Data("not a valid SwiftData store".utf8).write(to: storeURL)

        let container = PersistenceController.makeContainer(url: storeURL)

        // The container must be usable: a fresh store was created in place
        // of the corrupted one rather than the process crashing.
        let context = ModelContext(container)
        let repository = SwiftDataCardRepository(modelContext: context)
        let card = Card(id: "OP01-001", name: "Monkey D. Luffy", setCode: "OP01", cardNumber: "OP01-001", rarity: "L")
        context.insert(card)
        try repository.addOwnedCard(card, quantity: 1, condition: .nearMint)

        XCTAssertEqual(try repository.fetchOwnedCards().count, 1)
    }

    func testMakeContainerStillOpensAHealthyExistingStore() throws {
        let storeURL = makeStoreURL()
        defer { try? FileManager.default.removeItem(at: storeURL) }

        do {
            let firstLaunchContainer = PersistenceController.makeContainer(url: storeURL)
            let firstLaunchContext = ModelContext(firstLaunchContainer)
            let firstLaunchRepository = SwiftDataCardRepository(modelContext: firstLaunchContext)

            let card = Card(id: "OP01-001", name: "Monkey D. Luffy", setCode: "OP01", cardNumber: "OP01-001", rarity: "L")
            firstLaunchContext.insert(card)
            try firstLaunchRepository.addOwnedCard(card, quantity: 2, condition: .nearMint)
        }

        // Reopening a healthy store must not wipe it, even though
        // makeContainer is willing to delete-and-retry on a failed open.
        let secondLaunchContainer = PersistenceController.makeContainer(url: storeURL)
        let secondLaunchContext = ModelContext(secondLaunchContainer)
        let secondLaunchRepository = SwiftDataCardRepository(modelContext: secondLaunchContext)

        let owned = try secondLaunchRepository.fetchOwnedCards()
        XCTAssertEqual(owned.count, 1)
        XCTAssertEqual(owned.first?.quantity, 2)
    }

    func testMakeContainerFallsBackToInMemoryWhenEvenAFreshStoreCannotBeCreated() throws {
        // Point at a path whose parent directory doesn't exist and can't be
        // created implicitly (a file, not a directory, sits where a parent
        // directory is expected) so both the initial open and the
        // delete-and-retry attempt fail, forcing the in-memory fallback.
        let blockingFile = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString)
        try Data("blocking file".utf8).write(to: blockingFile)
        defer { try? FileManager.default.removeItem(at: blockingFile) }

        let storeURL = blockingFile.appendingPathComponent("nested").appendingPathExtension("store")

        // Must not crash: falls back to a usable in-memory container.
        let container = PersistenceController.makeContainer(url: storeURL)
        let context = ModelContext(container)
        let repository = SwiftDataCardRepository(modelContext: context)
        let card = Card(id: "OP01-001", name: "Monkey D. Luffy", setCode: "OP01", cardNumber: "OP01-001", rarity: "L")
        context.insert(card)
        try repository.addOwnedCard(card, quantity: 1, condition: .nearMint)

        XCTAssertEqual(try repository.fetchOwnedCards().count, 1)
    }
}

import XCTest
import SwiftData
@testable import OnePieceTCG

final class CardRepositoryTests: XCTestCase {
    private var container: ModelContainer!
    private var context: ModelContext!
    private var repository: SwiftDataCardRepository!

    override func setUpWithError() throws {
        container = PersistenceController.makeContainer(inMemory: true)
        context = ModelContext(container)
        repository = SwiftDataCardRepository(modelContext: context)
    }

    override func tearDownWithError() throws {
        container = nil
        context = nil
        repository = nil
    }

    private func makeCard(id: String = "OP01-001", setCode: String = "OP01") -> Card {
        Card(id: id, name: "Monkey D. Luffy", setCode: setCode, cardNumber: id, rarity: "L")
    }

    func testAddingOwnedCardCreatesEntry() throws {
        let card = makeCard()
        context.insert(card)

        try repository.addOwnedCard(card, quantity: 2, condition: .nearMint)

        let owned = try repository.fetchOwnedCards()
        XCTAssertEqual(owned.count, 1)
        XCTAssertEqual(owned.first?.quantity, 2)
        XCTAssertEqual(owned.first?.card?.id, card.id)
    }

    func testAddingOwnedCardTwiceIncrementsQuantityInsteadOfDuplicating() throws {
        let card = makeCard()
        context.insert(card)

        try repository.addOwnedCard(card, quantity: 1, condition: .nearMint)
        try repository.addOwnedCard(card, quantity: 2, condition: .nearMint)

        let owned = try repository.fetchOwnedCards()
        XCTAssertEqual(owned.count, 1)
        XCTAssertEqual(owned.first?.quantity, 3)
    }

    func testRemovingOwnedCard() throws {
        let card = makeCard()
        context.insert(card)
        let ownedCard = try repository.addOwnedCard(card, quantity: 1, condition: .nearMint)

        try repository.removeOwnedCard(ownedCard)

        XCTAssertTrue(try repository.fetchOwnedCards().isEmpty)
    }

    func testUpdatingQuantityToZeroRemovesTheOwnedCard() throws {
        let card = makeCard()
        context.insert(card)
        let ownedCard = try repository.addOwnedCard(card, quantity: 3, condition: .nearMint)

        try repository.updateOwnedCard(ownedCard, quantity: 0)

        XCTAssertTrue(try repository.fetchOwnedCards().isEmpty)
    }

    func testFetchCardsBySet() throws {
        let op01Card = makeCard(id: "OP01-001", setCode: "OP01")
        let op02Card = makeCard(id: "OP02-001", setCode: "OP02")
        context.insert(op01Card)
        context.insert(op02Card)

        let result = try repository.fetchCards(inSet: "OP01")

        XCTAssertEqual(result.map(\.id), ["OP01-001"])
    }

    func testDataPersistsAcrossRelaunchUsingFileBackedStore() throws {
        let storeURL = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString)
            .appendingPathExtension("store")
        defer { try? FileManager.default.removeItem(at: storeURL) }

        do {
            let firstLaunchContainer = PersistenceController.makeContainer(url: storeURL)
            let firstLaunchContext = ModelContext(firstLaunchContainer)
            let firstLaunchRepository = SwiftDataCardRepository(modelContext: firstLaunchContext)

            let card = makeCard()
            firstLaunchContext.insert(card)
            try firstLaunchRepository.addOwnedCard(card, quantity: 4, condition: .lightlyPlayed)
        }

        let secondLaunchContainer = PersistenceController.makeContainer(url: storeURL)
        let secondLaunchContext = ModelContext(secondLaunchContainer)
        let secondLaunchRepository = SwiftDataCardRepository(modelContext: secondLaunchContext)

        let owned = try secondLaunchRepository.fetchOwnedCards()
        XCTAssertEqual(owned.count, 1)
        XCTAssertEqual(owned.first?.quantity, 4)
        XCTAssertEqual(owned.first?.condition, .lightlyPlayed)
    }
}

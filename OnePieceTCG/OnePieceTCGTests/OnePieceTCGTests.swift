import XCTest
@testable import OnePieceTCG

final class OnePieceTCGTests: XCTestCase {
    func testRootTabViewInstantiates() throws {
        _ = RootTabView()
    }

    func testPlaceholderScreensInstantiate() throws {
        _ = BrowseView()
        _ = CollectionView()
        _ = StatsView()
        _ = SettingsView()
    }

    func testCardDetailViewInstantiates() throws {
        let card = Card(id: "OP01-001", name: "Monkey D. Luffy", setCode: "OP01", cardNumber: "OP01-001", rarity: "L")
        _ = CardDetailView(card: card)
    }
}

/// Covers `CollectionSort` (#7), the filter/sort logic behind the
/// Collection tab, exercised directly against plain model instances so it
/// doesn't need a `ModelContext`.
final class CollectionSortTests: XCTestCase {
    private func makeOwnedCard(setCode: String, quantity: Int) -> OwnedCard {
        let card = Card(id: "\(setCode)-\(quantity)-\(UUID())", name: "Test Card", setCode: setCode, cardNumber: "001", rarity: "C")
        return OwnedCard(card: card, quantity: quantity)
    }

    func testApplySortsDescendingByDefault() {
        let low = makeOwnedCard(setCode: "OP01", quantity: 1)
        let high = makeOwnedCard(setCode: "OP01", quantity: 5)

        let result = CollectionSort.apply(to: [low, high], setFilter: nil, sort: .quantityDescending)

        XCTAssertEqual(result.map(\.quantity), [5, 1])
    }

    func testApplySortsAscendingWhenRequested() {
        let low = makeOwnedCard(setCode: "OP01", quantity: 1)
        let high = makeOwnedCard(setCode: "OP01", quantity: 5)

        let result = CollectionSort.apply(to: [high, low], setFilter: nil, sort: .quantityAscending)

        XCTAssertEqual(result.map(\.quantity), [1, 5])
    }

    func testApplyFiltersBySetCode() {
        let op01 = makeOwnedCard(setCode: "OP01", quantity: 2)
        let op02 = makeOwnedCard(setCode: "OP02", quantity: 3)

        let result = CollectionSort.apply(to: [op01, op02], setFilter: "OP01", sort: .quantityDescending)

        XCTAssertEqual(result.map { $0.card?.setCode }, ["OP01"])
    }

    func testApplyWithNoFilterReturnsAllSets() {
        let op01 = makeOwnedCard(setCode: "OP01", quantity: 2)
        let op02 = makeOwnedCard(setCode: "OP02", quantity: 3)

        let result = CollectionSort.apply(to: [op01, op02], setFilter: nil, sort: .quantityDescending)

        XCTAssertEqual(result.count, 2)
    }

    func testAvailableSetCodesAreUniqueAndSorted() {
        let op01a = makeOwnedCard(setCode: "OP01", quantity: 1)
        let op01b = makeOwnedCard(setCode: "OP01", quantity: 2)
        let op02 = makeOwnedCard(setCode: "OP02", quantity: 3)

        let codes = CollectionSort.availableSetCodes(in: [op01a, op01b, op02])

        XCTAssertEqual(codes, ["OP01", "OP02"])
    }
}

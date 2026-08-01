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

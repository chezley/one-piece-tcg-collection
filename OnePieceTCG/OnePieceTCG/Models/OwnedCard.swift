import Foundation
import SwiftData

/// A card the user owns: a reference to a `Card` plus how many copies,
/// in what condition, and when it was added to the collection.
@Model
final class OwnedCard {
    var card: Card?
    var quantity: Int
    var condition: CardCondition
    var dateAdded: Date

    init(card: Card, quantity: Int = 1, condition: CardCondition = .nearMint, dateAdded: Date = Date()) {
        self.card = card
        self.quantity = quantity
        self.condition = condition
        self.dateAdded = dateAdded
    }
}

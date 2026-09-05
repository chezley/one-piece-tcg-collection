import Foundation
import SwiftData

/// A card the user owns: a reference to a `Card` plus how many copies,
/// in what condition, and when it was added to the collection.
@Model
final class OwnedCard {
    // Stable tiebreaker for SortDescriptor: persistentModelID isn't usable as
    // a KeyPath, so without this, fetches sorted only by name/dateAdded have
    // no deterministic order among ties (see issue #33).
    var id: UUID
    var card: Card?
    var quantity: Int
    var condition: CardCondition
    var dateAdded: Date

    init(id: UUID = UUID(), card: Card, quantity: Int = 1, condition: CardCondition = .nearMint, dateAdded: Date = Date()) {
        self.id = id
        self.card = card
        self.quantity = quantity
        self.condition = condition
        self.dateAdded = dateAdded
    }
}

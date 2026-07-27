import Foundation
import SwiftData

/// A single card in the catalog, e.g. "Monkey D. Luffy" (OP01-001).
@Model
final class Card {
    @Attribute(.unique) var id: String
    var name: String
    var setCode: String
    var cardNumber: String
    var rarity: String
    var cost: Int?
    var power: Int?
    var attribute: String?
    var type: String?
    var imageURL: String?

    init(
        id: String,
        name: String,
        setCode: String,
        cardNumber: String,
        rarity: String,
        cost: Int? = nil,
        power: Int? = nil,
        attribute: String? = nil,
        type: String? = nil,
        imageURL: String? = nil
    ) {
        self.id = id
        self.name = name
        self.setCode = setCode
        self.cardNumber = cardNumber
        self.rarity = rarity
        self.cost = cost
        self.power = power
        self.attribute = attribute
        self.type = type
        self.imageURL = imageURL
    }
}

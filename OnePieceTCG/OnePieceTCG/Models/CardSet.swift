import Foundation
import SwiftData

/// A released card set, e.g. "OP-01 Romance Dawn".
@Model
final class CardSet {
    @Attribute(.unique) var code: String
    var name: String
    var releaseDate: Date?

    init(code: String, name: String, releaseDate: Date? = nil) {
        self.code = code
        self.name = name
        self.releaseDate = releaseDate
    }
}

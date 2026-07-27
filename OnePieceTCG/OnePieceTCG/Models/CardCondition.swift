import Foundation

/// Physical condition of a physical card copy, used by `OwnedCard`.
enum CardCondition: String, Codable, CaseIterable, Identifiable {
    case nearMint = "Near Mint"
    case lightlyPlayed = "Lightly Played"
    case moderatelyPlayed = "Moderately Played"
    case heavilyPlayed = "Heavily Played"
    case damaged = "Damaged"

    var id: String { rawValue }
}

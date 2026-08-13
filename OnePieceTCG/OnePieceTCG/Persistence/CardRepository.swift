import Foundation
import SwiftData

/// Repository API for cards and owned cards. UI code depends on this
/// protocol, never on SwiftData directly, so the persistence framework can
/// be swapped without touching views.
protocol CardRepository {
    func fetchAllCards() throws -> [Card]
    func fetchCards(inSet setCode: String) throws -> [Card]
    func fetchOwnedCards() throws -> [OwnedCard]

    @discardableResult
    func addOwnedCard(_ card: Card, quantity: Int, condition: CardCondition) throws -> OwnedCard

    func updateOwnedCard(_ ownedCard: OwnedCard, quantity: Int) throws
    func removeOwnedCard(_ ownedCard: OwnedCard) throws
}

final class SwiftDataCardRepository: CardRepository {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func fetchAllCards() throws -> [Card] {
        try modelContext.fetch(FetchDescriptor<Card>(sortBy: [SortDescriptor(\.name)]))
    }

    func fetchCards(inSet setCode: String) throws -> [Card] {
        let descriptor = FetchDescriptor<Card>(
            predicate: #Predicate { $0.setCode == setCode },
            sortBy: [SortDescriptor(\.cardNumber)]
        )
        return try modelContext.fetch(descriptor)
    }

    func fetchOwnedCards() throws -> [OwnedCard] {
        try modelContext.fetch(FetchDescriptor<OwnedCard>(sortBy: [SortDescriptor(\.dateAdded)]))
    }

    @discardableResult
    func addOwnedCard(_ card: Card, quantity: Int = 1, condition: CardCondition = .nearMint) throws -> OwnedCard {
        // Different physical conditions of the same card are tracked as separate
        // rows, so the lookup must match on condition too — otherwise a copy
        // added in one condition silently merges into an existing row that's
        // actually a different condition, mislabeling it.
        if let existing = try existingOwnedCard(for: card, condition: condition) {
            existing.quantity += quantity
            try modelContext.save()
            return existing
        }

        let ownedCard = OwnedCard(card: card, quantity: quantity, condition: condition)
        modelContext.insert(ownedCard)
        try modelContext.save()
        return ownedCard
    }

    func updateOwnedCard(_ ownedCard: OwnedCard, quantity: Int) throws {
        guard quantity > 0 else {
            try removeOwnedCard(ownedCard)
            return
        }
        ownedCard.quantity = quantity
        try modelContext.save()
    }

    func removeOwnedCard(_ ownedCard: OwnedCard) throws {
        modelContext.delete(ownedCard)
        try modelContext.save()
    }

    private func existingOwnedCard(for card: Card, condition: CardCondition) throws -> OwnedCard? {
        let cardID = card.id
        let descriptor = FetchDescriptor<OwnedCard>(
            predicate: #Predicate { $0.card?.id == cardID && $0.condition == condition }
        )
        return try modelContext.fetch(descriptor).first
    }
}

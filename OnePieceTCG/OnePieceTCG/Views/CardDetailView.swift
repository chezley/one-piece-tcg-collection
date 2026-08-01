import SwiftUI
import SwiftData

/// Detail view for a single `Card`: shows its stats and lets the user mark
/// it owned with a quantity, persisting through `CardRepository` (#3).
struct CardDetailView: View {
    let card: Card

    @Environment(\.modelContext) private var modelContext
    @Query private var ownedCards: [OwnedCard]

    @State private var quantity: Int = 0
    @State private var errorMessage: String?

    init(card: Card) {
        self.card = card
        let cardID = card.id
        _ownedCards = Query(filter: #Predicate<OwnedCard> { $0.card?.id == cardID })
    }

    private var repository: CardRepository {
        SwiftDataCardRepository(modelContext: modelContext)
    }

    private var ownedCard: OwnedCard? { ownedCards.first }

    var body: some View {
        Form {
            Section {
                cardImage
                    .frame(maxWidth: .infinity)
                    .listRowInsets(EdgeInsets())
            }

            Section("Details") {
                LabeledContent("Name", value: card.name)
                LabeledContent("Set", value: card.setCode)
                LabeledContent("Card Number", value: card.cardNumber)
                LabeledContent("Rarity", value: card.rarity)
                if let cost = card.cost {
                    LabeledContent("Cost", value: "\(cost)")
                }
                if let power = card.power {
                    LabeledContent("Power", value: "\(power)")
                }
                if let attribute = card.attribute {
                    LabeledContent("Attribute", value: attribute)
                }
                if let type = card.type {
                    LabeledContent("Type", value: type)
                }
            }

            Section("Collection") {
                Toggle("Owned", isOn: ownedBinding)
                Stepper("Quantity: \(quantity)", value: quantityBinding, in: 0...99)
            }
        }
        .navigationTitle(card.name)
        .onAppear { quantity = ownedCard?.quantity ?? 0 }
        .onChange(of: ownedCard?.quantity) { _, newValue in
            quantity = newValue ?? 0
        }
        .alert("Couldn't update your collection", isPresented: errorAlertBinding) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "")
        }
    }

    @ViewBuilder
    private var cardImage: some View {
        if let imageURLString = card.imageURL, let url = URL(string: imageURLString) {
            AsyncImage(url: url) { phase in
                if case .success(let image) = phase {
                    image.resizable().aspectRatio(contentMode: .fit)
                } else {
                    placeholderImage
                }
            }
        } else {
            placeholderImage
        }
    }

    private var placeholderImage: some View {
        Image(systemName: "photo")
            .resizable()
            .aspectRatio(contentMode: .fit)
            .foregroundStyle(.secondary)
            .frame(height: 200)
            .padding()
    }

    private var ownedBinding: Binding<Bool> {
        Binding(
            get: { quantity > 0 },
            set: { isOwned in setQuantity(isOwned ? max(quantity, 1) : 0) }
        )
    }

    private var quantityBinding: Binding<Int> {
        Binding(get: { quantity }, set: setQuantity)
    }

    private var errorAlertBinding: Binding<Bool> {
        Binding(get: { errorMessage != nil }, set: { if !$0 { errorMessage = nil } })
    }

    /// Single entry point for every owned/quantity change: routes to the
    /// right repository call (add/update/remove) based on current state,
    /// so 0 always means "no `OwnedCard` row" rather than a row with 0.
    private func setQuantity(_ newQuantity: Int) {
        let clamped = max(0, newQuantity)
        do {
            if let ownedCard {
                if clamped == 0 {
                    try repository.removeOwnedCard(ownedCard)
                } else {
                    try repository.updateOwnedCard(ownedCard, quantity: clamped)
                }
            } else if clamped > 0 {
                try repository.addOwnedCard(card, quantity: clamped, condition: .nearMint)
            }
            quantity = clamped
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

#Preview {
    NavigationStack {
        CardDetailView(card: Card(
            id: "OP01-001",
            name: "Monkey D. Luffy",
            setCode: "OP01",
            cardNumber: "OP01-001",
            rarity: "L",
            cost: 0,
            power: 5000,
            attribute: "Strike",
            type: "Leader"
        ))
    }
    .modelContainer(PersistenceController.makeContainer(inMemory: true))
}

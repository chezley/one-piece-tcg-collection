import SwiftUI
import SwiftData

// Minimal list + navigation so cards are reachable for #6's card detail
// screen; search/filtering across the full catalog is #5's scope.
struct BrowseView: View {
    @Query(sort: \Card.name) private var cards: [Card]

    var body: some View {
        NavigationStack {
            Group {
                if cards.isEmpty {
                    ContentUnavailableView("No cards yet", systemImage: "square.grid.2x2")
                } else {
                    List(cards) { card in
                        NavigationLink(value: card) {
                            VStack(alignment: .leading) {
                                Text(card.name)
                                Text("\(card.setCode) · \(card.rarity)")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Browse")
            .navigationDestination(for: Card.self) { card in
                CardDetailView(card: card)
            }
        }
    }
}

#Preview {
    BrowseView()
        .modelContainer(PersistenceController.makeContainer(inMemory: true))
}

import SwiftUI
import SwiftData

/// How `CollectionView` orders rows by quantity. A separate type (rather
/// than inlining the comparator) so `CollectionSort.apply` is unit-testable
/// without instantiating SwiftUI or SwiftData (#7).
enum CollectionQuantitySort: String, CaseIterable, Identifiable {
    case quantityDescending = "Quantity: High to Low"
    case quantityAscending = "Quantity: Low to High"

    var id: String { rawValue }
}

/// Pure filter/sort logic for the Collection tab, factored out of the view
/// so it can be exercised directly in tests (see `CollectionSort.apply`)
/// instead of only through view instantiation.
enum CollectionSort {
    static func apply(
        to ownedCards: [OwnedCard],
        setFilter: String?,
        sort: CollectionQuantitySort
    ) -> [OwnedCard] {
        let filtered = setFilter.map { code in
            ownedCards.filter { $0.card?.setCode == code }
        } ?? ownedCards

        switch sort {
        case .quantityAscending:
            return filtered.sorted { $0.quantity < $1.quantity }
        case .quantityDescending:
            return filtered.sorted { $0.quantity > $1.quantity }
        }
    }

    static func availableSetCodes(in ownedCards: [OwnedCard]) -> [String] {
        Set(ownedCards.compactMap { $0.card?.setCode }).sorted()
    }
}

/// The "Collection" tab: shows only the cards the user owns (#7), with
/// filter-by-set and sort-by-quantity controls. Tapping a row reuses
/// `CardDetailView` (#6) to adjust quantity or remove the card entirely.
struct CollectionView: View {
    // Sorted by dateAdded as a stable base order from SwiftData; the
    // user-facing quantity sort/set filter is applied on top in
    // `CollectionSort.apply` since @Query's sort is fixed at query time.
    @Query(sort: \OwnedCard.dateAdded) private var ownedCards: [OwnedCard]

    @State private var setFilter: String?
    @State private var sort: CollectionQuantitySort = .quantityDescending

    private var visibleRows: [OwnedCard] {
        CollectionSort.apply(to: ownedCards, setFilter: setFilter, sort: sort)
    }

    private var availableSetCodes: [String] {
        CollectionSort.availableSetCodes(in: ownedCards)
    }

    var body: some View {
        NavigationStack {
            Group {
                if ownedCards.isEmpty {
                    ContentUnavailableView(
                        "Your collection is empty",
                        systemImage: "rectangle.stack",
                        description: Text("Browse to add cards")
                    )
                } else {
                    List(visibleRows) { ownedCard in
                        if let card = ownedCard.card {
                            NavigationLink(value: card) {
                                row(for: card, ownedCard: ownedCard)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Collection")
            .navigationDestination(for: Card.self) { card in
                CardDetailView(card: card)
            }
            .toolbar {
                if !ownedCards.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        filterMenu
                    }
                }
            }
        }
    }

    private func row(for card: Card, ownedCard: OwnedCard) -> some View {
        VStack(alignment: .leading) {
            Text(card.name)
            Text("\(card.setCode) · Qty \(ownedCard.quantity)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }

    private var filterMenu: some View {
        Menu {
            Picker("Set", selection: $setFilter) {
                Text("All Sets").tag(String?.none)
                ForEach(availableSetCodes, id: \.self) { code in
                    Text(code).tag(String?.some(code))
                }
            }

            Picker("Sort", selection: $sort) {
                ForEach(CollectionQuantitySort.allCases) { option in
                    Text(option.rawValue).tag(option)
                }
            }
        } label: {
            Image(systemName: "line.3.horizontal.decrease.circle")
        }
    }
}

#Preview {
    CollectionView()
        .modelContainer(PersistenceController.makeContainer(inMemory: true))
}

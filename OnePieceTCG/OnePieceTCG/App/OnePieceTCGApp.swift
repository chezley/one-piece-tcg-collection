import SwiftUI
import SwiftData

@main
struct OnePieceTCGApp: App {
    private let modelContainer = PersistenceController.makeContainer()

    init() {
        // Catalog seeding fetches from the store and can touch every
        // discovered set file (see CatalogLoader.seedCatalog); running it
        // synchronously here would block init() — and therefore the first
        // frame — on that work. Deferring it to an async Task lets the UI
        // appear immediately while seeding completes shortly after (#36).
        let container = modelContainer
        Task { @MainActor in
            do {
                try CatalogLoader.seedCatalog(into: ModelContext(container))
            } catch {
                assertionFailure("Failed to seed catalog: \(error)")
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            RootTabView()
        }
        .modelContainer(modelContainer)
    }
}

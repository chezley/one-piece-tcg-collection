import SwiftUI
import SwiftData

@main
struct OnePieceTCGApp: App {
    private let modelContainer = PersistenceController.makeContainer()

    init() {
        do {
            try CatalogLoader.seedCatalog(into: ModelContext(modelContainer))
        } catch {
            assertionFailure("Failed to seed catalog: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            RootTabView()
        }
        .modelContainer(modelContainer)
    }
}

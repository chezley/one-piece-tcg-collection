import SwiftUI
import SwiftData

@main
struct OnePieceTCGApp: App {
    private let modelContainer = PersistenceController.makeContainer()

    var body: some Scene {
        WindowGroup {
            RootTabView()
                // Catalog seeding used to run synchronously in `init()`,
                // blocking the main thread before the first frame. Running
                // it here instead lets the UI render immediately (screens
                // observing the catalog update once seeding completes) and
                // moves the actual work off the main thread.
                .task {
                    await Self.seedCatalog(into: modelContainer)
                }
        }
        .modelContainer(modelContainer)
    }

    private static func seedCatalog(into modelContainer: ModelContainer) async {
        await Task.detached(priority: .utility) {
            do {
                try CatalogLoader.seedCatalog(into: ModelContext(modelContainer))
            } catch {
                assertionFailure("Failed to seed catalog: \(error)")
            }
        }.value
    }
}

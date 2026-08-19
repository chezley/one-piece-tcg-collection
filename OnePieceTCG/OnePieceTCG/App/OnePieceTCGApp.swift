import SwiftUI
import SwiftData
import os

@main
struct OnePieceTCGApp: App {
    private static let logger = Logger(subsystem: "com.chezley.onepiecetcg", category: "OnePieceTCGApp")

    private let modelContainer = PersistenceController.makeContainer()

    init() {
        do {
            try CatalogLoader.seedCatalog(into: ModelContext(modelContainer))
        } catch {
            // `assertionFailure` compiles to a no-op in Release, so a seed
            // failure here would otherwise be invisible outside of Debug
            // builds. Log at `.fault` so it's diagnosable via device logs —
            // the app still launches (possibly with an empty catalog) rather
            // than crashing, since a partial/empty catalog is preferable to
            // an unusable app.
            Self.logger.fault("Failed to seed catalog: \(String(describing: error), privacy: .public)")
        }
    }

    var body: some Scene {
        WindowGroup {
            RootTabView()
        }
        .modelContainer(modelContainer)
    }
}

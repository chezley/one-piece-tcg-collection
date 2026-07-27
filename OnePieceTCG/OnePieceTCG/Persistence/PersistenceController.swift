import Foundation
import SwiftData

/// Builds the app's `ModelContainer`. UI code should never construct a
/// `ModelContainer`/`ModelConfiguration` directly — go through here so the
/// schema stays in one place.
enum PersistenceController {
    static var schema: Schema {
        Schema([Card.self, CardSet.self, OwnedCard.self])
    }

    /// - Parameters:
    ///   - inMemory: use a transient in-memory store (tests, previews).
    ///   - url: use a file-backed store at this location. Takes precedence
    ///     over `inMemory`; used by tests to reopen the same store across
    ///     two containers to simulate an app relaunch.
    static func makeContainer(inMemory: Bool = false, url: URL? = nil) -> ModelContainer {
        let configuration: ModelConfiguration
        if let url {
            configuration = ModelConfiguration(schema: schema, url: url)
        } else {
            configuration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: inMemory)
        }

        do {
            return try ModelContainer(for: schema, configurations: [configuration])
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
    }
}

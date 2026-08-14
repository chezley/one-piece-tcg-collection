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
            // The on-disk store failed to open (corruption, an unmigratable
            // schema change, etc). Rather than crash-loop the user out of the
            // app forever (see #18), remove it and try again with a fresh
            // store — the catalog reseeds itself on next launch via
            // CatalogLoader, so this trades the user's collection data for
            // an app that still starts.
            removeStoreFiles(at: configuration.url)

            if let recovered = try? ModelContainer(for: schema, configurations: [configuration]) {
                return recovered
            }

            // Even a brand-new on-disk store couldn't be opened (e.g. no
            // writable disk space left). Fall back to an in-memory store so
            // the app can still launch; the user loses persistence for this
            // session instead of being stuck in a permanent crash loop.
            let inMemoryConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
            if let inMemoryContainer = try? ModelContainer(for: schema, configurations: [inMemoryConfiguration]) {
                return inMemoryContainer
            }

            fatalError("Failed to create ModelContainer, including the in-memory fallback: \(error)")
        }
    }

    /// Deletes the SQLite store file and its `-wal`/`-shm` sidecars at `url`,
    /// ignoring errors (e.g. the files may not exist for a store that never
    /// opened successfully in the first place).
    private static func removeStoreFiles(at url: URL) {
        let fileManager = FileManager.default
        for suffix in ["", "-wal", "-shm"] {
            try? fileManager.removeItem(at: URL(fileURLWithPath: url.path + suffix))
        }
    }
}

import Foundation
import SwiftData
import os

/// Builds the app's `ModelContainer`. UI code should never construct a
/// `ModelContainer`/`ModelConfiguration` directly — go through here so the
/// schema stays in one place.
enum PersistenceController {
    private static let logger = Logger(subsystem: "com.chezley.onepiecetcg", category: "PersistenceController")

    static var schema: Schema {
        Schema([Card.self, CardSet.self, OwnedCard.self])
    }

    /// - Parameters:
    ///   - inMemory: use a transient in-memory store (tests, previews).
    ///   - url: use a file-backed store at this location. Takes precedence
    ///     over `inMemory`; used by tests to reopen the same store across
    ///     two containers to simulate an app relaunch.
    ///
    /// If the store fails to open (corruption, an incompatible on-disk
    /// schema left behind by an older build, etc.), this does not crash the
    /// app: it deletes the store and retries once with a fresh one, trading
    /// the user's existing local data for an app that still launches (the
    /// catalog reseeds itself via `CatalogLoader`). If even a brand-new
    /// on-disk store can't be opened (e.g. no writable disk space), it
    /// falls back to an in-memory store so the app still launches for that
    /// session. See #18.
    static func makeContainer(inMemory: Bool = false, url: URL? = nil) -> ModelContainer {
        let configuration: ModelConfiguration
        if let url {
            configuration = ModelConfiguration(schema: schema, url: url)
        } else {
            configuration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: inMemory)
        }

        if let container = try? ModelContainer(for: schema, configurations: [configuration]) {
            return container
        }

        logger.error("Failed to open store at \(configuration.url.path, privacy: .public); deleting it and retrying with a fresh store")
        removeStoreFiles(at: configuration.url)

        if let container = try? ModelContainer(for: schema, configurations: [configuration]) {
            return container
        }

        logger.fault("Failed to open a fresh store at \(configuration.url.path, privacy: .public); falling back to an in-memory store")
        let fallbackConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)

        if let container = try? ModelContainer(for: schema, configurations: [fallbackConfiguration]) {
            return container
        }

        fatalError("Failed to create ModelContainer even with an in-memory fallback store")
    }

    /// Deletes a SQLite store's main file plus its `-wal`/`-shm` sidecars.
    private static func removeStoreFiles(at url: URL) {
        let fileManager = FileManager.default
        for suffix in ["", "-wal", "-shm"] {
            let fileURL = URL(fileURLWithPath: url.path + suffix)
            try? fileManager.removeItem(at: fileURL)
        }
    }
}

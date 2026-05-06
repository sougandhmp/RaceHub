import Foundation

/// One-time side effects produced by `CreateThreadViewModel`.
enum CreateThreadEffect {
    /// Posted successfully — the View should dismiss back to the forum.
    case threadCreated
}

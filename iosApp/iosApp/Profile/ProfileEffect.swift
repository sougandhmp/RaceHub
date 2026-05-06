import Foundation

/// One-time side effects produced by `ProfileViewModel` — typically navigation
/// events that must not be replayed when the View re-renders.
enum ProfileEffect {

    /// Sign the current user out and return to the Login screen.
    case signedOut
}

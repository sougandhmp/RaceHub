import Foundation

/// One-time side effects produced by `LoginViewModel`.
///
/// Effects represent events that must not be replayed when the view re-renders
/// (e.g. navigation triggers). They are delivered via a `PassthroughSubject`
/// so they are consumed exactly once by the View.
enum LoginEffect {

    /// Instructs the host view to navigate away from Login to Home.
    case navigateToHome
}

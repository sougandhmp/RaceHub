import Foundation

/// One-time side effects produced by `SignUpViewModel`.
///
/// Effects represent events that must not be replayed when the view re-renders
/// (e.g. navigation triggers). They are delivered via a `PassthroughSubject`
/// so they are consumed exactly once by the View.
enum SignUpEffect {

    /// Instructs the host view to navigate away from Sign-Up to Home.
    case navigateToHome
}

import Foundation
import Shared

/// Immutable snapshot of the Profile tab. Produced by `ProfileViewModel` on
/// every state change; the View never mutates this struct directly.
struct ProfileState {

    /// The currently signed-in user, or `nil` while no user is in the session.
    var user: User? = nil

    /// `true` while a sign-out request is in flight.
    var isSigningOut: Bool = false

    /// Non-`nil` when an error should be shown to the user.
    var errorMessage: String? = nil
}

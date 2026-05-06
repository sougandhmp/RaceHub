import Foundation

/// Immutable snapshot of the Profile tab. The screen is a placeholder for now;
/// fields will be added as the profile feature is built out.
struct ProfileState {

    /// `true` while profile data is being fetched.
    var isLoading: Bool = false

    /// Non-`nil` when an error should be shown to the user.
    var errorMessage: String? = nil
}

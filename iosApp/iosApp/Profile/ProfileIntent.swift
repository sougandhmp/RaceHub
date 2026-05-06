import Foundation

/// Every user interaction targeted at the Profile tab expressed as an explicit event.
/// The View dispatches intents; `ProfileViewModel` is the sole handler.
enum ProfileIntent {

    /// Clears the active error message without changing any other state.
    case dismissError
}

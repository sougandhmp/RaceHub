import Foundation

/// Every user interaction targeted at the Race tab expressed as an explicit event.
/// The View dispatches intents; `RaceViewModel` is the sole handler.
enum RaceIntent {

    /// User triggered a pull-to-refresh or tapped a retry button.
    case refresh

    /// Clears the active error message without changing any other state.
    case dismissError
}

import Foundation

/// Every user interaction on the Home screen expressed as an explicit event.
///
/// The View dispatches intents; `HomeViewModel` is the sole handler.
/// This one-way data flow makes state changes auditable and testable.
enum HomeIntent {

    /// User tapped the Schedule or Standings tab.
    case tabSelected(HomeTab)

    /// User triggered a pull-to-refresh gesture or tapped a retry button.
    case refresh
}

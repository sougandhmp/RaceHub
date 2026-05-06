import Foundation

/// Every user interaction on the Home shell expressed as an explicit event.
/// Tab content interactions are dispatched to the per-tab feature ViewModels.
enum HomeIntent {

    /// User tapped the Race, Forum or Profile tab.
    case tabSelected(HomeTab)
}

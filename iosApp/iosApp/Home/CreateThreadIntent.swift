import Foundation

/// Every user interaction on the Create Thread screen expressed as an
/// explicit event. The View dispatches intents; `CreateThreadViewModel`
/// is the sole handler.
enum CreateThreadIntent {
    case titleChanged(String)
    case categoryChanged(String)
    case contentChanged(String)
    case submit
    case dismissError
}

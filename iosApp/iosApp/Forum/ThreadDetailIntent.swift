import Foundation

enum ThreadDetailIntent {
    case commentInputChanged(String)
    case submitComment(threadId: String)
    case dismissError
}

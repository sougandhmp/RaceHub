import Foundation
import Shared

struct ProfileState {
    var user: User? = nil
    var isLoadingProfile: Bool = false
    var postsCount: Int = 0
    var savedCount: Int = 0
    var recentThreadTitles: [String] = []
    var savedThreadTitles: [String] = []
    var isSigningOut: Bool = false
    var errorMessage: String? = nil
}

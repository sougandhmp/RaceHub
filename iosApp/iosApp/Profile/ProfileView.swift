import SwiftUI

struct ProfileView: View {

    @StateObject private var viewModel = ProfileViewModel()

    var body: some View {
        ScrollView {
            VStack {
                Spacer().frame(height: 100)
                Text("Profile")
                    .font(.largeTitle)
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

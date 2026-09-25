import SwiftUI

/// Inline error shown above a screen's content when loading or refreshing fails.
/// Cached content stays visible underneath, so the user still sees the last data.
struct ErrorBanner: View {
    let message: String
    let colors: AppColors
    let onRetry: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: Theme.Spacing.sm) {
            Text(message)
                .font(.system(size: 13))
                .foregroundColor(colors.primaryText)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.vertical, Theme.Spacing.md)

            Button("Try again", action: onRetry)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(AppColors.racingRed)

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(colors.mutedText)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel(Text("Dismiss error"))
        }
        .padding(.leading, Theme.Spacing.lg)
        .background(colors.card)
        .clipShape(RoundedRectangle(cornerRadius: Theme.Radius.tile))
        .overlay(
            RoundedRectangle(cornerRadius: Theme.Radius.tile)
                .stroke(AppColors.racingRed, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.15), radius: 4, y: 2)
        .padding(.horizontal, Theme.Spacing.screenGutter)
        .padding(.vertical, Theme.Spacing.sm)
    }
}

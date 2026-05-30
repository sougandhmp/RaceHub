import CoreGraphics

/// Centralized spacing, radius and sizing tokens for the whole app.
///
/// Views must use these instead of hardcoding point values so gutters, corner
/// radii and vertical rhythm stay consistent across every screen (and in sync
/// with the Android `Dimens` object).
enum Theme {

    enum Spacing {
        /// Standard horizontal screen gutter used by every screen.
        static let screenGutter: CGFloat = 20

        // Vertical rhythm — use these for spacing between elements.
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let xl: CGFloat = 24
    }

    enum Radius {
        /// Cards / primary surfaces.
        static let card: CGFloat = 16
        /// Inner tiles, stat boxes, segmented controls.
        static let tile: CGFloat = 12
        /// Small chips, badges, tags, buttons.
        static let chip: CGFloat = 8
    }
}

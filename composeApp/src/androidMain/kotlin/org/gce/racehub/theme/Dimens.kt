package org.gce.racehub.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Centralized spacing, radius and sizing tokens for the whole app.
 *
 * Screens must use these instead of hardcoding dp values so gutters, corner
 * radii and vertical rhythm stay consistent across every screen (and in sync
 * with the iOS `Theme.Spacing` / `Theme.Radius` constants).
 */
object Dimens {
    /** Standard horizontal screen gutter used by every screen. */
    val screenGutter = 20.dp

    // Vertical rhythm — use these for spacing between elements.
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 24.dp

    // Corner radii.
    /** Cards / primary surfaces. */
    val radiusCard = 16.dp
    /** Inner tiles, stat boxes, segmented controls. */
    val radiusTile = 12.dp
    /** Small chips, badges, tags, buttons. */
    val radiusChip = 8.dp

    // Reusable shapes built from the radii above.
    val cardShape = RoundedCornerShape(radiusCard)
    val tileShape = RoundedCornerShape(radiusTile)
    val chipShape = RoundedCornerShape(radiusChip)
}

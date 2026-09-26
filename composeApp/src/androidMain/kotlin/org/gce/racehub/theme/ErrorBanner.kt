package org.gce.racehub.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.action_try_again
import racehub.composeapp.generated.resources.contentdesc_dismiss_error

/**
 * Inline error shown above a screen's content when loading or refreshing fails.
 * Cached content stays visible underneath, so the user still sees the last data.
 */
@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Surface(
        color = colors.card,
        shape = Dimens.tileShape,
        shadowElevation = Dimens.spaceXs,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenGutter, vertical = Dimens.spaceSm)
            .border(width = 1.dp, color = colors.racingRed, shape = Dimens.tileShape)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
            modifier = Modifier.padding(start = Dimens.spaceLg, end = Dimens.spaceXs)
        ) {
            Text(
                text = message,
                color = colors.primaryText,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f).padding(vertical = Dimens.spaceMd)
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(Res.string.action_try_again), color = colors.racingRed, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.contentdesc_dismiss_error),
                    tint = colors.mutedText
                )
            }
        }
    }
}

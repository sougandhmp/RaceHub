package org.gce.racehub.ui

import org.gce.racehub.core.domain.DataError
import org.jetbrains.compose.resources.StringResource
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.error_network
import racehub.composeapp.generated.resources.error_server
import racehub.composeapp.generated.resources.error_unknown

/** Generic user-facing message for a [DataError]. */
internal fun DataError.message(): StringResource = when (this) {
    DataError.Network -> Res.string.error_network
    DataError.Server -> Res.string.error_server
    DataError.Unknown -> Res.string.error_unknown
}

package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.dataOrNull
import org.gce.racehub.core.domain.errorOrNull
import org.gce.racehub.core.domain.mapError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthFailureTest {

    @Test
    fun `data errors map onto the matching auth errors`() {
        assertEquals(AuthFailure(AuthError.Network), AuthFailure.from(DataError.Network))
        assertEquals(AuthFailure(AuthError.Server), AuthFailure.from(DataError.Server))
        assertEquals(AuthFailure(AuthError.Unknown), AuthFailure.from(DataError.Unknown))
    }

    @Test
    fun `authFailure carries the reason and server message`() {
        val result = authFailure(AuthError.Rejected, "Invalid credentials")
        assertEquals(AuthFailure(AuthError.Rejected, "Invalid credentials"), result.errorOrNull())
        assertNull(result.dataOrNull())
    }

    @Test
    fun `mapError converts failures and leaves successes alone`() {
        val failed: DataResult<Int, DataError> = DataResult.Failure(DataError.Network)
        assertEquals(AuthFailure(AuthError.Network), failed.mapError(AuthFailure::from).errorOrNull())

        val ok: DataResult<Int, DataError> = DataResult.Success(1)
        assertEquals(1, ok.mapError(AuthFailure::from).dataOrNull())
    }
}

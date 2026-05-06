package org.gce.racehub.auth.data.repository

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.repository.AuthRepository

/**
 * Fake in-memory implementation of [AuthRepository].
 *
 * Simulates a backend without any network I/O. Replace this with a real
 * Ktor/Retrofit implementation when the API is ready — nothing outside
 * this class needs to change because the rest of the code depends on the
 * [AuthRepository] interface, not this concrete class.
 *
 * Hard-coded test credentials (temporary):
 * 1. driver@racehub.com / race123
 * 2. sougandhmp@gmail.com / Sylasree*#0#
 */
class AuthRepositoryImpl : AuthRepository {

    /**
     * Accepts hard-coded credential pairs; rejects everything else.
     * In production this would make an authenticated HTTP request.
     */
    override suspend fun login(email: String, password: String): AuthResult {
        return when {
            email == "driver@racehub.com" && password == "race123" ->
                AuthResult.success(User(id = "1", email = email, name = "Race Driver"))
            email == "sougandhmp@gmail.com" && password == "Sylasree*#0#" ->
                AuthResult.success(User(id = "2", email = email, name = "Sougandhmp"))
            else ->
                AuthResult.failure("Invalid email or password")
        }
    }

    /**
     * Always succeeds for any validated input.
     * In production this would POST to a registration endpoint.
     */
    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        return AuthResult.success(User(id = "2", email = email, name = name))
    }

    override suspend fun logout(token: String): Boolean = true
}

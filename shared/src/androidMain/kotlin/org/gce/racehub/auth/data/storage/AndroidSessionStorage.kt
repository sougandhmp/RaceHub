// security-crypto is deprecated with no drop-in replacement; migrating to DataStore + Tink
// would invalidate every persisted session, so keep it until that migration is planned.
@file:Suppress("DEPRECATION")

package org.gce.racehub.auth.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.gce.racehub.auth.domain.model.User
import androidx.core.content.edit

class AndroidSessionStorage(context: Context) : SessionStorage {

    // The session holds the auth bearer token, so persist it encrypted at rest
    // (AES-256) rather than in plaintext SharedPreferences.
    private val prefs = run {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun saveUser(user: User) {
        prefs.edit {
            putString(KEY_ID, user.id)
                .putString(KEY_EMAIL, user.email)
                .putString(KEY_NAME, user.name)
                .putString(KEY_TOKEN, user.token)
                .putString(KEY_USERNAME, user.username)
                .putString(KEY_COUNTRY, user.country)
                .putString(KEY_AVATAR, user.avatar)
                .putString(KEY_ROLE, user.role)
                .putString(KEY_JOINED_AT, user.joinedAt)
                .putInt(KEY_POSTS_COUNT, user.postsCount)
        }
    }

    override fun restoreUser(): User? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_NAME, null) ?: return null
        return User(
            id = id,
            email = email,
            name = name,
            token = prefs.getString(KEY_TOKEN, null),
            username = prefs.getString(KEY_USERNAME, null),
            country = prefs.getString(KEY_COUNTRY, null),
            avatar = prefs.getString(KEY_AVATAR, null),
            role = prefs.getString(KEY_ROLE, null),
            joinedAt = prefs.getString(KEY_JOINED_AT, null),
            postsCount = prefs.getInt(KEY_POSTS_COUNT, 0)
        )
    }

    override fun clearUser() {
        prefs.edit { clear() }
    }

    companion object {
        // Excluded from backups in res/xml/data_extraction_rules.xml and backup_rules.xml (composeApp); keep in sync.
        private const val PREFS_NAME = "racehub_session"
        private const val KEY_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
        private const val KEY_TOKEN = "user_token"
        private const val KEY_USERNAME = "user_username"
        private const val KEY_COUNTRY = "user_country"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_ROLE = "user_role"
        private const val KEY_JOINED_AT = "user_joined_at"
        private const val KEY_POSTS_COUNT = "user_posts_count"
    }
}

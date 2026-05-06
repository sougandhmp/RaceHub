package org.gce.racehub.auth.data.storage

import org.gce.racehub.auth.domain.model.User
import platform.Foundation.NSUserDefaults

class IosSessionStorage : SessionStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveUser(user: User) {
        defaults.setObject(user.id, forKey = KEY_ID)
        defaults.setObject(user.email, forKey = KEY_EMAIL)
        defaults.setObject(user.name, forKey = KEY_NAME)
        user.token?.let { defaults.setObject(it, forKey = KEY_TOKEN) }
            ?: defaults.removeObjectForKey(KEY_TOKEN)
        user.username?.let { defaults.setObject(it, forKey = KEY_USERNAME) }
            ?: defaults.removeObjectForKey(KEY_USERNAME)
        user.country?.let { defaults.setObject(it, forKey = KEY_COUNTRY) }
            ?: defaults.removeObjectForKey(KEY_COUNTRY)
        user.avatar?.let { defaults.setObject(it, forKey = KEY_AVATAR) }
            ?: defaults.removeObjectForKey(KEY_AVATAR)
        user.role?.let { defaults.setObject(it, forKey = KEY_ROLE) }
            ?: defaults.removeObjectForKey(KEY_ROLE)
        user.joinedAt?.let { defaults.setObject(it, forKey = KEY_JOINED_AT) }
            ?: defaults.removeObjectForKey(KEY_JOINED_AT)
        defaults.setInteger(user.postsCount.toLong(), forKey = KEY_POSTS_COUNT)
    }

    override fun restoreUser(): User? {
        val id = defaults.stringForKey(KEY_ID) ?: return null
        val email = defaults.stringForKey(KEY_EMAIL) ?: return null
        val name = defaults.stringForKey(KEY_NAME) ?: return null
        return User(
            id = id,
            email = email,
            name = name,
            token = defaults.stringForKey(KEY_TOKEN),
            username = defaults.stringForKey(KEY_USERNAME),
            country = defaults.stringForKey(KEY_COUNTRY),
            avatar = defaults.stringForKey(KEY_AVATAR),
            role = defaults.stringForKey(KEY_ROLE),
            joinedAt = defaults.stringForKey(KEY_JOINED_AT),
            postsCount = defaults.integerForKey(KEY_POSTS_COUNT).toInt()
        )
    }

    override fun clearUser() {
        listOf(
            KEY_ID, KEY_EMAIL, KEY_NAME, KEY_TOKEN, KEY_USERNAME,
            KEY_COUNTRY, KEY_AVATAR, KEY_ROLE, KEY_JOINED_AT, KEY_POSTS_COUNT
        ).forEach { defaults.removeObjectForKey(it) }
    }

    companion object {
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

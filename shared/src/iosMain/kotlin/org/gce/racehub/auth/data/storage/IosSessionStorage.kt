package org.gce.racehub.auth.data.storage

import org.gce.racehub.auth.domain.session.SessionStorage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import org.gce.racehub.auth.domain.model.User
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS [SessionStorage] backed by the system Keychain.
 *
 * The session holds the auth bearer token, so values are persisted to the
 * Keychain (encrypted at rest, excluded from device backups) rather than
 * `NSUserDefaults`, which is a plaintext plist. Items use
 * `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` so they are readable in the
 * background after first unlock but never migrate to another device.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSessionStorage : SessionStorage {

    override fun saveUser(user: User) {
        set(KEY_ID, user.id)
        set(KEY_EMAIL, user.email)
        set(KEY_NAME, user.name)
        set(KEY_TOKEN, user.token)
        set(KEY_USERNAME, user.username)
        set(KEY_COUNTRY, user.country)
        set(KEY_AVATAR, user.avatar)
        set(KEY_ROLE, user.role)
        set(KEY_JOINED_AT, user.joinedAt)
        set(KEY_POSTS_COUNT, user.postsCount.toString())
    }

    override fun restoreUser(): User? {
        val id = get(KEY_ID) ?: return null
        val email = get(KEY_EMAIL) ?: return null
        val name = get(KEY_NAME) ?: return null
        return User(
            id = id,
            email = email,
            name = name,
            token = get(KEY_TOKEN),
            username = get(KEY_USERNAME),
            country = get(KEY_COUNTRY),
            avatar = get(KEY_AVATAR),
            role = get(KEY_ROLE),
            joinedAt = get(KEY_JOINED_AT),
            postsCount = get(KEY_POSTS_COUNT)?.toIntOrNull() ?: 0
        )
    }

    override fun clearUser() {
        listOf(
            KEY_ID, KEY_EMAIL, KEY_NAME, KEY_TOKEN, KEY_USERNAME,
            KEY_COUNTRY, KEY_AVATAR, KEY_ROLE, KEY_JOINED_AT, KEY_POSTS_COUNT
        ).forEach { delete(it) }
    }

    /** Upserts [value] for [account]; a null value removes the item. */
    private fun set(account: String, value: String?) {
        delete(account)
        if (value == null) return
        val data = value.toNSData()

        val query = baseQuery(account) ?: return
        CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        val cfData = CFBridgingRetain(data)
        CFDictionarySetValue(query, kSecValueData, cfData)
        SecItemAdd(query, null)
        cfData?.let { CFRelease(it) }
        CFRelease(query)
    }

    private fun get(account: String): String? {
        val query = baseQuery(account) ?: return null
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        return memScoped {
            val result = alloc<COpaquePointerVar>()
            val status = SecItemCopyMatching(query, result.ptr.reinterpret())
            CFRelease(query)
            if (status != errSecSuccess) {
                result.value?.let { CFRelease(it) }
                return@memScoped null
            }
            // CFBridgingRelease consumes the +1 retain from kSecReturnData.
            val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
            NSString.create(data, NSUTF8StringEncoding)?.toString()
        }
    }

    private fun delete(account: String) {
        val query = baseQuery(account) ?: return
        SecItemDelete(query)
        CFRelease(query)
    }

    /** Builds a mutable generic-password query scoped to this app's service. */
    private fun baseQuery(account: String): platform.CoreFoundation.CFMutableDictionaryRef? {
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        cfString(SERVICE)?.let {
            CFDictionarySetValue(dict, kSecAttrService, it)
            CFRelease(it)
        }
        cfString(account)?.let {
            CFDictionarySetValue(dict, kSecAttrAccount, it)
            CFRelease(it)
        }
        return dict
    }

    private fun cfString(value: String): CFTypeRef? =
        CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8)

    private fun String.toNSData(): NSData {
        val bytes = encodeToByteArray()
        if (bytes.isEmpty()) return NSData()
        return bytes.usePinned {
            NSData.create(bytes = it.addressOf(0), length = bytes.size.convert())
        }
    }

    companion object {
        private const val SERVICE = "org.gce.racehub.session"
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

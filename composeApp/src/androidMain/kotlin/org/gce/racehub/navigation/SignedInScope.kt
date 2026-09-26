package org.gce.racehub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Holds the [ViewModelStore] of the signed-in part of the app. It is itself a
 * ViewModel of the activity, so the store survives rotation; [clear] on sign-out
 * drops every signed-in ViewModel, and the next sign-in starts from a fresh store.
 */
internal class SignedInScopeHolder : ViewModel() {
    private var store: ViewModelStore? = null

    fun owner(): ViewModelStoreOwner {
        val store = store ?: ViewModelStore().also { store = it }
        return object : ViewModelStoreOwner {
            override val viewModelStore = store
        }
    }

    fun clear() {
        store?.clear()
        store = null
    }

    override fun onCleared() = clear()
}

/**
 * Makes [holder]'s store the ViewModel owner for [content], so ViewModels created
 * there (including per-entry ones from the nav entry decorator) exist only while
 * someone is signed in.
 */
@Composable
internal fun SignedInScope(holder: SignedInScopeHolder, content: @Composable () -> Unit) {
    val owner = remember(holder) { holder.owner() }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}

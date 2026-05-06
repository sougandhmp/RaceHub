package org.gce.racehub.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Owns the Home shell state — currently just the selected tab. Each tab's
 * content (Race, Forum, Profile) is loaded by its own feature ViewModel.
 */
class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TabSelected ->
                _state.update { it.copy(selectedTab = intent.tab) }
        }
    }
}

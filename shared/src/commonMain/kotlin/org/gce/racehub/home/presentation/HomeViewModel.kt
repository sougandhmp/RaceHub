package org.gce.racehub.home.presentation

import androidx.lifecycle.ViewModel
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Shared MVI ViewModel for the home shell's bottom tabs (Android and iOS). */
class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TabSelected -> _state.update { HomeReducer.reduce(it, HomeMutation.TabSelected(intent.tab)) }
        }
    }
}

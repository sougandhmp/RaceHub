package org.gce.racehub.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase

data class RaceDetailState(
    val detail: RaceDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Loads the detail of one race for the Race Detail screen.
 *
 * Scoped to that screen's navigation entry: each opened race gets its own instance,
 * which is cleared when the screen is popped.
 */
class RaceDetailViewModel(
    private val slug: String,
    private val getRaceDetailUseCase: GetRaceDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RaceDetailState())
    val state: StateFlow<RaceDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getRaceDetailUseCase(slug)
            _state.update { it.copy(isLoading = false, detail = result.data ?: it.detail, errorMessage = result.error?.message) }
        }
    }
}

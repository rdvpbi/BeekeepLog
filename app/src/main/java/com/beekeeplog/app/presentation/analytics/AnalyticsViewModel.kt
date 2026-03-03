package com.beekeeplog.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.KpiColor
import com.beekeeplog.app.domain.model.Preset
import com.beekeeplog.app.domain.model.Stage
import com.beekeeplog.app.domain.usecase.BuildAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** ViewModel for the Analytics screen. Delegates data retrieval to [BuildAnalyticsUseCase]. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val buildAnalytics: BuildAnalyticsUseCase
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _isListView = MutableStateFlow(false)

    val uiState: StateFlow<AnalyticsUiState> = _filterState
        .flatMapLatest { filter -> buildAnalytics(filter) }
        .map { result ->
            AnalyticsUiState(
                kpiCount = result.kpiCount,
                kpiLabel = result.kpiLabel,
                kpiColor = result.kpiColor,
                activePreset = _filterState.value.preset,
                nucList = result.nucList,
                isListView = _isListView.value,
                availableLines = result.availableLines,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState(kpiLabel = "НУКЛЕУСОВ", kpiColor = KpiColor.GREEN)
        )

    // -------------------------------------------------------------------------
    // Filter actions
    // -------------------------------------------------------------------------

    fun onPresetChange(preset: Preset?) {
        _filterState.update { it.copy(preset = preset) }
    }

    fun onGeneticsChange(genetics: Genetics?) {
        _filterState.update { it.copy(genetics = genetics, preset = null) }
    }

    fun onLineNameChange(lineName: String?) {
        _filterState.update { it.copy(lineName = lineName, preset = null) }
    }

    fun onStageChange(stage: Stage?) {
        _filterState.update { it.copy(stage = stage, preset = null) }
    }

    fun onSectorChange(sector: String?) {
        _filterState.update { it.copy(sector = sector, preset = null) }
    }

    fun onRowChange(row: String?) {
        _filterState.update { it.copy(row = row, preset = null) }
    }

    fun onShowList() { _isListView.value = true }
    fun onBackToFilters() { _isListView.value = false }
}

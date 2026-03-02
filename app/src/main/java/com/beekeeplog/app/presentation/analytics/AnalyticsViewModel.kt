package com.beekeeplog.app.presentation.analytics

import androidx.lifecycle.ViewModel
import com.beekeeplog.app.domain.usecase.BuildAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** ViewModel for the Analytics screen. Delegates data retrieval to [BuildAnalyticsUseCase]. */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val buildAnalytics: BuildAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
}

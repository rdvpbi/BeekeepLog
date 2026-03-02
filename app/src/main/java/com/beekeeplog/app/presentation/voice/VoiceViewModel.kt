package com.beekeeplog.app.presentation.voice

import androidx.lifecycle.ViewModel
import com.beekeeplog.app.domain.usecase.CancelIntentUseCase
import com.beekeeplog.app.domain.usecase.ConfirmIntentUseCase
import com.beekeeplog.app.domain.usecase.StartInspectionSessionUseCase
import com.beekeeplog.app.domain.usecase.StopInspectionSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** ViewModel for the Voice screen. Delegates all business logic to Use Cases. */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val startSession: StartInspectionSessionUseCase,
    private val stopSession: StopInspectionSessionUseCase,
    private val confirmIntent: ConfirmIntentUseCase,
    private val cancelIntent: CancelIntentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()
}

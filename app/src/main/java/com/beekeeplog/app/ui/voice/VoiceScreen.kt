package com.beekeeplog.app.ui.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.app.Activity
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.beekeeplog.app.domain.model.VoicePhase
import com.beekeeplog.app.presentation.voice.VoiceViewModel
import com.beekeeplog.app.ui.theme.Black

/**
 * Voice screen: 3-zone layout — status bar / recording content + controls / alerts.
 * Keeps the screen on during listening.
 */
@Composable
fun VoiceScreen(viewModel: VoiceViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Keep screen on while listening
    DisposableEffect(uiState.phase) {
        if (uiState.phase == VoicePhase.LISTENING) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Permission launcher — redirects to Settings if permanently denied
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startSession()
        } else if (activity != null &&
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
        ) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Zone 1: Status bar
        StatusBar(
            phase = uiState.phase,
            mode = uiState.mode,
            currentNucId = uiState.currentNucId,
            sessionDurationSec = uiState.sessionDurationSec
        )

        // Zone 2: Main content + controls
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState.phase) {
                    VoicePhase.IDLE -> {
                        Spacer(Modifier.weight(1f))
                        MicButton(
                            phase = uiState.phase,
                            onClick = {
                                val perm = Manifest.permission.RECORD_AUDIO
                                if (ContextCompat.checkSelfPermission(context, perm) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                    viewModel.startSession()
                                } else {
                                    permissionLauncher.launch(perm)
                                }
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                        ModeSwitcher(
                            currentMode = uiState.mode,
                            onModeChange = viewModel::onModeChange,
                            enabled = true
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    VoicePhase.LISTENING -> {
                        StreamingTranscript(
                            text = uiState.streamingText,
                            mode = uiState.mode,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AmplitudeVisualizer(
                            rmsValues = uiState.rmsValues,
                            mode = uiState.mode
                        )
                        Spacer(Modifier.weight(1f))
                        MicButton(
                            phase = uiState.phase,
                            onClick = viewModel::stopSession
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    VoicePhase.CONFIRMING -> {
                        val intent = uiState.recognizedIntent
                        if (intent != null) {
                            ConfirmationCard(
                                recognizedText = uiState.recognizedText,
                                intentResult = intent,
                                currentNucId = uiState.currentNucId
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        ConfirmButtons(
                            onCancel = viewModel::cancelIntent,
                            onConfirm = viewModel::confirmIntent
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    VoicePhase.SUCCESS -> {
                        Spacer(Modifier.weight(1f))
                        SuccessDisplay(
                            mode = uiState.mode,
                            answerText = uiState.answerText
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }

                if (uiState.errorText.isNotBlank()) {
                    Text(
                        text = uiState.errorText,
                        color = Color(0xFFFF1744),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Zone 3: Alerts
        AlertsBlock(alerts = uiState.todayAlerts)
    }
}

package com.beekeeplog.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.beekeeplog.app.ui.navigation.NavGraph
import com.beekeeplog.app.ui.theme.BeekeepLogTheme

/** Single-activity host for the Compose navigation graph. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            BeekeepLogTheme {
                NavGraph()
            }
        }
    }
}

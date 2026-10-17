package com.example.disipline.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.disipline.UI.onboarding.OnboardingScreen
import com.example.disipline.data.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun DisiplineApp() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context.applicationContext) }
    val scope = rememberCoroutineScope()

    // null = still loading from DataStore (prevents onboarding flashing for returning users)
    val hasSeen: Boolean? by prefs.hasSeenOnboarding.collectAsState(initial = null)

    when (hasSeen) {
        null -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F2EB)) // same cream as onboarding
        )

        false -> OnboardingScreen(
            onFinish = { scope.launch { prefs.setOnboardingSeen() } }
        )

        true -> AppNavigation()
    }
}
package com.example.disipline.UI

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.disipline.UI.onboarding.OnboardingScreen
import com.example.disipline.UI.onboarding.SplashScreen
import com.example.disipline.data.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun DisiplineApp() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    val hasSeen by prefs.hasSeenOnboarding.collectAsState(initial = false)

    // Has the user tapped "Get Started" on the splash this session?
    var splashDismissed by remember { mutableStateOf(false) }

    when {
        !splashDismissed -> {
            SplashScreen(onGetStarted = { splashDismissed = true })
        }

        !hasSeen -> {
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        prefs.setOnboardingSeen()
                    }
                }
            )
        }

        else -> {
            AppNavigation()
        }
    }
}

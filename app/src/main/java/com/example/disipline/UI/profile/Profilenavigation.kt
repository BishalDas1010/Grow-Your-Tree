package com.example.disipline.UI.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary

// ---------- 1. Route names (one place, no typos) ----------

object Routes {
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val GOALS = "goals"
    const val PROGRESS = "progress"
    const val FOCUS_SETTINGS = "focus_settings"
    const val THEMES = "themes"
    const val NOTIFICATIONS = "notifications"
    const val APP_BLOCKING = "app_blocking"
    const val SOUND = "sound"
    const val HELP = "help"
    const val ABOUT = "about"
    const val SETTINGS = "settings"
}

// ---------- 2. NavHost ----------
// Call this from MainActivity: setContent { ProfileNavHost() }
// If your app already has a NavHost (e.g. with a bottom bar), DON'T add a
// second one. Copy only the composable(...) blocks into your existing NavHost.

@Composable
fun ProfileNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PROFILE
    ) {
        composable(Routes.PROFILE) {
            ProfileRoute(
                actions = ProfileActions(
                    onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onGoals = { navController.navigate(Routes.GOALS) },
                    onProgress = { navController.navigate(Routes.PROGRESS) },
                    onFocusSettings = { navController.navigate(Routes.FOCUS_SETTINGS) },
                    onThemes = { navController.navigate(Routes.THEMES) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onAppBlocking = { navController.navigate(Routes.APP_BLOCKING) },
                    onSound = { navController.navigate(Routes.SOUND) },
                    onHelp = { navController.navigate(Routes.HELP) },
                    onAbout = { navController.navigate(Routes.ABOUT) }
                ),
                onEditClick = { navController.navigate(Routes.EDIT_PROFILE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // Replace each PlaceholderScreen with your real screen when it's ready.
        // Every screen gets a back action: navController.popBackStack()
        val back: () -> Unit = { navController.popBackStack() }

        composable(Routes.EDIT_PROFILE) { PlaceholderScreen("Edit Profile", back) }
        composable(Routes.GOALS) { PlaceholderScreen("My Goals", back) }
        composable(Routes.PROGRESS) { PlaceholderScreen("Progress & Stats", back) }
        composable(Routes.FOCUS_SETTINGS) { PlaceholderScreen("Focus Settings", back) }
        composable(Routes.THEMES) { PlaceholderScreen("Themes", back) }
        composable(Routes.NOTIFICATIONS) { PlaceholderScreen("Notifications", back) }
        composable(Routes.APP_BLOCKING) { PlaceholderScreen("App Blocking", back) }
        composable(Routes.SOUND) { PlaceholderScreen("Sound & White Noise", back) }
        composable(Routes.HELP) { PlaceholderScreen("Help & Feedback", back) }
        composable(Routes.ABOUT) { PlaceholderScreen("About", back) }
        composable(Routes.SETTINGS) { PlaceholderScreen("Settings", back) }
    }
}

// ---------- 3. Temporary screen so every click visibly works ----------

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "‹ Back",
            color = Accent,
            fontSize = 16.sp,
            modifier = Modifier.clickable(onClick = onBack)
        )
        Text(
            title,
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text("This screen is not built yet.", color = TextSecondary, fontSize = 14.sp)
    }
}
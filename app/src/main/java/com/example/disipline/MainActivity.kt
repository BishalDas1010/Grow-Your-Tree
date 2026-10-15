package com.example.disipline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.disipline.UI.AppNavigation
import com.example.disipline.UI.FocusSessionInfo
import com.example.disipline.UI.FocusSessionScreen
import com.example.disipline.UI.FocusSettings
import com.example.disipline.UI.Habits.habit_screen.HabitsRoute
import com.example.disipline.UI.HomeScreen
import com.example.disipline.UI.SegmentType
import com.example.disipline.UI.focusInfoFor
import com.example.disipline.data.HomeViewModel
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.DisiplineTheme
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import com.example.disipline.data.reposotry.HabitRepository



import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.disipline.UI.profile.ProfileNavHost
import com.example.disipline.data.reposotry.ProfileRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProfileRepository.init(applicationContext)

        HabitRepository.init(applicationContext)
        enableEdgeToEdge()

        // Force LIGHT (white) status bar icons, regardless of system theme.
        // false = light icons (white). true = dark icons (black).
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        // Same for the bottom nav bar icons.
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        setContent {
            AppNavigation()


        }
    }
}
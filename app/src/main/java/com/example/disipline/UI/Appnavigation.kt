package com.example.disipline.UI

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.disipline.R
import com.example.disipline.UI.Habits.habit_screen.HabitsRoute
import com.example.disipline.UI.Stas.StatisticsRoute
import com.example.disipline.UI.profile.EditProfileRoute
import com.example.disipline.UI.profile.ProfileRoute
import com.example.disipline.UI.taskcomple.FocusCompleteScreen
import com.example.disipline.Viewmodel.FocusTimerViewModel
import com.example.disipline.data.HomeViewModel
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.TextSecondary

object Routes {
    const val HOME = "home"
    const val FOCUS = "focus"
    const val TIMER = "timer"
    const val FINISH = "finish"
    const val HABITS = "habits"
    const val STATS = "stats"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
}

// =====================================================================
// TAB ICONS
// =====================================================================
private val ICON_HOME = R.drawable.home
private val ICON_FOCUS = R.drawable.concentrate
private val ICON_HABITS = R.drawable.habits
private val ICON_STATS = R.drawable.barchart
private val ICON_PROFILE = R.drawable.user

private data class BottomTab(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", ICON_HOME),
    BottomTab(Routes.FOCUS, "Focus", ICON_FOCUS),
    BottomTab(Routes.HABITS, "Habits", ICON_HABITS),
    BottomTab(Routes.STATS, "Stats", ICON_STATS),
    BottomTab(Routes.PROFILE, "Profile", ICON_PROFILE)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    var focusSettings by remember { mutableStateOf<FocusSettings?>(null) }
    var focusInfo by remember { mutableStateOf<FocusSessionInfo?>(null) }
    var focusHabitId by rememberSaveable { mutableStateOf<Int?>(null) }
    var focusSessionId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Hide bottom bar on full-screen flows (timer, finish, edit profile).
    val showBottomBar = currentDestination?.route != Routes.TIMER &&
            currentDestination?.route != Routes.FINISH &&
            currentDestination?.route != Routes.EDIT_PROFILE

    fun goTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openFocus(
        info: FocusSessionInfo?,
        habitId: Int? = null,
        sessionId: Int? = null
    ) {
        focusInfo = info
        focusHabitId = habitId
        focusSessionId = sessionId
        goTo(Routes.FOCUS)
    }

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Outline)
                    )
                    NavigationBar(
                        containerColor = BgDark,
                        tonalElevation = 0.dp
                    ) {
                        bottomTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy
                                    ?.any { it.route == tab.route } == true,
                                onClick = {
                                    if (tab.route == Routes.FOCUS) openFocus(null)
                                    else goTo(tab.route)
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(tab.icon),
                                        modifier = Modifier.size(30.dp),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(text = tab.label, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Accent,
                                    selectedTextColor = Accent,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ---- HOME ----
            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel()
                val homeState by homeViewModel.uiState.collectAsState()

                HomeScreen(
                    state = homeState,
                    onNotificationClick = { /* open notifications later */ },
                    onStartFocusClick = { openFocus(null) },
                    onGoalClick = { _ -> goTo(Routes.HABITS) }
                )
            }

            // ---- FOCUS ----
            composable(Routes.FOCUS) {
                FocusSessionScreen(
                    sessionInfo = focusInfo,
                    onBack = { goTo(Routes.HOME) },
                    onStartSession = { settings ->
                        focusSettings = settings
                        navController.navigate(Routes.TIMER)
                    }
                )
            }

            // ---- TIMER ----
            composable(Routes.TIMER) {
                val timerViewModel: FocusTimerViewModel = viewModel()
                val timerState by timerViewModel.uiState.collectAsState()

                LaunchedEffect(focusSettings, focusHabitId, focusSessionId) {
                    focusSettings?.let {
                        timerViewModel.start(
                            settings = it,
                            habitId = focusHabitId,
                            sessionId = focusSessionId
                        )
                    }
                }

                LaunchedEffect(timerState.completed) {
                    if (timerState.completed) {
                        navController.navigate(Routes.FINISH) {
                            popUpTo(Routes.TIMER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                FocusTimerScreen(
                    focusSettings = focusSettings,
                    state = timerState,
                    onBack = {
                        timerViewModel.end()
                        navController.popBackStack()
                    },
                    onPauseResume = { timerViewModel.togglePause() },
                    onEnd = {
                        timerViewModel.end()
                        navController.popBackStack()
                    },
                    onWhiteNoiseToggle = { timerViewModel.toggleWhiteNoise() }
                )
            }

            // ---- FINISH ----
            composable(Routes.FINISH) {
                val seconds = (focusSettings?.minutes ?: 25) * 60
                val xp = focusSettings?.minutes ?: 25
                val trees = 1

                FocusCompleteScreen(
                    focusSeconds = seconds,
                    xp = xp,
                    treesGrown = trees,
                    onClose = {
                        focusSettings = null
                        focusHabitId = null
                        focusSessionId = null
                        goTo(Routes.HOME)
                    },
                    onPlantAnother = {
                        navController.navigate(Routes.TIMER) {
                            popUpTo(Routes.FINISH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBackHome = {
                        focusSettings = null
                        focusHabitId = null
                        focusSessionId = null
                        goTo(Routes.HOME)
                    }
                )
            }

            // ---- HABITS ----
            composable(Routes.HABITS) {
                HabitsRoute(
                    onBack = { goTo(Routes.HOME) },
                    onStartSession = { habit, session ->
                        openFocus(
                            info = focusInfoFor(habit, session),
                            habitId = habit.id,
                            sessionId = session.id
                        )
                    }
                )
            }

            // ---- STATS ----
            composable(Routes.STATS) {
                StatisticsRoute(
                    onBack = { goTo(Routes.HOME) },
                    onSettingsClick = { /* open settings later */ }
                )
            }

            // ---- PROFILE (one block only) ----
            composable(Routes.PROFILE) {
                ProfileRoute(
                    onEditClick = { navController.navigate(Routes.EDIT_PROFILE) },
                    onSettingsClick = { /* open settings later */ }
                )
            }

            // ---- EDIT PROFILE (new) ----
            composable(Routes.EDIT_PROFILE) {
                EditProfileRoute(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
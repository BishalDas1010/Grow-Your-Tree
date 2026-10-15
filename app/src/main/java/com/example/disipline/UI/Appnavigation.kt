package com.example.disipline.UI

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
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
import com.example.disipline.UI.blockapps.BlockAppsScreen
import com.example.disipline.UI.permission.PermissionGuideScreen
import com.example.disipline.UI.profile.EditProfileRoute
import com.example.disipline.UI.profile.ProfileActions
import com.example.disipline.UI.profile.ProfileRoute
import com.example.disipline.UI.taskcomple.FocusCompleteScreen
import com.example.disipline.Viewmodel.FocusTimerViewModel
import com.example.disipline.blocker.PermissionGuidePrefs
import com.example.disipline.blocker.isBlockerServiceEnabled
import com.example.disipline.data.HomeViewModel
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.TextPrimary
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
    const val PERMISSION_GUIDE = "permission_guide"

    // ---- Profile sub-screens ----
    const val SETTINGS = "settings"
    const val GOALS = "goals"
    const val PROGRESS = "progress"
    const val FOCUS_SETTINGS = "focus_settings"
    const val THEMES = "themes"
    const val NOTIFICATIONS = "notifications"
    const val APP_BLOCKING = "app_blocking"   // also used by Focus -> "Manage"
    const val SOUND = "sound"
    const val HELP = "help"
    const val ABOUT = "about"
}

// Screens opened on top of a tab. The bottom bar is hidden on these.
private val fullScreenRoutes = setOf(
    Routes.TIMER,
    Routes.FINISH,
    Routes.PERMISSION_GUIDE,
    Routes.EDIT_PROFILE,
    Routes.SETTINGS,
    Routes.GOALS,
    Routes.PROGRESS,
    Routes.FOCUS_SETTINGS,
    Routes.THEMES,
    Routes.NOTIFICATIONS,
    Routes.APP_BLOCKING,
    Routes.SOUND,
    Routes.HELP,
    Routes.ABOUT
)

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

private val tabRoutes = bottomTabs.map { it.route }.toSet()

// Tab -> tab gets a quick fade. Everything else slides.
private fun isTabSwitch(from: String?, to: String?) = from in tabRoutes && to in tabRoutes

private const val SLIDE_MS = 280
private const val FADE_MS = 160

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    var focusSettings by remember { mutableStateOf<FocusSettings?>(null) }
    var focusInfo by remember { mutableStateOf<FocusSessionInfo?>(null) }
    var focusHabitId by rememberSaveable { mutableStateOf<Int?>(null) }
    var focusSessionId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Show the permission guide on launch only if the permission is off
    // and the user has not pressed "Skip for now" before.
    val startRoute = remember {
        if (isBlockerServiceEnabled(context) || PermissionGuidePrefs.wasSkipped(context))
            Routes.HOME
        else
            Routes.PERMISSION_GUIDE
    }

    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute != null && currentRoute !in fullScreenRoutes

    // ---------------------------------------------------------------
    // Navigation helpers
    // ---------------------------------------------------------------

    // True only when the current screen is fully on screen. Stops double taps
    // from navigating (or popping) twice while an animation is still running.
    fun isIdle(): Boolean =
        navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

    // Switch tab without piling up screens; each tab keeps its own state.
    fun goTo(route: String) {
        if (!isIdle() && currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Open a screen on top of the current one (Back returns to it).
    fun push(route: String) {
        if (!isIdle()) return
        navController.navigate(route) { launchSingleTop = true }
    }

    // Safe back: ignores a second tap while the first pop is animating.
    fun back() {
        if (isIdle()) navController.popBackStack()
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

    fun clearSessionAndGoHome() {
        focusSettings = null
        focusHabitId = null
        focusSessionId = null
        goTo(Routes.HOME)
    }

    // ---------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Animated, so the bar slides away instead of popping out
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(tween(SLIDE_MS)) + expandVertically(tween(SLIDE_MS)),
                exit = fadeOut(tween(FADE_MS)) + shrinkVertically(tween(SLIDE_MS))
            ) {
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
            startDestination = startRoute,
            modifier = Modifier
                .padding(innerPadding)
                .background(BgDark),   // no white/black flash between screens

            // ---- Transitions (the default 700 ms cross-fade is what felt slow) ----
            enterTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route))
                    fadeIn(tween(FADE_MS))
                else
                    slideInHorizontally(tween(SLIDE_MS)) { it / 4 } + fadeIn(tween(SLIDE_MS))
            },
            exitTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route))
                    fadeOut(tween(FADE_MS))
                else
                    slideOutHorizontally(tween(SLIDE_MS)) { -it / 5 } + fadeOut(tween(SLIDE_MS))
            },
            popEnterTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route))
                    fadeIn(tween(FADE_MS))
                else
                    slideInHorizontally(tween(SLIDE_MS)) { -it / 5 } + fadeIn(tween(SLIDE_MS))
            },
            popExitTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route))
                    fadeOut(tween(FADE_MS))
                else
                    slideOutHorizontally(tween(SLIDE_MS)) { it / 4 } + fadeOut(tween(SLIDE_MS))
            }
        ) {
            // ---- PERMISSION GUIDE ----
            composable(Routes.PERMISSION_GUIDE) {
                // Opened at launch -> go Home. Opened from Block Apps -> go back there.
                val finishGuide: () -> Unit = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PERMISSION_GUIDE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                PermissionGuideScreen(
                    onGranted = finishGuide,
                    onSkip = {
                        PermissionGuidePrefs.setSkipped(context, true)
                        finishGuide()
                    }
                )
            }

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
                    onOpenBlockApps = { push(Routes.APP_BLOCKING) },
                    onStartSession = { settings ->
                        focusSettings = settings
                        push(Routes.TIMER)
                    }
                )
            }

            // ---- TIMER ----
            composable(Routes.TIMER) {
                val timerViewModel: FocusTimerViewModel = viewModel()
                val timerState by timerViewModel.uiState.collectAsState()

                // Start the countdown ONCE for this screen. Without this guard the
                // timer restarted after a rotation or any recomposition of the screen.
                var started by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    if (!started) {
                        focusSettings?.let {
                            timerViewModel.start(
                                settings = it,
                                habitId = focusHabitId,
                                sessionId = focusSessionId
                            )
                            started = true
                        }
                    }
                }

                // Countdown reached 0 -> show the finish screen
                LaunchedEffect(timerState.completed) {
                    if (timerState.completed) {
                        navController.navigate(Routes.FINISH) {
                            popUpTo(Routes.TIMER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                // System back button behaves like the End button
                BackHandler {
                    timerViewModel.end()
                    back()
                }

                FocusTimerScreen(
                    focusSettings = focusSettings,
                    state = timerState,
                    onBack = {
                        timerViewModel.end()
                        back()
                    },
                    onPauseResume = { timerViewModel.togglePause() },
                    onEnd = {
                        timerViewModel.end()
                        back()
                    },
                    onWhiteNoiseToggle = { timerViewModel.toggleWhiteNoise() }
                )
            }

            // ---- FINISH ----
            composable(Routes.FINISH) {
                val seconds = (focusSettings?.minutes ?: 25) * 60
                val xp = focusSettings?.minutes ?: 25
                val trees = 1

                // Back from the finish screen goes Home, not into the old Focus form
                BackHandler { clearSessionAndGoHome() }

                FocusCompleteScreen(
                    focusSeconds = seconds,
                    xp = xp,
                    treesGrown = trees,
                    onClose = { clearSessionAndGoHome() },
                    onPlantAnother = {
                        navController.navigate(Routes.TIMER) {
                            popUpTo(Routes.FINISH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBackHome = { clearSessionAndGoHome() }
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

            // ---- PROFILE ----
            composable(Routes.PROFILE) {
                ProfileRoute(
                    actions = ProfileActions(
                        onEditProfile = { push(Routes.EDIT_PROFILE) },
                        onGoals = { push(Routes.GOALS) },
                        onProgress = { push(Routes.PROGRESS) },
                        onFocusSettings = { push(Routes.FOCUS_SETTINGS) },
                        onThemes = { push(Routes.THEMES) },
                        onNotifications = { push(Routes.NOTIFICATIONS) },
                        onAppBlocking = { push(Routes.APP_BLOCKING) },
                        onSound = { push(Routes.SOUND) },
                        onHelp = { push(Routes.HELP) },
                        onAbout = { push(Routes.ABOUT) }
                    ),
                    onEditClick = { push(Routes.EDIT_PROFILE) },
                    onSettingsClick = { push(Routes.SETTINGS) }
                )
            }

            // ---- EDIT PROFILE ----
            composable(Routes.EDIT_PROFILE) {
                EditProfileRoute(onBack = { back() })
            }

            // ---- PROFILE SUB-SCREENS ----
            composable(Routes.SETTINGS) {
                EditProfileRoute(onBack = { back() })
            }
            composable(Routes.GOALS) {
                // Back goes to Profile (it used to jump to Home).
                HabitsRoute(
                    onBack = { back() },
                    onStartSession = { habit, session ->
                        openFocus(
                            info = focusInfoFor(habit, session),
                            habitId = habit.id,
                            sessionId = session.id
                        )
                    }
                )
            }
            composable(Routes.PROGRESS) {
                PlaceholderScreen("Progress & Stats") { back() }
            }
            composable(Routes.FOCUS_SETTINGS) {
                PlaceholderScreen("Focus Settings") { back() }
            }
            composable(Routes.THEMES) {
                PlaceholderScreen("Themes") { back() }
            }
            composable(Routes.NOTIFICATIONS) {
                PlaceholderScreen("Notifications") { back() }
            }

            // ---- BLOCK APPS (from Profile and from the Focus screen) ----
            composable(Routes.APP_BLOCKING) {
                BlockAppsScreen(
                    onBack = { back() },
                    onOpenPermissionGuide = { push(Routes.PERMISSION_GUIDE) }
                )
            }

            composable(Routes.SOUND) {
                PlaceholderScreen("Sound & White Noise") { back() }
            }
            composable(Routes.HELP) {
                PlaceholderScreen("Help & Feedback") { back() }
            }
            composable(Routes.ABOUT) {
                PlaceholderScreen("About") { back() }
            }
        }
    }
}

// =====================================================================
// TEMPORARY SCREEN — so every Profile row visibly works
// =====================================================================

@Composable
private fun PlaceholderScreen(title: String, onBack: () -> Unit) {
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
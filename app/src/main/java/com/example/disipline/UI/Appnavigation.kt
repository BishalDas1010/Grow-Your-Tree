package com.example.disipline.UI

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

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
import com.example.disipline.ui.bottombarNavi.BottomNavigationBar
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
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

    const val SETTINGS = "settings"
    const val GOALS = "goals"
    const val PROGRESS = "progress"
    const val FOCUS_SETTINGS = "focus_settings"
    const val THEMES = "themes"
    const val NOTIFICATIONS = "notifications"
    const val APP_BLOCKING = "app_blocking"
    const val SOUND = "sound"
    const val HELP = "help"
    const val ABOUT = "about"
}

/** Screens that hide the bottom bar. */
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

/** Tab routes — kept here so `isTabSwitch` can use them. */
private val tabRoutes = setOf(
    Routes.HOME, Routes.FOCUS, Routes.HABITS, Routes.STATS, Routes.PROFILE
)

private fun isTabSwitch(from: String?, to: String?) =
    from in tabRoutes && to in tabRoutes

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

    val startRoute = remember {
        if (isBlockerServiceEnabled(context) || PermissionGuidePrefs.wasSkipped(context))
            Routes.HOME
        else
            Routes.PERMISSION_GUIDE
    }

    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute != null && currentRoute !in fullScreenRoutes

    // ---- helpers (unchanged) ----
    fun isIdle(): Boolean =
        navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

    fun goTo(route: String) {
        if (!isIdle() && currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun push(route: String) {
        if (!isIdle()) return
        navController.navigate(route) { launchSingleTop = true }
    }

    fun back() {
        if (isIdle()) navController.popBackStack()
    }

    fun openFocus(info: FocusSessionInfo?, habitId: Int? = null, sessionId: Int? = null) {
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

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // 👇 Extracted navbar — the ONLY thing that changed here
            BottomNavigationBar(
                visible = showBottomBar,
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    if (route == Routes.FOCUS) openFocus(null)
                    else goTo(route)
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier
                .padding(innerPadding)
                .background(BgDark),

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
                    onNotificationClick = { },
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

                LaunchedEffect(timerState.completed) {
                    if (timerState.completed) {
                        navController.navigate(Routes.FINISH) {
                            popUpTo(Routes.TIMER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

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
                BackHandler { clearSessionAndGoHome() }
                FocusCompleteScreen(
                    focusSeconds = seconds,
                    xp = xp,
                    treesGrown = 1,
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
                    onSettingsClick = { }
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
            composable(Routes.EDIT_PROFILE) { EditProfileRoute(onBack = { back() }) }

            // ---- PROFILE SUB-SCREENS ----
            composable(Routes.SETTINGS) { EditProfileRoute(onBack = { back() }) }
            composable(Routes.GOALS) {
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
            composable(Routes.PROGRESS)      { PlaceholderScreen("Progress & Stats") { back() } }
            composable(Routes.FOCUS_SETTINGS){ PlaceholderScreen("Focus Settings") { back() } }
            composable(Routes.THEMES)        { PlaceholderScreen("Themes") { back() } }
            composable(Routes.NOTIFICATIONS) { PlaceholderScreen("Notifications") { back() } }
            composable(Routes.APP_BLOCKING) {
                BlockAppsScreen(
                    onBack = { back() },
                    onOpenPermissionGuide = { push(Routes.PERMISSION_GUIDE) }
                )
            }
            composable(Routes.SOUND) { PlaceholderScreen("Sound & White Noise") { back() } }
            composable(Routes.HELP)  { PlaceholderScreen("Help & Feedback") { back() } }
            composable(Routes.ABOUT) { PlaceholderScreen("About") { back() } }
        }
    }
}

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
        Text(title, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("This screen is not built yet.", color = TextSecondary, fontSize = 14.sp)
    }
}
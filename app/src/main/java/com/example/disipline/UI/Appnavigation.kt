//package com.example.disipline.UI
//
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.NavigationBarItemDefaults
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavDestination.Companion.hierarchy
//import androidx.navigation.NavGraph.Companion.findStartDestination
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.disipline.R
//
//
//import com.example.disipline.UI.Habits.habit_screen.HabitsRoute
//
//import com.example.disipline.data.HomeViewModel
//import com.example.disipline.ui.theme.Accent
//import com.example.disipline.ui.theme.BgDark
//import com.example.disipline.ui.theme.OnAccent
//import com.example.disipline.ui.theme.Outline
//import com.example.disipline.ui.theme.TextPrimary
//import com.example.disipline.ui.theme.TextSecondary
//
//object Routes {
//    const val HOME = "home"
//    const val FOCUS = "focus"
//    const val TIMER = "timer"       // opened by the green Start button, not a tab
//    const val HABITS = "habits"
//    const val STATS = "stats"
//    const val PROFILE = "profile"
//}
//
//// =====================================================================
//// TAB ICONS
//// Every tab points to the placeholder below.
//// To use real icons: right click res/drawable -> New -> Vector Asset,
//// then change the line here. Example: R.drawable.ic_home
//// =====================================================================
//private val ICON_HOME = R.drawable.home
//private val ICON_FOCUS = R.drawable.concentrate
//private val ICON_HABITS = R.drawable.habits
//private val ICON_STATS = R.drawable.barchart
//private val ICON_PROFILE = R.drawable.user
//
//private data class BottomTab(
//    val route: String,
//    val label: String,
//    @DrawableRes val icon: Int
//)
//
//// To add a tab: add one line here and one composable(...) in the NavHost below.
//private val bottomTabs = listOf(
//    BottomTab(Routes.HOME, "Home", ICON_HOME),
//    BottomTab(Routes.FOCUS, "Focus", ICON_FOCUS),
//    BottomTab(Routes.HABITS, "Habits", ICON_HABITS),
//    BottomTab(Routes.STATS, "Stats", ICON_STATS),
//    BottomTab(Routes.PROFILE, "Profile", ICON_PROFILE)
//)
//
//@Composable
//fun AppNavigation() {
//    val navController = rememberNavController()
//    val backStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = backStackEntry?.destination
//
//    // The settings chosen on the Focus screen, read by the Timer screen.
//    var focusSettings by remember { mutableStateOf<FocusSettings?>(null) }
//
//    // The habit session chosen with "Start Next Session" (null = plain Focus screen).
//    var focusInfo by remember { mutableStateOf<FocusSessionInfo?>(null) }
//
//    // The tab bar is hidden while the timer screen is open.
//    val showBottomBar = currentDestination?.route != Routes.TIMER
//
//    // Switches tab without piling up screens, and keeps each tab's state.
//    fun goTo(route: String) {
//        navController.navigate(route) {
//            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
//            launchSingleTop = true
//            restoreState = true
//        }
//    }
//
//    // Opens the Focus screen, with session details or with an empty form (null).
//    fun openFocus(info: FocusSessionInfo?) {
//        focusInfo = info
//        goTo(Routes.FOCUS)
//    }
//
//    Scaffold(
//        containerColor = BgDark,
//        // The screens inside already handle the status bar, so only the bar's height is added.
//        contentWindowInsets = WindowInsets(0, 0, 0, 0),
//        bottomBar = {
//            if (showBottomBar) {
//                Column {
//                    // Thin line on top of the bar, like the mockup
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(1.dp)
//                            .background(Outline)
//                    )
//                    NavigationBar(
//                        containerColor = BgDark,
//                        tonalElevation = 0.dp
//                    ) {
//                        bottomTabs.forEach { tab ->
//                            NavigationBarItem(
//                                selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
//                                onClick = {
//                                    // Tapping the Focus tab opens a blank form
//                                    if (tab.route == Routes.FOCUS) openFocus(null) else goTo(tab.route)
//                                },
//                                icon = {
//                                    Icon(
//                                        painter = painterResource(tab.icon),
//                                        modifier = Modifier.size(30.dp),
//                                        contentDescription = tab.label
//                                    )
//                                },
//                                label = { Text(text = tab.label, fontSize = 11.sp) },
//                                colors = NavigationBarItemDefaults.colors(
//                                    selectedIconColor = Accent,
//                                    selectedTextColor = Accent,
//                                    unselectedIconColor = TextSecondary,
//                                    unselectedTextColor = TextSecondary,
//                                    indicatorColor = Color.Transparent   // no pill, like the mockup
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = Routes.HOME,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(Routes.HOME) {
//                // HomeViewModel builds the state from the shared habits data.
//                val homeViewModel: HomeViewModel = viewModel()
//                val homeState by homeViewModel.uiState.collectAsState()
//
//                HomeScreen(
//                    state = homeState,
//                    onNotificationClick = { /* open notifications later */ },
//                    onStartFocusClick = { openFocus(null) },      // big green button -> Focus tab
//                    onGoalClick = { _ -> goTo(Routes.HABITS) }    // tapping a goal -> Habits tab
//                )
//            }
//
//            composable(Routes.FOCUS) {
//                FocusSessionScreen(
//                    sessionInfo = focusInfo,
//                    onBack = { goTo(Routes.HOME) },
//                    onStartSession = { settings ->
//                        focusSettings = settings
//                        navController.navigate(Routes.TIMER)   // Start Session -> Timer screen
//                    }
//                )
//            }
//
//            composable(Routes.TIMER) {
//                FocusTimerScreen(
//                    state = FocusTimerUiState(
//                        taskName = "Read 20 pages",
//                        totalSeconds = 25 * 60,
//                        remainingSeconds = 60,
//                        isRunning = false,
//                        whiteNoiseOn = true
//                    )
//                )
//            }
//
//            composable(Routes.HABITS) {
//                // HabitsRoute creates HabitsViewModel and connects every callback.
//                HabitsRoute(
//                    onBack = { goTo(Routes.HOME) },
//                    // "Start Next Session" -> Focus screen with that session's details
//                    onStartSession = { habit, session ->
//                        openFocus(focusInfoFor(habit, session))
//                    }
//                )
//            }
//        }
//    }
//}
////
////// Placeholder timer screen. Replace with the real countdown later.
////@Composable
////private fun TimerScreen(
////    settings: FocusSettings?,
////    onStop: () -> Unit
////) {
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(24.dp),
////        verticalArrangement = Arrangement.Center,
////        horizontalAlignment = Alignment.CenterHorizontally
////    ) {
////        Text(
////            text = "${settings?.minutes ?: 25} min",
////            color = TextPrimary,
////            fontSize = 48.sp,
////            fontWeight = FontWeight.Bold
////        )
////        Spacer(Modifier.height(8.dp))
////        Text(
////            text = settings?.mode?.label ?: "Focus",
////            color = TextSecondary,
////            fontSize = 16.sp
////        )
////        if (!settings?.task.isNullOrBlank()) {
////            Spacer(Modifier.height(4.dp))
////            Text(
////                text = "Focus on: ${settings?.task}",
////                color = TextSecondary,
////                fontSize = 14.sp
////            )
////        }
////        settings?.plan?.let { plan ->
////            Spacer(Modifier.height(12.dp))
////            Text(
////                text = plan.joinToString("  →  ") {
////                    "${if (it.type == SegmentType.Focus) "Focus" else "Break"} ${it.minutes}m"
////                },
////                color = TextSecondary,
////                fontSize = 13.sp,
////                textAlign = TextAlign.Center
////            )
////        }
////        if (!settings?.blockedApps.isNullOrEmpty()) {
////            Spacer(Modifier.height(8.dp))
////            Text(
////                text = "Blocked: ${settings?.blockedApps?.joinToString(", ")}",
////                color = TextSecondary,
////                fontSize = 12.sp
////            )
////        }
////        Spacer(Modifier.height(32.dp))
////        Box(
////            modifier = Modifier
////                .fillMaxWidth()
////                .height(52.dp)
////                .clip(RoundedCornerShape(50))
////                .background(Accent)
////                .clickable(onClick = onStop),
////            contentAlignment = Alignment.Center
////        ) {
////            Text(
////                text = "Stop",
////                color = OnAccent,
////                fontSize = 16.sp,
////                fontWeight = FontWeight.Bold
////            )
////        }
////    }
////}
////
////// Replace these with your real screens.
////@Composable
////private fun PlaceholderScreen(title: String) {
////    Box(
////        modifier = Modifier.fillMaxSize(),
////        contentAlignment = Alignment.Center
////    ) {
////        Text(text = title, color = TextPrimary)
////    }
////}


package com.example.disipline.UI

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.disipline.Viewmodel.FocusTimerViewModel
import com.example.disipline.data.HomeViewModel
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary

object Routes {
    const val HOME = "home"
    const val FOCUS = "focus"
    const val TIMER = "timer"       // opened by the green Start button, not a tab
    const val HABITS = "habits"
    const val STATS = "stats"
    const val PROFILE = "profile"
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

    // The settings chosen on the Focus screen; read by the Timer route.
    var focusSettings by remember { mutableStateOf<FocusSettings?>(null) }

    // The habit session chosen with "Start Next Session" (null = plain Focus screen).
    var focusInfo by remember { mutableStateOf<FocusSessionInfo?>(null) }

    // Hide the bottom bar while the timer is running.
    val showBottomBar = currentDestination?.route != Routes.TIMER

    fun goTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openFocus(info: FocusSessionInfo?) {
        focusInfo = info
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

            composable(Routes.TIMER) {
                // ViewModel scoped to this destination; lives until TIMER is popped.
                val timerViewModel: FocusTimerViewModel = viewModel()
                val timerState by timerViewModel.uiState.collectAsState()

                // Convert FocusSettings -> FocusTimerUiState once, when settings arrive.
                LaunchedEffect(focusSettings) {
                    focusSettings?.let { timerViewModel.start(it) }
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

            composable(Routes.HABITS) {
                HabitsRoute(
                    onBack = { goTo(Routes.HOME) },
                    onStartSession = { habit, session ->
                        openFocus(focusInfoFor(habit, session))
                    }
                )
            }

            composable(Routes.STATS) { PlaceholderScreen("Stats") }

            composable(Routes.PROFILE) { PlaceholderScreen("Profile") }
        }
    }
}

// Placeholder for tabs that don't have a real screen yet.
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = TextPrimary)
    }
}
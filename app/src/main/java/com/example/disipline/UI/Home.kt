package com.example.disipline.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R
import com.example.disipline.data.HomeUiState
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.IconCircle
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import java.util.Calendar


// One line in "Today's Goals".
data class Goal(
    val id: Int,
    val title: String,
    val done: Boolean       // true = filled green checkbox
)




// Picks "Good Morning / Afternoon / Evening" from the phone's clock.
private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}


private val ICON_BELL = R.drawable.ic_launcher_foreground
private val ICON_ARROW = R.drawable.ic_launcher_foreground
private val ICON_CHECK = R.drawable.ic_launcher_foreground

// Red dot on the bell (notification badge)
private val BadgeRed = Color(0xFFE5484D)

private data class HomeNavItem(val label: String, val iconRes: Int)

// The label is the value sent to onNavSelected.
private val homeNavItems = listOf(
    HomeNavItem("Home", R.drawable.ic_launcher_foreground),
    HomeNavItem("Focus", R.drawable.ic_launcher_foreground),
    HomeNavItem("Habits", R.drawable.ic_launcher_foreground),
    HomeNavItem("Stats", R.drawable.ic_launcher_foreground),
    HomeNavItem("Profile", R.drawable.ic_launcher_foreground)
)

@Composable
fun HomeScreen(
    state: HomeUiState,                          // data to show
    selectedNav: String = "Home",                // highlighted bottom tab
    onNotificationClick: () -> Unit = {},        // bell tapped
    onStartFocusClick: () -> Unit = {},          // big green button tapped
    onGoalClick: (Goal) -> Unit = {},            // a goal row tapped
    onNavSelected: (String) -> Unit = {}         // bottom tab tapped
) {
    // Scaffold = content area + bottom bar
    Scaffold(
        containerColor = BgDark,

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting + bell
            item {
                HomeHeader(
                    greeting = greetingForNow(),
                    userName = state.userName,
                    messgae = "keep growing",
                    onNotificationClick = onNotificationClick
                )
            }

            // Quote card
            item { QuoteCard(quote = state.quote) }

            // Three stat cards
            item {
                StatsRow(
                    focusSessions = state.focusSessions,
                    focusTime = state.focusTime,
                    treesGrown = state.treesGrown
                )
            }

            // Big green button
            item { StartFocusButton(onClick = onStartFocusClick) }

            // "Today's Goals" title with the "2/4" counter
            item {
                GoalsHeader(
                    done = state.goals.count { it.done },
                    total = state.goals.size
                )
            }

            // One row per goal
            items(state.goals, key = { it.id }) { goal ->
                GoalRow(goal = goal, onClick = { onGoalClick(goal) })
            }
        }
    }
}

// =====================================================================
// 4. UI PIECES
// =====================================================================

// ---- Top: "Good Morning / Alex 👋" on the left, bell on the right ----
@Composable
private fun HomeHeader(
    greeting: String,
    userName: String,
    messgae: String?,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = greeting, color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "$userName 👋",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$messgae ",
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Box stacks the red dot on top of the bell
        Box {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    painter = painterResource(ICON_BELL),
                    contentDescription = "Notifications",
                    tint = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BadgeRed)
            )
        }
    }
}

// ---- Quote card: text on the left, plant badge on the right ----
@Composable
private fun QuoteCard(quote: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // weight(1f) = text takes all leftover width, badge stays on the right
        Text(
            text = quote,
            color = TextPrimary,
            fontSize = 18.sp,
            lineHeight = 30.sp,
            modifier = Modifier.weight(1.5f)
        )

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(IconCircle),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌱", fontSize = 28.sp)
        }
    }
}

// ---- Row of three equal stat cards ----
@Composable
private fun StatsRow(focusSessions: Int, focusTime: String, treesGrown: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // weight(1f) inside StatCard makes all three the same width
        StatCard(emoji = "⏱️", value = "$focusSessions", label = "Focus Sessions", modifier = Modifier.weight(1f))
        StatCard(emoji = "📊", value = focusTime, label = "Focus Time", modifier = Modifier.weight(1f))
        StatCard(emoji = "🌱", value = "$treesGrown", label = "Trees Grown", modifier = Modifier.weight(1f))
    }
}

// ---- One stat card: icon, big number, small label ----
@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier        // the parent passes weight(1f) through here
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(IconCircle),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = value,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
    }
}

// ---- Big green "Start Focus Session  →" button ----
@Composable
private fun StartFocusButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))           // pill shape
            .background(Accent)
            .clickable(onClick = onClick),          // after clip so the ripple stays rounded
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Start Focus Session",
            color = OnAccent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(ICON_ARROW),
            contentDescription = null,              // decorative
            tint = OnAccent,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ---- "Today's Goals ........ 2/4" ----
@Composable
private fun GoalsHeader(done: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Today's Goals",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)          // pushes the counter to the right
        )
        Text(
            text = "$done/$total",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

// ---- One goal: checkbox + title ----
@Composable
private fun GoalRow(goal: Goal, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom checkbox: filled green when done, grey outline when not
        if (goal.done) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(ICON_CHECK),
                    contentDescription = "Completed",
                    tint = OnAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(BorderStroke(2.dp, Outline), RoundedCornerShape(6.dp))
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(text = goal.title, color = TextPrimary, fontSize = 16.sp)
    }
}

// ---- Bottom navigation bar (same look as the Habits screen) ----
// TIP: later, move this into its own file (AppBottomBar.kt) and use it on
// every screen, so you only maintain

// =====================================================================
// 5. PREVIEW - fake data; delete when you wire in the real ViewModel.
// =====================================================================

private val sampleHomeState = HomeUiState(
    userName = "Alex",
    quote = "\"Discipline today creates the life you want tomorrow.\"",
    focusSessions = 3,
    focusTime = "2h 15m",
    treesGrown = 5,
    goals = listOf(
        Goal(1, "Study DSA", true),
        Goal(2, "Read a book", true),
        Goal(3, "Exercise", false),
        Goal(4, "Learn something new", false)
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(state = sampleHomeState)
}
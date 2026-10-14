package com.example.disipline.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R
import com.example.disipline.TreeDesign
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
    val subText: String,
    val done: Boolean
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
private val ICON_PLAY = R.drawable.ic_launcher_foreground

private val BadgeRed = Color(0xFFE5484D)

@Composable
fun HomeScreen(
    state: HomeUiState,
    selectedNav: String = "Home",
    onNotificationClick: () -> Unit = {},
    onStartFocusClick: () -> Unit = {},
    onGoalClick: (Goal) -> Unit = {},
    onNavSelected: (String) -> Unit = {}
) {
    Scaffold(
        containerColor = BgDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with buttons
            item {
                HomeHeader(
                    greeting = greetingForNow(),
                    userName = state.userName,
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

            // "Today's Goals" title
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

            // Quick Actions Section
            item {
                Text(
                    text = "Quick Actions",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                QuickActionsRow()
            }


        }
    }
}

// =====================================================================
// UI PIECES
// =====================================================================

@Composable
private fun HomeHeader(
    greeting: String,
    userName: String,
    onNotificationClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Row: ✕, ⋮, ⬇, Share


        Spacer(Modifier.height(8.dp))

        // Greeting Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = greeting, color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "$userName 👋",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Subtext
        Text(
            text = "Keep going, you're doing great!",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}


@Composable
private fun QuoteCard(quote: String) {
    // 1. Use a Box as the root so we can stack the text and the tree
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
    ) {
        // 2. Place the tree first so it sits behind the text (or in front if you prefer)
        // Align it to the BottomEnd (bottom right corner)
        TreeDesign(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(width = 130.dp, height = 90.dp)
            // You can adjust the size above to make it bigger or smaller
        )

        // 3. The text column sits on top of the tree
        Column(
            modifier = Modifier
                .padding(16.dp)
                // 4. Limit the text width to 75% so it doesn't overlap the tree
                .fillMaxWidth(0.75f)
        ) {
            // "DAILY MOTIVATION" label
            Text(
                text = "DAILY MOTIVATION",
                color = Accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = quote,
                color = TextPrimary,
                fontSize = 18.sp,
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(12.dp))


        }
    }
}
@Composable
private fun StatsRow(focusSessions: Int, focusTime: String, treesGrown: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(emoji = "⏱️", value = "$focusSessions", label = "Focus Sessions", modifier = Modifier.weight(1f))
        StatCard(emoji = "📊", value = focusTime, label = "Focus Time", modifier = Modifier.weight(1f))
        StatCard(emoji = "🌱", value = "$treesGrown", label = "Trees Grown", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
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

@Composable
private fun StartFocusButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(Accent)
            .clickable(onClick = onClick),
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
        // Play icon placeholder
        Text("▶", color = OnAccent, fontSize = 14.sp)
    }
}

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
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$done of $total completed",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun GoalRow(goal: Goal, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Column(modifier = Modifier.weight(1f)) {
            Text(text = goal.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = goal.subText, color = TextSecondary, fontSize = 12.sp)
        }

        // Chevron Right
        Text(">", color = TextSecondary, fontSize = 18.sp)
    }
}

@Composable
private fun QuickActionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // keep items off the screen edges
        horizontalArrangement = Arrangement.SpaceBetween // SpaceEvenly also works
    ) {
        // PROBLEM 1 FIXED: parameter is named `icon`, not `emoji`.
        // We're passing a Painter (from painterResource), so the call
        // must match the function signature — no `emoji =` keyword here.
        QuickActionItem(icon = painterResource(R.drawable.target), label = "Focus Mode")
        QuickActionItem(icon = painterResource(R.drawable.googledocs), label = "Add Habit")
        QuickActionItem(icon = painterResource(R.drawable.spreadsheetapp),   label = "View Stats")
        QuickActionItem(icon = painterResource(R.drawable.leaf1),      label = "Plant Tree")
    }
}

@Composable
private fun QuickActionItem(
    icon: Painter,      // PROBLEM 2: this is a Painter, not an emoji String.
    // If you ever want text emojis, rename this param
    // to `emoji: String` and use Text instead of Image.
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                // PROBLEM 3: without contentScale, non-square or oversized
                // PNGs can stretch. Fit keeps aspect ratio inside the 28dp box.
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center // long labels center nicely
        )
    }
}


// =====================================================================
// PREVIEW
// =====================================================================

private val sampleHomeState = HomeUiState(
    userName = "Alex",
    quote = "\"Discipline today creates the life you want tomorrow.\"",
    focusSessions = 2,
    focusTime = "2h 0m",
    treesGrown = 2,
    goals = listOf(
        Goal(1, "Study DSA", "Daily target • 2 sessions", false)
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(state = sampleHomeState)
}
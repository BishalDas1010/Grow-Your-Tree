package com.example.disipline.UI.profile

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disipline.R
import com.example.disipline.UI.Them.RotatingChevron
import com.example.disipline.UI.Them.ThemeDropdownSurface

import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.AppColors
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import com.example.disipline.ui.theme.ThemeManager
import com.example.disipline.ui.theme.ThemePalette

// =====================================================================
// DATA MODELS
// =====================================================================

data class ProfileStat(
    @DrawableRes val icon: Int,
    val value: String,
    val label: String
)

data class ProfileMenuItem(
    @DrawableRes val icon: Int,
    val title: String,
    val subtitle: String,
    val showsThemeDropdown: Boolean = false,   // 👈 NEW
    val onClick: () -> Unit = {}
)

data class ProfileMenuSection(
    val title: String,
    val items: List<ProfileMenuItem>
)

data class ProfileData(
    val name: String,
    val subtitle: String,
    val quote: String,
    val avatarPath: String?,
    val avatarFallbackRes: Int,
    val bannerRes: Int,
    val stats: List<ProfileStat>,
    val sections: List<ProfileMenuSection>
)

/**
 * One callback per clickable menu row.
 */
data class ProfileActions(
    val onEditProfile: () -> Unit = {},
    val onGoals: () -> Unit = {},
    val onProgress: () -> Unit = {},
    val onFocusSettings: () -> Unit = {},
    val onThemes: () -> Unit = {},                       // opens full Themes screen (optional)
    val onThemeSelected: (ThemePalette) -> Unit = {},    // 👈 NEW — fired by dropdown
    val onNotifications: () -> Unit = {},
    val onAppBlocking: () -> Unit = {},
    val onSound: () -> Unit = {},
    val onHelp: () -> Unit = {},
    val onAbout: () -> Unit = {}
)

// =====================================================================
// ROUTE
// =====================================================================

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = viewModel(),
    actions: ProfileActions = ProfileActions(),
    onEditClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val entity by viewModel.profile.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.updateAvatar(it) } }

    val data = remember(entity, stats, actions) {
        buildProfileData(
            name = entity?.name?.takeIf { it.isNotBlank() } ?: "Your Name",
            subtitle = entity?.subtitle?.takeIf { it.isNotBlank() }
                ?: "Student • Learner • Builder",
            quote = entity?.quote?.takeIf { it.isNotBlank() }
                ?: "Discipline today creates the life you want tomorrow.",
            avatarPath = entity?.avatarPath,
            stats = stats,
            actions = actions
        )
    }

    ProfileScreen(
        data = data,
        onAvatarClick = { picker.launch("image/*") },
        onEditClick = onEditClick,
        onSettingsClick = onSettingsClick,
        onThemeSelected = actions.onThemeSelected
    )
}

// =====================================================================
// SCREEN
// =====================================================================

@Composable
fun ProfileScreen(
    data: ProfileData,
    onAvatarClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onThemeSelected: (ThemePalette) -> Unit = {}
) {
    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            ProfileHeader(onEditClick, onSettingsClick)

            Spacer(Modifier.height(20.dp))

            ProfileCard(data = data, onAvatarClick = onAvatarClick)

            Spacer(Modifier.height(20.dp))

            StatsRow(data.stats)

            data.sections.forEach { section ->
                Spacer(Modifier.height(24.dp))
                SectionHeader(section.title)
                Spacer(Modifier.height(10.dp))
                MenuSection(
                    items = section.items,
                    onThemeSelected = onThemeSelected
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// HEADER
// =====================================================================

@Composable
private fun ProfileHeader(
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Profile",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Build a better you", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text("🍃", fontSize = 14.sp)
            }
        }
        IconCircleButton(R.drawable.ic_launcher_foreground, "Edit", onEditClick)
        Spacer(Modifier.width(10.dp))
        IconCircleButton(R.drawable.ic_launcher_foreground, "Settings", onSettingsClick)
    }
}

@Composable
private fun IconCircleButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Surface)
            .border(1.dp, Outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// =====================================================================
// PROFILE CARD
// =====================================================================

@Composable
private fun ProfileCard(data: ProfileData, onAvatarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(22.dp))
    ) {
        Image(
            painter = painterResource(data.bannerRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.ElevatedSurface.copy(alpha = 0.8f),
                            AppColors.Background.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(data.avatarPath, data.avatarFallbackRes, onAvatarClick)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        data.name,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(data.subtitle, color = TextSecondary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text("🍃", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = data.quote,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Avatar(avatarPath: String?, fallbackRes: Int, onClick: () -> Unit) {
    val bitmap: ImageBitmap? = remember(avatarPath) {
        avatarPath?.let { path ->
            runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Accent)
                .padding(3.dp)
                .clip(CircleShape)
                .background(BgDark)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(fallbackRes),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Accent)
                .border(2.dp, BgDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Change photo",
                tint = OnAccent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// =====================================================================
// STATS ROW
// =====================================================================

@Composable
private fun StatsRow(stats: List<ProfileStat>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { StatChip(it, Modifier.weight(1f)) }
    }
}

@Composable
private fun StatChip(stat: ProfileStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(stat.icon),
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(22.dp)
        )
        Text(
            stat.value,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            stat.label,
            color = TextSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// =====================================================================
// SECTIONS
// =====================================================================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun MenuSection(
    items: List<ProfileMenuItem>,
    onThemeSelected: (ThemePalette) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(20.dp))
    ) {
        items.forEachIndexed { index, item ->
            MenuRow(item = item, onThemeSelected = onThemeSelected)
            if (index < items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 72.dp)
                        .background(Outline)
                )
            }
        }
    }
}

// =====================================================================
// MENU ROW  (single, merged version with optional theme dropdown)
// =====================================================================

@Composable
private fun MenuRow(
    item: ProfileMenuItem,
    onThemeSelected: (ThemePalette) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (item.showsThemeDropdown) expanded = !expanded
                    else item.onClick()
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(item.subtitle, color = TextSecondary, fontSize = 12.sp)
            }

            // Rotating chevron for the theme row; static arrow otherwise
            if (item.showsThemeDropdown) {
                RotatingChevron(expanded = expanded)
            } else {
                Text(
                    "›",
                    color = TextSecondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Inline theme dropdown
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)) + expandVertically(tween(220)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(200))
        ) {
            ThemeDropdownSurface(
                activeId = ThemeManager.active.id,
                onPick = { palette ->
                    ThemeManager.apply(palette)   // re-colors the entire app
                    onThemeSelected(palette)      // optional persistence / side-effects
                    expanded = false
                },
                modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 12.dp)
            )
        }
    }
}

// =====================================================================
// DATA BUILDER
// =====================================================================

private fun buildProfileData(
    name: String,
    subtitle: String,
    quote: String,
    avatarPath: String?,
    stats: ProfileStats,
    actions: ProfileActions
): ProfileData = ProfileData(
    name = name,
    subtitle = subtitle,
    quote = quote,
    avatarPath = avatarPath,
    avatarFallbackRes = R.drawable.habits,
    bannerRes = R.drawable.leaf1,
    stats = listOf(
        ProfileStat(R.drawable.leaf1, stats.focusSessions.toString(), "Focus Sessions"),
        ProfileStat(R.drawable.stopwatch, stats.totalFocusLabel, "Total Focus"),
        ProfileStat(R.drawable.barchart, stats.habitsTracked.toString(), "Habits Tracked"),
        ProfileStat(R.drawable.leaf1, stats.treesGrown.toString(), "Trees Grown")
    ),
    sections = listOf(
        ProfileMenuSection(
            "Account",
            listOf(
                ProfileMenuItem(
                    R.drawable.habits, "Edit Profile",
                    "Update your name, photo and bio",
                    onClick = actions.onEditProfile
                ),
                ProfileMenuItem(
                    R.drawable.stopwatch, "My Goals",
                    "Set and track your personal goals",
                    onClick = actions.onGoals
                ),
                ProfileMenuItem(
                    R.drawable.barchart, "Progress & Stats",
                    "View your focus, habits and growth",
                    onClick = actions.onProgress
                )
            )
        ),
        ProfileMenuSection(
            "App Settings",
            listOf(
                ProfileMenuItem(
                    R.drawable.concentrate, "Focus Settings",
                    "Timer, break time and notifications",
                    onClick = actions.onFocusSettings
                ),
                ProfileMenuItem(
                    R.drawable.home, "Themes",
                    ThemeManager.active.name,   // 👈 live subtitle: current theme name
                    showsThemeDropdown = true,  // 👈 opens dropdown instead of navigating
                    onClick = actions.onThemes  // (unused while dropdown is open)
                ),
                ProfileMenuItem(
                    R.drawable.user, "Notifications",
                    "Session reminders and alerts",
                    onClick = actions.onNotifications
                ),
                ProfileMenuItem(
                    R.drawable.habits, "App Blocking",
                    "Manage blocked apps",
                    onClick = actions.onAppBlocking
                ),
                ProfileMenuItem(
                    R.drawable.leaf1, "Sound & White Noise",
                    "Ambient sounds for better focus",
                    onClick = actions.onSound
                )
            )
        ),
        ProfileMenuSection(
            "Support",
            listOf(
                ProfileMenuItem(
                    R.drawable.concentrate, "Help & Feedback",
                    "Get help or share your feedback",
                    onClick = actions.onHelp
                ),
                ProfileMenuItem(
                    R.drawable.habits, "About",
                    "Version 1.0.0",
                    onClick = actions.onAbout
                )
            )
        )
    )
)

// =====================================================================
// PREVIEW
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF101814, widthDp = 390, heightDp = 900)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        data = buildProfileData(
            name = "Vishal Das",
            subtitle = "Student • Learner • Builder",
            quote = "Discipline today creates the life you want tomorrow.",
            avatarPath = null,
            stats = ProfileStats(
                focusSessions = 7,
                totalFocusMinutes = 270,
                habitsTracked = 12,
                treesGrown = 3
            ),
            actions = ProfileActions()
        )
    )
}
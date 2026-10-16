package com.example.disipline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.disipline.data.NavItem
import com.example.disipline.ui.components.DisciplineBottomBar

private val defaultNavItems = listOf(
    NavItem("Home", R.drawable.ic_launcher_foreground),
    NavItem("Focus", R.drawable.ic_launcher_foreground),
    NavItem("Habits", R.drawable.ic_launcher_foreground),
    NavItem("Stats", R.drawable.ic_launcher_foreground),
    NavItem("Profile", R.drawable.ic_launcher_foreground)
)

@Composable
fun HabitsBottomBar(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DisciplineBottomBar(
        navItems = defaultNavItems,
        selectedRoute = selected,
        onItemSelected = onSelected,
        modifier = modifier
    )
}

package com.example.disipline

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.disipline.data.NavItem
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.TextSecondary

private val navItems = listOf(
    NavItem("Home", R.drawable.ic_launcher_foreground),
    NavItem("Focus", R.drawable.ic_launcher_foreground),
    NavItem("Habits", R.drawable.ic_launcher_foreground),
    NavItem("Stats", R.drawable.ic_launcher_foreground),
    NavItem("Profile", R.drawable.ic_launcher_foreground)
)

@Composable
private fun HabitsBottomBar(selected: String, onSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = BgDark,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = item.label == selected,
                onClick = { onSelected(item.label) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, fontSize = 12.sp) },
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


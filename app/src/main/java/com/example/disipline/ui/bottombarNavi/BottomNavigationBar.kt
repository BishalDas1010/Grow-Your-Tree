package com.example.disipline.ui.bottombarNavi

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.disipline.R
import com.example.disipline.ui.theme.AppColors

/**
 * All routes the bottom bar can point at.
 * Keep these in sync with the ones registered in AppNavigation().
 */
object BottomTabRoutes {
    const val HOME = "home"
    const val FOCUS = "focus"
    const val HABITS = "habits"
    const val STATS = "stats"
    const val PROFILE = "profile"
}

private data class BottomTab(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
)

private val bottomTabs = listOf(
    BottomTab(BottomTabRoutes.HOME,    "Home",    R.drawable.home),
    BottomTab(BottomTabRoutes.FOCUS,   "Focus",   R.drawable.concentrate),
    BottomTab(BottomTabRoutes.HABITS,  "Habits",  R.drawable.habits),
    BottomTab(BottomTabRoutes.STATS,   "Stats",   R.drawable.barchart),
    BottomTab(BottomTabRoutes.PROFILE, "Profile", R.drawable.user)
)

/**
 * Floating sage-green pill navbar matching the Sage & Forest design.
 *
 * @param visible         slide/fade the bar in & out
 * @param currentRoute    the route the NavController is currently on
 * @param onTabSelected   called with the tab's route string
 */
@Composable
fun BottomNavigationBar(
    visible: Boolean,
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + expandVertically(tween(280)),
        exit = fadeOut(tween(160)) + shrinkVertically(tween(280)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(28.dp),
            color = AppColors.Surface,
            border = BorderStroke(1.dp, AppColors.Outline),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomTabs.forEach { tab ->
                    NavBarItem(
                        tab = tab,
                        isSelected = tab.route == currentRoute,
                        onClick = { onTabSelected(tab.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.PrimaryAccent.copy(alpha = 0.18f)
        else Color.Transparent,
        animationSpec = tween(220),
        label = "navBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.PrimaryAccent
        else AppColors.SecondaryText,
        animationSpec = tween(220),
        label = "navContent"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = tab.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
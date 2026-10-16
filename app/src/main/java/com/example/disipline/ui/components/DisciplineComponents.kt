package com.example.disipline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.data.NavItem
import com.example.disipline.ui.theme.AppColors

/**
 * Reusable Card component for DisciplineX.
 */
@Composable
fun DisciplineCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.Surface,
    borderColor: Color = AppColors.Outline,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .then(
            if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, shape)
            else Modifier
        )
        .then(
            if (onClick != null) Modifier.clickable { onClick() }
            else Modifier
        )

    Box(modifier = baseModifier) {
        content()
    }
}

/**
 * Reusable Button component for DisciplineX.
 */
@Composable
fun DisciplineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.PrimaryButton,
    contentColor: Color = AppColors.ButtonText,
    enabled: Boolean = true,
    cornerRadius: Dp = 12.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Standardized Section Title component.
 */
@Composable
fun DisciplineSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppColors.PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = AppColors.PrimaryAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

/**
 * Reusable DisciplineX Navigation Bottom Bar.
 */
@Composable
fun DisciplineBottomBar(
    navItems: List<NavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = AppColors.Background,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val isSelected = item.label.equals(selectedRoute, ignoreCase = true)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.label) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.PrimaryAccent,
                    selectedTextColor = AppColors.PrimaryAccent,
                    unselectedIconColor = AppColors.SecondaryText,
                    unselectedTextColor = AppColors.SecondaryText,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

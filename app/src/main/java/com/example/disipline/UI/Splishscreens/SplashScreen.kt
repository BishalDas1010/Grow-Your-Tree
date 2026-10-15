package com.example.disipline.UI.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Illustration
        ForestBackground(sunY = 0.30f)

        // Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            // Leaf logo
            Text(text = "🌿", fontSize = 56.sp)

            Spacer(Modifier.height(12.dp))

            Text(
                "DisciplineX",
                color = TextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Focus Today",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Build a Better You",
                color = TextSecondary,
                fontSize = 15.sp
            )

            Spacer(Modifier.weight(1f))

            // Feature row: Focus / Grow / Achieve
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SplashFeature(icon = "⏻", label = "Focus")
                SplashFeature(icon = "🌱", label = "Grow")
                SplashFeature(icon = "✓", label = "Achieve")
            }

            Spacer(Modifier.height(28.dp))

            // Get Started button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Accent)
                    .clickable(onClick = onGetStarted),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Get Started  →",
                    color = OnAccent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "A focused life, a brighter tomorrow.",
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SplashFeature(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, color = Accent, fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(text = label, color = TextPrimary, fontSize = 12.sp)
    }
}
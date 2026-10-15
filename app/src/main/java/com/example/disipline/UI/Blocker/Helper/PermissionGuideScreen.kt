package com.example.disipline.UI.permission

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.disipline.blocker.isBlockerServiceEnabled
import com.example.disipline.ui.theme.*

@Composable
fun PermissionGuideScreen(
    onGranted: () -> Unit,   // permission is on -> continue into the app
    onSkip: () -> Unit       // user chose "Skip for now"
) {
    val context = LocalContext.current
    var showHelp by remember { mutableStateOf(false) }

    // Every time the user returns from Settings, check again and continue if allowed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var hasNavigated = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !hasNavigated && isBlockerServiceEnabled(context)) {
                hasNavigated = true
                onGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(IconCircle),
                contentAlignment = Alignment.Center
            ) { Text("🛡️", fontSize = 40.sp) }

            Spacer(Modifier.height(20.dp))
            Text(
                "Allow App Blocking",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "To block distracting apps during a focus session, Disipline needs " +
                        "the Accessibility permission. It is only used to notice when a " +
                        "blocked app opens. Nothing you type or see is read or stored.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            StepCard(1, "Tap “Open Settings” below")
            Spacer(Modifier.height(10.dp))
            StepCard(2, "Choose “Installed apps” or “Downloaded apps”")
            Spacer(Modifier.height(10.dp))
            StepCard(3, "Tap “Disipline App Blocker”")
            Spacer(Modifier.height(10.dp))
            StepCard(4, "Turn it On, then tap “Allow”")

            Spacer(Modifier.height(16.dp))

            // Help for the greyed-out switch
            Text(
                if (showHelp) "Hide help ▲" else "Switch is greyed out? ▼",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showHelp = !showHelp }
                    .padding(8.dp)
            )
            if (showHelp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface)
                        .padding(14.dp)
                ) {
                    Text(
                        "Android blocks this for apps installed outside the Play Store:\n\n" +
                                "1. Long-press the Disipline icon and tap App info\n" +
                                "2. Tap the ⋮ menu (top right)\n" +
                                "3. Tap “Allow restricted settings”\n" +
                                "4. Come back and turn the switch on",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Primary button
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Accent)
                    .clickable {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Open Settings", color = OnAccent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSkip) {
                Text("Skip for now", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun StepCard(number: Int, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Accent),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = OnAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(text, color = TextPrimary, fontSize = 15.sp)
    }
}

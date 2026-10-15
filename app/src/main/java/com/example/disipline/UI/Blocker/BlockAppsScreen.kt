package com.example.disipline.UI.blockapps

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disipline.UI.Habits.habit_screen.ScreenTopBar
import com.example.disipline.ui.theme.*

@Composable
fun BlockAppsScreen(
    onBack: () -> Unit,
    viewModel: BlockAppsViewModel = viewModel(),
    onOpenPermissionGuide: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var showInfo by remember { mutableStateOf(false) }

    // Re-check the accessibility permission when returning from Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAccessibility()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    val selectedApps = state.selectedApps

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenTopBar(title = "Block Apps", onBack = onBack)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ---- Enable App Blocker card ----
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Enable App Blocker",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Prevent distracting apps during focus sessions.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = state.blockerEnabled,
                            onCheckedChange = { on ->
                                viewModel.onToggleBlocker(on)
                                // Turning on without permission -> send user to enable it
                                if (on && !state.accessibilityGranted) openAccessibilitySettings()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnAccent,
                                checkedTrackColor = Accent,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Surface,
                                uncheckedBorderColor = Outline
                            )
                        )
                    }
                }

                // ---- Permission warning (only if needed) ----
                if (state.blockerEnabled && !state.accessibilityGranted) {
                    item {
                        Text(
                            "Accessibility permission is off. Tap to enable it.",
                            color = Accent,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { openAccessibilitySettings() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // ---- Selected Apps header ----
                item {
                    Text(
                        "Selected Apps",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                            CircularProgressIndicator(color = Accent)
                        }
                    }
                } else if (selectedApps.isEmpty()) {
                    item {
                        Text(
                            "No apps selected yet. Tap Add Apps.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(selectedApps, key = { it.packageName }) { app ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Surface)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                app.label,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            // Minus (remove) button
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(1.dp, Outline), CircleShape)
                                    .clickable { viewModel.onRemoveApp(app.packageName) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("−", color = TextPrimary, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // ---- Add Apps button ----
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                            .border(BorderStroke(1.5.dp, Accent), RoundedCornerShape(50))
                            .clickable(enabled = !state.loading) { viewModel.onAddAppsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+  Add Apps",
                            color = Accent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ---- How it works card ----
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .clickable { showInfo = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(IconCircle),
                            contentAlignment = Alignment.Center
                        ) { Text("💡", fontSize = 16.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "How it works",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Uses Android's accessibility permissions to block selected apps during focus sessions.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text("›", color = TextSecondary, fontSize = 24.sp)
                    }
                }
            }
        }
    }

    // ---- App picker bottom sheet ----
    if (state.showPicker) {
        AppPickerSheet(
            state = state,
            onDismiss = viewModel::onDismissPicker,
            onQueryChange = viewModel::onQueryChange,
            onToggle = viewModel::onTogglePending,
            onConfirm = viewModel::onConfirmAdd
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            containerColor = Surface,
            title = { Text("How it works", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "While a focus timer is running, the app watches which app opens. " +
                            "If it is on your list, you are sent back to the home screen. " +
                            "Blocking stops when the timer ends or you press End.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Got it", color = Accent) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(
    state: BlockAppsUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val apps = state.pickerApps

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgDark,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Add Apps",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search apps", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Outline,
                    cursorColor = Accent
                )
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (apps.isEmpty()) {
                    item {
                        Text(
                            "No apps found.",
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
                items(apps, key = { it.packageName }) { app ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface)
                            .clickable { onToggle(app.packageName) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            app.label,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = app.packageName in state.pending,
                            onCheckedChange = { onToggle(app.packageName) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Accent,
                                checkmarkColor = OnAccent,
                                uncheckedColor = Outline
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            val count = state.pending.size
            Button(
                onClick = onConfirm,
                enabled = count > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = OnAccent,
                    disabledContainerColor = Surface,
                    disabledContentColor = TextSecondary
                )
            ) {
                Text(
                    if (count == 0) "Select apps to add" else "Add $count app${if (count > 1) "s" else ""}",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
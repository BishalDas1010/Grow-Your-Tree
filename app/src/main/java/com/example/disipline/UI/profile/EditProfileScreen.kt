package com.example.disipline.UI.profile

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary

// =====================================================================
// EDIT PROFILE SCREEN
//
// Route owns the ViewModel + photo picker.
// Screen is pure UI, takes current values + callbacks.
//
//   Save → ViewModel.updateName / updateSubtitle / updateQuote / updateAvatar
//        → Room row updates → ProfileRepository.profile emits
//        → ProfileScreen recomposes with new values
// =====================================================================

@Composable
fun EditProfileRoute(
    viewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val entity by viewModel.profile.collectAsState()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.updateAvatar(it) } }

    EditProfileScreen(
        initialName = entity?.name.orEmpty(),
        initialSubtitle = entity?.subtitle.orEmpty(),
        initialQuote = entity?.quote.orEmpty(),
        avatarPath = entity?.avatarPath,
        onPickPhoto = { picker.launch("image/*") },
        onSave = { name, subtitle, quote ->
            // Trim so accidental spaces don't get persisted.
            viewModel.updateName(name.trim())
            viewModel.updateSubtitle(subtitle.trim())
            viewModel.updateQuote(quote.trim())
            onBack()
        },
        onBack = onBack
    )
}

@Composable
fun EditProfileScreen(
    initialName: String,
    initialSubtitle: String,
    initialQuote: String,
    avatarPath: String?,
    onPickPhoto: () -> Unit,
    onSave: (name: String, subtitle: String, quote: String) -> Unit,
    onBack: () -> Unit = {}
) {
    // Local text-field state, seeded once from the DB values.
    // Keyed on the initials so re-entering the screen with new DB values
    // re-seeds the fields.
    var name by remember(initialName) { mutableStateOf(initialName) }
    var subtitle by remember(initialSubtitle) { mutableStateOf(initialSubtitle) }
    var quote by remember(initialQuote) { mutableStateOf(initialQuote) }

    val canSave = name.isNotBlank()

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                SaveButton(
                    enabled = canSave,
                    onClick = { onSave(name, subtitle, quote) }
                )
            }
        }
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

            EditTopBar(title = "Edit Profile", onBack = onBack)

            Spacer(Modifier.height(24.dp))

            // ---- Avatar picker ----
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EditableAvatar(
                    avatarPath = avatarPath,
                    fallbackRes = R.drawable.habits,
                    onClick = onPickPhoto
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap to change photo",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---- Name ----
            FieldLabel("Name")
            Spacer(Modifier.height(8.dp))
            DarkTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Your name",
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ---- Bio / Subtitle ----
            FieldLabel("Bio")
            Spacer(Modifier.height(8.dp))
            DarkTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                placeholder = "Student • Learner • Builder",
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ---- Quote ----
            FieldLabel("Quote")
            Spacer(Modifier.height(8.dp))
            DarkTextField(
                value = quote,
                onValueChange = { quote = it },
                placeholder = "A short line that keeps you going…",
                singleLine = false,
                minHeight = 96.dp
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// Pieces
// =====================================================================

@Composable
private fun EditTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )
        }
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun EditableAvatar(
    avatarPath: String?,
    fallbackRes: Int,
    onClick: () -> Unit
) {
    val bitmap: ImageBitmap? = remember(avatarPath) {
        avatarPath?.let { path ->
            runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
    }

    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Accent)
                .padding(3.dp)
                .clip(CircleShape)
                .background(BgDark)
                .clickable(onClick = onClick)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(fallbackRes),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Accent)
                .border(2.dp, BgDark, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Change photo",
                tint = OnAccent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary,
                fontSize = 15.sp
            )
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TextPrimary,
            fontSize = 15.sp
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            unfocusedBorderColor = Outline,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Accent else Accent.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Save",
            color = OnAccent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// =====================================================================
// Preview — pure UI, no DB
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0E0E10, widthDp = 390, heightDp = 800)
@Composable
private fun EditProfileScreenPreview() {
    EditProfileScreen(
        initialName = "Vishal Das",
        initialSubtitle = "Student • Learner • Builder",
        initialQuote = "Discipline today creates the life you want tomorrow.",
        avatarPath = null,
        onPickPhoto = {},
        onSave = { _, _, _ -> }
    )
}
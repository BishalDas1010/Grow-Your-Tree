package com.example.disipline.data.reposotry



import android.content.Context
import android.net.Uri
import com.example.disipline.data.Local.HabitDatabase
import com.example.disipline.data.Local.ProfileDao
import com.example.disipline.data.Local.profile.BlockedAppEntity
import com.example.disipline.data.Local.profile.ProfileEntity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

object ProfileRepository {

    private lateinit var dao: ProfileDao
    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        if (!::dao.isInitialized) {
            appContext = context.applicationContext
            dao = HabitDatabase.get(appContext).profileDao()
        }
    }

    val profile: StateFlow<ProfileEntity?> by lazy {
        dao.observeProfile()
            .stateIn(scope, SharingStarted.Eagerly, null)
    }

    val blockedApps: StateFlow<List<BlockedAppEntity>> by lazy {
        dao.observeBlockedApps()
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    fun updateName(name: String) = updateProfile { it.copy(name = name) }
    fun updateSubtitle(subtitle: String) = updateProfile { it.copy(subtitle = subtitle) }
    fun updateQuote(quote: String) = updateProfile { it.copy(quote = quote) }

    fun updateAvatarFromUri(uri: Uri) {
        scope.launch {
            val path = copyImageToPrivateStorage(uri) ?: return@launch
            val old = profile.value?.avatarPath
            updateProfileBlocking { it.copy(avatarPath = path) }
            if (old != null && old != path) runCatching { File(old).delete() }
        }
    }

    fun clearAvatar() {
        scope.launch {
            val old = profile.value?.avatarPath
            updateProfileBlocking { it.copy(avatarPath = null) }
            if (old != null) runCatching { File(old).delete() }
        }
    }

    fun addBlockedApp(packageName: String, appName: String) {
        scope.launch { dao.run { insertBlockedApp(BlockedAppEntity(packageName, appName)) } }
    }

    fun removeBlockedApp(packageName: String) {
        scope.launch { dao.deleteBlockedApp(packageName) }
    }

    private fun updateProfile(change: (ProfileEntity) -> ProfileEntity) {
        scope.launch { updateProfileBlocking(change) }
    }

    private suspend fun updateProfileBlocking(change: (ProfileEntity) -> ProfileEntity) {
        val current = profile.value ?: ProfileEntity()
        dao.upsertProfile(change(current))
    }

    private fun copyImageToPrivateStorage(uri: Uri): String? = try {
        val dir = File(appContext.filesDir, "profile").apply { mkdirs() }
        val dest = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    } catch (e: Exception) {
        null
    }
}
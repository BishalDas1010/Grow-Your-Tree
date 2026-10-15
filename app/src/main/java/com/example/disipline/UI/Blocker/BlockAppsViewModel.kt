package com.example.disipline.UI.blockapps

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.disipline.UI.blockapps.BlockedAppsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap
)

data class BlockAppsUiState(
    val blockerEnabled: Boolean = true,
    val accessibilityGranted: Boolean = false,
    val allApps: List<InstalledApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val loading: Boolean = true,
    val showPicker: Boolean = false,
    val pending: Set<String> = emptySet(),   // ticked in the picker, not added yet
    val query: String = ""
) {
    /** Apps shown in the main list. */
    val selectedApps: List<InstalledApp>
        get() = allApps.filter { it.packageName in selectedPackages }

    /** Apps shown in the picker: not already selected, filtered by search. */
    val pickerApps: List<InstalledApp>
        get() = allApps.filter {
            it.packageName !in selectedPackages &&
                    (query.isBlank() || it.label.contains(query, ignoreCase = true))
        }
}

class BlockAppsViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(
        BlockAppsUiState(
            blockerEnabled = BlockedAppsStore.isEnabled(app),
            selectedPackages = BlockedAppsStore.load(app)
        )
    )
    val state: StateFlow<BlockAppsUiState> = _state.asStateFlow()

    init {
        loadInstalledApps()
        refreshAccessibility()
    }

    // ---------- loading ----------

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val list = withContext(Dispatchers.IO) {
                val pm = ctx.packageManager
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { it.packageName != ctx.packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
                    .distinctBy { it.packageName }
                    .map {
                        InstalledApp(
                            packageName = it.packageName,
                            label = it.loadLabel(pm).toString(),
                            icon = it.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
                        )
                    }
                    .sortedBy { it.label.lowercase() }
            }
            _state.update { it.copy(allApps = list, loading = false) }
        }
    }

    /** Call when the screen resumes (user may have just enabled the service). */
    fun refreshAccessibility() {
        val ctx = getApplication<Application>()
        val enabled = Settings.Secure.getString(
            ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val me = ComponentName(ctx, AppBlockerService::class.java).flattenToString()
        val granted = enabled.split(':').any { it.equals(me, ignoreCase = true) }
        _state.update { it.copy(accessibilityGranted = granted) }
    }

    // ---------- main screen actions ----------

    fun onToggleBlocker(enabled: Boolean) {
        BlockedAppsStore.setEnabled(getApplication(), enabled)
        _state.update { it.copy(blockerEnabled = enabled) }
    }

    fun onRemoveApp(packageName: String) {
        _state.update { it.copy(selectedPackages = it.selectedPackages - packageName) }
        BlockedAppsStore.save(getApplication(), _state.value.selectedPackages)
    }

    // ---------- picker actions ----------

    fun onAddAppsClick() {
        _state.update { it.copy(showPicker = true, pending = emptySet(), query = "") }
    }

    fun onDismissPicker() {
        _state.update { it.copy(showPicker = false, pending = emptySet(), query = "") }
    }

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
    }

    fun onTogglePending(packageName: String) {
        _state.update {
            val next = if (packageName in it.pending) it.pending - packageName
            else it.pending + packageName
            it.copy(pending = next)
        }
    }

    fun onConfirmAdd() {
        _state.update {
            it.copy(
                selectedPackages = it.selectedPackages + it.pending,
                showPicker = false,
                pending = emptySet(),
                query = ""
            )
        }
        BlockedAppsStore.save(getApplication(), _state.value.selectedPackages)
    }
}
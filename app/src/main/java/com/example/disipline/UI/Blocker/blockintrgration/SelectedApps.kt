package com.example.disipline.blocker

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.disipline.UI.blockapps.BlockedAppsStore

data class BlockedApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap
)

/** Reads the saved package names and turns them into name + icon. */
fun loadSelectedApps(context: Context): List<BlockedApp> {
    val pm = context.packageManager
    return BlockedAppsStore.load(context).mapNotNull { pkg ->
        try {
            val info = pm.getApplicationInfo(pkg, 0)
            BlockedApp(
                packageName = pkg,
                label = info.loadLabel(pm).toString(),
                icon = info.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null   // app was uninstalled, skip it
        }
    }.sortedBy { it.label.lowercase() }
}
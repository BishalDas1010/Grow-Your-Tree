package com.example.disipline.blocker

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.disipline.UI.blockapps.AppBlockerService

fun isBlockerServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ).orEmpty()
    val me = ComponentName(context, AppBlockerService::class.java).flattenToString()
    return enabled.split(':').any { it.equals(me, ignoreCase = true) }
}

/** Remembers that the user pressed "Skip for now", so the guide isn't forced every launch. */
object PermissionGuidePrefs {
    private const val PREFS = "permission_guide"
    private const val KEY_SKIPPED = "skipped"

    fun wasSkipped(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SKIPPED, false)

    fun setSkipped(context: Context, skipped: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SKIPPED, skipped).apply()
    }
}
package com.example.disipline.UI.blockapps


import android.content.Context

object BlockedAppsStore {
    private const val PREFS = "blocked_apps"
    private const val KEY_PACKAGES = "packages"
    private const val KEY_ENABLED = "enabled"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PACKAGES, emptySet())?.toSet() ?: emptySet()

    fun save(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_PACKAGES, packages).apply()
    }

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}

object BlockerState {
    @Volatile var active: Boolean = false
}
package com.example.disipline.ui.theme

import android.content.Context

object ThemePrefs {
    private const val FILE = "disciplinex_theme"
    private const val KEY  = "active_palette_id"

    fun save(context: Context, id: String) {
        context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, id)
            .apply()
    }

    fun load(context: Context): String? =
        context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY, null)
}
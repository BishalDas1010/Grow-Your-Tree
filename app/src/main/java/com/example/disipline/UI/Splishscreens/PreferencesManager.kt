package com.example.disipline.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

class PreferencesManager(private val context: Context) {

    private val KEY_HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")

    /** false = first launch, true = returning user. */
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { it[KEY_HAS_SEEN_ONBOARDING] = true }
    }

    /** For testing: wipe the flag so onboarding shows again. */
    suspend fun resetOnboarding() {
        context.dataStore.edit { it.remove(KEY_HAS_SEEN_ONBOARDING) }
    }
}
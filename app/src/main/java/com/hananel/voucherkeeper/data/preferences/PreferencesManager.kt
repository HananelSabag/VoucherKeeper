package com.hananel.voucherkeeper.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * Manager for app preferences using DataStore.
 * Handles theme, language, notifications, and onboarding state.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dataStore = context.dataStore
    
    companion object {
        val THEME_KEY = stringPreferencesKey("theme") // "system", "light", "dark"
        val LANGUAGE_KEY = stringPreferencesKey("language") // "auto", "en", "he"
        val NOTIFY_APPROVED_KEY = booleanPreferencesKey("notify_approved")
        val NOTIFY_PENDING_KEY = booleanPreferencesKey("notify_pending")
        val STRICT_MODE_KEY = booleanPreferencesKey("strict_mode")
        val ONBOARDING_SHOWN_KEY = booleanPreferencesKey("onboarding_shown")
    }
    
    // Theme preference
    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }
    
    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
    
    // Language preference
    val languageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "auto"
    }
    
    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
        // Also save to SharedPreferences for early access in attachBaseContext()
        context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("language", language)
            .apply()
    }
    
    // Notification preferences
    val notifyApprovedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFY_APPROVED_KEY] ?: true
    }
    
    suspend fun setNotifyApproved(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFY_APPROVED_KEY] = enabled
        }
    }
    
    val notifyPendingFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFY_PENDING_KEY] ?: true
    }
    
    suspend fun setNotifyPending(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFY_PENDING_KEY] = enabled
        }
    }
    
    // Strict mode (only approved senders)
    val strictModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[STRICT_MODE_KEY] ?: false
    }
    
    suspend fun setStrictMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[STRICT_MODE_KEY] = enabled
        }
    }
    
    // Onboarding shown flag
    val onboardingShownFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_SHOWN_KEY] ?: false
    }
    
    suspend fun setOnboardingShown(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_SHOWN_KEY] = shown
        }
    }
}


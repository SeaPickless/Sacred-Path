package com.sacredpath.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.sacredpath.app.data.model.BibleTranslations
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val translationId: String = BibleTranslations.KJV.id,
    val themeMode: String     = "light",     // light | dark | parchment | night
    val fontSize: String      = "medium",    // small | medium | large | xl
    val lineSpacing: String   = "normal",    // compact | normal | relaxed
    val activeProfileId: String = "",
    val hasCompletedOnboarding: Boolean = false,
    val readingReminderTime: String = "08:00",
    val prayerReminderTime: String  = "20:00",
    val xp: Int  = 0,
    val streak: Int = 0,
    val lastActivityDate: String = ""
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val TRANSLATION_ID      = stringPreferencesKey("translation_id")
        val THEME_MODE          = stringPreferencesKey("theme_mode")
        val FONT_SIZE           = stringPreferencesKey("font_size")
        val LINE_SPACING        = stringPreferencesKey("line_spacing")
        val ACTIVE_PROFILE_ID   = stringPreferencesKey("active_profile_id")
        val ONBOARDING_DONE     = booleanPreferencesKey("onboarding_done")
        val READING_REMINDER    = stringPreferencesKey("reading_reminder_time")
        val PRAYER_REMINDER     = stringPreferencesKey("prayer_reminder_time")
        val XP                  = intPreferencesKey("xp")
        val STREAK              = intPreferencesKey("streak")
        val LAST_ACTIVITY_DATE  = stringPreferencesKey("last_activity_date")
    }

    val settingsFlow = dataStore.data.map { prefs ->
        AppSettings(
            translationId          = prefs[TRANSLATION_ID]     ?: BibleTranslations.KJV.id,
            themeMode              = prefs[THEME_MODE]         ?: "light",
            fontSize               = prefs[FONT_SIZE]          ?: "medium",
            lineSpacing            = prefs[LINE_SPACING]        ?: "normal",
            activeProfileId        = prefs[ACTIVE_PROFILE_ID]  ?: "",
            hasCompletedOnboarding = prefs[ONBOARDING_DONE]    ?: false,
            readingReminderTime    = prefs[READING_REMINDER]   ?: "08:00",
            prayerReminderTime     = prefs[PRAYER_REMINDER]    ?: "20:00",
            xp                     = prefs[XP]                 ?: 0,
            streak                 = prefs[STREAK]             ?: 0,
            lastActivityDate       = prefs[LAST_ACTIVITY_DATE] ?: ""
        )
    }

    suspend fun update(block: suspend (MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }

    suspend fun addXP(amount: Int) {
        dataStore.edit { it[XP] = (it[XP] ?: 0) + amount }
    }

    suspend fun setTheme(mode: String)        { dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setTranslation(id: String)    { dataStore.edit { it[TRANSLATION_ID] = id } }
    suspend fun setFontSize(size: String)     { dataStore.edit { it[FONT_SIZE] = size } }
    suspend fun setOnboardingDone()           { dataStore.edit { it[ONBOARDING_DONE] = true } }
    suspend fun setActiveProfile(id: String)  { dataStore.edit { it[ACTIVE_PROFILE_ID] = id } }
}

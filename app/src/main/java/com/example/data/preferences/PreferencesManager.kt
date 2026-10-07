package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("makeithappen_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _defaultPriority = MutableStateFlow(prefs.getString(KEY_DEFAULT_PRIORITY, "Medium") ?: "Medium")
    val defaultPriority: StateFlow<String> = _defaultPriority.asStateFlow()

    private val _defaultCategory = MutableStateFlow(prefs.getString(KEY_DEFAULT_CATEGORY, "Personal") ?: "Personal")
    val defaultCategory: StateFlow<String> = _defaultCategory.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _soundVibrateEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_VIBRATE_ENABLED, true))
    val soundVibrateEnabled: StateFlow<Boolean> = _soundVibrateEnabled.asStateFlow()

    private val _startOfWeek = MutableStateFlow(prefs.getString(KEY_START_OF_WEEK, "Monday") ?: "Monday")
    val startOfWeek: StateFlow<String> = _startOfWeek.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setDefaultPriority(priority: String) {
        prefs.edit().putString(KEY_DEFAULT_PRIORITY, priority).apply()
        _defaultPriority.value = priority
    }

    fun setDefaultCategory(category: String) {
        prefs.edit().putString(KEY_DEFAULT_CATEGORY, category).apply()
        _defaultCategory.value = category
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setSoundVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_VIBRATE_ENABLED, enabled).apply()
        _soundVibrateEnabled.value = enabled
    }

    fun setStartOfWeek(startDay: String) {
        prefs.edit().putString(KEY_START_OF_WEEK, startDay).apply()
        _startOfWeek.value = startDay
    }

    fun isFirstLaunch(): Boolean {
        val first = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        if (first) {
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return first
    }

    fun resetPreferences() {
        prefs.edit().clear().apply()
        _themeMode.value = "system"
        _defaultPriority.value = "Medium"
        _defaultCategory.value = "Personal"
        _notificationsEnabled.value = true
        _soundVibrateEnabled.value = true
        _startOfWeek.value = "Monday"
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DEFAULT_PRIORITY = "key_default_priority"
        private const val KEY_DEFAULT_CATEGORY = "key_default_category"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"
        private const val KEY_SOUND_VIBRATE_ENABLED = "key_sound_vibrate_enabled"
        private const val KEY_START_OF_WEEK = "key_start_of_week"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
    }
}

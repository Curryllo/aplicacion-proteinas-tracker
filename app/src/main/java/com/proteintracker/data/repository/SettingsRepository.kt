package com.proteintracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsStore {
    val goalGrams: Flow<Double>
    suspend fun setGoal(grams: Double)
    suspend fun lastCelebratedDay(): String?
    suspend fun setLastCelebratedDay(dayKey: String)
}

/**
 * Persists the daily protein goal and the day the celebration modal was last shown,
 * so the celebration fires exactly once per day.
 */
class SettingsRepository(
    context: Context,
) : SettingsStore {

    private val dataStore = context.applicationContext.settingsDataStore

    private val preferences: Flow<Preferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }

    override val goalGrams: Flow<Double> = preferences.map { it[GOAL_GRAMS] ?: DEFAULT_GOAL_GRAMS }

    override suspend fun setGoal(grams: Double) {
        dataStore.edit { it[GOAL_GRAMS] = grams }
    }

    override suspend fun lastCelebratedDay(): String? = preferences.first()[LAST_CELEBRATED_DAY]

    override suspend fun setLastCelebratedDay(dayKey: String) {
        dataStore.edit { it[LAST_CELEBRATED_DAY] = dayKey }
    }

    companion object {
        const val DEFAULT_GOAL_GRAMS = 100.0
        private val GOAL_GRAMS = doublePreferencesKey("goal_grams")
        private val LAST_CELEBRATED_DAY = stringPreferencesKey("last_celebrated_day")
    }
}

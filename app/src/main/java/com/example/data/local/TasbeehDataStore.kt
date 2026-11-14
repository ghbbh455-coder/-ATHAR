package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tasbeehDataStore: DataStore<Preferences> by preferencesDataStore(name = "athar_tasbeeh_datastore")

object TasbeehDataStore {
    private val KEY_SELECTED_INDEX = intPreferencesKey("tasbeeh_selected_index")

    private fun countKey(dhikrKey: String) = intPreferencesKey("tasbeeh_count_$dhikrKey")

    fun selectedIndexFlow(context: Context): Flow<Int> =
        context.applicationContext.tasbeehDataStore.data.map { prefs ->
            prefs[KEY_SELECTED_INDEX] ?: 0
        }

    fun countFlow(context: Context, dhikrKey: String): Flow<Int> =
        context.applicationContext.tasbeehDataStore.data.map { prefs ->
            prefs[countKey(dhikrKey)] ?: 0
        }

    suspend fun saveSelectedIndex(context: Context, index: Int) {
        context.applicationContext.tasbeehDataStore.edit { prefs ->
            prefs[KEY_SELECTED_INDEX] = index
        }
    }

    suspend fun saveCount(context: Context, dhikrKey: String, count: Int) {
        context.applicationContext.tasbeehDataStore.edit { prefs ->
            prefs[countKey(dhikrKey)] = count
        }
    }
}

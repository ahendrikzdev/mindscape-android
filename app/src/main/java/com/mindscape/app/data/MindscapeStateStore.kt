package com.mindscape.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.mindscapeDataStore by preferencesDataStore(name = "mindscape_state")

class MindscapeStateStore(context: Context) {

    private val appContext = context.applicationContext
    private val stateKey = stringPreferencesKey("state_json")

    suspend fun load(): MindscapeUiState? = runCatching {
        appContext.mindscapeDataStore.data.first()[stateKey]
            ?.let(MindscapeStateCodec::decode)
    }.getOrNull()

    suspend fun save(state: MindscapeUiState) {
        appContext.mindscapeDataStore.edit { preferences ->
            preferences[stateKey] = MindscapeStateCodec.encode(state)
        }
    }
}

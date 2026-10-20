package com.mindscape.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val STATE_VERSION = 1

@Serializable
private data class PersistedState(
    val version: Int = STATE_VERSION,
    val state: MindscapeUiState,
)

/** Pure JSON codec, separated from DataStore so it can be tested on the JVM. */
object MindscapeStateCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(state: MindscapeUiState): String =
        json.encodeToString(PersistedState(state = state))

    fun decode(raw: String): MindscapeUiState? = runCatching {
        val persisted = json.decodeFromString<PersistedState>(raw)
        if (persisted.version == STATE_VERSION) persisted.state else null
    }.getOrNull()
}

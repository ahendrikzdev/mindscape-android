package com.mindscape.app.data

import com.mindscape.app.R

/**
 * Thumbnail keys are persisted instead of Android resource ids, which are not stable across
 * app updates. Remote thumbnails (e.g. YouTube) can be added later using [thumbnailUrl].
 */
object ThumbnailCatalog {

    const val SUPERPOSITION = "superposition"
    const val DOUBLE_SLIT = "double_slit"
    const val ENTANGLEMENT = "entanglement"
    const val BELL_TEST = "bell_test"
    const val QUBITS = "qubits"

    val keys: List<String> = listOf(
        SUPERPOSITION,
        DOUBLE_SLIT,
        ENTANGLEMENT,
        BELL_TEST,
        QUBITS,
    )

    fun drawableFor(key: String?): Int = when (key) {
        SUPERPOSITION -> R.drawable.thumb_superposition
        DOUBLE_SLIT -> R.drawable.thumb_double_slit
        BELL_TEST -> R.drawable.thumb_bell_test
        QUBITS -> R.drawable.thumb_qubits
        ENTANGLEMENT -> R.drawable.thumb_entanglement
        else -> R.drawable.thumb_entanglement
    }

    fun keyForNode(nodeId: String): String = when (nodeId) {
        "q-mechanics-101" -> SUPERPOSITION
        "q-entanglement" -> ENTANGLEMENT
        "q-computing" -> QUBITS
        "q-cryptography" -> BELL_TEST
        "q-teleportation" -> QUBITS
        "h-mesopotamia" -> SUPERPOSITION
        "h-egypt-pyramids" -> BELL_TEST
        "h-rosetta-stone" -> ENTANGLEMENT
        else -> ENTANGLEMENT
    }
}

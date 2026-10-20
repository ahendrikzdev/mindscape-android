package com.mindscape.app.analysis

import java.net.URI
import java.net.URLDecoder

sealed interface VideoLink {
    val url: String

    data class YouTube(val id: String, override val url: String) : VideoLink

    data class Direct(override val url: String, val fileName: String) : VideoLink
}

/** URL parsing shared by the mock and remote analyzers. */
object VideoLinks {

    private val youtubeId = Regex("[A-Za-z0-9_-]{11}")
    private val directExtensions = setOf("mp4", "webm", "m3u8", "mpd", "mov", "m4v")

    fun parse(raw: String): VideoLink? {
        val value = raw.trim()
        val uri = try {
            URI(value)
        } catch (_: Exception) {
            return null
        }
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        val host = uri.host?.lowercase() ?: return null
        val path = uri.path.orEmpty()

        val id = when {
            host == "youtu.be" -> path.trim('/').substringBefore('/')
            host == "youtube.com" || host.endsWith(".youtube.com") -> when {
                path == "/watch" -> queryParam(uri.rawQuery, "v")
                path.startsWith("/shorts/") -> path.removePrefix("/shorts/").substringBefore('/')
                path.startsWith("/embed/") -> path.removePrefix("/embed/").substringBefore('/')
                path.startsWith("/live/") -> path.removePrefix("/live/").substringBefore('/')
                else -> null
            }
            else -> null
        }

        if (!id.isNullOrBlank() && youtubeId.matches(id)) {
            return VideoLink.YouTube(id = id, url = value)
        }

        val extension = path.substringAfterLast('.', "").lowercase()
        if (extension in directExtensions) {
            val fileName = decode(path.substringAfterLast('/'))
                .substringBeforeLast(".")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .ifBlank { "Imported video" }
            return VideoLink.Direct(url = value, fileName = fileName)
        }

        return null
    }

    private fun queryParam(rawQuery: String?, name: String): String? {
        if (rawQuery.isNullOrBlank()) return null
        return rawQuery.split('&').firstNotNullOfOrNull { pair ->
            if (pair.substringBefore('=') == name) {
                decode(pair.substringAfter('=', ""))
            } else {
                null
            }
        }
    }

    private fun decode(value: String): String = try {
        URLDecoder.decode(value, "UTF-8")
    } catch (_: Exception) {
        value
    }
}

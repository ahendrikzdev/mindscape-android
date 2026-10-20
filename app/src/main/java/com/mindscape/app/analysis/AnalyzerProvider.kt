package com.mindscape.app.analysis

object AnalyzerProvider {
    fun create(baseUrl: String, apiKey: String): LessonAnalyzer {
        val remote = RemoteLessonAnalyzer(baseUrl = baseUrl, apiKey = apiKey)
        return if (remote.isConfigured) remote else MockLessonAnalyzer()
    }
}

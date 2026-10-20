package com.mindscape.app.domain

import com.mindscape.app.data.Quiz
import kotlin.math.sqrt
import kotlin.random.Random

data class BuiltLesson(
    val summary: String,
    val keyPoints: List<String>,
    val quiz: Quiz?,
)

/**
 * Small, deterministic on-device lesson builder used by the mock analyzer.
 *
 * It does not pretend to understand video: it produces an extractive summary (frequency-based
 * sentence ranking) and a cloze question from a transcript the user pasted. That gives the
 * client a real end-to-end lesson pipeline before the remote LLM backend is deployed.
 */
object LessonBuilder {

    private val sentencePattern = Regex("(?<=[.!?])\\s+")
    private val wordPattern = Regex("[A-Za-z][A-Za-z'-]{2,}")
    private val whitespace = Regex("\\s+")

    private val stopWords = setOf(
        "the", "and", "that", "this", "with", "from", "have", "were", "their", "there",
        "which", "when", "what", "where", "about", "into", "than", "then", "they", "them",
        "these", "those", "because", "while", "your", "will", "would", "could", "should",
        "been", "being", "also", "more", "most", "some", "such", "other", "only", "over",
        "very", "just", "like", "through", "between", "after", "before", "under", "above",
        "does", "did", "can", "may", "might", "must", "not", "are", "was", "for", "you",
        "our", "its", "it's", "his", "her", "she", "he", "has", "had", "how", "all",
        "any", "each", "few", "many", "much", "own", "same", "too", "say", "says", "said",
        "here", "also", "onto", "upon", "within", "without", "both", "another", "using",
    )

    private val fallbackWords = listOf(
        "measurement", "particle", "experiment", "energy", "theory", "system",
    )

    fun build(rawTranscript: String): BuiltLesson? {
        val text = rawTranscript.replace(whitespace, " ").trim()
        if (text.isEmpty()) return null
        if (text.length < 120) return BuiltLesson(text.take(600), emptyList(), null)

        val sentences = sentencePattern.split(text)
            .map { it.trim() }
            .filter { it.length >= 25 && wordPattern.findAll(it).count() >= 5 }
        if (sentences.isEmpty()) return BuiltLesson(text.take(600), emptyList(), null)

        val frequencies = mutableMapOf<String, Int>()
        sentences.forEach { sentence ->
            tokens(sentence).forEach { token ->
                frequencies[token] = (frequencies[token] ?: 0) + 1
            }
        }

        val ranked = sentences.mapIndexed { index, sentence ->
            val tokens = tokens(sentence)
            val score = if (tokens.isEmpty()) {
                0.0
            } else {
                tokens.sumOf { frequencies[it] ?: 0 }.toDouble() / sqrt(tokens.size.toDouble()) +
                    if (index == 0) 2.0 else if (index == 1) 1.0 else 0.0
            }
            RankedSentence(index, sentence, score)
        }.sortedByDescending { it.score }

        val selected = ranked.take(3).sortedBy { it.index }
        val summary = selected.joinToString(" ") { it.text }.take(700)
        val keyPoints = selected.take(3).map { it.text.take(180) }
        val quiz = buildQuiz(selected.map { it.text }, frequencies)

        return BuiltLesson(summary = summary, keyPoints = keyPoints, quiz = quiz)
    }

    private fun tokens(sentence: String): List<String> =
        wordPattern.findAll(sentence)
            .map { it.value.lowercase() }
            .filter { it.length >= 4 && it !in stopWords }
            .toList()

    private fun buildQuiz(sentences: List<String>, frequencies: Map<String, Int>): Quiz? {
        val candidate = sentences.firstOrNull { sentence ->
            val wordCount = wordPattern.findAll(sentence).count()
            wordCount >= 8 && tokens(sentence).any { (frequencies[it] ?: 0) >= 2 }
        } ?: return null

        val keyword = tokens(candidate)
            .filter { (frequencies[it] ?: 0) >= 2 }
            .maxByOrNull { frequencies[it] ?: 0 }
            ?: return null

        val blanked = Regex("(?i)\\b${Regex.escape(keyword)}\\b")
            .replaceFirst(candidate, "_____")
        if (!blanked.contains("_____")) return null

        val distractors = frequencies.keys
            .filter { it != keyword && it.length >= 4 }
            .sortedByDescending { frequencies[it] ?: 0 }
            .distinct()
            .take(3)
            .toMutableList()

        fallbackWords.forEach { fallback ->
            if (distractors.size < 3 && fallback != keyword && fallback !in distractors) {
                distractors += fallback
            }
        }
        while (distractors.size < 3) {
            distractors += "concept${distractors.size + 1}"
        }

        val options = (listOf(keyword) + distractors.take(3))
            .shuffled(Random(blanked.hashCode()))
        val correctIndex = options.indexOf(keyword)
        if (correctIndex < 0) return null

        return Quiz(
            question = "Which term completes this key point? “${blanked.take(240)}”",
            options = options,
            correctIndex = correctIndex,
            explanation = "The transcript uses “$keyword” in this key point.",
        )
    }

    private data class RankedSentence(
        val index: Int,
        val text: String,
        val score: Double,
    )
}

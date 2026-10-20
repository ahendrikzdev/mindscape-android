package com.mindscape.app.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonBuilderTest {

    private val transcript = """
        Quantum entanglement is a physical phenomenon that occurs when pairs of particles interact.
        Entangled particles remain connected so that actions performed on one affect the other.
        Measurement of an entangled particle instantly determines the state of its partner.
        Einstein called entanglement spooky action at a distance because it seemed impossible.
        Physicists now use entanglement for quantum computing and quantum cryptography.
        Entanglement does not allow faster than light communication because the results are random.
    """.trimIndent()

    @Test
    fun `builds an extractive summary and a cloze quiz`() {
        val lesson = LessonBuilder.build(transcript)
        assertNotNull(lesson)
        val built = requireNotNull(lesson)

        assertTrue(built.summary.isNotBlank())
        assertTrue(built.summary.length < transcript.length)
        assertTrue(built.keyPoints.isNotEmpty())

        val quiz = requireNotNull(built.quiz)
        assertNotNull(quiz)
        assertTrue(quiz.options.size == 4)
        assertTrue(quiz.correctIndex in quiz.options.indices)
        assertTrue(quiz.question.contains("_____"))
        assertTrue(quiz.options.size == quiz.options.distinct().size)
    }

    @Test
    fun `short transcript is watch only`() {
        val lesson = LessonBuilder.build("Quantum physics is fascinating.")

        assertNotNull(lesson)
        assertNull(requireNotNull(lesson).quiz)
    }

    @Test
    fun `blank transcript produces no lesson`() {
        assertNull(LessonBuilder.build("   "))
    }
}

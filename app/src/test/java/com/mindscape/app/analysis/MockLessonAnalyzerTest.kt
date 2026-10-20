package com.mindscape.app.analysis

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockLessonAnalyzerTest {

    private val analyzer = MockLessonAnalyzer()

    private val transcript = """
        Quantum entanglement is a physical phenomenon that occurs when pairs of particles interact.
        Entangled particles remain connected so that actions performed on one affect the other.
        Measurement of an entangled particle instantly determines the state of its partner.
        Einstein called entanglement spooky action at a distance because it seemed impossible.
        Physicists now use entanglement for quantum computing and quantum cryptography.
        Entanglement does not allow faster than light communication because the results are random.
    """.trimIndent()

    @Test
    fun `youtube transcript produces a real summary and quiz`() = runTest {
        val stages = mutableListOf<AnalysisStage>()
        val analysis = analyzer.analyze(
            request = LessonRequest(
                url = "https://www.youtube.com/watch?v=ZuvK-od647c",
                nodeId = "q-entanglement",
                nodeTitle = "Entanglement",
                transcript = transcript,
            ),
            onStage = stages::add,
        )

        assertEquals("Quantum Entanglement & Spooky Action at a Distance", analysis.title)
        assertEquals("@veritasium", analysis.author)
        assertEquals("ZuvK-od647c", analysis.videoId)
        assertTrue(analysis.summary.isNotBlank())
        assertNotNull(analysis.quiz)
        assertTrue(stages.contains(AnalysisStage.ANALYZING))
        assertTrue(stages.contains(AnalysisStage.SAVING))
    }

    @Test
    fun `missing transcript imports a watch only video`() = runTest {
        val analysis = analyzer.analyze(
            request = LessonRequest(
                url = "https://youtu.be/ZuvK-od647c",
                nodeId = "q-entanglement",
                nodeTitle = "Entanglement",
                transcript = null,
            ),
            onStage = {},
        )

        assertNull(analysis.quiz)
        assertTrue(analysis.summary.contains("Paste a transcript"))
    }

    @Test
    fun `direct media url uses its filename as title`() = runTest {
        val analysis = analyzer.analyze(
            request = LessonRequest(
                url = "https://cdn.example.com/Big_Buck_Bunny.mp4",
                nodeId = "q-mechanics-101",
                nodeTitle = "Superposition",
                transcript = null,
            ),
            onStage = {},
        )

        assertEquals("Big Buck Bunny", analysis.title)
        assertNull(analysis.videoId)
    }

    @Test
    fun `invalid url is rejected`() = runTest {
        val error = runCatching {
            analyzer.analyze(
                request = LessonRequest(
                    url = "https://example.com/not-a-video",
                    nodeId = "q-entanglement",
                    nodeTitle = "Entanglement",
                    transcript = null,
                ),
                onStage = {},
            )
        }.exceptionOrNull()

        assertTrue(error is AnalysisException)
    }
}

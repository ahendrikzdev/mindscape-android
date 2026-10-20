package com.mindscape.app.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoLinksTest {

    @Test
    fun `parses youtube watch short and embed urls`() {
        val watch = VideoLinks.parse(
            "https://www.youtube.com/watch?v=ZuvK-od647c&t=30",
        )
        assertEquals("ZuvK-od647c", (watch as VideoLink.YouTube).id)

        val short = VideoLinks.parse("https://youtu.be/ZuvK-od647c")
        assertEquals("ZuvK-od647c", (short as VideoLink.YouTube).id)

        val shorts = VideoLinks.parse("https://www.youtube.com/shorts/ZuvK-od647c")
        assertEquals("ZuvK-od647c", (shorts as VideoLink.YouTube).id)

        val embed = VideoLinks.parse("https://www.youtube.com/embed/ZuvK-od647c")
        assertEquals("ZuvK-od647c", (embed as VideoLink.YouTube).id)
    }

    @Test
    fun `parses direct media urls and derives a title`() {
        val link = VideoLinks.parse("https://cdn.example.com/Big_Buck_Bunny.mp4")

        assertTrue(link is VideoLink.Direct)
        assertEquals("Big Buck Bunny", (link as VideoLink.Direct).fileName)
    }

    @Test
    fun `rejects unsupported and unsafe urls`() {
        assertNull(VideoLinks.parse("https://example.com/watch?v=ZuvK-od647c"))
        assertNull(VideoLinks.parse("javascript:alert(1)"))
        assertNull(VideoLinks.parse("ftp://example.com/video.mp4"))
        assertNull(VideoLinks.parse("not a url"))
    }
}

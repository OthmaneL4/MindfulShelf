package com.example.mindfulshelf.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la traduccion entre seleccion del usuario y query de Google Books.
 */
class BookSearchPresetTest {

    @Test
    fun fromSelection_buildsPresetWithExpectedMetadata() {
        val preset = BookSearchPreset.fromSelection(
            mood = MoodOption.CALM,
            readingLength = ReadingLength.SHORT
        )

        assertEquals(MoodOption.CALM, preset.mood)
        assertEquals(ReadingLength.SHORT, preset.readingLength)
        assertTrue(preset.query.contains("mindfulness"))
        assertTrue(preset.query.contains("short essays"))
        assertTrue(preset.fallbackQueries.isNotEmpty())
        assertTrue(preset.title.contains("calma", ignoreCase = true))
    }
}

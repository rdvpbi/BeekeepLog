package com.beekeeplog.app.nlp

import com.beekeeplog.app.domain.model.IntentType
import com.beekeeplog.app.segmenter.HiveDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for the NLP pipeline: NumberParser, HiveDetector, Normalizer, IntentExtractor. */
class NlpTests {

    private lateinit var numberParser: NumberParser
    private lateinit var hiveDetector: HiveDetector
    private lateinit var normalizer: Normalizer
    private lateinit var intentExtractor: IntentExtractor

    @Before
    fun setUp() {
        numberParser    = NumberParser()
        hiveDetector    = HiveDetector(numberParser)
        normalizer      = Normalizer()
        intentExtractor = IntentExtractor(numberParser)
    }

    // -------------------------------------------------------------------------
    // NumberParser
    // -------------------------------------------------------------------------

    @Test fun `parse ordinal first returns 1`() {
        val result = numberParser.parse("первый")
        assertEquals(1, result.firstOrNull()?.first)
    }

    @Test fun `parse twenty five returns 25`() {
        val result = numberParser.parse("двадцать пять")
        assertEquals(25, result.firstOrNull()?.first)
    }

    @Test fun `parse digit string 12`() {
        val result = numberParser.parse("12")
        assertEquals(12, result.firstOrNull()?.first)
    }

    @Test fun `parse sto dvadtsat returns 120`() {
        val result = numberParser.parse("сто двадцать")
        assertEquals(120, result.firstOrNull()?.first)
    }

    @Test fun `parse dvesti odin returns 201`() {
        val result = numberParser.parse("двести один")
        assertEquals(201, result.firstOrNull()?.first)
    }

    @Test fun `parse empty string returns empty`() {
        assertTrue(numberParser.parse("").isEmpty())
    }

    @Test fun `parse out of range 1000 returns empty`() {
        val result = numberParser.parse("тысяча")
        // "тысяча" is not in the dictionary — should return empty
        assertTrue(result.isEmpty())
    }

    // -------------------------------------------------------------------------
    // HiveDetector
    // -------------------------------------------------------------------------

    @Test fun `detect 'улей 3' returns 3`() {
        assertEquals(3, hiveDetector.detect("улей 3"))
    }

    @Test fun `detect 'первый сеет' returns 1`() {
        assertEquals(1, hiveDetector.detect("первый сеет"))
    }

    @Test fun `detect 'во втором облет' returns 2`() {
        assertEquals(2, hiveDetector.detect("во втором облет"))
    }

    @Test fun `detect random noise returns null`() {
        assertNull(hiveDetector.detect("сегодня хорошая погода"))
    }

    @Test fun `detect 'нуклеус 42' returns 42`() {
        assertEquals(42, hiveDetector.detect("нуклеус 42"))
    }

    @Test fun `detect out-of-range id 99 returns null`() {
        assertNull(hiveDetector.detect("улей 99"))
    }

    // -------------------------------------------------------------------------
    // IntentExtractor — test case set from spec "3 nucs" test
    // -------------------------------------------------------------------------

    @Test fun `extract laying intent`() {
        val r = intentExtractor.extract("матка откладывает яйца хорошая кладка")
        assertEquals(IntentType.UPDATE_STATUS_LAYING, r.intentType)
    }

    @Test fun `extract virgin intent`() {
        val r = intentExtractor.extract("матка неплодная облет")
        assertEquals(IntentType.UPDATE_STATUS_VIRGIN, r.intentType)
    }

    @Test fun `extract cell intent`() {
        val r = intentExtractor.extract("маточник запечатан")
        assertEquals(IntentType.UPDATE_STATUS_CELL, r.intentType)
    }

    @Test fun `extract elite flag`() {
        val r = intentExtractor.extract("матка элитная отборная")
        assertEquals(IntentType.MARK_ELITE, r.intentType)
    }

    @Test fun `extract reserved flag`() {
        val r = intentExtractor.extract("оставить в резерв")
        assertEquals(IntentType.MARK_RESERVED, r.intentType)
    }

    @Test fun `extract sold mark`() {
        val r = intentExtractor.extract("матка продана")
        assertEquals(IntentType.MARK_SOLD, r.intentType)
    }

    @Test fun `extract no changes`() {
        val r = intentExtractor.extract("всё хорошо без изменений")
        assertEquals(IntentType.NO_CHANGES, r.intentType)
    }

    @Test fun `extract aggression with score`() {
        val r = intentExtractor.extract("агрессия 3")
        assertEquals(IntentType.SET_AGGRESSION, r.intentType)
        assertEquals(3, r.entities["aggression"])
    }

    @Test fun `extract empty text returns UNKNOWN`() {
        val r = intentExtractor.extract("")
        assertEquals(IntentType.UNKNOWN, r.intentType)
    }

    @Test fun `extract noise returns UNKNOWN`() {
        val r = intentExtractor.extract("абракадабра непонятное")
        assertEquals(IntentType.UNKNOWN, r.intentType)
    }

    // -------------------------------------------------------------------------
    // Normalizer
    // -------------------------------------------------------------------------

    @Test fun `normalize typo in matoochnik`() {
        val norm = normalizer.normalize("маточнк")
        assertEquals("маточник", norm)
    }

    @Test fun `normalize exact word unchanged`() {
        val norm = normalizer.normalize("кладка")
        assertEquals("кладка", norm)
    }

    @Test fun `normalize karnika to karnika`() {
        // "карника" is not in Dictionary (karnica/carnica variant)
        // but "кардован" typo for "кордован" should be caught
        val norm = normalizer.normalize("кардован")
        assertEquals("кордован", norm)
    }

    @Test fun `normalize troyzeк typo`() {
        // "тройзэк" → "тройзек" (1 edit)
        val norm = normalizer.normalize("тройзэк")
        assertEquals("тройзек", norm)
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test fun `extractGenetics carnica line`() {
        val entities = intentExtractor.extractGenetics("матка скленар карника")
        assertEquals("CARNICA", entities["genetics"])
        assertEquals("скленар", entities["lineName"])
    }

    @Test fun `extractAggression no number returns null`() {
        assertNull(intentExtractor.extractAggression("матка агрессивная"))
    }
}

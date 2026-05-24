package com.veritasbible.app.study

import com.veritasbible.app.study.ui.computeRange
import com.veritasbible.app.study.ui.tokenize
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkupTokenizerTest {

    @Test
    fun `tokenize splits on whitespace and preserves offsets`() {
        val text = "태초에 말씀이 계시니라"
        val tokens = tokenize(text)
        assertEquals(3, tokens.size)
        assertEquals("태초에", tokens[0].text)
        assertEquals(0, tokens[0].start)
        assertEquals(3, tokens[0].end)
        assertEquals("말씀이", tokens[1].text)
        assertEquals(4, tokens[1].start)
        assertEquals(7, tokens[1].end)
        assertEquals("계시니라", tokens[2].text)
        assertEquals(8, tokens[2].start)
        assertEquals(12, tokens[2].end)
    }

    @Test
    fun `tokenize handles leading and trailing whitespace`() {
        val tokens = tokenize("   foo  bar  ")
        assertEquals(2, tokens.size)
        assertEquals("foo", tokens[0].text)
        assertEquals("bar", tokens[1].text)
    }

    @Test
    fun `compute range returns lowest start and highest end across tokens`() {
        val tokens = tokenize("a bb ccc dddd")
        // pick token 1 ("bb") and token 3 ("dddd")
        val (start, end) = computeRange(tokens, anchorIndex = 3, extendIndex = 1)
        assertEquals(tokens[1].start, start)
        assertEquals(tokens[3].end, end)
    }

    @Test
    fun `compute range returns zero pair when nothing selected`() {
        val tokens = tokenize("a b c")
        assertEquals(0 to 0, computeRange(tokens, null, null))
    }
}

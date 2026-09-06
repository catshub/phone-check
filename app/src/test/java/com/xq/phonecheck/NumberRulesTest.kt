package com.xq.phonecheck

import com.xq.phonecheck.identity.NumberRules
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberRulesTest {

    @Test
    fun `removes user configured prefix`() {
        assertEquals("123456", NumberRules.apply("116451123456", "116451=>"))
    }

    @Test
    fun `replaces user configured prefix`() {
        assertEquals("900123456", NumberRules.apply("116451123456", "116451=>900"))
    }

    @Test
    fun `uses longest matching prefix first`() {
        val rules = """
            1164=>A
            116451=>B
        """.trimIndent()
        assertEquals("B123456", NumberRules.apply("116451123456", rules))
    }

    @Test
    fun `does not make number empty by removing exact prefix`() {
        assertEquals("116451", NumberRules.apply("116451", "116451=>"))
    }

    @Test
    fun `ignores comments and malformed rules`() {
        assertEquals("123456", NumberRules.apply("116451123456", "# comment\n\n116451=>\n=>bad"))
    }
}

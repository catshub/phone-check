package com.xq.phonecheck

import com.xq.phonecheck.identity.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumbersTest {
    @Test
    fun `normalizes chinese mobile number formats`() {
        assertEquals("13800138000", PhoneNumbers.normalize("+86 138-0013-8000"))
        assertEquals("13800138000", PhoneNumbers.normalize("8613800138000"))
        assertEquals("13800138000", PhoneNumbers.normalize("008613800138000"))
    }

    @Test
    fun `chinese mobile check`() {
        assertTrue(PhoneNumbers.isLikelyChineseMobile("13800138000"))
        assertFalse(PhoneNumbers.isLikelyChineseMobile("02112345678"))
    }

    @Test
    fun `empty input returns empty`() {
        assertEquals("", PhoneNumbers.normalize(null))
        assertEquals("", PhoneNumbers.normalize(" "))
    }
}

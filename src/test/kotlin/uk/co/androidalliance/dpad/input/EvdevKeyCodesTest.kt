package uk.co.androidalliance.dpad.input

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EvdevKeyCodesTest {

    @Test
    fun `DPAD_UP maps to KEY_UP`() {
        assertEquals(103, EvdevKeyCodes.toEvdev(19))
    }

    @Test
    fun `DPAD_DOWN maps to KEY_DOWN`() {
        assertEquals(108, EvdevKeyCodes.toEvdev(20))
    }

    @Test
    fun `DPAD_LEFT maps to KEY_LEFT`() {
        assertEquals(105, EvdevKeyCodes.toEvdev(21))
    }

    @Test
    fun `DPAD_RIGHT maps to KEY_RIGHT`() {
        assertEquals(106, EvdevKeyCodes.toEvdev(22))
    }

    @Test
    fun `DPAD_CENTER maps to KEY_CENTER not KEY_ENTER`() {
        // 232 is what the emulator's own Extended Controls D-Pad sends, and what
        // qwerty2.kl maps to DPAD_CENTER. 28 (KEY_ENTER) maps to ENTER, a different key.
        assertEquals(232, EvdevKeyCodes.toEvdev(23))
    }

    @Test
    fun `HOME maps to KEY_HOME`() {
        assertEquals(102, EvdevKeyCodes.toEvdev(3))
    }

    @Test
    fun `BACK maps to KEY_BACK`() {
        assertEquals(158, EvdevKeyCodes.toEvdev(4))
    }

    @Test
    fun `BOOKMARK is not mapped so it falls back to ADB`() {
        // evdev 156 (KEY_BOOKMARKS) is unmapped in qwerty2.kl, so sending it over gRPC
        // would be silently swallowed. null routes the key to ADB, which handles it.
        assertNull(EvdevKeyCodes.toEvdev(174))
    }

    @Test
    fun `unmapped keycode returns null`() {
        assertNull(EvdevKeyCodes.toEvdev(999))
    }

    @Test
    fun `negative keycode returns null`() {
        assertNull(EvdevKeyCodes.toEvdev(-1))
    }
}

package uk.co.androidalliance.dpad.input

import com.android.emulation.control.EmulatorControllerProto.KeyboardEvent
import com.android.emulation.control.EmulatorControllerProto.KeyboardEvent.KeyEventType
import io.grpc.Metadata
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.ActionDown
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.ActionLongPress
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.ActionUp

class EmulatorGrpcSenderTest {

    private fun sender(
        descriptor: EmulatorDescriptor = EmulatorDescriptor.fallback(5554),
        longPressHoldMs: Long = 600L,
    ) = EmulatorGrpcKeyEventSender(
        descriptor = descriptor,
        longPressHoldMs = longPressHoldMs,
        channelFactory = { _, _ -> error("no channel should be created in these tests") },
    )

    // --- auth header ---

    @Test
    fun `auth metadata carries the mandatory Bearer prefix`() {
        // StaticTokenAuth compares verbatim against "Bearer " + token. Without the prefix
        // every call is rejected with UNAUTHENTICATED.
        val metadata = EmulatorGrpcKeyEventSender.authMetadata("s3cr3t")

        val value = metadata.get(
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
        )

        assertEquals("Bearer s3cr3t", value)
    }

    @Test
    fun `auth header name is lowercase`() {
        // gRPC rejects header names that are not valid HTTP2.
        val metadata = EmulatorGrpcKeyEventSender.authMetadata("s3cr3t")

        assertTrue(metadata.keys().all { it == it.lowercase() }) { metadata.keys().toString() }
    }

    // --- proto construction ---

    @Test
    fun `key events are sent as raw evdev codes`() {
        val event = EmulatorGrpcKeyEventSender.keyboardEvent(232, KeyEventType.keypress)

        assertEquals(KeyboardEvent.KeyCodeType.Evdev, event.codeType)
        assertEquals(232, event.keyCode)
        assertEquals(KeyEventType.keypress, event.eventType)
    }

    @Test
    fun `key events do not set the pressed bit`() {
        // The emulator's KeyEventSender ORs in 0x400 for keydown itself; doing it here too
        // would corrupt the code.
        val event = EmulatorGrpcKeyEventSender.keyboardEvent(232, KeyEventType.keydown)

        assertEquals(0, event.keyCode and 0x400)
        assertEquals(232, event.keyCode)
    }

    // --- action semantics ---

    @Test
    fun `a tap is a single keypress RPC`() {
        // DPadPanel emits ActionUp for a complete tap, so one round trip suffices.
        assertEquals(
            listOf(EmulatorGrpcKeyEventSender.Step.Send(KeyEventType.keypress)),
            sender().stepsFor(ActionUp),
        )
    }

    @Test
    fun `ActionDown sends only keydown`() {
        assertEquals(
            listOf(EmulatorGrpcKeyEventSender.Step.Send(KeyEventType.keydown)),
            sender().stepsFor(ActionDown),
        )
    }

    @Test
    fun `a long press holds the key down before releasing it`() {
        // The emulator has no server-side hold, so the delay has to happen client-side.
        assertEquals(
            listOf(
                EmulatorGrpcKeyEventSender.Step.Send(KeyEventType.keydown),
                EmulatorGrpcKeyEventSender.Step.Wait(600L),
                EmulatorGrpcKeyEventSender.Step.Send(KeyEventType.keyup),
            ),
            sender().stepsFor(ActionLongPress),
        )
    }

    @Test
    fun `long press hold exceeds the Android long press threshold`() {
        val wait = sender().stepsFor(ActionLongPress)
            .filterIsInstance<EmulatorGrpcKeyEventSender.Step.Wait>()
            .single()

        assertTrue(wait.millis > 500L) { "hold was ${wait.millis}ms" }
    }

    @Test
    fun `long press hold is configurable`() {
        val wait = sender(longPressHoldMs = 1200L).stepsFor(ActionLongPress)
            .filterIsInstance<EmulatorGrpcKeyEventSender.Step.Wait>()
            .single()

        assertEquals(1200L, wait.millis)
    }

    // --- guard rails ---

    @Test
    fun `JWT only emulators are rejected before any connection attempt`() {
        val jwtOnly = EmulatorDescriptor(
            serialPort = 5554,
            grpcHost = "::1",
            grpcPort = 8554,
            token = null,
            requiresJwt = true,
        )

        assertThrows(UnsupportedAuthException::class.java) {
            runBlocking { sender(descriptor = jwtOnly).sendKeyEvent(23, ActionUp) }
        }
    }

    @Test
    fun `keycodes with no emulator mapping are rejected before any connection attempt`() {
        // 174 (BOOKMARK) is unmapped in qwerty2.kl; the router should fall back to ADB.
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sender().sendKeyEvent(174, ActionUp) }
        }
    }

    @Test
    fun `shutdown is safe when nothing was ever connected`() {
        sender().shutdown()
    }
}


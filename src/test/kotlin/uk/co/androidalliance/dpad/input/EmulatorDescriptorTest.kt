package uk.co.androidalliance.dpad.input

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EmulatorDescriptorTest {

    // --- serialPortOf ---

    @Test
    fun `serialPortOf extracts the console port`() {
        assertEquals(5554, EmulatorDescriptor.serialPortOf("emulator-5554"))
        assertEquals(5556, EmulatorDescriptor.serialPortOf("emulator-5556"))
    }

    @Test
    fun `serialPortOf rejects physical device serials`() {
        assertNull(EmulatorDescriptor.serialPortOf("R58M12ABCDE"))
        assertNull(EmulatorDescriptor.serialPortOf("device123"))
    }

    @Test
    fun `serialPortOf rejects a non-numeric emulator suffix`() {
        assertNull(EmulatorDescriptor.serialPortOf("emulator-abc"))
    }

    // --- fallback ---

    @Test
    fun `fallback guesses serial port plus 3000 on IPv6 loopback`() {
        val descriptor = EmulatorDescriptor.fallback(5554)

        assertEquals(5554, descriptor.serialPort)
        assertEquals(8554, descriptor.grpcPort)
        assertEquals("::1", descriptor.grpcHost)
        assertNull(descriptor.token)
        assertFalse(descriptor.requiresJwt)
    }

    // --- fromProperties ---

    @Test
    fun `fromProperties reads port and token`() {
        val descriptor = EmulatorDescriptor.fromProperties(
            mapOf(
                "port.serial" to "5554",
                "grpc.port" to "8556",
                "grpc.token" to "s3cr3t",
            )
        )

        requireNotNull(descriptor)
        assertEquals(5554, descriptor.serialPort)
        assertEquals(8556, descriptor.grpcPort)
        assertEquals("s3cr3t", descriptor.token)
        assertEquals("::1", descriptor.grpcHost, "emulator binds IPv6 loopback by default")
        assertFalse(descriptor.requiresJwt)
    }

    @Test
    fun `fromProperties does not assume serial port plus 3000`() {
        // The emulator searches a 1000-port range; 8554 may already be taken.
        val descriptor = EmulatorDescriptor.fromProperties(
            mapOf("port.serial" to "5554", "grpc.port" to "8601")
        )

        assertEquals(8601, descriptor?.grpcPort)
    }

    @Test
    fun `fromProperties returns null without a grpc port`() {
        assertNull(EmulatorDescriptor.fromProperties(mapOf("port.serial" to "5554")))
    }

    @Test
    fun `fromProperties returns null without a serial port`() {
        assertNull(EmulatorDescriptor.fromProperties(mapOf("grpc.port" to "8554")))
    }

    @Test
    fun `fromProperties treats a blank token as absent`() {
        val descriptor = EmulatorDescriptor.fromProperties(
            mapOf("port.serial" to "5554", "grpc.port" to "8554", "grpc.token" to "   ")
        )

        assertNull(descriptor?.token)
    }

    @Test
    fun `fromProperties flags JWT-only emulators as unsupported`() {
        val descriptor = EmulatorDescriptor.fromProperties(
            mapOf(
                "port.serial" to "5554",
                "grpc.port" to "8554",
                "grpc.jwks" to "/tmp/jwks",
            )
        )

        assertTrue(descriptor!!.requiresJwt)
    }

    @Test
    fun `fromProperties does not flag JWT when a static token is also offered`() {
        // Default emulator startup enables BOTH token and JWT; the token is enough for us.
        val descriptor = EmulatorDescriptor.fromProperties(
            mapOf(
                "port.serial" to "5554",
                "grpc.port" to "8554",
                "grpc.jwks" to "/tmp/jwks",
                "grpc.token" to "s3cr3t",
            )
        )

        assertFalse(descriptor!!.requiresJwt)
    }

    // --- hostFromAddress ---

    @Test
    fun `hostFromAddress handles bracketed IPv6 with a port`() {
        assertEquals("::1", EmulatorDescriptor.hostFromAddress("[::1]:8554"))
    }

    @Test
    fun `hostFromAddress handles bracketed IPv6 without a port`() {
        assertEquals("::1", EmulatorDescriptor.hostFromAddress("[::1]"))
    }

    @Test
    fun `hostFromAddress handles bare IPv6`() {
        assertEquals("fe80::1", EmulatorDescriptor.hostFromAddress("fe80::1"))
    }

    @Test
    fun `hostFromAddress handles host and port`() {
        assertEquals("localhost", EmulatorDescriptor.hostFromAddress("localhost:8554"))
        assertEquals("127.0.0.1", EmulatorDescriptor.hostFromAddress("127.0.0.1:8554"))
    }

    @Test
    fun `hostFromAddress handles a bare host`() {
        assertEquals("localhost", EmulatorDescriptor.hostFromAddress("localhost"))
    }

    @Test
    fun `hostFromAddress returns null for blank input`() {
        assertNull(EmulatorDescriptor.hostFromAddress(null))
        assertNull(EmulatorDescriptor.hostFromAddress("   "))
    }

    // --- hostCandidates ---

    @Test
    fun `hostCandidates falls back from IPv6 to IPv4 loopback`() {
        assertEquals(
            listOf("::1", "127.0.0.1"),
            EmulatorDescriptor.fallback(5554).hostCandidates(),
        )
    }

    @Test
    fun `hostCandidates puts the advertised host first without duplicates`() {
        val descriptor = EmulatorDescriptor.fallback(5554).copy(grpcHost = "127.0.0.1")

        assertEquals(listOf("127.0.0.1", "::1"), descriptor.hostCandidates())
    }
}


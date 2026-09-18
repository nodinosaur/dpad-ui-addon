package uk.co.androidalliance.dpad.input

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class EmulatorDiscoveryTest {

    @TempDir
    lateinit var tempDir: Path

    private fun runningDir(name: String = "root"): Path =
        Files.createDirectories(tempDir.resolve(name).resolve("avd").resolve("running"))

    private fun writePidFile(dir: Path, pid: Int, content: String): Path =
        Files.writeString(dir.resolve("pid_$pid.ini"), content.trimIndent())

    private fun discoveryOf(vararg roots: Path) = EmulatorDiscovery { roots.toList() }

    // --- ini parsing ---

    @Test
    fun `parseIni reads simple key value pairs`() {
        val dir = runningDir()
        val file = writePidFile(
            dir, 111,
            """
            port.serial=5554
            grpc.port=8554
            grpc.token=abc123
            """
        )

        val props = EmulatorDiscovery.parseIni(file)

        assertEquals("5554", props?.get("port.serial"))
        assertEquals("8554", props?.get("grpc.port"))
        assertEquals("abc123", props?.get("grpc.token"))
    }

    @Test
    fun `parseIni ignores comments and blank lines`() {
        val dir = runningDir()
        val file = writePidFile(
            dir, 111,
            """
            # a hash comment
            ; a semicolon comment

            port.serial=5554
            grpc.port=8554
            """
        )

        val props = EmulatorDiscovery.parseIni(file)

        assertEquals(2, props?.size)
        assertEquals("5554", props?.get("port.serial"))
    }

    @Test
    fun `parseIni trims surrounding whitespace`() {
        val dir = runningDir()
        val file = writePidFile(dir, 111, "  grpc.port  =  8554  ")

        assertEquals("8554", EmulatorDiscovery.parseIni(file)?.get("grpc.port"))
    }

    @Test
    fun `parseIni keeps values containing an equals sign`() {
        val dir = runningDir()
        // cmdline values routinely contain '='
        val file = writePidFile(dir, 111, "cmdline=emulator -avd Pixel -prop a=b")

        assertEquals("emulator -avd Pixel -prop a=b", EmulatorDiscovery.parseIni(file)?.get("cmdline"))
    }

    @Test
    fun `parseIni returns null for a missing file`() {
        assertNull(EmulatorDiscovery.parseIni(tempDir.resolve("nope.ini")))
    }

    // --- describe ---

    @Test
    fun `describe matches the emulator by console port`() {
        val dir = runningDir()
        writePidFile(dir, 111, "port.serial=5554\ngrpc.port=8554\ngrpc.token=tok-5554")
        writePidFile(dir, 222, "port.serial=5556\ngrpc.port=8556\ngrpc.token=tok-5556")

        val descriptor = discoveryOf(dir).describe("emulator-5556")

        assertEquals(5556, descriptor?.serialPort)
        assertEquals(8556, descriptor?.grpcPort)
        assertEquals("tok-5556", descriptor?.token)
    }

    @Test
    fun `describe scans every candidate root`() {
        val first = runningDir("first")
        val second = runningDir("second")
        writePidFile(second, 222, "port.serial=5554\ngrpc.port=8601\ngrpc.token=tok")

        val descriptor = discoveryOf(first, second).describe("emulator-5554")

        assertEquals(8601, descriptor?.grpcPort)
    }

    @Test
    fun `describe falls back to serial plus 3000 when nothing matches`() {
        val dir = runningDir()
        writePidFile(dir, 111, "port.serial=5556\ngrpc.port=8556")

        val descriptor = discoveryOf(dir).describe("emulator-5554")

        assertEquals(5554, descriptor?.serialPort)
        assertEquals(8554, descriptor?.grpcPort)
        assertNull(descriptor?.token)
    }

    @Test
    fun `describe returns null for a physical device serial`() {
        assertNull(discoveryOf(runningDir()).describe("R58M12ABCDE"))
    }

    @Test
    fun `describe tolerates a missing discovery directory`() {
        val absent = tempDir.resolve("does-not-exist").resolve("avd").resolve("running")

        val descriptor = discoveryOf(absent).describe("emulator-5554")

        assertEquals(8554, descriptor?.grpcPort, "should fall back rather than throw")
    }

    @Test
    fun `describe ignores malformed files rather than throwing`() {
        val dir = runningDir()
        writePidFile(dir, 111, "this file is nonsense\nno equals signs here")
        writePidFile(dir, 222, "port.serial=5554\ngrpc.port=8554\ngrpc.token=tok")

        val descriptor = discoveryOf(dir).describe("emulator-5554")

        assertEquals("tok", descriptor?.token)
    }

    @Test
    fun `describe ignores files that are not pid files`() {
        val dir = runningDir()
        Files.writeString(dir.resolve("notes.txt"), "port.serial=5554\ngrpc.port=9999")
        Files.writeString(dir.resolve("pid_abc.ini"), "port.serial=5554\ngrpc.port=9999")

        val descriptor = discoveryOf(dir).describe("emulator-5554")

        assertEquals(8554, descriptor?.grpcPort, "should have used the fallback")
    }

    @Test
    fun `describe surfaces JWT-only emulators`() {
        val dir = runningDir()
        writePidFile(dir, 111, "port.serial=5554\ngrpc.port=8554\ngrpc.jwks=/tmp/jwks")

        assertTrue(discoveryOf(dir).describe("emulator-5554")!!.requiresJwt)
    }

    // --- discoverAll ---

    @Test
    fun `discoverAll returns every usable emulator`() {
        val dir = runningDir()
        writePidFile(dir, 111, "port.serial=5554\ngrpc.port=8554")
        writePidFile(dir, 222, "port.serial=5556\ngrpc.port=8556")
        writePidFile(dir, 333, "port.serial=5558") // gRPC disabled — no port

        val all = discoveryOf(dir).discoverAll()

        assertEquals(setOf(5554, 5556), all.map { it.serialPort }.toSet())
    }

    // --- default roots ---

    @Test
    fun `default discovery roots all end in avd-running`() {
        val roots = EmulatorDiscovery.defaultDiscoveryRoots()

        assertTrue(roots.isNotEmpty())
        assertTrue(roots.all { it.endsWith(Path.of("avd", "running")) }) { roots.toString() }
    }
}


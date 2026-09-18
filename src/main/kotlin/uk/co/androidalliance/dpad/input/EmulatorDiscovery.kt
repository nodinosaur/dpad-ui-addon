package uk.co.androidalliance.dpad.input

import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Locates running emulators by reading the discovery files they publish.
 *
 * The emulator writes `pid_<PID>.ini` into `<discovery-root>/avd/running/` describing its
 * gRPC endpoint and auth token. See `docs/implementation/emulator-extended-controls.md` §5,
 * and upstream `EmulatorAdvertisement.cpp` / `ConfigDirs::getDiscoveryDirectory()`.
 *
 * The emulator writes to exactly one root (platform temp dir, falling back to `~/.android`),
 * so readers must scan every candidate.
 *
 * @param rootsProvider injection seam for tests; yields `avd/running` directories to scan.
 */
class EmulatorDiscovery(
    private val rootsProvider: () -> List<Path> = { defaultDiscoveryRoots() },
) {

    /**
     * Describes the emulator behind an ADB serial such as `emulator-5554`.
     *
     * Returns `null` if the serial is not an emulator serial. If no discovery file matches,
     * falls back to the historical guess of `serialPort + 3000` on IPv6 loopback so that
     * behaviour never regresses relative to the previous implementation.
     */
    fun describe(serial: String): EmulatorDescriptor? {
        val serialPort = EmulatorDescriptor.serialPortOf(serial) ?: return null
        return findBySerialPort(serialPort) ?: EmulatorDescriptor.fallback(serialPort)
    }

    /** All emulators that published a usable discovery file. */
    fun discoverAll(): List<EmulatorDescriptor> =
        discoveryFiles().mapNotNull { file ->
            EmulatorDescriptor.fromProperties(parseIni(file) ?: return@mapNotNull null)
        }

    private fun findBySerialPort(serialPort: Int): EmulatorDescriptor? =
        discoverAll().firstOrNull { it.serialPort == serialPort }

    private fun discoveryFiles(): List<Path> =
        rootsProvider()
            .filter { runCatching { Files.isDirectory(it) }.getOrDefault(false) }
            .flatMap { root ->
                runCatching {
                    Files.list(root).use { stream ->
                        stream.filter { PID_FILE.matches(it.fileName.toString()) }.toList()
                    }
                }.getOrDefault(emptyList())
            }

    companion object {

        /** Discovery files are named `pid_<PID>.ini`. */
        private val PID_FILE = Regex("""pid_\d+\.ini""")

        private const val RUNNING_SUBDIR_PARENT = "avd"
        private const val RUNNING_SUBDIR = "running"

        /**
         * Parses an emulator discovery file.
         *
         * These are plain `key=value` files. Blank lines and `#` / `;` comments are ignored.
         * Returns `null` if the file cannot be read; a malformed file must never bring the
         * plugin down.
         */
        internal fun parseIni(file: Path): Map<String, String>? = try {
            Files.readAllLines(file)
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith(";") }
                .filter { it.contains('=') }
                .map { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
                .filter { (key, _) -> key.isNotEmpty() }
                .toMap()
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }

        /**
         * Candidate `avd/running` directories, mirroring
         * `ConfigDirs::getDiscoveryDirectory()` plus the user-directory fallbacks that the
         * reference clients scan.
         */
        fun defaultDiscoveryRoots(): List<Path> =
            discoveryRootCandidates()
                .mapNotNull { it.toPathOrNull() }
                .map { it.resolve(RUNNING_SUBDIR_PARENT).resolve(RUNNING_SUBDIR) }
                .distinct()

        private fun discoveryRootCandidates(): List<String> {
            val roots = mutableListOf<String>()
            val os = System.getProperty("os.name").orEmpty().lowercase()
            val home = System.getProperty("user.home").orEmpty()

            when {
                os.contains("win") ->
                    System.getenv("LOCALAPPDATA")?.let { roots += "$it${sep}Temp" }

                os.contains("mac") || os.contains("darwin") ->
                    if (home.isNotEmpty()) {
                        roots += "$home${sep}Library${sep}Caches${sep}TemporaryItems"
                    }

                else -> {
                    System.getenv("XDG_RUNTIME_DIR")?.takeIf { it.isNotEmpty() }?.let { roots += it }
                    currentUid()?.let { roots += "/run/user/$it" }
                }
            }

            // Fallbacks used when the platform root is unavailable, plus the standard
            // emulator user directories the reference clients also scan.
            System.getenv("ANDROID_EMULATOR_HOME")?.takeIf { it.isNotEmpty() }?.let { roots += it }
            System.getenv("ANDROID_AVD_HOME")?.takeIf { it.isNotEmpty() }?.let { roots += it }
            System.getenv("ANDROID_SDK_HOME")?.takeIf { it.isNotEmpty() }
                ?.let { roots += "$it$sep.android" }
            if (home.isNotEmpty()) roots += "$home$sep.android"

            return roots
        }

        /**
         * POSIX uid of the current user, read via NIO file attributes to avoid depending on
         * `jdk.security.auth`, which is not guaranteed to be reachable from a plugin
         * classloader. `null` on platforms without the `unix` attribute view.
         */
        private fun currentUid(): String? = runCatching {
            val home = System.getProperty("user.home") ?: return@runCatching null
            Files.getAttribute(Paths.get(home), "unix:uid")?.toString()
        }.getOrNull()

        private val sep: String get() = java.io.File.separator

        private fun String.toPathOrNull(): Path? = try {
            Paths.get(this)
        } catch (e: InvalidPathException) {
            null
        }
    }
}




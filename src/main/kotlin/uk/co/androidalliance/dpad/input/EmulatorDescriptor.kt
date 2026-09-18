package uk.co.androidalliance.dpad.input

/**
 * Everything needed to talk to a single running emulator's gRPC endpoint.
 *
 * Populated from the emulator's discovery file (`pid_<PID>.ini`). See
 * `docs/implementation/emulator-extended-controls.md` §5.
 *
 * @param serialPort the emulator console port, i.e. the numeric suffix of the ADB serial
 *                   (`emulator-5554` -> `5554`). Matches the `port.serial` ini key.
 * @param grpcHost   host to dial. The emulator binds `[::1]` (IPv6 loopback) by default, so
 *                   this is *not* `localhost` — that may resolve to IPv4 and be refused.
 * @param grpcPort   the port actually bound by the emulator (`grpc.port`). Do not assume
 *                   `serialPort + 3000`; that is only the start of a 1000-port search range.
 * @param token      the value of `grpc.token`, a random 64-byte token regenerated on every
 *                   emulator launch. `null` when the endpoint is unauthenticated.
 * @param requiresJwt `true` when the emulator advertises JWT auth but no static token, i.e.
 *                   it was started with `-grpc-use-jwt` alone. Unsupported by this plugin.
 */
data class EmulatorDescriptor(
    val serialPort: Int,
    val grpcHost: String,
    val grpcPort: Int,
    val token: String?,
    val requiresJwt: Boolean,
) {

    /**
     * Hosts to try, in order. The emulator binds IPv6 loopback by default, but a
     * `-grpc <port>` launch binds `[::]`, and some environments have IPv6 loopback disabled.
     */
    fun hostCandidates(): List<String> =
        listOf(grpcHost, DEFAULT_GRPC_HOST, IPV4_LOOPBACK).distinct()

    companion object {

        /** The emulator's default gRPC bind address (`qemu-setup.cpp`: `address = "[::1]"`). */
        const val DEFAULT_GRPC_HOST = "::1"

        const val IPV4_LOOPBACK = "127.0.0.1"

        /** `grpc_start = android_serial_number_port + 3000` (`qemu-setup.cpp`). */
        const val GRPC_PORT_OFFSET = 3000

        const val KEY_SERIAL_PORT = "port.serial"
        const val KEY_GRPC_PORT = "grpc.port"
        const val KEY_GRPC_ADDRESS = "grpc.address"
        const val KEY_GRPC_TOKEN = "grpc.token"
        const val KEY_GRPC_JWKS = "grpc.jwks"

        /**
         * Best-effort descriptor for when no discovery file could be found. Preserves the
         * historical behaviour of guessing `serialPort + 3000` rather than hard-failing.
         */
        fun fallback(serialPort: Int): EmulatorDescriptor = EmulatorDescriptor(
            serialPort = serialPort,
            grpcHost = DEFAULT_GRPC_HOST,
            grpcPort = serialPort + GRPC_PORT_OFFSET,
            token = null,
            requiresJwt = false,
        )

        /**
         * Builds a descriptor from parsed discovery-file properties.
         *
         * Returns `null` when the file carries no usable gRPC port, which is the case for
         * emulators started with gRPC disabled.
         */
        fun fromProperties(props: Map<String, String>): EmulatorDescriptor? {
            val serialPort = props[KEY_SERIAL_PORT]?.trim()?.toIntOrNull() ?: return null
            val grpcPort = props[KEY_GRPC_PORT]?.trim()?.toIntOrNull() ?: return null
            if (grpcPort <= 0) return null

            val token = props[KEY_GRPC_TOKEN]?.trim()?.takeIf { it.isNotEmpty() }
            val hasJwks = props[KEY_GRPC_JWKS]?.trim()?.isNotEmpty() == true

            return EmulatorDescriptor(
                serialPort = serialPort,
                grpcHost = hostFromAddress(props[KEY_GRPC_ADDRESS]) ?: DEFAULT_GRPC_HOST,
                grpcPort = grpcPort,
                token = token,
                requiresJwt = hasJwks && token == null,
            )
        }

        /**
         * Extracts the host from a `grpc.address` value.
         *
         * Current emulator builds do not write this key, but reference clients honour it and
         * it may carry `host:port`, `[::1]:8554` or a bare host.
         */
        internal fun hostFromAddress(address: String?): String? {
            val value = address?.trim()?.takeIf { it.isNotEmpty() } ?: return null

            // Bracketed IPv6, optionally with a port: "[::1]" or "[::1]:8554"
            if (value.startsWith("[")) {
                val close = value.indexOf(']')
                if (close > 1) return value.substring(1, close)
                return null
            }

            // Bare IPv6 has several colons and no port suffix we can safely split on.
            if (value.count { it == ':' } > 1) return value

            // "host:port" or bare host
            return value.substringBefore(':').takeIf { it.isNotEmpty() }
        }

        /**
         * The console port encoded in an ADB serial, e.g. `emulator-5554` -> `5554`.
         * Returns `null` for anything that is not an emulator serial.
         */
        fun serialPortOf(serial: String): Int? {
            if (!serial.startsWith(SERIAL_PREFIX)) return null
            return serial.removePrefix(SERIAL_PREFIX).toIntOrNull()
        }

        private const val SERIAL_PREFIX = "emulator-"
    }
}


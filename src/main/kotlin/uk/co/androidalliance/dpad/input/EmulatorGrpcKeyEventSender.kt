package uk.co.androidalliance.dpad.input

import com.android.emulation.control.EmulatorControllerGrpcKt
import com.android.emulation.control.EmulatorControllerProto.KeyboardEvent
import com.android.emulation.control.EmulatorControllerProto.KeyboardEvent.KeyEventType
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import kotlinx.coroutines.delay
import uk.co.androidalliance.dpad.DPadPanel.DpadAction
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.ActionDown
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.ActionLongPress
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.ActionUp

/**
 * Raised when the emulator only accepts JWT-signed tokens, which this plugin does not
 * implement. The router reports this distinctly so the user gets an actionable message
 * rather than a generic connection failure.
 */
class UnsupportedAuthException(message: String) : Exception(message)

/**
 * Sends key events to an emulator over its `EmulatorController` gRPC endpoint — the same
 * mechanism Android Studio's embedded emulator uses.
 *
 * Key points, all verified against the emulator source (see
 * `docs/implementation/emulator-extended-controls.md`):
 *
 * - Codes are sent as `Evdev`, which skips server-side translation entirely.
 * - The server applies the `0x400` "pressed" bit itself; we must not.
 * - The auth header must be `authorization: Bearer <grpc.token>`.
 * - There is no server-side key hold, so a long press must be timed client-side.
 */
class EmulatorGrpcKeyEventSender(
    private val descriptor: EmulatorDescriptor,
    private val longPressHoldMs: Long = DEFAULT_LONG_PRESS_HOLD_MS,
    private val channelFactory: (host: String, port: Int) -> ManagedChannel = ::plaintextChannel,
) : KeyEventSender {

    private val lock = Any()

    @Volatile
    private var channel: ManagedChannel? = null

    @Volatile
    private var stub: EmulatorControllerGrpcKt.EmulatorControllerCoroutineStub? = null

    /** Index into [EmulatorDescriptor.hostCandidates]; advanced when a host proves dead. */
    @Volatile
    private var hostIndex: Int = 0

    override suspend fun sendKeyEvent(keyCode: Int, action: DpadAction) {
        if (descriptor.requiresJwt) {
            throw UnsupportedAuthException(
                "Emulator on port ${descriptor.grpcPort} requires JWT authentication, " +
                    "which the D-Pad plugin does not support."
            )
        }

        val evdev = EvdevKeyCodes.toEvdev(keyCode)
            ?: throw IllegalArgumentException(
                "Android keycode $keyCode has no evdev mapping the emulator can deliver"
            )

        for (step in stepsFor(action)) {
            when (step) {
                is Step.Send -> send(evdev, step.type)
                is Step.Wait -> delay(step.millis)
            }
        }
    }

    override fun shutdown() {
        synchronized(lock) {
            // Deliberately no awaitTermination: shutdown() is reached from dispose(), which
            // may run on the EDT. Blocking there freezes the IDE on close.
            channel?.shutdown()
            channel = null
            stub = null
        }
    }

    private suspend fun send(evdev: Int, type: KeyEventType) {
        val candidates = descriptor.hostCandidates()
        var lastFailure: Exception? = null

        // The emulator binds IPv6 loopback by default, but not in every configuration.
        // Walk the candidates once, remembering whichever works.
        while (hostIndex < candidates.size) {
            try {
                currentStub().sendKey(keyboardEvent(evdev, type))
                return
            } catch (e: Exception) {
                lastFailure = e
                synchronized(lock) {
                    channel?.shutdown()
                    channel = null
                    stub = null
                    hostIndex++
                }
            }
        }

        // Exhausted every host: reset so a later attempt can start again from the top.
        hostIndex = 0
        throw lastFailure ?: IllegalStateException("No gRPC host candidates available")
    }

    private fun currentStub(): EmulatorControllerGrpcKt.EmulatorControllerCoroutineStub {
        stub?.let { return it }

        return synchronized(lock) {
            stub ?: run {
                val host = descriptor.hostCandidates()[hostIndex]
                val created = channelFactory(host, descriptor.grpcPort)
                channel = created

                val target = descriptor.token
                    ?.let { ClientInterceptors.intercept(created, authInterceptor(it)) }
                    ?: created

                EmulatorControllerGrpcKt.EmulatorControllerCoroutineStub(target)
                    .also { stub = it }
            }
        }
    }

    internal sealed interface Step {
        data class Send(val type: KeyEventType) : Step
        data class Wait(val millis: Long) : Step
    }

    /**
     * Translates a UI action into the sequence of RPCs it requires.
     *
     * Note that [uk.co.androidalliance.dpad.DPadPanel] never emits [ActionDown] today — its
     * emission is commented out — so [ActionUp] means "a complete tap" and [ActionLongPress]
     * means "a complete long press" with no release event to follow. Hence the synthetic
     * hold rather than relying on a later key-up.
     */
    internal fun stepsFor(action: DpadAction): List<Step> = when (action) {
        // One RPC: the server emits keydown immediately followed by keyup.
        ActionUp -> listOf(Step.Send(KeyEventType.keypress))

        ActionDown -> listOf(Step.Send(KeyEventType.keydown))

        // No server-side hold exists, so hold the key ourselves for longer than Android's
        // 500 ms ViewConfiguration long-press threshold.
        ActionLongPress -> listOf(
            Step.Send(KeyEventType.keydown),
            Step.Wait(longPressHoldMs),
            Step.Send(KeyEventType.keyup),
        )
    }

    companion object {

        /** Comfortably above Android's 500 ms `ViewConfiguration` long-press threshold. */
        const val DEFAULT_LONG_PRESS_HOLD_MS = 600L

        internal fun keyboardEvent(evdev: Int, type: KeyEventType): KeyboardEvent =
            KeyboardEvent.newBuilder()
                // Evdev bypasses the server's keycode translation tables entirely.
                .setCodeType(KeyboardEvent.KeyCodeType.Evdev)
                .setEventType(type)
                // Send the bare code: the server ORs in 0x400 for keydown itself.
                .setKeyCode(evdev)
                .build()

        /**
         * The emulator's `StaticTokenAuth` compares the header verbatim against
         * `"Bearer " + token`, so the prefix is mandatory. Header names must be lowercase
         * because gRPC rejects anything that is not valid HTTP/2.
         */
        internal fun authMetadata(token: String): Metadata = Metadata().apply {
            put(
                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                "Bearer $token",
            )
        }

        private fun authInterceptor(token: String) =
            MetadataUtils.newAttachHeadersInterceptor(authMetadata(token))

        private fun plaintextChannel(host: String, port: Int): ManagedChannel =
            ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build()
    }
}


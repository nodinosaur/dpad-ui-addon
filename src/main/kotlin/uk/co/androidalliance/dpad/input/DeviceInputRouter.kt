package uk.co.androidalliance.dpad.input

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.androidalliance.dpad.DPadPanel.DpadAction
import uk.co.androidalliance.dpad.adb.Intent
import uk.co.androidalliance.dpad.notify.NotificationHelper
import java.util.concurrent.TimeUnit

class DeviceInputRouter(
    private val project: Project,
    private val discovery: EmulatorDiscovery = EmulatorDiscovery(),
) {

    private var cachedSender: KeyEventSender? = null
    private var cachedDevice: IDevice? = null

    /**
     * Set when gRPC has proved unusable for the cached device (bad auth, JWT-only, no
     * endpoint). Prevents retrying — and re-notifying — on every single key press.
     */
    private var grpcPoisoned: Boolean = false

    companion object {
        private val LOG = Logger.getInstance(DeviceInputRouter::class.java)
    }

    suspend fun sendKeyEvent(keyCode: Int, action: DpadAction) {
        val device = getFirstDevice() ?: return
        val sender = getSender(device)
        LOG.info("Sending keyCode=$keyCode action=$action to ${device.serialNumber} via ${sender::class.simpleName}")
        try {
            sender.sendKeyEvent(keyCode, action)
        } catch (e: Exception) {
            LOG.warn("Key event failed via ${sender::class.simpleName}: ${e.message}", e)
            if (sender is EmulatorGrpcKeyEventSender) {
                poisonGrpc(e)
                AdbKeyEventSender(device).sendKeyEvent(keyCode, action)
            } else {
                NotificationHelper.error("Failed to send key event: ${e.message}")
            }
        }
    }

    /**
     * Stops using gRPC for the cached device and explains why, once.
     *
     * [UnsupportedAuthException] and an unmapped keycode are permanent conditions, so they
     * are reported at a lower volume than a genuine connection failure.
     */
    private fun poisonGrpc(cause: Exception) {
        val alreadyPoisoned = grpcPoisoned
        grpcPoisoned = true
        cachedSender?.shutdown()
        cachedSender = null

        if (alreadyPoisoned) return

        when (cause) {
            is UnsupportedAuthException ->
                NotificationHelper.warn("${cause.message} Using ADB instead.")

            is IllegalArgumentException ->
                LOG.info("Key not deliverable over gRPC, using ADB: ${cause.message}")

            else ->
                NotificationHelper.warn("gRPC unavailable, falling back to ADB: ${cause.message}")
        }
    }

    suspend fun startActivity(intent: Intent) {
        val device = getFirstDevice() ?: return
        val launcher = getActivityLauncher(device)
        try {
            launcher.startActivity(intent)
        } catch (e: Exception) {
            LOG.warn("Start activity failed: ${e.message}", e)
            NotificationHelper.error("Failed to start activity: ${e.message}")
        }
    }

    suspend fun logConnectionStatus() {
        val device = getFirstDevice()
        if (device == null) {
            LOG.warn("D-Pad plugin: No device connected")
            return
        }
        if (!device.isEmulator) {
            LOG.info("D-Pad plugin connected: ${device.serialNumber} (physical device) via ADB")
            return
        }

        val descriptor = discovery.describe(device.serialNumber)
        if (descriptor == null) {
            LOG.info("D-Pad plugin connected: ${device.serialNumber} (emulator) via ADB — no gRPC endpoint")
            return
        }

        val auth = when {
            descriptor.requiresJwt -> "JWT required (unsupported)"
            descriptor.token != null -> "token auth"
            else -> "no auth"
        }
        LOG.info(
            "D-Pad plugin connected: ${device.serialNumber} (emulator) via gRPC " +
                "[${descriptor.grpcHost}]:${descriptor.grpcPort}, $auth"
        )
    }

    fun shutdown() {
        cachedSender?.shutdown()
        cachedSender = null
        cachedDevice = null
        grpcPoisoned = false
    }

    private fun getSender(device: IDevice): KeyEventSender {
        // Return cached sender if still for the same device
        if (cachedDevice?.serialNumber == device.serialNumber && cachedSender != null) {
            return cachedSender!!
        }

        // Different device: previous sender and any gRPC verdict no longer apply.
        cachedSender?.shutdown()
        if (cachedDevice?.serialNumber != device.serialNumber) {
            grpcPoisoned = false
        }

        val descriptor = if (device.isEmulator && !grpcPoisoned) {
            discovery.describe(device.serialNumber)
        } else {
            null
        }

        val sender = if (descriptor != null) {
            LOG.info(
                "Device ${device.serialNumber} is an emulator — using gRPC " +
                    "[${descriptor.grpcHost}]:${descriptor.grpcPort}"
            )
            EmulatorGrpcKeyEventSender(descriptor)
        } else {
            LOG.info("Device ${device.serialNumber} — using ADB")
            AdbKeyEventSender(device)
        }

        cachedSender = sender
        cachedDevice = device
        return sender
    }

    private fun getActivityLauncher(device: IDevice): ActivityLauncher {
        // ADB-based activity launching works for both emulators and physical devices
        return AdbKeyEventSender(device)
    }

    private suspend fun getFirstDevice(): IDevice? = withContext(Dispatchers.IO) {
        try {
            val adbFuture = AdbService.getInstance().getDebugBridge(project)
            val adb = adbFuture.get(5, TimeUnit.SECONDS)
            if (adb == null || !adb.isConnected) {
                LOG.warn("ADB bridge not connected")
                NotificationHelper.warn("ADB bridge not connected.")
                return@withContext null
            }
            val devices = adb.devices
            if (devices.isEmpty()) {
                LOG.warn("No ADB devices found")
                NotificationHelper.warn("No ADB devices found.")
                return@withContext null
            }
            LOG.info("Found ${devices.size} device(s), using: ${devices[0].serialNumber}")
            devices[0]
        } catch (e: Exception) {
            LOG.error("Failed to get ADB device: ${e.message}", e)
            NotificationHelper.error("Failed to connect to ADB: ${e.message}")
            null
        }
    }
}

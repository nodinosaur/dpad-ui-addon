package uk.co.androidalliance.dpad.input

import com.android.ddmlib.IDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.androidalliance.dpad.DPadPanel.DpadAction
import uk.co.androidalliance.dpad.DPadPanel.DpadAction.*
import uk.co.androidalliance.dpad.adb.AdbShellResponseHandler
import uk.co.androidalliance.dpad.adb.Intent
import uk.co.androidalliance.dpad.notify.NotificationHelper

class AdbKeyEventSender(private val device: IDevice) : KeyEventSender, ActivityLauncher {

    override suspend fun sendKeyEvent(keyCode: Int, action: DpadAction) {
        withContext(Dispatchers.IO) {
            NotificationHelper.info("Sending key event $keyCode to device ${device.serialNumber}")
            val keyAction = when (action) {
                ActionDown -> ""
                ActionUp -> ""
                ActionLongPress -> "--longpress"
            }
            device.executeShellCommand(
                "input keyevent $keyAction $keyCode",
                AdbShellResponseHandler()
            )
        }
    }

    override suspend fun startActivity(intent: Intent) {
        withContext(Dispatchers.IO) {
            NotificationHelper.info("Sending adb start activity to device ${device.serialNumber}")
            device.executeShellCommand(
                intent.toString(),
                AdbShellResponseHandler(),
                15L,
                java.util.concurrent.TimeUnit.SECONDS
            )
        }
    }

    override fun shutdown() {
        // No resources to clean up for ADB
    }
}

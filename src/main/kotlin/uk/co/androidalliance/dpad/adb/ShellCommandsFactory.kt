package uk.co.androidalliance.dpad.adb

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import uk.co.androidalliance.dpad.notify.NotificationHelper
import java.util.concurrent.TimeUnit

object ShellCommandsFactory {

    @JvmStatic
    fun startActivity(project: Project, intent: Intent) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val adbFuture = AdbService.getInstance().getDebugBridge(project)
                val adb = adbFuture.get()
                if (adb == null || !adb.isConnected) {
                    NotificationHelper.warn("ADB bridge not connected.")
                    return@executeOnPooledThread
                }
                val devices = adb.devices
                if (devices.isEmpty()) {
                    NotificationHelper.warn("No ADB devices found.")
                    return@executeOnPooledThread
                }
                val device: IDevice = devices[0] // Simple: Use first device
                NotificationHelper.info("Sending adb start activity to device ${device.serialNumber}")
                val shellCommand = intent.toString()
                device.executeShellCommand(shellCommand, AdbShellResponseHandler(), 15L, TimeUnit.SECONDS)
            } catch (e: Exception) {
                NotificationHelper.error(
                    String.format(
                        "Error sending start activity to device <b>%s</b> is not installed on %s",
                        intent.toString(),
                        e.message
                    )
                )
            }
        }
    }

    @JvmStatic
    fun startActivity(packageName: String, activityName: String, attachDebugger: Boolean): String {
        val debugFlag = if (attachDebugger) "-D " else ""
        return "am start $debugFlag-n $packageName/$activityName"
    }

    /** Sends an ADB key event to the connected device */
    @JvmStatic
    fun sendAdbKeyEvent(project: Project, keyCode: Int) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val adbFuture = AdbService.getInstance().getDebugBridge(project)
                val adb = adbFuture.get()
                if (adb == null || !adb.isConnected) {
                    NotificationHelper.warn("ADB bridge not connected.")
                    return@executeOnPooledThread
                }
                val devices = adb.devices
                if (devices.isEmpty()) {
                    NotificationHelper.warn("No ADB devices found.")
                    return@executeOnPooledThread
                }
                val device: IDevice = devices[0] // Simple: Use first device
                NotificationHelper.info("Sending key event $keyCode to device ${device.serialNumber}")
                device.executeShellCommand("input keyevent $keyCode", AdbShellResponseHandler())
            } catch (e: Exception) {
                NotificationHelper.error(
                    String.format(
                        "Error sending ADB key event <b>%s</b> is not installed on %s",
                        keyCode,
                        e.message
                    )
                )
            }
        }
    }
}
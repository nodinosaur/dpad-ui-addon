package uk.co.androidalliance.dpad.adb

import com.intellij.openapi.diagnostic.Logger
import uk.co.androidalliance.dpad.notify.NotificationHelper

/** Handles ADB shell command output */
class AdbShellResponseHandler : com.android.ddmlib.IShellOutputReceiver {
    val output = StringBuilder()

    override fun addOutput(data: ByteArray, offset: Int, length: Int) {
        output.append(String(data, offset, length))
    }

    override fun flush() {
        if (output.isNotEmpty()) {
            NotificationHelper.info("ADB Shell Output:\n$output")
        } else {
            NotificationHelper.info("ADB Shell command executed (no output).")
        }
    }

    override fun isCancelled(): Boolean = false
}
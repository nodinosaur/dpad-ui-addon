package uk.co.androidalliance.dpad

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/** Main panel containing the D-pad and additional control buttons */
class DPadToolWindowContent(private val project: Project) : JPanel() {

    private val LOG = Logger.getInstance(DPadToolWindowContent::class.java)

    init {

        val panel = JBPanel<JBPanel<*>>(BorderLayout())
        panel.border = EmptyBorder(0, 0, 0, 0)

        // Main row panel with horizontal layout
        val rowPanel = JBPanel<JBPanel<*>>(GridLayout(1, 2, 10, 0))

        // Column with buttons
        val buttonColumn = JBPanel<JBPanel<*>>(GridLayout(3, 1, 0, 5))

        buttonColumn.add(createLabelButton("Back", KEYCODE_BACK))
        buttonColumn.add(createLabelButton("Home", KEYCODE_HOME))
        buttonColumn.add(createLabelButton("Settings", KEYCODE_SETTINGS))

        // D-pad panel
        val dPadPanel = DPadPanel()

        // Set D-pad direction click callback
        dPadPanel.setOnDirectionClickListener { direction ->
            val directionName = when (direction) {
                DPadPanel.UP -> "UP"
                DPadPanel.RIGHT -> "RIGHT"
                DPadPanel.DOWN -> "DOWN"
                DPadPanel.LEFT -> "LEFT"
                DPadPanel.CENTER -> "CENTER"
                else -> "UNKNOWN"
            }
            LOG.info("D-pad direction clicked: $directionName")

            // Example of performing an action based on direction
            when (direction) {
                DPadPanel.UP -> {
                    sendAdbKeyEvent(KEYCODE_DPAD_UP)
                }

                DPadPanel.RIGHT -> {
                    sendAdbKeyEvent(KEYCODE_DPAD_RIGHT)
                }

                DPadPanel.DOWN -> {
                    sendAdbKeyEvent(KEYCODE_DPAD_DOWN)
                }

                DPadPanel.LEFT -> {
                    sendAdbKeyEvent(KEYCODE_DPAD_LEFT)
                }

                DPadPanel.CENTER -> {
                    sendAdbKeyEvent(KEYCODE_DPAD_CENTER)
                }
            }
        }

        // Add components to main row
        rowPanel.add(buttonColumn)
        rowPanel.add(dPadPanel)

        panel.add(rowPanel, BorderLayout.CENTER)
        this.add(panel, BorderLayout.CENTER)
    }

    /** Creates a text-based button with the specified label and key code */
    private fun createLabelButton(text: String, keyCode: Int): JButton {
        val button = JButton(text)
        button.addActionListener {
            sendAdbKeyEvent(keyCode)
        }
        return button
    }

    /** Sends an ADB key event to the connected device */
    private fun sendAdbKeyEvent(keyCode: Int) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val adbFuture = AdbService.getInstance().getDebugBridge(project)
                val adb = adbFuture.get()
                if (adb == null || !adb.isConnected) {
                    LOG.warn("ADB bridge not connected.")
                    return@executeOnPooledThread
                }
                val devices = adb.devices
                if (devices.isEmpty()) {
                    LOG.warn("No ADB devices found.")
                    return@executeOnPooledThread
                }
                val device: IDevice = devices[0] // Simple: Use first device
                LOG.info("Sending key event $keyCode to device ${device.serialNumber}")
                device.executeShellCommand("input keyevent $keyCode", AdbShellResponseHandler())
            } catch (e: Exception) {
                LOG.error("Error sending ADB key event $keyCode", e)
                // TODO: Show error to user in UI thread
            }
        }
    }

    /** Handles ADB shell command output */
    private class AdbShellResponseHandler : com.android.ddmlib.IShellOutputReceiver {
        private val LOG = Logger.getInstance(AdbShellResponseHandler::class.java)
        val output = StringBuilder()

        override fun addOutput(data: ByteArray, offset: Int, length: Int) {
            output.append(String(data, offset, length))
        }

        override fun flush() {
            if (output.isNotEmpty()) {
                LOG.info("ADB Shell Output:\n$output")
            } else {
                LOG.info("ADB Shell command executed (no output).")
            }
        }

        override fun isCancelled(): Boolean = false
    }

    companion object AdbKeyCodes {
        const val KEYCODE_DPAD_UP = 19
        const val KEYCODE_DPAD_DOWN = 20
        const val KEYCODE_DPAD_LEFT = 21
        const val KEYCODE_DPAD_RIGHT = 22
        const val KEYCODE_DPAD_CENTER = 23
        const val KEYCODE_BACK = 4
        const val KEYCODE_HOME = 3
        const val KEYCODE_SETTINGS = 176
    }

}

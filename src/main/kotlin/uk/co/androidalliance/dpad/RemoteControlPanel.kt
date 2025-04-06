package uk.co.androidalliance.dpad

import com.intellij.openapi.project.Project
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JPanel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger // Use platform logger
import com.android.ddmlib.IDevice // For potentially getting the device
import com.android.tools.idea.adb.AdbService // Preferred way to get AdbBridge
import java.awt.Component // Import Component for general swing components

// Keycodes from android.view.KeyEvent
object AdbKeyCodes {
    const val KEYCODE_DPAD_UP = 19
    const val KEYCODE_DPAD_DOWN = 20
    const val KEYCODE_DPAD_LEFT = 21
    const val KEYCODE_DPAD_RIGHT = 22
    const val KEYCODE_DPAD_CENTER = 23
    const val KEYCODE_ENTER = 66 // Often same as DPAD_CENTER
    const val KEYCODE_BACK = 4
    const val KEYCODE_HOME = 3
    const val KEYCODE_SETTINGS = 176 // Settings key
    // Add others as needed: POWER=26, MENU=82, VOLUME_UP=24, VOLUME_DOWN=25 etc.
}


class RemoteControlPanel(private val project: Project) : JPanel() {

    private val LOG = Logger.getInstance(RemoteControlPanel::class.java)

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        // Consistent padding for all components
        gbc.insets = Insets(5, 5, 5, 5)
        // Default anchor and fill (can be overridden per component)
        gbc.anchor = GridBagConstraints.CENTER
        gbc.fill = GridBagConstraints.NONE
        gbc.gridwidth = 1
        gbc.gridheight = 1
        gbc.weightx = 0.0 // Don't stretch horizontally by default
        gbc.weighty = 0.0 // Don't stretch vertically by default


        // --- NEW: Back/Home/Settings Column (Left) ---
        gbc.gridx = 0 // Column 0
        gbc.anchor = GridBagConstraints.NORTHWEST // Align buttons to top-left within their cell
        // Optional: Make buttons fill the width of the column if desired
        // gbc.fill = GridBagConstraints.HORIZONTAL
        // gbc.weightx = 0.1 // Give slight weight if filling

        // Back Button
        gbc.gridy = 0
        add(createAdbButton("Back", AdbKeyCodes.KEYCODE_BACK), gbc)

        // Home Button
        gbc.gridy = 1
        add(createAdbButton("Home", AdbKeyCodes.KEYCODE_HOME), gbc)

        // Settings Button
        gbc.gridy = 2
        add(createAdbButton("Settings", AdbKeyCodes.KEYCODE_SETTINGS), gbc)

        // Reset anchor/fill/weight if they were changed and shouldn't apply to D-Pad
        gbc.anchor = GridBagConstraints.CENTER
        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0


        // --- D-Pad (Shifted Right) ---
        // Up
        gbc.gridx = 2 // Was 1
        gbc.gridy = 0
        add(createAdbButton("↑", AdbKeyCodes.KEYCODE_DPAD_UP), gbc)

        // Left
        gbc.gridx = 1 // Was 0
        gbc.gridy = 1
        add(createAdbButton("←", AdbKeyCodes.KEYCODE_DPAD_LEFT), gbc)

        // Center (OK/Enter)
        gbc.gridx = 2 // Was 1
        gbc.gridy = 1
        add(createAdbButton("OK", AdbKeyCodes.KEYCODE_DPAD_CENTER), gbc) // Or KEYCODE_ENTER

        // Right
        gbc.gridx = 3 // Was 2
        gbc.gridy = 1
        add(createAdbButton("→", AdbKeyCodes.KEYCODE_DPAD_RIGHT), gbc)

        // Down
        gbc.gridx = 2 // Was 1
        gbc.gridy = 2
        add(createAdbButton("↓", AdbKeyCodes.KEYCODE_DPAD_DOWN), gbc)

        // --- Optional: Add glue components to push everything to the top-left ---
        // Add a component that takes up all remaining horizontal space
        gbc.gridx = 4 // Next available column after the D-Pad
        gbc.gridy = 0
        gbc.weightx = 1.0 // Give this component horizontal weight
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(JPanel().apply { isOpaque = false }, gbc) // Add an invisible panel

        // Add a component that takes up all remaining vertical space
        gbc.gridx = 0
        gbc.gridy = 3 // Next available row
        gbc.weighty = 1.0 // Give this component vertical weight
        gbc.gridwidth = 5 // Span all columns used
        gbc.fill = GridBagConstraints.VERTICAL
        add(JPanel().apply { isOpaque = false }, gbc) // Add an invisible panel

    }

    // --- Rest of the class (createAdbButton, sendAdbKeyEvent, AdbShellResponseHandler) remains the same ---

    private fun createAdbButton(text: String, keyCode: Int): JButton {
        val button = JButton(text)
        button.addActionListener {
            sendAdbKeyEvent(keyCode)
        }
        return button
    }

    private fun sendAdbKeyEvent(keyCode: Int) {
        // IMPORTANT: Run ADB commands off the UI thread!
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                // Get the ADB bridge instance for the project
                val adbFuture = AdbService.getInstance().getDebugBridge(project) // Use AdbService
                val adb = adbFuture.get() // This might block, already on pooled thread is okay

                if (adb == null || !adb.isConnected) {
                    LOG.warn("ADB bridge not connected.")
                    // TODO: Show error to user
                    return@executeOnPooledThread
                }

                val devices = adb.devices
                if (devices.isEmpty()) {
                    LOG.warn("No ADB devices found.")
                    // TODO: Show error to user
                    return@executeOnPooledThread
                }

                // Simple: Use the first device found. A real plugin needs device selection.
                val device: IDevice = devices[0]

                LOG.info("Sending key event $keyCode to device ${device.serialNumber}")
                // Using executeShellCommand - more robust than Runtime.exec
                device.executeShellCommand("input keyevent $keyCode", AdbShellResponseHandler())

            } catch (e: Exception) {
                LOG.error("Error sending ADB key event $keyCode", e)
                // TODO: Show detailed error to user
            }
        }
    }

    // Simple handler to just log output/errors from the shell command
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
            // Potentially parse output here if needed
        }

        override fun isCancelled(): Boolean {
            return false // Set to true to cancel execution
        }
    }
}
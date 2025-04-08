package uk.co.androidalliance.dpad

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import uk.co.androidalliance.dpad.theme.Typography.controlButtons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBPanel
import com.intellij.ui.scale.JBUIScale
import java.awt.*
import javax.swing.JButton
import javax.swing.JPanel
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentUpDark

/** Main panel containing the D-pad and additional control buttons */
class DPadToolWindowContent(private val project: Project) : JPanel() {

    private val LOG = Logger.getInstance(DPadToolWindowContent::class.java)

    init {
        layout = FlowLayout(FlowLayout.CENTER, 0, 0)

        // Create a panel for all controls
        val controlsPanel = JPanel() // Keep internal spacing

        // Create a panel for all controls with FlowLayout aligned LEFT
        //val controlsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 10, 10)) // Keep internal spacing

        // Column with buttons
        val lhButtonColumn: JBPanel<JBPanel<*>> = JBPanel(GridLayout(3, 1, 0, 5))
        lhButtonColumn.add(createIconButton("/icons/outline/settings_24dp", "Settings", KEYCODE_SETTINGS))
        lhButtonColumn.add(createIconButton("/icons/fill/home_24dp", "Home", KEYCODE_HOME))
        lhButtonColumn.add(createIconButton("/icons/fill/arrow_back_24dp.svg", "Back", KEYCODE_BACK))

        // D-pad panel
        val dPadPanel = DPadPanel(dPadSize = 120)

        val rhButtonColumn: JBPanel<JBPanel<*>> = JBPanel(GridLayout(3, 1, 0, 5))
        rhButtonColumn.add(createIconButton("/icons/outline/bookmark_24dp.svg", "Bookmark", KEYCODE_BOOKMARK))
        rhButtonColumn.add(createIconButton("/icons/fill/person_24dp.svg", "Accounts", KEYCODE_CONTACTS))
        rhButtonColumn.add(createIconButton("/icons/outline/live_tv_24dp.svg", "Live TV", KEYCODE_TV))

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

        // Add components to the grouping panel
        controlsPanel.add(lhButtonColumn)
        controlsPanel.add(dPadPanel)
        controlsPanel.add(rhButtonColumn)

        add(controlsPanel)
    }

    /** Creates a text-based button with the specified label and key code */
    private fun createLabelButton(text: String, keyCode: Int): JButton {
        val button = JButton(text)
        button.font = controlButtons
        button.addActionListener {
            sendAdbKeyEvent(keyCode)
        }
        return button
    }

    private fun createIconButton(
        iconPath: String, tooltipText: String,
        keyCode: Int
    ): JButton {
        val icon = IconLoader.getIcon(iconPath, this::class.java)
        val button = JButton(icon)
        button.background = SegmentUpDark
        button.toolTipText = tooltipText
        button.isBorderPainted = false
        button.isContentAreaFilled = true
        button.preferredSize = Dimension(
            JBUIScale.scale(40),
            JBUIScale.scale(40)
        )
        button.addActionListener { sendAdbKeyEvent(keyCode) }
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
        // D-Pad navigation
        const val KEYCODE_DPAD_UP = 19
        const val KEYCODE_DPAD_DOWN = 20
        const val KEYCODE_DPAD_LEFT = 21
        const val KEYCODE_DPAD_RIGHT = 22
        const val KEYCODE_DPAD_CENTER = 23

        // System navigation
        const val KEYCODE_BACK = 4
        const val KEYCODE_HOME = 3
        const val KEYCODE_APP_SWITCH = 187  // Recent apps/app switcher
        const val KEYCODE_SETTINGS = 176

        // Volume controls
        const val KEYCODE_VOLUME_UP = 24
        const val KEYCODE_VOLUME_DOWN = 25
        const val KEYCODE_VOLUME_MUTE = 164

        // Media controls
        const val KEYCODE_MEDIA_PLAY_PAUSE = 85
        const val KEYCODE_MEDIA_NEXT = 87
        const val KEYCODE_MEDIA_PREVIOUS = 88

        // Power
        const val KEYCODE_POWER = 26

        // Standard keys
        const val KEYCODE_ENTER = 66
        const val KEYCODE_TAB = 61
        const val KEYCODE_SPACE = 62
        const val KEYCODE_DEL = 67  // Backspace

        // Navigation in content
        const val KEYCODE_PAGE_UP = 92
        const val KEYCODE_PAGE_DOWN = 93
        const val KEYCODE_MOVE_HOME = 122
        const val KEYCODE_MOVE_END = 123

        // TV specific
        const val KEYCODE_TV = 170
        const val KEYCODE_GUIDE = 172       // Opens the program guide within TV apps
        const val KEYCODE_TV_POWER = 177    // TV power on/off
        const val KEYCODE_TV_INPUT = 178    // Used to change TV input sources

        // Android TV navigation
        const val KEYCODE_NAVIGATE_NEXT = 261
        const val KEYCODE_NAVIGATE_PREVIOUS = 262

        // Search
        const val KEYCODE_SEARCH = 84

        // Useful for testing
        const val KEYCODE_MENU = 82
        const val KEYCODE_NOTIFICATION = 83


        const val KEYCODE_BOOKMARK = 174
        const val KEYCODE_CONTACTS = 207
    }

}

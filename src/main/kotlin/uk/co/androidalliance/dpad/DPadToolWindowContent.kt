package uk.co.androidalliance.dpad

import com.intellij.openapi.project.Project
import java.awt.*
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JButton
import javax.swing.JPanel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService

/** Main panel containing the D-pad and additional control buttons */
class DPadToolWindowContent(private val project: Project) : JPanel() {

    private val LOG = Logger.getInstance(DPadToolWindowContent::class.java)

    init {
        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        // Default settings (can be overridden)
        gbc.anchor = GridBagConstraints.CENTER
        gbc.fill = GridBagConstraints.NONE
        gbc.gridwidth = 1
        gbc.gridheight = 1
        gbc.weightx = 0.0
        gbc.weighty = 0.0

        // --- Back/Home/Settings Column (Left) ---
        gbc.gridx = 0
        gbc.anchor = GridBagConstraints.NORTHWEST
        gbc.insets = Insets(5, 5, 5, 5) // Keep 5px padding around this column

        // Back Button
        gbc.gridy = 0
        add(createLabelButton("Back", AdbKeyCodes.KEYCODE_BACK), gbc)

        // Home Button
        gbc.gridy = 1
        add(createLabelButton("Home", AdbKeyCodes.KEYCODE_HOME), gbc)

        // Settings Button
        gbc.gridy = 2
        add(createLabelButton("Settings", AdbKeyCodes.KEYCODE_SETTINGS), gbc)

        // --- D-Pad (Shifted Right) ---
        gbc.anchor = GridBagConstraints.CENTER // Reset anchor for D-pad group

        // **** KEY CHANGE: Reduce insets for the D-pad cluster ****
        // Use 1px spacing, or 0 for completely adjacent
        //gbc.insets = Insets(1, 1, 1, 1)
        // For zero spacing:
        gbc.insets = Insets(0, 0, 0, 0)

        // Up
        gbc.gridx = 2
        gbc.gridy = 0
        add(createDpadButton(DpadType.UP, AdbKeyCodes.KEYCODE_DPAD_UP), gbc)

        // Left
        gbc.gridx = 1
        gbc.gridy = 1
        add(createDpadButton(DpadType.LEFT, AdbKeyCodes.KEYCODE_DPAD_LEFT), gbc)

        // Center
        gbc.gridx = 2
        gbc.gridy = 1
        add(createDpadButton(DpadType.CENTER, AdbKeyCodes.KEYCODE_DPAD_CENTER), gbc)

        // Right
        gbc.gridx = 3
        gbc.gridy = 1
        add(createDpadButton(DpadType.RIGHT, AdbKeyCodes.KEYCODE_DPAD_RIGHT), gbc)

        // Down
        gbc.gridx = 2
        gbc.gridy = 2
        add(createDpadButton(DpadType.DOWN, AdbKeyCodes.KEYCODE_DPAD_DOWN), gbc)

        // --- Glue components for layout flexibility ---
        // Reset insets if desired for glue, though likely not necessary
        // gbc.insets = Insets(0, 0, 0, 0) // Or back to (5, 5, 5, 5)
        gbc.gridx = 4
        gbc.gridy = 0
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(JPanel().apply { isOpaque = false }, gbc) // Horizontal glue

        gbc.gridx = 0
        gbc.gridy = 3
        gbc.weightx = 0.0 // Reset weightx
        gbc.weighty = 1.0
        gbc.gridwidth = 5 // Span all columns
        gbc.fill = GridBagConstraints.VERTICAL
        add(JPanel().apply { isOpaque = false }, gbc) // Vertical glue

        // Optional: Suggest an initial size for the whole panel
        preferredSize = Dimension(120, 120) // Adjust as needed
    }

    // --- createDpadButton, createLabelButton, sendAdbKeyEvent, AdbShellResponseHandler remain the same ---
    /** Creates a D-pad button with the specified type and key code */
    private fun createDpadButton(type: DpadType, keyCode: Int): JButton {
        val button = DpadButton(type) // Use the existing custom button
        button.addActionListener {
            sendAdbKeyEvent(keyCode)
        }
        return button
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
}

/** Enum to represent the different D-pad button types */
enum class DpadType { UP, LEFT, CENTER, RIGHT, DOWN }

// --- DpadButton class remains the same as the last version using simplified SVG ---
/** Custom JButton that draws D-pad shapes based on the simplified SVG */
class DpadButton(private val type: DpadType) : JButton() {
    // ... (Keep the DpadButton code from the previous response) ...
    private val prefSize = 45
    init {
        preferredSize = Dimension(prefSize, prefSize)
        isBorderPainted = false
        isContentAreaFilled = false
        isFocusPainted = false
        isOpaque = false
    }

    private fun scalePoint(svgX: Double, svgY: Double, width: Int, height: Int): Point2D.Double {
        val scaleX = width / 120.0
        val scaleY = height / 120.0
        return Point2D.Double(svgX * scaleX, svgY * scaleY)
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g.create() as Graphics2D
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val currentWidth = width
            val currentHeight = height
            val drawColor = if (model.isArmed && model.isPressed) foreground.darker()
            else if (model.isRollover) foreground.brighter()
            else foreground
            g2d.color = drawColor

            when (type) {
                DpadType.CENTER -> {
                    val p1 = scalePoint(41.0, 41.0, currentWidth, currentHeight)
                    val rectWidth = scalePoint(38.0, 0.0, currentWidth, currentHeight).x
                    val rectHeight = scalePoint(0.0, 38.0, currentWidth, currentHeight).y
                    g2d.fill(Rectangle2D.Double(p1.x, p1.y, rectWidth, rectHeight))
                }
                DpadType.LEFT -> {
                    val p1 = scalePoint(39.0, 40.4, currentWidth, currentHeight); val p2 = scalePoint(39.0, 79.6, currentWidth, currentHeight)
                    val p3 = scalePoint(0.0, 118.6, currentWidth, currentHeight); val p4 = scalePoint(0.0, 1.4, currentWidth, currentHeight)
                    val path = Path2D.Double(); path.moveTo(p1.x, p1.y); path.lineTo(p2.x, p2.y); path.lineTo(p3.x, p3.y); path.lineTo(p4.x, p4.y); path.closePath()
                    g2d.fill(path)
                }
                DpadType.DOWN -> {
                    val p1 = scalePoint(40.4, 81.0, currentWidth, currentHeight); val p2 = scalePoint(79.6, 81.0, currentWidth, currentHeight)
                    val p3 = scalePoint(118.6, 120.0, currentWidth, currentHeight); val p4 = scalePoint(1.4, 120.0, currentWidth, currentHeight)
                    val path = Path2D.Double(); path.moveTo(p1.x, p1.y); path.lineTo(p2.x, p2.y); path.lineTo(p3.x, p3.y); path.lineTo(p4.x, p4.y); path.closePath()
                    g2d.fill(path)
                }
                DpadType.RIGHT -> {
                    val p1 = scalePoint(120.0, 1.4, currentWidth, currentHeight); val p2 = scalePoint(120.0, 118.6, currentWidth, currentHeight)
                    val p3 = scalePoint(81.0, 79.6, currentWidth, currentHeight); val p4 = scalePoint(81.0, 40.4, currentWidth, currentHeight)
                    val path = Path2D.Double(); path.moveTo(p1.x, p1.y); path.lineTo(p2.x, p2.y); path.lineTo(p3.x, p3.y); path.lineTo(p4.x, p4.y); path.closePath()
                    g2d.fill(path)
                }
                DpadType.UP -> {
                    val p1 = scalePoint(1.4, 0.0, currentWidth, currentHeight); val p2 = scalePoint(118.6, 0.0, currentWidth, currentHeight)
                    val p3 = scalePoint(79.6, 39.0, currentWidth, currentHeight); val p4 = scalePoint(40.4, 39.0, currentWidth, currentHeight)
                    val path = Path2D.Double(); path.moveTo(p1.x, p1.y); path.lineTo(p2.x, p2.y); path.lineTo(p3.x, p3.y); path.lineTo(p4.x, p4.y); path.closePath()
                    g2d.fill(path)
                }
            }
        } finally {
            g2d.dispose()
        }
    }
}


// --- AdbKeyCodes object should also be present ---
object AdbKeyCodes {
    const val KEYCODE_DPAD_UP = 19
    const val KEYCODE_DPAD_DOWN = 20
    const val KEYCODE_DPAD_LEFT = 21
    const val KEYCODE_DPAD_RIGHT = 22
    const val KEYCODE_DPAD_CENTER = 23
    const val KEYCODE_BACK = 4
    const val KEYCODE_HOME = 3
    const val KEYCODE_SETTINGS = 176
}
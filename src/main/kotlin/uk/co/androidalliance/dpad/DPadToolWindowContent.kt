package uk.co.androidalliance.dpad

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_BACK
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_BOOKMARK
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_CONTACTS
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_CENTER
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_DOWN
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_LEFT
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_RIGHT
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_UP
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_HOME
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_SETTINGS
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_TV
import uk.co.androidalliance.dpad.adb.ShellCommandsFactory
import uk.co.androidalliance.dpad.theme.Typography.controlButtons
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel

/** Main panel containing the D-pad and additional control buttons */
/** Main panel containing the D-pad and additional control buttons */
class DPadToolWindowContent(private val project: Project) : JPanel() {

    private val LOG = Logger.getInstance(DPadToolWindowContent::class.java)

    // UI components that need to be updated when theme changes
    private var lhButtonColumn: JBPanel<JBPanel<*>>
    private var rhButtonColumn: JBPanel<JBPanel<*>>
    private var dPadPanel: DPadPanel
    private var controlsPanel: JPanel

    init {
        layout = FlowLayout(FlowLayout.CENTER, 0, 0)

        // Create a panel for all controls
        controlsPanel = JBPanel<Nothing>(FlowLayout(FlowLayout.CENTER, JBUIScale.scale(10), 0))
        controlsPanel.border = JBUI.Borders.empty(5)

        // Column with buttons
        lhButtonColumn = JBPanel(GridLayout(3, 1, 0, JBUIScale.scale(5)))
        lhButtonColumn.add(createIconButton("/icons/outline/settings_24dp", "Settings", KEYCODE_SETTINGS))
        lhButtonColumn.add(createIconButton("/icons/fill/home_24dp", "Home", KEYCODE_HOME))
        lhButtonColumn.add(createIconButton("/icons/fill/arrow_back_24dp.svg", "Back", KEYCODE_BACK))

        // D-pad panel
        dPadPanel = DPadPanel(dPadSize = JBUIScale.scale(120))

        rhButtonColumn = JBPanel(GridLayout(3, 1, 0, JBUIScale.scale(5)))
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
                DPadPanel.UP -> sendAdbKeyEvent(KEYCODE_DPAD_UP)
                DPadPanel.RIGHT -> sendAdbKeyEvent(KEYCODE_DPAD_RIGHT)
                DPadPanel.DOWN -> sendAdbKeyEvent(KEYCODE_DPAD_DOWN)
                DPadPanel.LEFT -> sendAdbKeyEvent(KEYCODE_DPAD_LEFT)
                DPadPanel.CENTER -> sendAdbKeyEvent(KEYCODE_DPAD_CENTER)
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
        val size = JBUIScale.scale(32)
        button.background = JBColor.PanelBackground
        button.toolTipText = tooltipText
        button.isBorderPainted = false
        button.isContentAreaFilled = false
        button.margin = JBUI.emptyInsets()
        button.preferredSize = Dimension(size, size)
        button.minimumSize = button.preferredSize
        button.maximumSize = button.preferredSize

        button.addActionListener { sendAdbKeyEvent(keyCode) }
        return button
    }

    /** Sends an ADB key event to the connected device */
    private fun sendAdbKeyEvent(keyCode: Int) {
        ShellCommandsFactory.sendAdbKeyEvent(project, keyCode)
    }

}
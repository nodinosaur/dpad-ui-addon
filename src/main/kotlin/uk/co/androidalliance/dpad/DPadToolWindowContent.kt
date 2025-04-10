package uk.co.androidalliance.dpad

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import uk.co.androidalliance.dpad.adb.Intent
import uk.co.androidalliance.dpad.adb.Intent.Companion.FLAG_ACTIVITY_NEW_TASK
import uk.co.androidalliance.dpad.adb.Intent.Companion.FLAG_ACTIVITY_REQUIRE_DEFAULT
import uk.co.androidalliance.dpad.adb.Intent.Companion.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_BACK
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_BOOKMARK
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_CENTER
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_DOWN
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_LEFT
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_RIGHT
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_DPAD_UP
import uk.co.androidalliance.dpad.adb.KeyCodes.KEYCODE_HOME
import uk.co.androidalliance.dpad.adb.ShellCommandsFactory
import uk.co.androidalliance.dpad.theme.Typography.controlButtons
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel

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

        lhButtonColumn = JBPanel(GridLayout(3, 1, 0, JBUIScale.scale(5)))
        val settingsIntent = Intent(
            flags = FLAG_ACTIVITY_NEW_TASK,
            componentName = "com.android.tv.settings/.MainSettings",
        )
        lhButtonColumn.add(
            createIconButton(
                iconPath = "/icons/outline/settings_24dp.svg",
                tooltipText = "Settings",
                keyCode = null /*KEYCODE_SETTINGS*/,
                intent = settingsIntent
            )
        )
        lhButtonColumn.add(
            createIconButton(
                iconPath = "/icons/fill/home_24dp.svg",
                tooltipText = "Home",
                keyCode = KEYCODE_HOME
            )
        )
        lhButtonColumn.add(
            createIconButton(
                iconPath = "/icons/fill/arrow_back_24dp.svg",
                tooltipText = "Back",
                keyCode = KEYCODE_BACK
            )
        )

        dPadPanel = DPadPanel(dPadSize = JBUIScale.scale(120))

        rhButtonColumn = JBPanel(GridLayout(3, 1, 0, JBUIScale.scale(5)))
        rhButtonColumn.add(
            createIconButton(
                iconPath = "/icons/outline/bookmark_24dp.svg",
                tooltipText = "Bookmark",
                keyCode = KEYCODE_BOOKMARK
            )
        )

        val accountsIntent = Intent(
            action = "android.app.action.TOGGLE_NOTIFICATION_HANDLER_PANEL",
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER or FLAG_ACTIVITY_REQUIRE_DEFAULT
        )
        rhButtonColumn.add(
            createIconButton(
                iconPath = "/icons/fill/person_24dp.svg",
                tooltipText = "Accounts",
                keyCode = null,
                intent = accountsIntent
            )
        )

        val liveTvIntent = Intent(
            flags = FLAG_ACTIVITY_NEW_TASK,
            componentName = "com.android.tv/.MainActivity",
        )
        rhButtonColumn.add(createIconButton("/icons/outline/live_tv_24dp.svg", "Live TV", null, liveTvIntent))

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

            when (direction) {
                DPadPanel.UP -> sendAdbKeyEvent(KEYCODE_DPAD_UP)
                DPadPanel.RIGHT -> sendAdbKeyEvent(KEYCODE_DPAD_RIGHT)
                DPadPanel.DOWN -> sendAdbKeyEvent(KEYCODE_DPAD_DOWN)
                DPadPanel.LEFT -> sendAdbKeyEvent(KEYCODE_DPAD_LEFT)
                DPadPanel.CENTER -> sendAdbKeyEvent(KEYCODE_DPAD_CENTER)
            }
        }

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
        keyCode: Int? = null,
        intent: Intent? = null
    ): JButton {
        val icon = IconLoader.getIcon(iconPath, this::class.java)
        val button = JButton(icon)
        val size = JBUIScale.scale(40)
        button.background = JBColor.PanelBackground
        button.toolTipText = tooltipText
        button.isBorderPainted = false
        button.isContentAreaFilled = false
        button.margin = JBUI.emptyInsets()
        button.preferredSize = Dimension(size, size)
        button.minimumSize = button.preferredSize
        button.maximumSize = button.preferredSize

        keyCode?.let { button.addActionListener { sendAdbKeyEvent(keyCode) } }
        intent?.let { button.addActionListener { startActivity(intent) } }

        return button
    }

    /** Sends an ADB key event to the connected device */
    private fun sendAdbKeyEvent(keyCode: Int) {
        ShellCommandsFactory.sendAdbKeyEvent(project, keyCode)
    }

    private fun startActivity(intent: Intent) {
        ShellCommandsFactory.startActivity(project, intent)
    }

}
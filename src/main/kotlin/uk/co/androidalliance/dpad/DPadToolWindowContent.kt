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
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel

/** Main panel containing the D-pad and additional control buttons */
class DPadToolWindowContent(private val project: Project) : JPanel() {

    private companion object {
        val LOG = Logger.getInstance(DPadToolWindowContent::class.java)

        // Layout Constants
        val GRID_LAYOUT_V_GAP: Int = JBUIScale.scale(5)
        val CONTROLS_PANEL_H_GAP: Int = JBUIScale.scale(10)
        val CONTROLS_PANEL_BORDER = JBUI.Borders.empty(5)
        val BUTTON_SIZE = Dimension(JBUIScale.scale(40), JBUIScale.scale(40))
        val DPAD_SIZE = JBUIScale.scale(120)

        // Icon Paths
        const val ICON_SETTINGS = "/icons/outline/settings_24dp.svg"
        const val ICON_HOME = "/icons/fill/home_24dp.svg"
        const val ICON_BACK = "/icons/fill/arrow_back_24dp.svg"
        const val ICON_BOOKMARK = "/icons/outline/bookmark_24dp.svg"
        const val ICON_PERSON = "/icons/fill/person_24dp.svg"
        const val ICON_LIVE_TV = "/icons/outline/live_tv_24dp.svg"

        // D-Pad Mapping
        val DPAD_KEYCODE_MAP = mapOf(
            DPadPanel.UP to KEYCODE_DPAD_UP,
            DPadPanel.RIGHT to KEYCODE_DPAD_RIGHT,
            DPadPanel.DOWN to KEYCODE_DPAD_DOWN,
            DPadPanel.LEFT to KEYCODE_DPAD_LEFT,
            DPadPanel.CENTER to KEYCODE_DPAD_CENTER
        )

        val DPAD_DIRECTION_NAME_MAP = mapOf(
            DPadPanel.UP to "UP",
            DPadPanel.RIGHT to "RIGHT",
            DPadPanel.DOWN to "DOWN",
            DPadPanel.LEFT to "LEFT",
            DPadPanel.CENTER to "CENTER"
        )
    }

    // --- Action Definition ---
    /** Represents the action triggered by a button press. */
    private sealed class ButtonAction {
        data class SendKeyCode(val keyCode: Int) : ButtonAction()
        data class StartActivity(val intent: Intent) : ButtonAction()
    }

    /** Data class to hold the configuration for creating an icon button. */
    private data class ButtonConfig(
        val iconPath: String,
        val tooltipText: String,
        val action: ButtonAction
    )

    // --- UI Components (initialized via helper functions) ---
    private val lhButtonColumn: JPanel = createButtonColumnPanel(createLeftButtonConfigs())
    private val rhButtonColumn: JPanel = createButtonColumnPanel(createRightButtonConfigs())
    private val dPadPanel: DPadPanel = createDPadPanel()
    private val controlsPanel: JPanel = createControlsPanel(lhButtonColumn, dPadPanel, rhButtonColumn)

    init {
        layout = FlowLayout(FlowLayout.CENTER, 0, 0)
        add(controlsPanel)
    }

    // --- UI Creation Helpers ---

    /** Creates the main panel holding the button columns and D-pad. */
    private fun createControlsPanel(leftCol: Component, dPad: Component, rightCol: Component): JPanel {
        return JBPanel<Nothing>(FlowLayout(FlowLayout.CENTER, CONTROLS_PANEL_H_GAP, 0)).apply {
            border = CONTROLS_PANEL_BORDER
            add(leftCol)
            add(dPad)
            add(rightCol)
        }
    }

    /** Creates a panel containing a column of icon buttons based on the provided configurations. */
    private fun createButtonColumnPanel(configs: List<ButtonConfig>): JPanel {
        // Using JBPanel<JBPanel<*>> to match original type hint, though JBPanel<*> might suffice
        return JBPanel<JBPanel<*>>(GridLayout(configs.size, 1, 0, GRID_LAYOUT_V_GAP)).apply {
            configs.forEach { config ->
                add(createIconButton(config))
            }
        }
    }

    /** Creates the DPadPanel and sets up its click listener. */
    private fun createDPadPanel(): DPadPanel {
        return DPadPanel(dPadSize = DPAD_SIZE).apply {
            setOnDirectionClickListener { direction ->
                handleDPadClick(direction)
            }
        }
    }

    /** Creates a styled JButton with an icon and an associated action. */
    private fun createIconButton(config: ButtonConfig): JButton {
        val icon = IconLoader.getIcon(config.iconPath, DPadToolWindowContent::class.java)
        return JButton(icon).apply {
            background = JBColor.PanelBackground
            toolTipText = config.tooltipText
            isBorderPainted = false
            isContentAreaFilled = false // Recommended for icon-only buttons
            margin = JBUI.emptyInsets() // Remove default margin
            preferredSize = BUTTON_SIZE
            minimumSize = BUTTON_SIZE // Prevent shrinking
            maximumSize = BUTTON_SIZE // Prevent stretching

            addActionListener { handleButtonAction(config.action) }
        }
    }

    // --- Configuration Data ---

    /** Returns the list of button configurations for the left column. */
    private fun createLeftButtonConfigs(): List<ButtonConfig> {
        val settingsIntent = Intent(
            flags = FLAG_ACTIVITY_NEW_TASK,
            componentName = "com.android.tv.settings/.MainSettings",
        )
        return listOf(
            ButtonConfig(ICON_SETTINGS, "Settings", ButtonAction.StartActivity(settingsIntent)),
            ButtonConfig(ICON_HOME, "Home", ButtonAction.SendKeyCode(KEYCODE_HOME)),
            ButtonConfig(ICON_BACK, "Back", ButtonAction.SendKeyCode(KEYCODE_BACK))
        )
    }

    /** Returns the list of button configurations for the right column. */
    private fun createRightButtonConfigs(): List<ButtonConfig> {
        val accountsIntent = Intent(
            action = "android.app.action.TOGGLE_NOTIFICATION_HANDLER_PANEL",
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER or FLAG_ACTIVITY_REQUIRE_DEFAULT
        )
        val liveTvIntent = Intent(
            flags = FLAG_ACTIVITY_NEW_TASK,
            componentName = "com.android.tv/.MainActivity",
        )
        return listOf(
            ButtonConfig(ICON_BOOKMARK, "Bookmark", ButtonAction.SendKeyCode(KEYCODE_BOOKMARK)),
            ButtonConfig(ICON_PERSON, "Accounts", ButtonAction.StartActivity(accountsIntent)),
            ButtonConfig(ICON_LIVE_TV, "Live TV", ButtonAction.StartActivity(liveTvIntent))
        )
    }

    // --- Action Handlers ---

    /** Logs the D-pad click and sends the corresponding ADB key event. */
    private fun handleDPadClick(direction: Int) {
        val directionName = DPAD_DIRECTION_NAME_MAP.getOrDefault(direction, "UNKNOWN")
        LOG.info("D-pad direction clicked: $directionName")

        DPAD_KEYCODE_MAP[direction]?.let { keyCode ->
            sendAdbKeyEvent(keyCode)
        } ?: LOG.warn("No keycode mapping found for D-pad direction: $direction")
    }

    /** Executes the appropriate action based on the ButtonAction type. */
    private fun handleButtonAction(action: ButtonAction) {
        when (action) {
            is ButtonAction.SendKeyCode -> sendAdbKeyEvent(action.keyCode)
            is ButtonAction.StartActivity -> startActivity(action.intent)
        }
    }

    // --- ADB Command Wrappers ---

    /** Sends an ADB key event to the connected device. */
    private fun sendAdbKeyEvent(keyCode: Int) {
        LOG.debug("Sending ADB key event: $keyCode")
        ShellCommandsFactory.sendAdbKeyEvent(project, keyCode)
    }

    /** Starts an Activity on the connected device using an Intent. */
    private fun startActivity(intent: Intent) {
        LOG.debug("Starting Activity with Intent: $intent")
        ShellCommandsFactory.startActivity(project, intent)
    }

    // Optional: Keep if needed for future use, otherwise remove.
    /** Creates a text-based button with the specified label and key code */
    private fun createLabelButton(text: String, keyCode: Int): JButton {
        return JButton(text).apply {
            font = controlButtons // Assuming 'controlButtons' is defined elsewhere
            addActionListener { sendAdbKeyEvent(keyCode) }
        }
    }


}
package uk.co.androidalliance.dpad

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.ui.UIUtil
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentDown
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentLabel
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentOutline
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentUp
import uk.co.androidalliance.dpad.theme.Typography.dPadNavigation
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel


class DPadPanel(val dPadSize: Int = 120) : JPanel() {
    private val LOG = Logger.getInstance(DPadPanel::class.java)

    private val segments = arrayOf(
        Polygon(), // Up
        Polygon(), // Right
        Polygon(), // Down
        Polygon()  // Left
    )

    // Store center square separately
    private var centerSquare = Rectangle()

    private var activeSegment: Int? = null
    private var clickHandler: ((Int) -> Unit)? = null

    init {
        preferredSize = Dimension(dPadSize, dPadSize)

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val segment = getSegmentAt(e.x, e.y)
                activeSegment = segment
                if (segment != null) {
                    clickHandler?.invoke(segment)
                    repaint()
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                activeSegment = null
                repaint()
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val segment = getSegmentAt(e.x, e.y)
                cursor = if (segment != null) {
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    Cursor.getDefaultCursor()
                }
            }
        })
    }

    /**
     * Set a callback for when a direction is clicked
     * @param handler Lambda function that receives the direction constant (UP, RIGHT, DOWN, LEFT, CENTER)
     */
    fun setOnDirectionClickListener(handler: (Int) -> Unit) {
        clickHandler = handler
    }

    private fun getSegmentAt(x: Int, y: Int): Int? {
        // Check the center square first
        if (centerSquare.contains(x, y)) {
            return CENTER
        }

        // Then check the directional segments
        for (i in segments.indices) {
            if (segments[i].contains(x, y)) {
                return i
            }
        }
        return null
    }

    //private fun getSegmentUpColor(): Color {
    //    return if (isDarkTheme) SegmentUpDark else SegmentUpLight
    //}
//
    //private fun getSegmentDownColor(): Color {
    //    return if (isDarkTheme) SegmentDownDark else SegmentDownLight
    //}
//
    //private fun getSegmentOutlineColor(): Color {
    //    return if (isDarkTheme) SegmentOutlineDark else SegmentOutlineLight
    //}

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw background to ensure the panel is visible
        g2d.color = UIUtil.getPanelBackground()
        g2d.fillRect(0, 0, width, height)

        val width = width
        val height = height
        val centerX = width / 2
        val centerY = height / 2
        val squareSize = minOf(width, height) / 3

        // Create center square
        centerSquare = Rectangle(
            centerX - squareSize / 2,
            centerY - squareSize / 2,
            squareSize,
            squareSize
        )

        // Define segments (Up, Right, Down, Left)

        // Up segment
        segments[UP].reset()
        segments[UP].addPoint(centerX - squareSize / 2, centerY - squareSize / 2)
        segments[UP].addPoint(centerX + squareSize / 2, centerY - squareSize / 2)
        segments[UP].addPoint(width, 0)
        segments[UP].addPoint(0, 0)

        // Right segment
        segments[RIGHT].reset()
        segments[RIGHT].addPoint(centerX + squareSize / 2, centerY - squareSize / 2)
        segments[RIGHT].addPoint(centerX + squareSize / 2, centerY + squareSize / 2)
        segments[RIGHT].addPoint(width, height)
        segments[RIGHT].addPoint(width, 0)

        // Down segment
        segments[DOWN].reset()
        segments[DOWN].addPoint(centerX - squareSize / 2, centerY + squareSize / 2)
        segments[DOWN].addPoint(centerX + squareSize / 2, centerY + squareSize / 2)
        segments[DOWN].addPoint(width, height)
        segments[DOWN].addPoint(0, height)

        // Left segment
        segments[LEFT].reset()
        segments[LEFT].addPoint(centerX - squareSize / 2, centerY - squareSize / 2)
        segments[LEFT].addPoint(centerX - squareSize / 2, centerY + squareSize / 2)
        segments[LEFT].addPoint(0, height)
        segments[LEFT].addPoint(0, 0)

        // Draw segments with different colors based on active state and theme
        for (i in segments.indices) {
            g2d.color = if (i == activeSegment) SegmentDown else SegmentUp
            g2d.fill(segments[i]) // Draws the fill
            g2d.color = SegmentOutline
            g2d.draw(segments[i]) // Draws the outline
        }

        // Draw center square
        g2d.color = if (activeSegment == CENTER) SegmentDown else SegmentUp
        g2d.fill(centerSquare) // Draws the fill
        g2d.color = SegmentOutline
        g2d.draw(centerSquare) // Draws the outline

        // Optional: Add direction labels with appropriate color for theme
        g2d.color = SegmentLabel
        g2d.font = dPadNavigation
        g2d.drawString("↑", centerX - 5, centerY - squareSize)
        g2d.drawString("→", centerX + squareSize, centerY + 5)
        g2d.drawString("↓", centerX - 5, centerY + squareSize + 15)
        g2d.drawString("←", centerX - squareSize - 10, centerY + 5)
        g2d.drawString("OK", centerX - 8, centerY + 4) // Add "OK" text to center
    }

    companion object {
        const val UP = 0
        const val RIGHT = 1
        const val DOWN = 2
        const val LEFT = 3
        const val CENTER = 4  // Add CENTER constant
    }
}
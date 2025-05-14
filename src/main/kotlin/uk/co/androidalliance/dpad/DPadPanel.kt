package uk.co.androidalliance.dpad

import uk.co.androidalliance.dpad.DPadPanel.DpadAction.*
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentDown
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentLabel
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentOutline
import uk.co.androidalliance.dpad.theme.DpadColors.SegmentUp
import uk.co.androidalliance.dpad.theme.Typography.dPadNavigation
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Rectangle2D
import javax.swing.JPanel
import javax.swing.Timer


/**
 * A custom JPanel that renders and handles interactions for a D-pad control.
 * Calculates shapes based on component size and redraws efficiently.
 *
 * @param dPadSize The preferred initial size (width and height) for the D-pad.
 */
class DPadPanel(
    dPadSize: Int = 120,
    private val longPressDelayMs: Int = 500
) : JPanel() {

    // --- Constants ---
    companion object {
        const val UP = 0
        const val RIGHT = 1
        const val DOWN = 2
        const val LEFT = 3
        const val CENTER = 4

        // Labels (can be localized if needed)
        private const val LABEL_UP = "↑" // Using arrows for clarity
        private const val LABEL_RIGHT = "→"
        private const val LABEL_DOWN = "↓"
        private const val LABEL_LEFT = "←"
        private const val LABEL_CENTER = "OK"
    }

    // --- Action Definition ---
    sealed interface DpadAction {
        data object ActionUp : DpadAction
        data object ActionDown : DpadAction
        data object ActionLongPress : DpadAction
    }

    // private val LOG = Logger.getInstance(DPadPanel::class.java) // Uncomment if needed

    // --- State ---
    private var activeSegment: Int? = null // Which segment is currently pressed (UP, DOWN, etc.)
    private var hoverSegment: Int? = null // Which segment is the mouse currently over
    private var clickHandler: ((Int, DpadAction) -> Unit)? = null // Callback for clicks

    // --- Shape Cache ---
    // Store calculated shapes to avoid recalculation on every paint
    private val directionalShapes = mutableMapOf<Int, Shape>()
    private var centerShape: Shape = Rectangle2D.Double() // Default empty shape
    private var lastBounds: Rectangle? = null // To detect size changes

    // --- Long Press State ---
    private var longPressTimer: Timer? = null
    private var longPressSegment: Int? = null // Track segment for timer
    private var longPressFired: Boolean = false // Prevent multiple long presses per hold

    init {
        preferredSize = Dimension(dPadSize, dPadSize)
        isOpaque = false // Ensure background is painted correctly if needed elsewhere

        setupMouseListeners()
    }

    /**
     * Sets the callback function to be invoked when a D-pad direction or center is clicked.
     * @param handler Lambda function receiving the direction constant (UP, RIGHT, DOWN, LEFT, CENTER).
     */
    fun setOnDirectionClickListener(handler: (Int, DpadAction) -> Unit) {
        clickHandler = handler
    }

    // --- Core Painting Logic ---
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g) // Let the superclass paint (e.g., background)

        // Check if bounds changed, recalculate shapes if necessary
        if (bounds != lastBounds) {
            updateShapes()
            lastBounds = bounds
        }

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw background explicitly if needed (useful if isOpaque=false sometimes)
        // g2d.color = background ?: UIUtil.getPanelBackground()
        // g2d.fillRect(0, 0, width, height)

        // Draw directional segments
        for ((direction, shape) in directionalShapes) {
            drawSegment(g2d, shape, direction == activeSegment, direction == hoverSegment)
        }

        // Draw center segment
        drawSegment(g2d, centerShape, CENTER == activeSegment, CENTER == hoverSegment)

        // Draw labels (optional enhancement: use FontMetrics for precise centering)
        drawLabels(g2d)
    }

    /** Draws a single segment (directional or center) with appropriate styling. */
    private fun drawSegment(g2d: Graphics2D, shape: Shape, isActive: Boolean, isHover: Boolean) {
        // Determine fill color based on state
        g2d.color = when {
            isActive -> SegmentDown
            // isHover -> SegmentHover // Optional: Add a hover color
            else -> SegmentUp
        }
        g2d.fill(shape)

        // Draw outline
        g2d.color = SegmentOutline
        g2d.draw(shape)
    }

    /** Draws the directional labels and center text. */
    private fun drawLabels(g2d: Graphics2D) {
        g2d.color = SegmentLabel
        g2d.font = dPadNavigation

        val metrics = g2d.fontMetrics
        val centerX = width / 2.0
        val centerY = height / 2.0
        // Use shape bounds for more robust positioning if needed, or keep relative for simplicity
        val shapeBounds = centerShape.bounds2D
        val labelPadding = metrics.height * 0.3 // Small padding

        // Helper to draw centered string
        fun drawCenteredString(text: String, x: Double, y: Double) {
            val textWidth = metrics.stringWidth(text)
            val textAscent = metrics.ascent
            g2d.drawString(
                text,
                (x - textWidth / 2.0).toFloat(),
                (y + textAscent / 2.0 - metrics.descent / 2.0).toFloat()
            ) // Center vertically too
        }

        // Position labels relative to the center or shape bounds
        drawCenteredString(
            text = LABEL_UP,
            x = centerX,
            y = shapeBounds.minY - labelPadding - metrics.ascent
        )

        drawCenteredString(
            text = LABEL_RIGHT,
            x = shapeBounds.maxX + labelPadding + metrics.stringWidth(LABEL_RIGHT),
            y = centerY
        ) // Adjust x slightly outward

        drawCenteredString(
            text = LABEL_DOWN,
            x = centerX,
            y = shapeBounds.maxY + labelPadding + metrics.ascent
        ) // Adjust y slightly downward

        drawCenteredString(
            text = LABEL_LEFT,
            x = shapeBounds.minX - labelPadding - metrics.stringWidth(LABEL_LEFT),
            y = centerY
        ) // Adjust x slightly outward

        drawCenteredString(
            text = LABEL_CENTER,
            x = centerX,
            y = centerY
        )
    }


    // --- Shape Calculation ---

    /** Recalculates the shapes based on the current component bounds. */
    private fun updateShapes() {
        val currentWidth = width
        val currentHeight = height
        if (currentWidth <= 0 || currentHeight <= 0) return // Avoid division by zero or weird shapes

        val centerX = currentWidth / 2.0
        val centerY = currentHeight / 2.0
        // Make square size relative to the smaller dimension
        val squareSize = minOf(currentWidth, currentHeight) / 3.0

        // Define Center Rect (using Rectangle2D for potential double precision)
        centerShape = Rectangle2D.Double(
            centerX - squareSize / 2.0,
            centerY - squareSize / 2.0,
            squareSize,
            squareSize
        )
        val cs = centerShape.bounds2D // Use bounds for easier access to min/max x/y

        // Define Directional Polygons
        directionalShapes[UP] = Polygon().apply {
            addPoint(cs.minX.toInt(), cs.minY.toInt())
            addPoint(cs.maxX.toInt(), cs.minY.toInt())
            addPoint(currentWidth, 0)
            addPoint(0, 0)
        }
        directionalShapes[RIGHT] = Polygon().apply {
            addPoint(cs.maxX.toInt(), cs.minY.toInt())
            addPoint(cs.maxX.toInt(), cs.maxY.toInt())
            addPoint(currentWidth, currentHeight)
            addPoint(currentWidth, 0)
        }
        directionalShapes[DOWN] = Polygon().apply {
            addPoint(cs.minX.toInt(), cs.maxY.toInt())
            addPoint(cs.maxX.toInt(), cs.maxY.toInt())
            addPoint(currentWidth, currentHeight)
            addPoint(0, currentHeight)
        }
        directionalShapes[LEFT] = Polygon().apply {
            addPoint(cs.minX.toInt(), cs.minY.toInt())
            addPoint(cs.minX.toInt(), cs.maxY.toInt())
            addPoint(0, currentHeight)
            addPoint(0, 0)
        }
    }

    // --- Event Handling ---

    /** Sets up the mouse listeners for interaction. */
    private fun setupMouseListeners() {
        val mouseAdapter = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val segment = getSegmentAt(e.point)
                if (segment != null) {
                    activeSegment = segment
                    // Enabling this is causing skipping across buttons
                    // clickHandler?.invoke(segment, ActionDown)
                    startLongPressTimer(segment) // Start timer for potential long press
                    repaint()
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                stopLongPressTimer() // Always stop timer on release

                activeSegment?.let { segment ->
                    // Only trigger ActionUp if a long press hasn't already been fired for this press
                    if (!longPressFired) {
                        clickHandler?.invoke(segment, ActionUp)
                    }
                    activeSegment = null
                    repaint()
                }
                // Update hover state regardless
                updateHover(e.point)
            }

            override fun mouseMoved(e: MouseEvent) {
                updateHover(e.point)
            }

            override fun mouseDragged(e: MouseEvent) {
                updateHover(e.point)
                // If dragging outside the active segment, cancel the press and timer
                val currentSegment = getSegmentAt(e.point)
                if (activeSegment != null && currentSegment != activeSegment) {
                    stopLongPressTimer() // Stop timer if dragged off
                    activeSegment = null
                    repaint()
                }
            }

            override fun mouseExited(e: MouseEvent) {
                stopLongPressTimer() // Stop timer if mouse leaves the panel
                if (hoverSegment != null || activeSegment != null) {
                    cursor = Cursor.getDefaultCursor()
                    hoverSegment = null
                    activeSegment = null
                    repaint()
                }
            }
        }
        addMouseListener(mouseAdapter)
        addMouseMotionListener(mouseAdapter)
    }

    /** Starts the timer to detect a long press on the given segment. */
    private fun startLongPressTimer(segment: Int) {
        stopLongPressTimer() // Stop any existing timer first
        longPressSegment = segment
        longPressFired = false // Reset fired flag for this new press

        // Create the ActionListener for the timer
        val timerAction = ActionListener {
            // Timer fired - check if the press is still active on the same segment
            if (activeSegment == longPressSegment && !longPressFired) {
                longPressFired = true // Mark long press as fired for this hold
                clickHandler?.invoke(segment, ActionLongPress) // Trigger long press action
                // Optionally: Add visual feedback for long press if needed (e.g., slight color change)
                // repaint()
            }
            // Timer automatically stops because isRepeats is false
        }

        // Create and start a non-repeating timer
        longPressTimer = Timer(longPressDelayMs, timerAction).apply {
            isRepeats = false // Only fire once
            start()
        }
    }

    /** Stops and nullifies the long press timer if it's running. */
    private fun stopLongPressTimer() {
        longPressTimer?.stop()
        longPressTimer = null
        longPressSegment = null
        // Do not reset longPressFired here, it's reset on the next mousePressed
    }

    /** Updates the hover state and cursor based on the mouse position. */
    private fun updateHover(point: Point) {
        val segment = getSegmentAt(point)
        if (segment != hoverSegment) {
            hoverSegment = segment
            cursor = if (segment != null) {
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            } else {
                Cursor.getDefaultCursor()
            }
            // Optional: Repaint for visual hover effects
            // repaint()
        }
    }

    /** Determines which segment (directional or center) contains the given point. */
    private fun getSegmentAt(point: Point): Int? {
        // Check center first (usually smaller and distinct)
        if (centerShape.contains(point)) {
            return CENTER
        }
        // Check directional segments
        for ((direction, shape) in directionalShapes) {
            if (shape.contains(point)) {
                return direction
            }
        }
        return null // No segment hit
    }
}
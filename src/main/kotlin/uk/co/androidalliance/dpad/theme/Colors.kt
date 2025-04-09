package uk.co.androidalliance.dpad.theme

import com.intellij.ui.JBColor
import java.awt.Color

object DpadColors {
    // Define colors for light and dark themes
    val SegmentDown = JBColor(
        Color(192, 192, 192), // Light
        Color(61, 80, 89) // Dark
    )
    val SegmentUp = JBColor(
        Color(224, 224, 224), // Light
        Color(84, 110, 122) // Dark
    )    // Light, Dark
    val SegmentOutline = JBColor(
        Color(150, 150, 150), // Light
        Color(50, 50, 50) // Dark
    )     // Light, Dark (Adjust as needed)
    val SegmentLabel = JBColor(
        Color.BLACK, // Light
        Color(187, 187, 187)// Dark
    )
}
package uk.co.androidalliance.dpad.theme

import java.util.*
import javax.swing.UIManager

fun isDarkTheme(): Boolean {
    val lookAndFeel = UIManager.getLookAndFeel().name.lowercase(Locale.getDefault())
    return lookAndFeel.contains("darcula") || lookAndFeel.contains("dark")
}
package uk.co.androidalliance.dpad.input

import uk.co.androidalliance.dpad.DPadPanel.DpadAction

interface KeyEventSender {
    suspend fun sendKeyEvent(keyCode: Int, action: DpadAction)
    fun shutdown()
}

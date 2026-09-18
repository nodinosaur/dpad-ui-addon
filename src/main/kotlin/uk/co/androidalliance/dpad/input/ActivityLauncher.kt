package uk.co.androidalliance.dpad.input

import uk.co.androidalliance.dpad.adb.Intent

interface ActivityLauncher {
    suspend fun startActivity(intent: Intent)
}

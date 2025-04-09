package uk.co.androidalliance.dpad.adb

data class Intent(
    val action: String? = null,
    val flags: Int? = null,
    val packageName: String? = null,
    val componentName: String? = null,
    val attachDebugger: Boolean = false
) {

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("am start ")
        action?.let { stringBuilder.append("-a $action ") }
        flags?.let { stringBuilder.append("-f $flags ") }
        packageName?.let { stringBuilder.append("-p $packageName ") }
        componentName?.let { stringBuilder.append("-n $componentName ") }
        if (attachDebugger) stringBuilder.append("-D ")
        return stringBuilder.toString()
    }
    companion object{
        const val FLAG_ACTIVITY_NEW_TASK = 0x10000000
        const val FLAG_ACTIVITY_REQUIRE_NON_BROWSER = 0x00000400
        const val FLAG_ACTIVITY_REQUIRE_DEFAULT = 0x00000200
    }
}

package uk.co.androidalliance.dpad.input

/**
 * Android keycode -> Linux evdev scancode, for the **emulator gRPC transport only**.
 *
 * ## Why these specific values
 *
 * These are the codes the emulator's own "Extended Controls" D-Pad sends
 * (`kKeyCodeDpad*` in `android/skin/keycode.h`, which alias `LINUX_KEY_*`). The emulator
 * names its virtual keyboard `qwerty2`, so the guest resolves them through
 * `device/generic/goldfish/input/qwerty2.kl` rather than the `Generic.kl` fallback used by
 * physical devices.
 *
 * ## Do not reuse these on a physical-device path
 *
 * The two layouts disagree on exactly the keys we care about:
 *
 * | evdev | `qwerty2.kl` (emulator) | `Generic.kl` (physical) |
 * |-------|-------------------------|-------------------------|
 * | 232   | `DPAD_CENTER`           | *unmapped*              |
 * | 102   | `HOME`                  | `MOVE_HOME`             |
 * | 353   | *unmapped*              | `DPAD_CENTER`           |
 * | 172   | *unmapped*              | `HOME`                  |
 *
 * Physical devices go through [AdbKeyEventSender], which uses Android keycodes directly and
 * is unaffected.
 *
 * Codes with no emulator mapping return `null`; [DeviceInputRouter] then falls back to ADB,
 * which handles them correctly.
 *
 * See `docs/implementation/emulator-extended-controls.md` §3.
 */
object EvdevKeyCodes {

    private val ANDROID_TO_EVDEV = mapOf(
        19 to 103,  // KEYCODE_DPAD_UP     -> KEY_UP
        20 to 108,  // KEYCODE_DPAD_DOWN   -> KEY_DOWN
        21 to 105,  // KEYCODE_DPAD_LEFT   -> KEY_LEFT
        22 to 106,  // KEYCODE_DPAD_RIGHT  -> KEY_RIGHT
        23 to 232,  // KEYCODE_DPAD_CENTER -> KEY_CENTER, NOT 28/KEY_ENTER (which is ENTER)
        3 to 102,   // KEYCODE_HOME        -> KEY_HOME
        4 to 158,   // KEYCODE_BACK        -> KEY_BACK

        // Deliberately absent:
        //   174 KEYCODE_BOOKMARK -> evdev 156 (KEY_BOOKMARKS) is unmapped in qwerty2.kl,
        //       so the emulator would silently swallow it. Returning null routes it to ADB.
        //   evdev 208 (KEY_FASTFORWARD) is likewise unmapped in qwerty2.kl; the emulator's
        //       own D-Pad page sends it and the guest ignores it.
    )

    /** The evdev code for an Android keycode, or `null` if the emulator cannot deliver it. */
    fun toEvdev(androidKeyCode: Int): Int? = ANDROID_TO_EVDEV[androidKeyCode]
}

package uk.co.androidalliance.dpad.adb

object KeyCodes {
    const val KEYCODE_UNKNOWN = 0

    /** Key code constant: Soft Left key.
     * Usually situated below the display on phones and used as a multi-function
     * feature key for selecting a software defined function shown on the bottom left
     * of the display. */
    const val KEYCODE_SOFT_LEFT = 1

    /** Key code constant: Soft Right key.
     * Usually situated below the display on phones and used as a multi-function
     * feature key for selecting a software defined function shown on the bottom right
     * of the display. */
    const val KEYCODE_SOFT_RIGHT = 2

    /** Key code constant: Home key.
     * This key is handled by the framework and is never delivered to applications. */
    const val KEYCODE_HOME = 3

    /** Key code constant: Back key. */
    const val KEYCODE_BACK = 4

    /** Key code constant: Call key. */
    const val KEYCODE_CALL = 5

    /** Key code constant: End Call key. */
    const val KEYCODE_ENDCALL = 6

    /** Key code constant: '0' key. */
    const val KEYCODE_0 = 7

    /** Key code constant: '1' key. */
    const val KEYCODE_1 = 8

    /** Key code constant: '2' key. */
    const val KEYCODE_2 = 9

    /** Key code constant: '3' key. */
    const val KEYCODE_3 = 10

    /** Key code constant: '4' key. */
    const val KEYCODE_4 = 11

    /** Key code constant: '5' key. */
    const val KEYCODE_5 = 12

    /** Key code constant: '6' key. */
    const val KEYCODE_6 = 13

    /** Key code constant: '7' key. */
    const val KEYCODE_7 = 14

    /** Key code constant: '8' key. */
    const val KEYCODE_8 = 15

    /** Key code constant: '9' key. */
    const val KEYCODE_9 = 16

    /** Key code constant: '*' key. */
    const val KEYCODE_STAR = 17

    /** Key code constant: '#' key. */
    const val KEYCODE_POUND = 18

    /** Key code constant: Directional Pad Up key.
     * May also be synthesized from trackball motions. */
    const val KEYCODE_DPAD_UP = 19

    /** Key code constant: Directional Pad Down key.
     * May also be synthesized from trackball motions. */
    const val KEYCODE_DPAD_DOWN = 20

    /** Key code constant: Directional Pad Left key.
     * May also be synthesized from trackball motions. */
    const val KEYCODE_DPAD_LEFT = 21

    /** Key code constant: Directional Pad Right key.
     * May also be synthesized from trackball motions. */
    const val KEYCODE_DPAD_RIGHT = 22

    /** Key code constant: Directional Pad Center key.
     * May also be synthesized from trackball motions. */
    const val KEYCODE_DPAD_CENTER = 23

    /** Key code constant: Volume Up key.
     * Adjusts the speaker volume up. */
    const val KEYCODE_VOLUME_UP = 24

    /** Key code constant: Volume Down key.
     * Adjusts the speaker volume down. */
    const val KEYCODE_VOLUME_DOWN = 25

    /** Key code constant: Power key. */
    const val KEYCODE_POWER = 26

    /** Key code constant: Camera key.
     * Used to launch a camera application or take pictures. */
    const val KEYCODE_CAMERA = 27

    /** Key code constant: Clear key. */
    const val KEYCODE_CLEAR = 28

    /** Key code constant: 'A' key. */
    const val KEYCODE_A = 29

    /** Key code constant: 'B' key. */
    const val KEYCODE_B = 30

    /** Key code constant: 'C' key. */
    const val KEYCODE_C = 31

    /** Key code constant: 'D' key. */
    const val KEYCODE_D = 32

    /** Key code constant: 'E' key. */
    const val KEYCODE_E = 33

    /** Key code constant: 'F' key. */
    const val KEYCODE_F = 34

    /** Key code constant: 'G' key. */
    const val KEYCODE_G = 35

    /** Key code constant: 'H' key. */
    const val KEYCODE_H = 36

    /** Key code constant: 'I' key. */
    const val KEYCODE_I = 37

    /** Key code constant: 'J' key. */
    const val KEYCODE_J = 38

    /** Key code constant: 'K' key. */
    const val KEYCODE_K = 39

    /** Key code constant: 'L' key. */
    const val KEYCODE_L = 40

    /** Key code constant: 'M' key. */
    const val KEYCODE_M = 41

    /** Key code constant: 'N' key. */
    const val KEYCODE_N = 42

    /** Key code constant: 'O' key. */
    const val KEYCODE_O = 43

    /** Key code constant: 'P' key. */
    const val KEYCODE_P = 44

    /** Key code constant: 'Q' key. */
    const val KEYCODE_Q = 45

    /** Key code constant: 'R' key. */
    const val KEYCODE_R = 46

    /** Key code constant: 'S' key. */
    const val KEYCODE_S = 47

    /** Key code constant: 'T' key. */
    const val KEYCODE_T = 48

    /** Key code constant: 'U' key. */
    const val KEYCODE_U = 49

    /** Key code constant: 'V' key. */
    const val KEYCODE_V = 50

    /** Key code constant: 'W' key. */
    const val KEYCODE_W = 51

    /** Key code constant: 'X' key. */
    const val KEYCODE_X = 52

    /** Key code constant: 'Y' key. */
    const val KEYCODE_Y = 53

    /** Key code constant: 'Z' key. */
    const val KEYCODE_Z = 54

    /** Key code constant: ',' key. */
    const val KEYCODE_COMMA = 55

    /** Key code constant: '.' key. */
    const val KEYCODE_PERIOD = 56

    /** Key code constant: Left Alt modifier key. */
    const val KEYCODE_ALT_LEFT = 57

    /** Key code constant: Right Alt modifier key. */
    const val KEYCODE_ALT_RIGHT = 58

    /** Key code constant: Left Shift modifier key. */
    const val KEYCODE_SHIFT_LEFT = 59

    /** Key code constant: Right Shift modifier key. */
    const val KEYCODE_SHIFT_RIGHT = 60

    /** Key code constant: Tab key. */
    const val KEYCODE_TAB = 61

    /** Key code constant: Space key. */
    const val KEYCODE_SPACE = 62

    /** Key code constant: Symbol modifier key.
     * Used to enter alternate symbols. */
    const val KEYCODE_SYM = 63

    /** Key code constant: Explorer special function key.
     * Used to launch a browser application. */
    const val KEYCODE_EXPLORER = 64

    /** Key code constant: Envelope special function key.
     * Used to launch a mail application. */
    const val KEYCODE_ENVELOPE = 65

    /** Key code constant: Enter key. */
    const val KEYCODE_ENTER = 66

    /** Key code constant: Backspace key.
     * Deletes characters before the insertion point, unlike {@link #KEYCODE_FORWARD_DEL}. */
    const val KEYCODE_DEL = 67

    /** Key code constant: '`' (backtick) key. */
    const val KEYCODE_GRAVE = 68

    /** Key code constant: '-'. */
    const val KEYCODE_MINUS = 69

    /** Key code constant: '=' key. */
    const val KEYCODE_EQUALS = 70

    /** Key code constant: '[' key. */
    const val KEYCODE_LEFT_BRACKET = 71

    /** Key code constant: ']' key. */
    const val KEYCODE_RIGHT_BRACKET = 72

    /** Key code constant: '\' key. */
    const val KEYCODE_BACKSLASH = 73

    /** Key code constant: '' key. */
    const val KEYCODE_SEMICOLON = 74

    /** Key code constant: ''' (apostrophe) key. */
    const val KEYCODE_APOSTROPHE = 75

    /** Key code constant: '/' key. */
    const val KEYCODE_SLASH = 76

    /** Key code constant: '@' key. */
    const val KEYCODE_AT = 77

    /** Key code constant: Number modifier key.
     * Used to enter numeric symbols.
     * This key is not Num Lock it is more like {@link #KEYCODE_ALT_LEFT} and is
     * interpreted as an ALT key by {@link android.text.method.MetaKeyKeyListener}. */
    const val KEYCODE_NUM = 78

    /** Key code constant: Headset Hook key.
     * Used to hang up calls and stop media. */
    const val KEYCODE_HEADSETHOOK = 79

    /** Key code constant: Camera Focus key.
     * Used to focus the camera. */
    const val KEYCODE_FOCUS = 80   // *Camera* focus

    /** Key code constant: '+' key. */
    const val KEYCODE_PLUS = 81

    /** Key code constant: Menu key. */
    const val KEYCODE_MENU = 82

    /** Key code constant: Notification key. */
    const val KEYCODE_NOTIFICATION = 83

    /** Key code constant: Search key. */
    const val KEYCODE_SEARCH = 84

    /** Key code constant: Play/Pause media key. */
    const val KEYCODE_MEDIA_PLAY_PAUSE = 85

    /** Key code constant: Stop media key. */
    const val KEYCODE_MEDIA_STOP = 86

    /** Key code constant: Play Next media key. */
    const val KEYCODE_MEDIA_NEXT = 87

    /** Key code constant: Play Previous media key. */
    const val KEYCODE_MEDIA_PREVIOUS = 88

    /** Key code constant: Rewind media key. */
    const val KEYCODE_MEDIA_REWIND = 89

    /** Key code constant: Fast Forward media key. */
    const val KEYCODE_MEDIA_FAST_FORWARD = 90

    /** Key code constant: Mute key.
     * Mute key for the microphone (unlike {@link #KEYCODE_VOLUME_MUTE}, which is the speaker mute
     * key). */
    const val KEYCODE_MUTE = 91

    /** Key code constant: Page Up key. */
    const val KEYCODE_PAGE_UP = 92

    /** Key code constant: Page Down key. */
    const val KEYCODE_PAGE_DOWN = 93

    /** Key code constant: Picture Symbols modifier key.
     * Used to switch symbol sets (Emoji, Kao-moji). */
    const val KEYCODE_PICTSYMBOLS = 94   // switch symbol-sets (Emoji,Kao-moji)

    /** Key code constant: Switch Charset modifier key.
     * Used to switch character sets (Kanji, Katakana). */
    const val KEYCODE_SWITCH_CHARSET = 95   // switch char-sets (Kanji,Katakana)

    /** Key code constant: A Button key.
     * On a game controller, the A button should be either the button labeled A
     * or the first button on the bottom row of controller buttons. */
    const val KEYCODE_BUTTON_A = 96

    /** Key code constant: B Button key.
     * On a game controller, the B button should be either the button labeled B
     * or the second button on the bottom row of controller buttons. */
    const val KEYCODE_BUTTON_B = 97

    /** Key code constant: C Button key.
     * On a game controller, the C button should be either the button labeled C
     * or the third button on the bottom row of controller buttons. */
    const val KEYCODE_BUTTON_C = 98

    /** Key code constant: X Button key.
     * On a game controller, the X button should be either the button labeled X
     * or the first button on the upper row of controller buttons. */
    const val KEYCODE_BUTTON_X = 99

    /** Key code constant: Y Button key.
     * On a game controller, the Y button should be either the button labeled Y
     * or the second button on the upper row of controller buttons. */
    const val KEYCODE_BUTTON_Y = 100

    /** Key code constant: Z Button key.
     * On a game controller, the Z button should be either the button labeled Z
     * or the third button on the upper row of controller buttons. */
    const val KEYCODE_BUTTON_Z = 101

    /** Key code constant: L1 Button key.
     * On a game controller, the L1 button should be either the button labeled L1 (or L)
     * or the top left trigger button. */
    const val KEYCODE_BUTTON_L1 = 102

    /** Key code constant: R1 Button key.
     * On a game controller, the R1 button should be either the button labeled R1 (or R)
     * or the top right trigger button. */
    const val KEYCODE_BUTTON_R1 = 103

    /** Key code constant: L2 Button key.
     * On a game controller, the L2 button should be either the button labeled L2
     * or the bottom left trigger button. */
    const val KEYCODE_BUTTON_L2 = 104

    /** Key code constant: R2 Button key.
     * On a game controller, the R2 button should be either the button labeled R2
     * or the bottom right trigger button. */
    const val KEYCODE_BUTTON_R2 = 105

    /** Key code constant: Left Thumb Button key.
     * On a game controller, the left thumb button indicates that the left (or only)
     * joystick is pressed. */
    const val KEYCODE_BUTTON_THUMBL = 106

    /** Key code constant: Right Thumb Button key.
     * On a game controller, the right thumb button indicates that the right
     * joystick is pressed. */
    const val KEYCODE_BUTTON_THUMBR = 107

    /** Key code constant: Start Button key.
     * On a game controller, the button labeled Start. */
    const val KEYCODE_BUTTON_START = 108

    /** Key code constant: Select Button key.
     * On a game controller, the button labeled Select. */
    const val KEYCODE_BUTTON_SELECT = 109

    /** Key code constant: Mode Button key.
     * On a game controller, the button labeled Mode. */
    const val KEYCODE_BUTTON_MODE = 110

    /** Key code constant: Escape key. */
    const val KEYCODE_ESCAPE = 111

    /** Key code constant: Forward Delete key.
     * Deletes characters ahead of the insertion point, unlike {@link #KEYCODE_DEL}. */
    const val KEYCODE_FORWARD_DEL = 112

    /** Key code constant: Left Control modifier key. */
    const val KEYCODE_CTRL_LEFT = 113

    /** Key code constant: Right Control modifier key. */
    const val KEYCODE_CTRL_RIGHT = 114

    /** Key code constant: Caps Lock key. */
    const val KEYCODE_CAPS_LOCK = 115

    /** Key code constant: Scroll Lock key. */
    const val KEYCODE_SCROLL_LOCK = 116

    /** Key code constant: Left Meta modifier key. */
    const val KEYCODE_META_LEFT = 117

    /** Key code constant: Right Meta modifier key. */
    const val KEYCODE_META_RIGHT = 118

    /** Key code constant: Function modifier key. */
    const val KEYCODE_FUNCTION = 119

    /**
     * Key code constant: System Request / Print Screen key.
     *
     * This key is sent to the app first and only if the app doesn't handle it, the framework
     * handles it (to take a screenshot), unlike {@code KEYCODE_TAKE_SCREENSHOT} which is
     * fully handled by the framework.
     */
    const val KEYCODE_SYSRQ = 120

    /** Key code constant: Break / Pause key. */
    const val KEYCODE_BREAK = 121

    /** Key code constant: Home Movement key.
     * Used for scrolling or moving the cursor around to the start of a line
     * or to the top of a list. */
    const val KEYCODE_MOVE_HOME = 122

    /** Key code constant: End Movement key.
     * Used for scrolling or moving the cursor around to the end of a line
     * or to the bottom of a list. */
    const val KEYCODE_MOVE_END = 123

    /** Key code constant: Insert key.
     * Toggles insert / overwrite edit mode. */
    const val KEYCODE_INSERT = 124

    /** Key code constant: Forward key.
     * Navigates forward in the history stack.  Complement of {@link #KEYCODE_BACK}. */
    const val KEYCODE_FORWARD = 125

    /** Key code constant: Play media key. */
    const val KEYCODE_MEDIA_PLAY = 126

    /** Key code constant: Pause media key. */
    const val KEYCODE_MEDIA_PAUSE = 127

    /** Key code constant: Close media key.
     * May be used to close a CD tray, for example. */
    const val KEYCODE_MEDIA_CLOSE = 128

    /** Key code constant: Eject media key.
     * May be used to eject a CD tray, for example. */
    const val KEYCODE_MEDIA_EJECT = 129

    /** Key code constant: Record media key. */
    const val KEYCODE_MEDIA_RECORD = 130

    /** Key code constant: F1 key. */
    const val KEYCODE_F1 = 131

    /** Key code constant: F2 key. */
    const val KEYCODE_F2 = 132

    /** Key code constant: F3 key. */
    const val KEYCODE_F3 = 133

    /** Key code constant: F4 key. */
    const val KEYCODE_F4 = 134

    /** Key code constant: F5 key. */
    const val KEYCODE_F5 = 135

    /** Key code constant: F6 key. */
    const val KEYCODE_F6 = 136

    /** Key code constant: F7 key. */
    const val KEYCODE_F7 = 137

    /** Key code constant: F8 key. */
    const val KEYCODE_F8 = 138

    /** Key code constant: F9 key. */
    const val KEYCODE_F9 = 139

    /** Key code constant: F10 key. */
    const val KEYCODE_F10 = 140

    /** Key code constant: F11 key. */
    const val KEYCODE_F11 = 141

    /** Key code constant: F12 key. */
    const val KEYCODE_F12 = 142

    /** Key code constant: Num Lock key.
     * This is the Num Lock key it is different from {@link #KEYCODE_NUM}.
     * This key alters the behavior of other keys on the numeric keypad. */
    const val KEYCODE_NUM_LOCK = 143

    /** Key code constant: Numeric keypad '0' key. */
    const val KEYCODE_NUMPAD_0 = 144

    /** Key code constant: Numeric keypad '1' key. */
    const val KEYCODE_NUMPAD_1 = 145

    /** Key code constant: Numeric keypad '2' key. */
    const val KEYCODE_NUMPAD_2 = 146

    /** Key code constant: Numeric keypad '3' key. */
    const val KEYCODE_NUMPAD_3 = 147

    /** Key code constant: Numeric keypad '4' key. */
    const val KEYCODE_NUMPAD_4 = 148

    /** Key code constant: Numeric keypad '5' key. */
    const val KEYCODE_NUMPAD_5 = 149

    /** Key code constant: Numeric keypad '6' key. */
    const val KEYCODE_NUMPAD_6 = 150

    /** Key code constant: Numeric keypad '7' key. */
    const val KEYCODE_NUMPAD_7 = 151

    /** Key code constant: Numeric keypad '8' key. */
    const val KEYCODE_NUMPAD_8 = 152

    /** Key code constant: Numeric keypad '9' key. */
    const val KEYCODE_NUMPAD_9 = 153

    /** Key code constant: Numeric keypad '/' key (for division). */
    const val KEYCODE_NUMPAD_DIVIDE = 154

    /** Key code constant: Numeric keypad '*' key (for multiplication). */
    const val KEYCODE_NUMPAD_MULTIPLY = 155

    /** Key code constant: Numeric keypad '-' key (for subtraction). */
    const val KEYCODE_NUMPAD_SUBTRACT = 156

    /** Key code constant: Numeric keypad '+' key (for addition). */
    const val KEYCODE_NUMPAD_ADD = 157

    /** Key code constant: Numeric keypad '.' key (for decimals or digit grouping). */
    const val KEYCODE_NUMPAD_DOT = 158

    /** Key code constant: Numeric keypad ',' key (for decimals or digit grouping). */
    const val KEYCODE_NUMPAD_COMMA = 159

    /** Key code constant: Numeric keypad Enter key. */
    const val KEYCODE_NUMPAD_ENTER = 160

    /** Key code constant: Numeric keypad '=' key. */
    const val KEYCODE_NUMPAD_EQUALS = 161

    /** Key code constant: Numeric keypad '(' key. */
    const val KEYCODE_NUMPAD_LEFT_PAREN = 162

    /** Key code constant: Numeric keypad ')' key. */
    const val KEYCODE_NUMPAD_RIGHT_PAREN = 163

    /** Key code constant: Volume Mute key.
     * Mute key for speaker (unlike {@link #KEYCODE_MUTE}, which is the mute key for the
     * microphone). This key should normally be implemented as a toggle such that the first press
     * mutes the speaker and the second press restores the original volume.
     */
    const val KEYCODE_VOLUME_MUTE = 164

    /** Key code constant: Info key.
     * Common on TV remotes to show additional information related to what is
     * currently being viewed. */
    const val KEYCODE_INFO = 165

    /** Key code constant: Channel up key.
     * On TV remotes, increments the television channel. */
    const val KEYCODE_CHANNEL_UP = 166

    /** Key code constant: Channel down key.
     * On TV remotes, decrements the television channel. */
    const val KEYCODE_CHANNEL_DOWN = 167

    /** Key code constant: Zoom in key. */
    const val KEYCODE_ZOOM_IN = 168

    /** Key code constant: Zoom out key. */
    const val KEYCODE_ZOOM_OUT = 169

    /** Key code constant: TV key.
     * On TV remotes, switches to viewing live TV. */
    const val KEYCODE_TV = 170

    /** Key code constant: Window key.
     * On TV remotes, toggles picture-in-picture mode or other windowing functions.
     * On Android Wear devices, triggers a display offset. */
    const val KEYCODE_WINDOW = 171

    /** Key code constant: Guide key.
     * On TV remotes, shows a programming guide. */
    const val KEYCODE_GUIDE = 172

    /** Key code constant: DVR key.
     * On some TV remotes, switches to a DVR mode for recorded shows. */
    const val KEYCODE_DVR = 173

    /** Key code constant: Bookmark key.
     * On some TV remotes, bookmarks content or web pages. */
    const val KEYCODE_BOOKMARK = 174

    /** Key code constant: Toggle captions key.
     * Switches the mode for closed-captioning text, for example during television shows. */
    const val KEYCODE_CAPTIONS = 175

    /** Key code constant: Settings key.
     * Starts the system settings activity. */
    const val KEYCODE_SETTINGS = 176

    /**
     * Key code constant: TV power key.
     * On HDMI TV panel devices and Android TV devices that don't support HDMI, toggles the power
     * state of the device.
     * On HDMI source devices, toggles the power state of the HDMI-connected TV via HDMI-CEC and
     * makes the source device follow this power state.
     */
    const val KEYCODE_TV_POWER = 177

    /** Key code constant: TV input key.
     * On TV remotes, switches the input on a television screen. */
    const val KEYCODE_TV_INPUT = 178

    /** Key code constant: Set-top-box power key.
     * On TV remotes, toggles the power on an external Set-top-box. */
    const val KEYCODE_STB_POWER = 179

    /** Key code constant: Set-top-box input key.
     * On TV remotes, switches the input mode on an external Set-top-box. */
    const val KEYCODE_STB_INPUT = 180

    /** Key code constant: A/V Receiver power key.
     * On TV remotes, toggles the power on an external A/V Receiver. */
    const val KEYCODE_AVR_POWER = 181

    /** Key code constant: A/V Receiver input key.
     * On TV remotes, switches the input mode on an external A/V Receiver. */
    const val KEYCODE_AVR_INPUT = 182

    /** Key code constant: Red "programmable" key.
     * On TV remotes, acts as a contextual/programmable key. */
    const val KEYCODE_PROG_RED = 183

    /** Key code constant: Green "programmable" key.
     * On TV remotes, actsas a contextual/programmable key. */
    const val KEYCODE_PROG_GREEN = 184

    /** Key code constant: Yellow "programmable" key.
     * On TV remotes, acts as a contextual/programmable key. */
    const val KEYCODE_PROG_YELLOW = 185

    /** Key code constant: Blue "programmable" key.
     * On TV remotes, acts as a contextual/programmable key. */
    const val KEYCODE_PROG_BLUE = 186

    /** Key code constant: App switch key.
     * Should bring up the application switcher dialog. */
    const val KEYCODE_APP_SWITCH = 187

    /** Key code constant: Generic Game Pad Button #1.*/
    const val KEYCODE_BUTTON_1 = 188

    /** Key code constant: Generic Game Pad Button #2.*/
    const val KEYCODE_BUTTON_2 = 189

    /** Key code constant: Generic Game Pad Button #3.*/
    const val KEYCODE_BUTTON_3 = 190

    /** Key code constant: Generic Game Pad Button #4.*/
    const val KEYCODE_BUTTON_4 = 191

    /** Key code constant: Generic Game Pad Button #5.*/
    const val KEYCODE_BUTTON_5 = 192

    /** Key code constant: Generic Game Pad Button #6.*/
    const val KEYCODE_BUTTON_6 = 193

    /** Key code constant: Generic Game Pad Button #7.*/
    const val KEYCODE_BUTTON_7 = 194

    /** Key code constant: Generic Game Pad Button #8.*/
    const val KEYCODE_BUTTON_8 = 195

    /** Key code constant: Generic Game Pad Button #9.*/
    const val KEYCODE_BUTTON_9 = 196

    /** Key code constant: Generic Game Pad Button #10.*/
    const val KEYCODE_BUTTON_10 = 197

    /** Key code constant: Generic Game Pad Button #11.*/
    const val KEYCODE_BUTTON_11 = 198

    /** Key code constant: Generic Game Pad Button #12.*/
    const val KEYCODE_BUTTON_12 = 199

    /** Key code constant: Generic Game Pad Button #13.*/
    const val KEYCODE_BUTTON_13 = 200

    /** Key code constant: Generic Game Pad Button #14.*/
    const val KEYCODE_BUTTON_14 = 201

    /** Key code constant: Generic Game Pad Button #15.*/
    const val KEYCODE_BUTTON_15 = 202

    /** Key code constant: Generic Game Pad Button #16.*/
    const val KEYCODE_BUTTON_16 = 203

    /** Key code constant: Language Switch key.
     * Toggles the current input language such as switching between English and Japanese on
     * a QWERTY keyboard.  On some devices, the same function may be performed by
     * pressing Shift+Spacebar. */
    const val KEYCODE_LANGUAGE_SWITCH = 204

    /** Key code constant: Manner Mode key.
     * Toggles silent or vibrate mode on and off to make the device behave more politely
     * in certain settings such as on a crowded train.  On some devices, the key may only
     * operate when long-pressed. */
    const val KEYCODE_MANNER_MODE = 205

    /** Key code constant: 3D Mode key.
     * Toggles the display between 2D and 3D mode. */
    const val KEYCODE_3D_MODE = 206

    /** Key code constant: Contacts special function key.
     * Used to launch an address book application. */
    const val KEYCODE_CONTACTS = 207

    /** Key code constant: Calendar special function key.
     * Used to launch a calendar application. */
    const val KEYCODE_CALENDAR = 208

    /** Key code constant: Music special function key.
     * Used to launch a music player application. */
    const val KEYCODE_MUSIC = 209

    /** Key code constant: Calculator special function key.
     * Used to launch a calculator application. */
    const val KEYCODE_CALCULATOR = 210

    /** Key code constant: Japanese full-width / half-width key. */
    const val KEYCODE_ZENKAKU_HANKAKU = 211

    /** Key code constant: Japanese alphanumeric key. */
    const val KEYCODE_EISU = 212

    /** Key code constant: Japanese non-conversion key. */
    const val KEYCODE_MUHENKAN = 213

    /** Key code constant: Japanese conversion key. */
    const val KEYCODE_HENKAN = 214

    /** Key code constant: Japanese katakana / hiragana key. */
    const val KEYCODE_KATAKANA_HIRAGANA = 215

    /** Key code constant: Japanese Yen key. */
    const val KEYCODE_YEN = 216

    /** Key code constant: Japanese Ro key. */
    const val KEYCODE_RO = 217

    /** Key code constant: Japanese kana key. */
    const val KEYCODE_KANA = 218

    /** Key code constant: Assist key.
     * Launches the global assist activity.  Not delivered to applications. */
    const val KEYCODE_ASSIST = 219

    /** Key code constant: Brightness Down key.
     * Adjusts the screen brightness down. */
    const val KEYCODE_BRIGHTNESS_DOWN = 220

    /** Key code constant: Brightness Up key.
     * Adjusts the screen brightness up. */
    const val KEYCODE_BRIGHTNESS_UP = 221

    /** Key code constant: Audio Track key.
     * Switches the audio tracks. */
    const val KEYCODE_MEDIA_AUDIO_TRACK = 222

    /** Key code constant: Sleep key.
     * Puts the device to sleep.  Behaves somewhat like {@link #KEYCODE_POWER} but it
     * has no effect if the device is already asleep. */
    const val KEYCODE_SLEEP = 223

    /** Key code constant: Wakeup key.
     * Wakes up the device.  Behaves somewhat like {@link #KEYCODE_POWER} but it
     * has no effect if the device is already awake. */
    const val KEYCODE_WAKEUP = 224

    /** Key code constant: Pairing key.
     * Initiates peripheral pairing mode. Useful for pairing remote control
     * devices or game controllers, especially if no other input mode is
     * available. */
    const val KEYCODE_PAIRING = 225

    /** Key code constant: Media Top Menu key.
     * Goes to the top of media menu. */
    const val KEYCODE_MEDIA_TOP_MENU = 226

    /** Key code constant: '11' key. */
    const val KEYCODE_11 = 227

    /** Key code constant: '12' key. */
    const val KEYCODE_12 = 228

    /** Key code constant: Last Channel key.
     * Goes to the last viewed channel. */
    const val KEYCODE_LAST_CHANNEL = 229

    /** Key code constant: TV data service key.
     * Displays data services like weather, sports. */
    const val KEYCODE_TV_DATA_SERVICE = 230

    /** Key code constant: Voice Assist key.
     * Launches the global voice assist activity. Not delivered to applications. */
    const val KEYCODE_VOICE_ASSIST = 231

    /** Key code constant: Radio key.
     * Toggles TV service / Radio service. */
    const val KEYCODE_TV_RADIO_SERVICE = 232

    /** Key code constant: Teletext key.
     * Displays Teletext service. */
    const val KEYCODE_TV_TELETEXT = 233

    /** Key code constant: Number entry key.
     * Initiates to enter multi-digit channel nubmber when each digit key is assigned
     * for selecting separate channel. Corresponds to Number Entry Mode (0x1D) of CEC
     * User Control Code. */
    const val KEYCODE_TV_NUMBER_ENTRY = 234

    /** Key code constant: Analog Terrestrial key.
     * Switches to analog terrestrial broadcast service. */
    const val KEYCODE_TV_TERRESTRIAL_ANALOG = 235

    /** Key code constant: Digital Terrestrial key.
     * Switches to digital terrestrial broadcast service. */
    const val KEYCODE_TV_TERRESTRIAL_DIGITAL = 236

    /** Key code constant: Satellite key.
     * Switches to digital satellite broadcast service. */
    const val KEYCODE_TV_SATELLITE = 237

    /** Key code constant: BS key.
     * Switches to BS digital satellite broadcasting service available in Japan. */
    const val KEYCODE_TV_SATELLITE_BS = 238

    /** Key code constant: CS key.
     * Switches to CS digital satellite broadcasting service available in Japan. */
    const val KEYCODE_TV_SATELLITE_CS = 239

    /** Key code constant: BS/CS key.
     * Toggles between BS and CS digital satellite services. */
    const val KEYCODE_TV_SATELLITE_SERVICE = 240

    /** Key code constant: Toggle Network key.
     * Toggles selecting broacast services. */
    const val KEYCODE_TV_NETWORK = 241

    /** Key code constant: Antenna/Cable key.
     * Toggles broadcast input source between antenna and cable. */
    const val KEYCODE_TV_ANTENNA_CABLE = 242

    /** Key code constant: HDMI #1 key.
     * Switches to HDMI input #1. */
    const val KEYCODE_TV_INPUT_HDMI_1 = 243

    /** Key code constant: HDMI #2 key.
     * Switches to HDMI input #2. */
    const val KEYCODE_TV_INPUT_HDMI_2 = 244

    /** Key code constant: HDMI #3 key.
     * Switches to HDMI input #3. */
    const val KEYCODE_TV_INPUT_HDMI_3 = 245

    /** Key code constant: HDMI #4 key.
     * Switches to HDMI input #4. */
    const val KEYCODE_TV_INPUT_HDMI_4 = 246

    /** Key code constant: Composite #1 key.
     * Switches to composite video input #1. */
    const val KEYCODE_TV_INPUT_COMPOSITE_1 = 247

    /** Key code constant: Composite #2 key.
     * Switches to composite video input #2. */
    const val KEYCODE_TV_INPUT_COMPOSITE_2 = 248

    /** Key code constant: Component #1 key.
     * Switches to component video input #1. */
    const val KEYCODE_TV_INPUT_COMPONENT_1 = 249

    /** Key code constant: Component #2 key.
     * Switches to component video input #2. */
    const val KEYCODE_TV_INPUT_COMPONENT_2 = 250

    /** Key code constant: VGA #1 key.
     * Switches to VGA (analog RGB) input #1. */
    const val KEYCODE_TV_INPUT_VGA_1 = 251

    /** Key code constant: Audio description key.
     * Toggles audio description off / on. */
    const val KEYCODE_TV_AUDIO_DESCRIPTION = 252

    /** Key code constant: Audio description mixing volume up key.
     * Louden audio description volume as compared with normal audio volume. */
    const val KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP = 253

    /** Key code constant: Audio description mixing volume down key.
     * Lessen audio description volume as compared with normal audio volume. */
    const val KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN = 254

    /** Key code constant: Zoom mode key.
     * Changes Zoom mode (Normal, Full, Zoom, Wide-zoom, etc.) */
    const val KEYCODE_TV_ZOOM_MODE = 255

    /** Key code constant: Contents menu key.
     * Goes to the title list. Corresponds to Contents Menu (0x0B) of CEC User Control
     * Code */
    const val KEYCODE_TV_CONTENTS_MENU = 256

    /** Key code constant: Media context menu key.
     * Goes to the context menu of media contents. Corresponds to Media Context-sensitive
     * Menu (0x11) of CEC User Control Code. */
    const val KEYCODE_TV_MEDIA_CONTEXT_MENU = 257

    /** Key code constant: Timer programming key.
     * Goes to the timer recording menu. Corresponds to Timer Programming (0x54) of
     * CEC User Control Code. */
    const val KEYCODE_TV_TIMER_PROGRAMMING = 258

    /** Key code constant: Help key. */
    const val KEYCODE_HELP = 259

    /** Key code constant: Navigate to previous key.
     * Goes backward by one item in an ordered collection of items. */
    const val KEYCODE_NAVIGATE_PREVIOUS = 260

    /** Key code constant: Navigate to next key.
     * Advances to the next item in an ordered collection of items. */
    const val KEYCODE_NAVIGATE_NEXT = 261

    /** Key code constant: Navigate in key.
     * Activates the item that currently has focus or expands to the next level of a navigation
     * hierarchy. */
    const val KEYCODE_NAVIGATE_IN = 262

    /** Key code constant: Navigate out key.
     * Backs out one level of a navigation hierarchy or collapses the item that currently has
     * focus. */
    const val KEYCODE_NAVIGATE_OUT = 263

    /** Key code constant: Primary stem key for Wear
     * Main power/reset button on watch. */
    const val KEYCODE_STEM_PRIMARY = 264

    /** Key code constant: Generic stem key 1 for Wear */
    const val KEYCODE_STEM_1 = 265

    /** Key code constant: Generic stem key 2 for Wear */
    const val KEYCODE_STEM_2 = 266

    /** Key code constant: Generic stem key 3 for Wear */
    const val KEYCODE_STEM_3 = 267

    /** Key code constant: Directional Pad Up-Left */
    const val KEYCODE_DPAD_UP_LEFT = 268

    /** Key code constant: Directional Pad Down-Left */
    const val KEYCODE_DPAD_DOWN_LEFT = 269

    /** Key code constant: Directional Pad Up-Right */
    const val KEYCODE_DPAD_UP_RIGHT = 270

    /** Key code constant: Directional Pad Down-Right */
    const val KEYCODE_DPAD_DOWN_RIGHT = 271

    /** Key code constant: Skip forward media key. */
    const val KEYCODE_MEDIA_SKIP_FORWARD = 272

    /** Key code constant: Skip backward media key. */
    const val KEYCODE_MEDIA_SKIP_BACKWARD = 273

    /** Key code constant: Step forward media key.
     * Steps media forward, one frame at a time. */
    const val KEYCODE_MEDIA_STEP_FORWARD = 274

    /** Key code constant: Step backward media key.
     * Steps media backward, one frame at a time. */
    const val KEYCODE_MEDIA_STEP_BACKWARD = 275

    /** Key code constant: put device to sleep unless a wakelock is held. */
    const val KEYCODE_SOFT_SLEEP = 276

    /** Key code constant: Cut key. */
    const val KEYCODE_CUT = 277

    /** Key code constant: Copy key. */
    const val KEYCODE_COPY = 278

    /** Key code constant: Paste key. */
    const val KEYCODE_PASTE = 279

    /** Key code constant: Consumed by the system for navigation up */
    const val KEYCODE_SYSTEM_NAVIGATION_UP = 280

    /** Key code constant: Consumed by the system for navigation down */
    const val KEYCODE_SYSTEM_NAVIGATION_DOWN = 281

    /** Key code constant: Consumed by the system for navigation left*/
    const val KEYCODE_SYSTEM_NAVIGATION_LEFT = 282

    /** Key code constant: Consumed by the system for navigation right */
    const val KEYCODE_SYSTEM_NAVIGATION_RIGHT = 283

    /** Key code constant: Show all apps */
    const val KEYCODE_ALL_APPS = 284

    /** Key code constant: Refresh key. */
    const val KEYCODE_REFRESH = 285

    /** Key code constant: Thumbs up key. Apps can use this to let user upvote content. */
    const val KEYCODE_THUMBS_UP = 286

    /** Key code constant: Thumbs down key. Apps can use this to let user downvote content. */
    const val KEYCODE_THUMBS_DOWN = 287

    /**
     * Key code constant: Used to switch current {@link android.accounts.Account} that is
     * consuming content. May be consumed by system to set account globally.
     */
    const val KEYCODE_PROFILE_SWITCH = 288

    /** Key code constant: Video Application key #1. */
    const val KEYCODE_VIDEO_APP_1 = 289

    /** Key code constant: Video Application key #2. */
    const val KEYCODE_VIDEO_APP_2 = 290

    /** Key code constant: Video Application key #3. */
    const val KEYCODE_VIDEO_APP_3 = 291

    /** Key code constant: Video Application key #4. */
    const val KEYCODE_VIDEO_APP_4 = 292

    /** Key code constant: Video Application key #5. */
    const val KEYCODE_VIDEO_APP_5 = 293

    /** Key code constant: Video Application key #6. */
    const val KEYCODE_VIDEO_APP_6 = 294

    /** Key code constant: Video Application key #7. */
    const val KEYCODE_VIDEO_APP_7 = 295

    /** Key code constant: Video Application key #8. */
    const val KEYCODE_VIDEO_APP_8 = 296

    /** Key code constant: Featured Application key #1. */
    const val KEYCODE_FEATURED_APP_1 = 297

    /** Key code constant: Featured Application key #2. */
    const val KEYCODE_FEATURED_APP_2 = 298

    /** Key code constant: Featured Application key #3. */
    const val KEYCODE_FEATURED_APP_3 = 299

    /** Key code constant: Featured Application key #4. */
    const val KEYCODE_FEATURED_APP_4 = 300

    /** Key code constant: Demo Application key #1. */
    const val KEYCODE_DEMO_APP_1 = 301

    /** Key code constant: Demo Application key #2. */
    const val KEYCODE_DEMO_APP_2 = 302

    /** Key code constant: Demo Application key #3. */
    const val KEYCODE_DEMO_APP_3 = 303

    /** Key code constant: Demo Application key #4. */
    const val KEYCODE_DEMO_APP_4 = 304

    /** Key code constant: Keyboard backlight down */
    const val KEYCODE_KEYBOARD_BACKLIGHT_DOWN = 305

    /** Key code constant: Keyboard backlight up */
    const val KEYCODE_KEYBOARD_BACKLIGHT_UP = 306

    /** Key code constant: Keyboard backlight toggle */
    const val KEYCODE_KEYBOARD_BACKLIGHT_TOGGLE = 307

    /**
     * Key code constant: The primary button on the barrel of a stylus.
     * This is usually the button closest to the tip of the stylus.
     */
    const val KEYCODE_STYLUS_BUTTON_PRIMARY = 308

    /**
     * Key code constant: The secondary button on the barrel of a stylus.
     * This is usually the second button from the tip of the stylus.
     */
    const val KEYCODE_STYLUS_BUTTON_SECONDARY = 309

    /**
     * Key code constant: The tertiary button on the barrel of a stylus.
     * This is usually the third button from the tip of the stylus.
     */
    const val KEYCODE_STYLUS_BUTTON_TERTIARY = 310

    /**
     * Key code constant: A button on the tail end of a stylus.
     * The use of this button does not usually correspond to the function of an eraser.
     */
    const val KEYCODE_STYLUS_BUTTON_TAIL = 311

    /**
     * Key code constant: To open recent apps view (a.k.a. Overview).
     * This key is handled by the framework and is never delivered to applications.
     */
    const val KEYCODE_RECENT_APPS = 312

    /**
     * Key code constant: A button whose usage can be customized by the user through
     *                    the system.
     * User customizable key #1.
     */
    const val KEYCODE_MACRO_1 = 313

    /**
     * Key code constant: A button whose usage can be customized by the user through
     *                    the system.
     * User customizable key #2.
     */
    const val KEYCODE_MACRO_2 = 314

    /**
     * Key code constant: A button whose usage can be customized by the user through
     *                    the system.
     * User customizable key #3.
     */
    const val KEYCODE_MACRO_3 = 315

    /**
     * Key code constant: A button whose usage can be customized by the user through
     *                    the system.
     * User customizable key #4.
     */
    const val KEYCODE_MACRO_4 = 316

    /** Key code constant: To open emoji picker */
    const val KEYCODE_EMOJI_PICKER = 317

    /**
     * Key code constant: To take a screenshot
     *
     * This key is fully handled by the framework and will not be sent to the foreground app,
     * unlike {@code KEYCODE_SYSRQ} which is sent to the app first and only if the app
     * doesn't handle it, the framework handles it (to take a screenshot).
     */
    const val KEYCODE_SCREENSHOT = 318

}
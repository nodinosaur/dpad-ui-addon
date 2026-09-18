# Android Emulator "Extended Controls" — How D‑Pad input actually reaches the guest

Research notes from reading the emulator source.

- **Repository:** `https://android.googlesource.com/platform/external/qemu/`
- **Branch:** `emu-master-dev`
- **Revision read:** `ae9d18d` — *"Bump Emulator to 35.6.3 Canary"*
- **Local checkout (sparse, blobless):** `/Users/gmedve/Projects/nodinosaur/emu-master-dev`

```bash
git clone --depth 1 --branch emu-master-dev --filter=blob:none --sparse \
  https://android.googlesource.com/platform/external/qemu/ emu-master-dev
cd emu-master-dev
git sparse-checkout set android/android-ui android/android-grpc android/android-emu android/emu
```

Two further AOSP repos were consulted for the guest-side key layouts (read via Gitiles,
`?format=TEXT`, branch `main`, on 2026‑09‑18):

- `device/generic/goldfish` → `input/qwerty2.kl` — **the layout emulator AVDs actually use**
- `platform/frameworks/base` → `data/keyboards/Generic.kl` — the fallback layout

```bash
curl -sS "https://android.googlesource.com/device/generic/goldfish/+/refs/heads/main/input/qwerty2.kl?format=TEXT" | base64 -D
curl -sS "https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/data/keyboards/Generic.kl?format=TEXT" | base64 -D
```

---

## 1. Where the code lives

The repo has been reorganised since the older docs. Actual paths on `emu-master-dev`:

| Concern | Path |
| --- | --- |
| Extended Controls pages (one CMake module per page) | `android/android-ui/modules/aemu-ext-pages/` |
| **D‑Pad page** | `android/android-ui/modules/aemu-ext-pages/dpad/src/android/skin/qt/extended-pages/dpad-page.{h,cpp,ui}` |
| **TV remote page** (Android TV AVDs) | `android/android-ui/modules/aemu-ext-pages/tv-remote/src/android/skin/qt/extended-pages/tv-remote-page.{h,cpp,ui}` |
| Pages that *do* use gRPC | `android/android-ui/modules/aemu-ext-pages-grpc/` |
| Skin keycodes / evdev tables | `android/android-ui/modules/aemu-ui-common/include/android/skin/{keycode.h,linux_keycodes.h,android_keycodes.h}` |
| Qt window + skin event pump | `android/android-ui/modules/aemu-ui-qt/`, `android/android-ui/modules/aemu-ui-window/` |
| gRPC services (all of them) | `android/android-grpc/services/` |
| `EmulatorController` proto | `android/android-grpc/services/emulator-controller/proto/emulator_controller.proto` |
| `EmulatorController` server impl | `android/android-grpc/services/emulator-controller/server/src/android/emulation/control/EmulatorService.cpp` |
| gRPC key translation | `.../server/src/android/emulation/control/keyboard/KeyEventSender.cpp` |
| Chromium keycode table | `.../server/include/android/emulation/control/keyboard/keycode_converter_data.inc` |
| Token/JWT auth | `android/android-grpc/security/src/android/emulation/control/secure/BasicTokenAuth.cpp` |
| gRPC bootstrap / port + token advertisement | `android-qemu2-glue/qemu-setup.cpp` |
| QEMU input injection | `android-qemu2-glue/qemu-user-event-agent-impl.c` |
| `UiController` service | `android/android-grpc/services/ui-controller/proto/ui_controller_service.proto` |

---

## 2. ⚠️ Correction: the D‑Pad page does **not** use gRPC

This is the single most important finding, and it contradicts the assumption in `CLAUDE.md`.

`DPadPage` and `TvRemotePage` run **inside the emulator process**. Clicking a button posts a
`SkinEvent` straight onto the emulator window's event queue — no sockets, no protobuf, no HTTP/2:

```cpp
// dpad-page.cpp
void DPadPage::toggleButtonPressed(QPushButton* button, const SkinKeyCode key_code, const bool pressed) {
    if (mEmulatorWindow) {
        SkinEvent skin_event = createSkinEvent(pressed ? kEventKeyDown : kEventKeyUp);
        skin_event.u.key.keycode = key_code;   // an evdev code, see §3
        skin_event.u.key.mod = 0;
        mEmulatorWindow->queueSkinEvent(std::move(skin_event));
    }
    ...
}
```

Only *some* Extended Controls panes were migrated to gRPC — those live in the separate
`aemu-ext-pages-grpc/` module tree (battery, cellular, telephony, camera, display, finger,
snapshot, bugreport…). D‑Pad, TV remote and rotary were **not** migrated.

**However**, both paths converge on exactly the same terminus, so the plugin's gRPC approach is
still functionally equivalent:

```
Extended Controls D-Pad ─┐
                         ├─► QAndroidUserEventAgent::sendKey(code, down)
gRPC EmulatorController  │      │
  .sendKey(KeyboardEvent)┘      │  (user_event_key: sets bit 0x400 when down)
                                ▼
                    user_event_keycode(code)        [qemu-user-event-agent-impl.c]
                                │
                                ▼
                    qemu_input_event_enqueue(QCODE = code & 0x3ff, down)
                                │
                                ▼
                goldfish_events / virtio-input  →  guest evdev  →  Android InputReader
```

Relevant glue:

```c
// android/android-ui/modules/aemu-ui-window/src/android/emulator-window.c:99
user_event_agent->sendKey(keycode, down);

// android-qemu2-glue/qemu-user-event-agent-impl.c
static void user_event_key(unsigned code, bool down) {
    if (down) code |= 0x400;
    user_event_keycode(code);
}
static void user_event_keycode(int code) {
    bool down = code & 0x400;
    key->u.qcode.data = code & 0x3ff;   // Android pre-translates; bypass QEMU's keymap
    qemu_input_event_enqueue(qemu_active_console(), evt);
}
```

So: **codes are raw Linux evdev codes, and "pressed" is encoded as bit `0x400`.**

---

## 3. Exact keycodes the Extended Controls D‑Pad sends

`kKeyCode*` are aliases for `LINUX_KEY_*` (`android/skin/keycode.h` → `linux_keycodes.h`):

| Extended Controls button | Skin constant | evdev value |
| --- | --- | --- |
| Up | `kKeyCodeDpadUp` = `LINUX_KEY_UP` | **103** |
| Down | `kKeyCodeDpadDown` = `LINUX_KEY_DOWN` | **108** |
| Left | `kKeyCodeDpadLeft` = `LINUX_KEY_LEFT` | **105** |
| Right | `kKeyCodeDpadRight` = `LINUX_KEY_RIGHT` | **106** |
| Select / OK | `kKeyCodeDpadCenter` = `LINUX_KEY_CENTER` | **232**  ⚠️ *not* 28 |
| Rewind | `kKeyCodeRewind` | 168 |
| Play/Pause | `kKeyCodePlaypause` | 164 |
| Fast forward | `kKeyCodeFastForward` | 208 |
| Back (TV remote) | `kKeyCodeBack` | 158 |
| Home (TV remote) | `kKeyCodeHome` | 102 |

The TV remote page also fires ADB intents for Settings / Dashboard / Program Guide /
Assistant / Watchlist rather than key events.

### 3.1 How the guest resolves those codes — `qwerty2.kl`, **not** `Generic.kl`

This matters, and it is easy to get wrong.

The emulator deliberately names its virtual keyboard **`qwerty2`**:

```c
// hw/input/goldfish_events.c:726
s->name = "qwerty2";

// hw/input/virtio-input-hid.c:20-23
// BUG: 136093985 Use name "qwerty2" so that Android system is able
// to associate the keyboard device with the "qwerty2.kl" ...
#define VIRTIO_ID_NAME_KEYBOARD "qwerty2"
```

Android's `EventHub` therefore loads `/system/usr/keylayout/qwerty2.kl`, which ships from
`device/generic/goldfish/input/qwerty2.kl` — **not** the `Generic.kl` fallback in
`frameworks/base/data/keyboards/`. The two layouts disagree on exactly the keys we care about:

| evdev | `qwerty2.kl` (what the AVD actually uses) | `Generic.kl` (fallback) |
| --- | --- | --- |
| 103 | `DPAD_UP` | `DPAD_UP` |
| 108 | `DPAD_DOWN` | `DPAD_DOWN` |
| 105 | `DPAD_LEFT` | `DPAD_LEFT` |
| 106 | `DPAD_RIGHT` | `DPAD_RIGHT` |
| **232** | **`DPAD_CENTER`** | *unmapped* |
| 28 | `ENTER` | `ENTER` |
| **102** | **`HOME`** | `MOVE_HOME` |
| 158 | `BACK` | `BACK` |
| 164 | `MEDIA_PLAY_PAUSE` | `MEDIA_PLAY_PAUSE` |
| 168 | `MEDIA_REWIND` | `MEDIA_REWIND` |
| 208 | *unmapped* | `MEDIA_FAST_FORWARD` |
| 353 | *unmapped* | `DPAD_CENTER` |
| 172 | *unmapped* | `HOME` |

Verified 2026‑09‑18 against `main` of both AOSP repos.

Consequences:

- **232 is correct for D‑Pad centre on an emulator, and only on an emulator.** On a real
  device using `Generic.kl` it is unmapped and does nothing; there `353` is `DPAD_CENTER`.
  Since `EmulatorGrpcKeyEventSender` is only ever used for emulators this is safe, but the
  constant must not be shared with any physical-device path.
- **102 is `HOME` on the emulator but `MOVE_HOME` (text cursor to line start) under
  `Generic.kl`.** Same caveat.
- **208 (fast forward) is unmapped in `qwerty2.kl`** — the emulator's own D‑Pad page sends it
  and the guest ignores it. Don't copy that particular button.

> **Gotcha for this plugin:** `EvdevKeyCodes` currently maps `KEYCODE_DPAD_CENTER (23) → 28`
> (`KEY_ENTER`). The emulator's own D‑Pad uses **232**, which `qwerty2.kl` maps to
> `DPAD_CENTER`; 28 maps to `ENTER`. They behave alike in most focus-based UIs but are *not*
> the same key — Android TV apps that distinguish them will misbehave. Send 232.

---

## 4. The gRPC path (`EmulatorController.sendKey`)

### 4.1 Proto

`emulator_controller.proto` (upstream is 1480 lines; this project vendors a 34‑line subset with
the same `package android.emulation.control` and service name, which is all that matters
on the wire).

```proto
service EmulatorController {
    rpc sendKey(KeyboardEvent) returns (google.protobuf.Empty) {}
    rpc streamInputEvent(stream InputEvent) returns (google.protobuf.Empty) {}   // ← low-latency
    ...
}

message KeyboardEvent {
    enum KeyCodeType { Usb = 0; Evdev = 1; XKB = 2; Win = 3; Mac = 4; }
    enum KeyEventType { keydown = 0; keyup = 1; keypress = 2; }

    KeyCodeType  codeType  = 1;
    KeyEventType eventType = 2;
    int32        keyCode   = 3;   // interpreted per codeType, translated to evdev
    string       key       = 4;   // W3C DOM key name, e.g. "GoBack", "GoHome", "AppSwitch"
    string       text      = 5;   // UTF-8 string, sent as a sequence of keypresses
}
```

Note `keypress` — one enum value that produces keydown **immediately followed by** keyup.
The plugin currently emulates this with two separate `sendKey` calls; `keypress` is one RPC.

### 4.2 Server side

`EmulatorService.cpp:458` simply forwards to the key event sender:

```cpp
Status sendKey(ServerContext*, const KeyboardEvent* request, Empty*) {
    mKeyEventSender.send(*request);   // marshals onto the main looper
    return Status::OK;
}
```

`KeyEventSender::sendKeyCode` is where the `0x400` convention is applied:

```cpp
uint32_t evdev = convertToEvDev(code, codeType);          // identity when codeType == Evdev
if (eventType == keydown || eventType == keypress) mAgents->user_event->sendKeyCode(evdev | 0x400);
if (eventType == keyup   || eventType == keypress) mAgents->user_event->sendKeyCode(evdev);
```

`convertToEvDev` walks the Chromium-derived `usb_keycode_map[]` in
`keycode_converter_data.inc`, picking the column that matches `codeType`
(`usb` / `evdev` / `xkb` / `win` / `mac`). Sending `codeType = Evdev` skips translation
entirely — which is what this plugin does, and is the cheapest/safest option.

For reference, the DOM entries the D‑Pad cares about:

```
USB_KEYMAP(0x070028, 0x001c /*28*/,  ..., "Enter",      ENTER)
USB_KEYMAP(0x070052, 0x0067 /*103*/, ..., "ArrowUp",    ARROW_UP)
USB_KEYMAP(0x070051, 0x006c /*108*/, ..., "ArrowDown",  ARROW_DOWN)
USB_KEYMAP(0x070050, 0x0069 /*105*/, ..., "ArrowLeft",  ARROW_LEFT)
USB_KEYMAP(0x07004f, 0x006a /*106*/, ..., "ArrowRight", ARROW_RIGHT)
USB_KEYMAP(0x0c0224, 0x009e /*158*/, ..., "BrowserBack", BROWSER_BACK)
USB_KEYMAP(0x0c0223, 0x00ac /*172*/, ..., "BrowserHome", BROWSER_HOME)
USB_KEYMAP(0x0c01a2, 0x0244,         ..., "SelectTask",  SELECT_TASK)
```

There is also a documented set of Android-specific `key` strings that avoid keycode tables
altogether: `"GoBack"`, `"GoHome"`, `"AppSwitch"`, `"Power"`.

---

## 5. Discovery, port and authentication (`qemu-setup.cpp::qemu_setup_grpc`)

```cpp
int grpc_start = android_serial_number_port + 3000;   // emulator-5554 → 8554
int grpc_end   = grpc_start + 1000;                   // ...but it is a RANGE
std::string address = "[::1]";                        // IPv6 loopback ONLY by default

if (has_grpc_flag && sscanf(cmdline->grpc, "%d", &grpc_start) == 1) {
    grpc_end = grpc_start + 1;
    address  = "[::]";                                // -grpc <port> ⇒ all interfaces
}
...
bool useToken = !has_grpc_flag || cmdline->grpc_use_token;
if (useToken) {
    auto token = generateToken(64);                   // random, per emulator RUN
    builder.withAuthToken(token);
    props["grpc.token"] = token;
}
if (!has_grpc_flag || cmdline->grpc_use_jwt) { ... props["grpc.jwks"] ... }

props["grpc.port"] = std::to_string(port);            // the port actually bound
```

`props` is written to a **discovery file**, `pid_<PID>.ini`, in
`<discovery-dir>/avd/running/`:

| OS | Discovery dir |
| --- | --- |
| macOS | `~/Library/Caches/TemporaryItems` *and* `~/.android` |
| Linux | `$XDG_RUNTIME_DIR` or `/run/user/<uid>`, *and* `~/.android` |
| Windows | `%LOCALAPPDATA%\Temp`, *and* `%USERPROFILE%\.android` |

(also honours `ANDROID_EMULATOR_HOME`, `ANDROID_SDK_HOME/.android`, `ANDROID_AVD_HOME`.)
See `android/android-grpc/python/aemu-grpc/src/aemu/discovery/emulator_discovery.py` and
`android/emu/studio-config/src/android/emulation/control/EmulatorAdvertisement.cpp`.

Relevant keys: `grpc.port`, `grpc.address`, `grpc.token`, `grpc.jwks`, `grpc.jwk_active`,
`port.serial`, `port.adb`, `avd.name`, `avd.id`.

### Auth header format

```cpp
// BasicTokenAuth.h
const static inline std::string DEFAULT_HEADER{"authorization"};
const static inline std::string DEFAULT_BEARER{"Bearer "};
// BasicTokenAuth.cpp
StaticTokenAuth::StaticTokenAuth(...) : mStaticToken(DEFAULT_BEARER + token) {}
bool StaticTokenAuth::canHandleToken(std::string_view t) { return t == mStaticToken; }
```

→ the metadata must be **`authorization: Bearer <token>`**, with the prefix.

Security preference order used by the reference clients: **JWT > Token > none**.
An allow-list (`$ANDROID_SDK_ROOT/emulator/lib/emulator_access.json`) can additionally
forbid specific URIs per issuer.

### ⚠️ Gotchas for this plugin

1. **`~/.emulator_console_auth_token` is the *telnet console* token, not the gRPC token.**
   The gRPC token is randomly regenerated on every emulator launch and only published in
   `pid_<PID>.ini` as `grpc.token`. `EmulatorGrpcKeyEventSender.readAuthToken()` is reading
   the wrong file.
2. **The header needs the `Bearer ` prefix** — the current code sends the raw value, which
   `StaticTokenAuth::canHandleToken` will reject.
3. **Port = serial + 3000 is only the *start of a range*.** With several emulators running,
   or when 8554 is taken, the bound port differs. Read `grpc.port` from the discovery file.
4. **Default bind address is `[::1]` (IPv6 loopback only).** A Java channel built with
   `forAddress("localhost", port)` may resolve to `127.0.0.1` and get connection-refused.
   Prefer `grpc.address` from the ini, or `[::1]` / `forTarget("ipv6:///[::1]:<port>")`.
5. **Android Studio may already hold the port.** Studio's own embedded-emulator support uses
   the same discovery + `streamInputEvent` mechanism; multiple clients are fine, it's a server.

---

## 6. Other Extended Controls services worth knowing

| Service | Proto | Purpose |
| --- | --- | --- |
| `EmulatorController` | `services/emulator-controller/proto/emulator_controller.proto` | Input (key/touch/mouse/pen/wheel), sensors, battery, GPS, clipboard, screenshot/audio streaming, VM state, displays, posture, brightness, XR |
| `UiController` | `services/ui-controller/proto/` | `showExtendedControls(PaneEntry)`, `closeExtendedControls`, `setUiTheme`, `getUserConfig` — this is how Studio opens a specific Extended Controls pane |
| `SnapshotService` | `services/snapshot/` | Save/load/list snapshots |
| `AdbService`, `Waterfall`, `Bluetooth`, `Gnss`, `EmulatorStats` | `services/*` | — |
| Incubating: `Modem`, `Car`, `Sensor`, `VirtualScene`, `ScreenRecording`, `Avd` | `services/incubating/` | — |

---

## 7. Recommended follow-ups for `dpad-ui-addon`

1. **Discovery-file based connection** — add an `EmulatorDiscovery` that scans the
   `avd/running/pid_*.ini` files, matches on `port.serial`/`port.adb` against the ADB serial,
   and returns `grpc.address` + `grpc.port` + `grpc.token`. Fall back to `serial + 3000`.
2. **Fix the auth header** to `authorization: Bearer <grpc.token>`.
3. **Fix D‑Pad center** to evdev `232` (`DPAD_CENTER` under `qwerty2.kl`) to match Extended
   Controls. Note `156` (`KEY_BOOKMARKS`, currently used for `KEYCODE_BOOKMARK`) and `208`
   (fast forward) are **unmapped in `qwerty2.kl`** and are silently dropped by emulators.
4. **Use `keypress`** for the tap case instead of two round-trips, and/or migrate to
   `streamInputEvent` (client-streaming) to remove per-key connection overhead — this is
   what the emulator itself expects for low-latency input.
5. **Real long-press** — the emulator has no server-side hold. `ActionLongPress` must send
   `keydown`, wait, then `keyup`; the current implementation fires them back to back.
6. Add `Back`/`Home` via the `key` field (`"GoBack"` / `"GoHome"`) as a robust alternative to
   evdev 158/102.


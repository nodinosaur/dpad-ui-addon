# CHANGELOG

## [2.0.0]

### Added

- Full Android Emulator compatibility via gRPC — D-Pad key events are sent to the emulator's `EmulatorController.sendKey` service as raw evdev codes, the same codes and the same guest-side input path (`user_event_keycode` → QEMU → guest evdev) that the Emulator's own Extended Controls D-Pad uses
- Automatic device routing: emulators are detected and controlled via gRPC, physical devices continue to use ADB
- Graceful fallback: if gRPC is unusable, the plugin falls back to ADB and stops retrying gRPC for that device
- Proper keydown/keyup event support for emulators via evdev keycodes (previously ADB did not distinguish between down and up)
- Emulator discovery: reads the `avd/running/pid_*.ini` files the emulator publishes to obtain its real gRPC port and per-launch auth token, matching on `port.serial`
- Auth token support: sends `authorization: Bearer <grpc.token>`, as required by the emulator's `StaticTokenAuth`
- Long-press support over gRPC: the key is held down client-side, since the emulator has no server-side hold
- Unit tests for discovery-file parsing, endpoint resolution, evdev keycode mappings, auth headers and long-press timing

### Changed

- Introduced `KeyEventSender` abstraction layer with `AdbKeyEventSender` and `EmulatorGrpcKeyEventSender` implementations
- Replaced synchronous `ShellCommandsFactory` calls with coroutine-based `DeviceInputRouter`
- Tool window now properly cleans up gRPC channels and coroutine scope on close

### Fixed

- D-Pad centre now sends evdev `232` (`KEY_CENTER`), not `28` (`KEY_ENTER`). The emulator guest uses `qwerty2.kl`, where only `232` maps to `DPAD_CENTER`
- The gRPC port is read from the emulator's discovery file instead of being guessed as `serial + 3000`; the emulator searches a 1000-port range, so the guess was frequently wrong
- Connections target the IPv6 loopback `[::1]` the emulator actually binds, with an IPv4 fallback, instead of `localhost`
- The auth token is read from the emulator's discovery file rather than `~/.emulator_console_auth_token`, which is the *console* token and never valid for gRPC
- Taps send a single `keypress` instead of a bare `keydown`, so keys no longer stick down in the guest
- `KEYCODE_BOOKMARK` is no longer sent over gRPC: its evdev code is unmapped in `qwerty2.kl`, so emulators silently swallowed it. It now routes to ADB
- Channel shutdown no longer blocks, so closing the tool window cannot stall the IDE

### Deprecated

- `ShellCommandsFactory` — replaced by `DeviceInputRouter` and the new `input` package

## [1.3.0] - 2025-04-21

### Added

- K2 support
- Sending click states (Down/Up)

## [1.2.0] - 2025-04-21

### Added

- Long-press functionality

## [1.1.1] - 2025-04-15

### Added

- Initial release of the plugin
- React to theme switching
- Call ADB actions like `startActivity`

## [1.0.0] - 2025-04-15

### Added

- Preparation and teaks for initial release

## [0.0.1] - 2025-04-06

### Added

- Initial scaffold created from IntelliJ Platform Plugin Template

[1.2.0]: https://github.com/nodinosaur/dpad-ui-addon/compare/v1.0.0...v1.2.0
[1.1.1]: https://github.com/nodinosaur/dpad-ui-addon/compare/v1.2.0...v1.1.1
[1.0.0]: https://github.com/nodinosaur/dpad-ui-addon/compare/v0.0.1...v1.0.0
[0.0.1]: https://github.com/nodinosaur/dpad-ui-addon/commits/v0.0.1

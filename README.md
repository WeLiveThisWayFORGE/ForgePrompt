# ForgePrompt

ForgePrompt is a programmable Android keyboard for phrases, lenny faces, macros, and one-tap replies. Each key can have its own color, and key size is Small, Medium, or Large.

The name on the launcher, in the keyboard list, and in Settings is ForgePrompt. Copyright and upstream credit are in `NOTICE`.

## Install beside the original keyboard

The application id is `com.adgad.kboard.plus`. The launcher name, the keyboard name in Android’s input-method list, and the accessibility service name are **ForgePrompt**. The custom-keys screen uses its own intent action (`com.adgad.kboard.plus.CustomKeysActivity`), so opening it does not land in the original keyboard app. The Java package stays `com.adgad.kboard`.

There is no content provider or account type in this app. Backup uses this app’s own preference file (`com.adgad.kboard.plus_preferences`).

## Key color

1. Open ForgePrompt.
2. Tap **Custom Keys**.
3. Tap a key, or the + button to add one.
4. Type the phrase or command, then tap a color. The × swatch clears the color.
5. Tap **OK** or **Add**.

The keyboard paints that color as the key background and picks black or white text so the label stays readable. Keys with no color keep the background color from **Options**. The same color is shown on the custom-key row.

Colors are stored on the key in the `userKeys-defaults` preference, so they are included in **Export Keys**, **Import Keys**, and Android backup. A file saved by an older version of the keyboard (a JSON array of strings) still imports; those keys simply have no color. ForgePrompt writes keys back as JSON objects (`{"text":"...","color":-16711681}`), and a missing `color` means “use the keyboard background”.

## Key size

In **Options**, **Key Size** is Small, Medium, or Large. Medium is the default and matches the original key height and text size. Small and Large change the key height and the label size. Keys stay four across (one across in single-row mode), so the label is ellipsized a little sooner on Large and a little later on Small. **Font Size** still applies on top of that.

## Macro help

**Macro Help** in Settings opens `docs/macro-help.md`, which is bundled in the app and works offline. It describes phrase keys, command keys, and time tokens. That bundled guide is the one the app shows.

## Time in a phrase

A phrase can contain `{time+5m}`, `{time+10m}`, `{time+20m}`, `{time+30m}`, or any other whole number of minutes up to 100000. Tapping the key replaces that token from the phone's clock, using the device's 12-hour or 24-hour setting. The saved text keeps the token, so the next tap reads the clock again. The tap does not send the message. `{time}` is the current time. Hours (`{time+2h}`, `{time+1h30m}`) and a minus sign work the same way. A broken token, including `{send}` and `{time+100001m}`, is inserted unchanged. `{send}` does not send and does not split a message.

## Build a debug APK

You need JDK 17 or newer and Android SDK 34.

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew assembleDebug test
```

The debug APK, signed with the Android debug key, is written to:

`app/build/outputs/apk/debug/app-debug.apk`

Install it with `adb install -r app/build/outputs/apk/debug/app-debug.apk`, then enable **ForgePrompt** under System settings → Languages & input → On-screen keyboard.

## Release build

Version `5.3.3-plus` (versionCode 47) keeps the application id `com.adgad.kboard.plus`. The release build is not debuggable. Gradle signs it with the permanent release key only when every one of these environment variables is set:

- `KBOARD_RELEASE_STORE_FILE` — path to the release keystore
- `KBOARD_RELEASE_STORE_PASSWORD` — keystore password
- `KBOARD_RELEASE_KEY_ALIAS` — key alias
- `KBOARD_RELEASE_KEY_PASSWORD` — key password

Keep those values out of the repository. With none of the variables set:

```bash
./gradlew assembleRelease
```

writes an unsigned APK to `app/build/outputs/apk/release/app-release-unsigned.apk`. The release build does not sign with the Android debug key. If only some of the variables are set, the build stops.

## Replacing a debug install

Export your custom keys before uninstalling a debug-signed install. In **Options**, use **Export Keys**, and keep that file. Android updates an installed app only when the new APK uses the same signing key. The permanent release key differs from the debug key, so the release build installs only after the debug app is uninstalled. Uninstalling deletes keys that were not exported.

After the release app is installed, use **Options → Import Keys**. The export file keeps each phrase and its color. An older export that is a JSON array of strings still imports; those keys have no per-key color until you set one.

A later update signed with that same release key installs over the previous release, and the keys already stored in the app stay in place. That includes the signed release that was labeled as the previous keyboard name.

Sideloading can still show Android’s install warning, including “install unknown apps” and Play Protect, including for this release build.

## Copyright

ForgePrompt is GPL-3.0. See `LICENSE` and `NOTICE`. The command language and most of the keyboard come from an upstream project named in `NOTICE`. This repository does not claim that work as new.

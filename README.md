# ForgePrompt

ForgePrompt is a programmable Android keyboard for phrases, macros, and one-tap replies. Each key can have its own color, and key size is Small, Medium, or Large.

The name on the launcher, in the keyboard list, and in Settings is ForgePrompt. The application id remains `com.adgad.kboard.plus`, so an update keeps keys already stored by this app.

Install the signed release [v5.3.3-plus](https://github.com/WeLiveThisWayFORGE/kboard-plus/releases/tag/v5.3.3-plus). That release was built from commit `48e6483fe234f2b74efb65a573d577f26a48805a`. The default branch can lag that tag while pull requests are open. Use the release tag when you want the tested release source.

A phrase in that release can contain `{time+5m}`, `{time+10m}`, `{time+20m}`, `{time+30m}`, or any other whole number of minutes up to 100000. The phone's local clock and its 12-hour or 24-hour setting fill the token when the key is tapped. The tap does not send the message. `{send}` is ordinary text.

Settings → Macro Help in that release opens an offline guide. Copyright and upstream credit are in `NOTICE`.

## Replacing a debug install

Export custom keys before uninstalling a debug-signed install. Android updates an app only when the new APK uses the same signing key. The permanent release key differs from the debug key, so uninstall the debug app only after **Export Keys**, install the release, then **Import Keys**. Uninstalling deletes keys that were not exported.

An update signed with the same release key installs over the previous signed release, and the keys already in the app stay. Sideloading can still show Android’s install warning.

## Copyright

ForgePrompt is GPL-3.0. See `LICENSE` and `NOTICE`. The command language and most of the keyboard come from an upstream project named in `NOTICE`. This repository does not claim that work as new.

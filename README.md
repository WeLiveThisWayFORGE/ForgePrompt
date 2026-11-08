# kboard Plus

kboard Plus is a fork of [kboard](https://github.com/adgad/kboard) by adgad (file headers credit arjun). The baseline is the F-Droid source at upstream commit `2660fb1` (`5.3.0`, "bump version number"). This repository keeps that master history. Upstream `github.properties` is not part of this history. The app is licensed under GPL-3.0; see `LICENSE`. Original copyright notices are unchanged.

It is a programmable Android keyboard for phrases, lenny faces, macros, and one-tap replies. This fork can be installed next to the official kboard app, and it adds a color for each key plus a key-size setting.

Upstream links: [F-Droid](https://f-droid.org/en/packages/com.adgad.kboard/) · [source](https://github.com/adgad/kboard)

## Install beside official kboard

The application id is `com.adgad.kboard.plus`. The launcher name, the keyboard name in Android’s input-method list, and the accessibility service name are **kboard Plus**. The custom-keys screen uses its own intent action (`com.adgad.kboard.plus.CustomKeysActivity`), so opening it does not land in the official app. The Java package stays `com.adgad.kboard`.

There is no content provider or account type in this app. Backup uses the app’s own preference file (`com.adgad.kboard.plus_preferences`).

## Key color

1. Open kboard Plus.
2. Tap **Custom Keys**.
3. Tap a key, or the + button to add one.
4. Type the phrase or command, then tap a color. The × swatch clears the color.
5. Tap **OK** or **Add**.

The keyboard paints that color as the key background and picks black or white text so the label stays readable. Keys with no color keep the background color from **Options**. The same color is shown on the custom-key row.

Colors are stored on the key in the `userKeys-defaults` preference, so they are included in **Export Keys**, **Import Keys**, and Android backup. A file saved by older kboard (a JSON array of strings) still imports; those keys simply have no color. kboard Plus writes keys back as JSON objects (`{"text":"...","color":-16711681}`), and a missing `color` means “use the keyboard background”.

## Key size

In **Options**, **Key Size** is Small, Medium, or Large. Medium is the default and matches the original key height and text size. Small and Large change the key height and the label size. Keys stay four across (one across in single-row mode), so the label is ellipsized a little sooner on Large and a little later on Small. **Font Size** still applies on top of that.

## Build a debug APK

You need JDK 17 or newer and Android SDK 34.

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew assembleDebug test
```

The debug APK, signed with the Android debug key, is written to:

`app/build/outputs/apk/debug/app-debug.apk`

Install it with `adb install -r app/build/outputs/apk/debug/app-debug.apk`, then enable **kboard Plus** under System settings → Languages & input → On-screen keyboard.

---

The rest of this file is the upstream command reference. Command keys work the same way as in kboard.


## Commands

kboard includes a VIM-like syntax for adding special macro keys, to perform tasks like cursor movements, copy/pasting and text manipulation.

A command key has the following format:

`/<friendly label>!<comma separated commands>`

Example:

`/Italicise!dw,i(_$0_)`

This creates a key titled "Italicise", which does the following: `dw` deletes the previous word and adds it to the buffer, `i` inserts the text in the brackets (which would be the deleted word wrapped in underscores.

Most commands can be preceded by a number, indicating how many times it will be run.

**Available commands:**

* `d` delete previous character
* `dw` delete previous word
* `3dw` delete previous three words
* `dd` delete all
* `dt(char)` deletes (backwards) up to and including the specified character
* `ds` - deletes either the selected text, or if nothing is selected then deletes everything.
* `yy` copy all
* `y` copy selection
* `p` paste from buffer
* `pc` paste from clipboard
* `sa` select all
* `sw` select a word, going backwards from the current cursor position
* `i(text)` output the content of the brackets ($0 gets replaced * with the last deleted/copied content)
* `iraw(text)` as i, but does not adhere to settings such as autospace or Passive Aggressive
* `upper(text)` as i, but in upper case
* `lower(text)` as i, but lower case
* `j` move cursor back by a character
* `k` move cursor forward by a character
* `b` move cursor back by a word
* `w` move cursor forward by a word
* `s` trigger a send command
* `e(...)` execute the command in the brackets
* `rnd` output a random kboard key
* `rnd(word1;word2;...)` output a random word from the comma separated list
* `rnde` output a random emoji
* `fr(from;to)` replace all instances of `from` to `to`
* `fancy(style)` outputs buffer text with funky unicode characters (circle, darkcircle,square,darksquare,double,monospace,fancy,fancybold)
* `curl(URL)` requests data from a URL and outputs it.
* `img(URL)` requests an image from a URL and enters it in compatible messaging apps (e.g. Whatsapp)
* `utf(unicode characters)` outputs the text unescaping all unicode character codes (e.g. `\u00A2`)
* `urlencode(string)` outputs the url encoded version of a string (e.g. `hello%3aworld`)
* `qq` switch to the keyboard application you have been using before (same as pressing the earth-wireframe key) after the rest of the commands has been executed.
* `undo` undo the last change (limited to changes made by kboard)

**Examples of use**

`/Italicise Previous!dw,i($0)`

`/Bolden Previous!dw,i($0)`

`/Italics!i(__),j` - this writes the two underscores then moves the cursor between them

`/Copy All!yy`

`/Repeat word x4!dw,4p` - repeats the previous word 4 times

`/rly sad!1000i(😥)`

`/darkcircle!ds,fancy(darkcircle)` - replaces selection or everything with 🅣🅔🅧🅣 🅛🅘🅚🅔 🅣🅗🅘🅢. Choose from `circle`, `darkcircle`, `square`,`darksquare`,`double`,`monospace`,`fancy`,`fancybold`.

_Warning_: weird unicode characters look cool but break accessibility software, so please don't use in public places!

`/exec!dt(!),e($0)` - this is meta, it enables you to write and execute commands inline in a text field by writing e.g. `!10e(^, i(* ), ^)` and hitting the exec key

`/bullets!10e(^, i(* ), ^)` - prepends "* " the the previous 10 lines

`/emojigame!10rnde` - outputs 10 random emoji

`/feeling lucky!rnd,s` - sends a random kboard key

`/birthday!dw,rnd(Happy Birthday name;Hey name, happy birthday.;hb2u;Have a great day name),fr(name,$0)` - write a name, then press this key to send a random birthday message to this name

`/bdaysend!birthday,s` - Can also have commands that refer to other command keys you've made'

`/uuid!curl(https://httpbin.org/uuid)` - Can make arbitrary GET requests to URLS

---

## CURL examples

The `curl` commands allows you to create your own HTTP endpoints to output anything you like!

Here are some examples of how it can be used:

`/yt!ds,curl(https://kboard-api.glitch.me/youtube/$0)`
An endpoint that searches the Youtube API, and returns the URL for the first video found.

`/wolfram!ds,curl(https://api.wolframalpha.com/v1/result?appid=<APP_ID>&i=$0)`
Uses the [Wolfram Short Answers API](https://products.wolframalpha.com/short-answers-api/documentation/) to retrieve answers to anything [Wolfram Alpha](https://www.wolframalpha.com/) can answer. (Requires API key)

`/cat!curl(https://kboard-api.glitch.me/catfact)`
Outputs a cat fact from a [Cat Fact API](https://alexwohlbruck.github.io/cat-facts/)

`/dad joke!curl(https://icanhazdadjoke.com/)`
Outputs a random lame joke.

## Accessibility Service (optional)

kboard provides an accessibility service which can be used to populate a person's name and messages from Whatsapp conversations.

This can be accessed with the keywords `$name`, `$fname` and `$lname`, and `$wachat`.

Getting the right content with the accessibility service is a bit flaky, generally going home/back will pick up the right content from a Whatsapp Conversation.

This service is optional, and the keyboard is fully functional without it. 

Any content retrieved from WhatsApp is not sent or logged anywhere unless you explicitly do so yourself (e.g. by using it alongside `curl`)


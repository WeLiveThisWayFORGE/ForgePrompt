# ForgePrompt macro help

This guide is bundled with ForgePrompt and opens from Settings → Macro Help without a network connection. The copy in the repository is `docs/macro-help.md`.

ForgePrompt is a fork of [kboard](https://github.com/adgad/kboard) by adgad (file headers credit arjun). The command language comes from that project. Its own notes are the [README](https://github.com/adgad/kboard/blob/master/README.md) and the short [wiki](https://github.com/adgad/kboard/wiki). This page describes what this app does, including where that differs from those notes. ForgePrompt is GPL-3.0; see `LICENSE`. The application id remains `com.adgad.kboard.plus`, so existing keys stay with the app.

A key is either a phrase or a command. The keyboard decides from the saved text. It does not look at the label on the key.

## Phrase keys

Any saved text that does not start with `/` and contain `!` is a phrase. Tapping it inserts the text into the current field.

- **Auto-space words**, on by default, puts a space in front when the field already has a character before the cursor.
- **Passive Aggressive.** capitalizes the phrase and adds a period when the last character is not already uppercase.
- **Auto-send words**, off by default, then tries the send action once. See [Sending](#sending). Leave it off unless you want that.

Commas, quotes, and more than one sentence are fine in a phrase. They are a problem inside a command list, because commas separate commands.

Colors and key size are stored with the key and are not part of the inserted text. Export Keys writes the saved text, including any `{time…}` token, so a later import still expands it on each tap.

## Time in a phrase

A phrase can include a time token. Tapping the key reads the phone's clock and replaces the token with a short local time. The saved key is unchanged, so the next tap reads the clock again. This uses no network, and the tap does not send the message.

The minute count is any whole number you type. `{time+5m}`, `{time+10m}`, `{time+20m}`, and `{time+30m}` are the same rule, not a fixed list.

```
{time}
{time+<minutes>m}
{time-<minutes>m}
{time+<hours>h}
{time-<hours>h}
{time+<hours>h<minutes>m}
{time-<hours>h<minutes>m}
```

`<minutes>` and `<hours>` are whole numbers with no sign of their own. The `+` or `-` is written once, before the number. Hours, when you use them, come before minutes. `{time+0m}` is the current time. There is no special case for five minutes.

The absolute total has to be at most 100000 minutes, a bit more than 69 days. A larger number is left as you typed it. So are a fraction, a missing unit, a space inside the braces, and a token this keyboard does not know. None of those crash the key.

The wording follows the phone's 12-hour or 24-hour setting, with the locale's usual order and markers. Around midnight, `{time+5m}` at 11:58 PM is 12:03 AM in 12-hour mode and 00:03 in 24-hour mode. Only the time is inserted, not the date.

Example, saved as a phrase:

`I'm unable to access your property. If you get this message before {time+5m} and can provide access, please respond.`

Tapped at 11:58 PM in 12-hour mode, the field receives:

`I'm unable to access your property. If you get this message before 12:03 AM and can provide access, please respond.`

The same tokens expand inside `i(...)`, `iraw(...)`, `upper(...)`, and `lower(...)`. Text you did not ask to insert is left alone, so the key label still shows `{time+5m}`.

A token the keyboard does not recognize is inserted unchanged. That includes `{send}`, `{Time+5m}`, `{time+}`, `{time+5}`, `{time+5x}`, `{time+5m }`, `{time+5m more}`, and `{time+100001m}`. `{send}` is not a command. See [Sending](#sending).

## Command keys

A command key has this shape:

`/Label!command,command`

The label is the text on the key, without the slash. The commands after `!` run in order when you tap the key. A whole number in front of a command repeats it: `3dw` deletes three words, `4p` pastes the buffer four times. `s` does not repeat; a number in front of it is ignored.

Several commands remember text in a buffer. In `i(...)`, `$0` is that buffer. The buffer lasts for the tap. It is not written to Export Keys.

You can run another command key by its label, without the slash: `/Send birthday!birthday,s` runs whatever you saved as `/birthday!...`, then `s`.

`e(...)` runs the commands inside the parentheses. The built-in `/exec!dt(!),e($0)` deletes back through `!` and runs what was deleted, which is how a line you typed can become a one-off command.

Commas separate commands. A comma inside `i(hello, there)` does not stay inside the text. For a sentence with commas, use a phrase key.

These commands run one after another with no pause between them.

### Delete and copy

| Command | What it does |
| --- | --- |
| `d` | Delete the previous character, or the selection if there is one, and store it in the buffer |
| `dw` | Delete the previous word and store it in the buffer |
| `dt(x)` | Delete backward through the character `x`, and store that text in the buffer |
| `dd` | Select everything, store it in the buffer, and delete it |
| `ds` | Store the selection in the buffer and delete it. With no selection, do that to the whole field |
| `yy` | Copy the whole field into the buffer and the system clipboard, then put the cursor back |
| `y` | Copy the selection into the buffer and the system clipboard |
| `ys` | Like `yy` when nothing is selected. When something is selected, store it in the buffer only |
| `p` | Insert the buffer. If the buffer is empty, paste from the system clipboard |
| `pc` | Paste from the system clipboard |
| `sa` | Select all |
| `sw` | Extend the selection back toward the previous space |

`d` and `dw` only look at a limited amount of text in front of the cursor (one count for `d`, about 30 characters per word for `dw`, about 50 for `dt`).

### Insert and case

| Command | What it does |
| --- | --- |
| `i(text)` | Insert text. Auto-space and Passive Aggressive apply. `$0` is the buffer. Time tokens expand |
| `iraw(text)` | Insert text exactly, without auto-space or Passive Aggressive. Time tokens still expand |
| `upper(text)` | Insert the text in upper case. Time tokens expand first |
| `lower(text)` | Insert the text in lower case. Time tokens expand first |

`$0` is replaced only on the `i` path. `iraw`, `upper`, and `lower` do not replace `$0`.

### Move the cursor

| Command | What it does |
| --- | --- |
| `j` | Move left one character |
| `k` | Move right one character |
| `b` | Move left one word |
| `w` | Move right one word |
| `^` | Move to the start of the line, looking back at most 500 characters |
| `$` | Move to the end of the line, looking forward at most 500 characters |

The upstream notes describe `^` and `$` mostly through examples such as `/bullets!10e(^, i(* ), ^)`. They are real commands here.

### Change the field

| Command | What it does |
| --- | --- |
| `fr(from;to)` | Treat the whole field as text, replace every match of `from`, and write it back. `from` is a regular expression, not a plain word |
| `fancy(style)` | Restyle the buffer with one of `circle`, `darkcircle`, `square`, `darksquare`, `double`, `monospace`, `fancy`, `fancybold`. An unknown style inserts the buffer unchanged. This needs Android 7 or newer; on older phones the text is unchanged |
| `utf(text)` | Insert `text` with Java `\uXXXX` escapes turned into characters |
| `urlencode(text)` | Insert the URL-encoded form of `text` |
| `undo` | Replace the field with an earlier snapshot from this keyboard session |

`undo` is not the messaging app's undo. The keyboard keeps snapshots after its own commands, and only while it is running. If the field changed some other way, the restored text can be wrong. Prefer deleting and inserting again when the result matters.

Fancy characters are easy to misread with a screen reader. Avoid them for anything someone else must act on.

### Random

| Command | What it does |
| --- | --- |
| `rnd` | Insert one of your phrase keys, chosen at random |
| `rnd(a;b;c)` | Insert one of those pieces, split on `;` |
| `rnde` | Insert a random emoji from the emoji set bundled with the app |

`rnd` without parentheses does not choose command keys.

### Network commands

`curl(URL)` performs an HTTP GET and inserts the response body through `i`, so auto-space, Passive Aggressive, `$0`, and time tokens apply to the response. The request uses the header `User-Agent: curl` and `Accept: text/plain`, waits up to 15 seconds, and does not retry. If the request fails, nothing is inserted.

`img(URL)` downloads a picture, waits up to 30 seconds, and commits it as image content. The field shows it only when the open app accepts an inline image. A failure inserts nothing.

kboard's README used sample hosts, including `kboard-api.glitch.me`, a cat-fact URL, and a Wolfram Alpha URL. This project does not run those hosts. A key that still points at one can fail, or can insert whatever that site returns now. Use a URL you trust. `curl` will send the finished URL, including any name or message you substituted into it.

The built-in cat-fact key is one of those old examples. It is not a service this app provides.

### Names from the accessibility service

The optional accessibility service can read a WhatsApp conversation title and a few message lines. In `i(...)` text:

| Token | Replacement |
| --- | --- |
| `$name` | The conversation title it last saw |
| `$fname` | The first word of that title |
| `$lname` | The second word, when the title has two or more words |
| `$wachat` | The message lines it last collected |

The service is off unless you turn it on in Android settings. The match is unreliable; leaving the chat and coming back is what the original notes suggested. Nothing from it is stored in Export Keys or sent anywhere unless you put it in the text you insert, or in a `curl` URL. The keyboard works with the service disabled.

`$0` is the macro buffer, not the accessibility service.

## Sending

`s` asks the current text field to perform its send action. It does that only when the field's IME action is Send, or Done. Done is included because upstream kboard found that WhatsApp reported Done while still accepting a send action. The keyboard does not check which app is open, does not look for a send button, and does not wait.

`s` ignores a repeat count, so `3s` is one attempt. `s,s` is two attempts, back to back, with no pause. There is no delay command.

Auto-send words calls `s` once after a phrase key. It does not run after a command key. A command that already uses `s`, together with Auto-send on a phrase, can submit twice if you use both.

The characters `{send}` inside a phrase are ordinary text. They do not send, and they do not split the phrase.

This is a poor fit for two separate SMS messages. `i(first),s,i(second),s` types the first text and immediately requests send, then types the second text and requests send again. Apps differ:

- A field whose action is Send often sends whatever is in the box, then clears it. The second insert can still land before that clear, so you get one combined message, two messages, or the second text left unsent.
- A field whose action is Done gets the same send request, including forms and dialogs that are not a conversation.
- Any other action, including a plain newline, does nothing. The text stays in the field.
- The request does not confirm that a message left the phone, and it does not notice if you switched chats while it ran.

The keyboard's own Enter key injects an Enter key press. It is not `s`, even when its label says Send.

For two messages, save two phrase keys and press the messaging app's send button between them. That is the reliable approach. Leave Auto-send words off while you do it.

`qq` asks Android to switch to the next keyboard when that command runs. Later commands in the same key still edit the field this keyboard was using. Put `qq` last.

## Short examples

Phrase, time only, nothing sent:

`I'm unable to access your property. If you get this message before {time+5m} and can provide access, please respond.`

Italicize the previous word, which is the upstream pattern:

`/Italicise previous!dw,i(_$0_)`

Put the cursor between two underscores:

`/Italics!i(__),j`

Copy the field:

`/Copy all!yy`

Run a one-off command you typed after `!`:

`/exec!dt(!),e($0)`

`undo` after a mistake:

`/Undo!undo`

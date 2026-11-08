package com.adgad.kboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;


/**
 * Created by arjun on 11/03/15.
 */
public class KboardIME  extends InputMethodService
    implements KboardView.OnKeyboardActionListener,
    SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "kboard";
    private InputMethodManager mInputMethodManager;
    private SharedPreferences sharedPref;
    private KboardView kv;
    private KBoard keyboard;
    private Vibrator vib;
    private List<Keyboard.Key> mKeys;

    private List<UserKey> keys;
    private String mKeySize = KeySize.MEDIUM;
    private boolean mPassiveAggressive;
    private boolean mAutoSpace;
    private boolean mAutoSend;
    private boolean mVibrateOnClick;
    private boolean mSoundOnClick;
    private int mScreen = 0;
    private int totalScreens = 0;
    private int mRows = 5;
    private int mKeysPerScreen = 12;
    private int mKeysPerRow = 4;



    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        initPrefs();
    }

    private void initPrefs() {
        mAutoSpace = sharedPref.getBoolean("autospace", true);
        mAutoSend = sharedPref.getBoolean("autosend", false);
        mVibrateOnClick = sharedPref.getBoolean("vibrate_on", false);
        mSoundOnClick = sharedPref.getBoolean("sound_on", false);
        mPassiveAggressive = sharedPref.getBoolean("passive_aggressive", false);
        mKeySize = sharedPref.getString(KeySize.PREF, KeySize.MEDIUM);
        mRows = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("rows", "5")));
        mKeysPerRow = (mRows == 1) ? 1 : 4;
        mKeysPerScreen = mRows * mKeysPerRow;
        setKeys();
    }


    private void setKeys() {
        String stored = sharedPref.getString(Keys.STORAGE_KEY, null);
        if (stored == null) {
            keys = Keys.getDefault();
        } else {
            try {
                keys = KeyStore.fromJson(stored);
            } catch (RuntimeException e) {
                Log.e(TAG, "Could not read saved keys, using defaults", e);
                keys = Keys.getDefault();
            }
        }
        totalScreens = (int)Math.ceil((double)keys.size() / (mRows * mKeysPerRow));

    }
    @Override public void onInitializeInterface() {
        setKeyboard();
    }

    private void setKeyboard() {
        if(mRows == 8) {
            keyboard = new KBoard(this, R.xml.eight_rows);
        } else if (mRows == 7) {
            keyboard = new KBoard(this, R.xml.seven_rows);
        } else if (mRows == 6) {
            keyboard = new KBoard(this, R.xml.six_rows);
        } else if (mRows == 5) {
            keyboard = new KBoard(this, R.xml.five_rows);
        } else if (mRows == 4) {
            keyboard = new KBoard(this, R.xml.four_rows);
        } else if (mRows == 1) {
            keyboard = new KBoard(this, R.xml.one_row);
        } else {
            keyboard = new KBoard(this, R.xml.normal);
        }
        keyboard.applyHeightScale(KeySize.heightScale(mKeySize));
        mKeys = keyboard.getKeys();
        resetKeyChars();
    }

    @Override
    public View onCreateInputView() {
        setKeyboard();
        kv = (KboardView)getLayoutInflater().inflate(R.layout.material_dark, null);
        kv.setKeyboard(keyboard);
        kv.setBackgroundColor(getResources().getColor(R.color.white));
        kv.setPreviewEnabled(false);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        keyboard.setImeOptions(this, attribute.imeOptions);
    }

    private UserKey userKeyForCode(int code) {
        if (code < -100 && code >= (-100 - mKeysPerScreen)) {
            int indOfKey = -(code + 101 - (mScreen * mKeysPerScreen));
            if (indOfKey >= 0 && indOfKey < keys.size()) {
                return keys.get(indOfKey);
            }
        }
        return null;
    }

    private String getKeyString(int code) {
        if (code == -6) {
            return (mScreen + 1) + "/" + totalScreens;
        } else if (code < -100 && code >= (-100 - mKeysPerScreen)) {
            UserKey userKey = userKeyForCode(code);
            if (userKey == null || userKey.text == null) {
                return "NO_VALUE";
            }
            return userKey.text;
        } else {
            return "";
        }
    }

    private List<String> keyTexts() {
        ArrayList<String> texts = new ArrayList<String>();
        for (UserKey key : keys) {
            texts.add(key.text == null ? "" : key.text);
        }
        return texts;
    }


    private void resetKeyChars() {
        String newString;
        for(Keyboard.Key key:mKeys) {
            newString = getKeyString(key.codes[0]);
            if (key instanceof KBoard.LatinKey) {
                UserKey userKey = userKeyForCode(key.codes[0]);
                ((KBoard.LatinKey) key).backgroundColor = userKey == null ? null : userKey.color;
            }
            if(newString == "NO_VALUE") {
                key.label = "";
                key.popupCharacters = "";
            }
            else if(newString != "") {
                key.label = newString;
                key.popupCharacters = newString;
            }
        }
        if(kv != null) {
            kv.invalidateAllKeys();
        }
    }

    private void playClick(){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
           am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.3f);
    }

    private void vibrate(){
        vib.vibrate(40);
    }

    @Override
    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        KCommands commands = new KCommands(
                this,
                ic,
                getCurrentInputEditorInfo(),
                keyTexts(),
                mAutoSpace,
                mPassiveAggressive);

        if(mSoundOnClick) {
            playClick();
        }

        if(mVibrateOnClick) {
            vibrate();
        }

        switch(primaryCode) {
            case -5: //backspace

                commands.d(1);
                break;
            case -6: //MAD
                switchScreens();
                break;
            case 10: //enter
               ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
               ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
               break;
            case -201: //subtype switcher
                switchIME();
                break;
            case -301: //settings
                Intent i = new Intent(this, PrefsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
            default:
                String keyString = getKeyString(primaryCode);
                if((keyString.startsWith("/") && keyString.contains("!"))) {
                    parseCommand(commands, keyString);
                } else {
                    sendReply(commands, keyString);
                }
                break;
            }
    }

    private void parseCommand(KCommands kc, String cmd) {
        String[] cmdSplit = cmd.split("!", 2);
        String cmdAction = cmdSplit[1];
        kc.e(1, cmdAction);
    }

    private void sendReply(KCommands commands, String key) {
        if(key == "NO_VALUE") {
            return;
        }
        commands.i(1, key);
        if(mAutoSend) {
            commands.s(1);
        }
    }

    public void switchIME() {
        //final String LATIN = "com.android.inputmethod.latin/.LatinIME";
// 'this' is an InputMethodService
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                final IBinder token = Objects.requireNonNull(this.getWindow().getWindow()).getAttributes().token;
                //imm.setInputMethod(token, LATIN);
                imm.switchToNextInputMethod(token, false);
            } catch (Throwable t) { // java.lang.NoSuchMethodError if API_level<11
                mInputMethodManager.showInputMethodPicker();
                Log.e(TAG, "cannot set the previous input method:");
                t.printStackTrace();
            }


    }
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
        switchScreens();
    }

    private void switchScreens() {
        mScreen = (mScreen < totalScreens - 1) ? mScreen + 1 : 0;
        resetKeyChars();
    }

    @Override
    public void swipeRight() {
        switchScreens();

    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        initPrefs();
        setKeyboard();
        if(keyboard != null && mKeys != null && kv != null) {
            kv.setKeyboard(keyboard);
            resetKeyChars();
        }
    }

    public static class Keys {
        public static ArrayList<UserKey> getDefault() {
            ArrayList<UserKey> defaultKeys = new ArrayList<UserKey>();
            defaultKeys.add(new UserKey("k"));
            defaultKeys.add(new UserKey("lol!"));
            defaultKeys.add(new UserKey("Good thanks, yourself?"));
            defaultKeys.add(new UserKey("thanks"));
            defaultKeys.add(new UserKey("👍"));
            defaultKeys.add(new UserKey("ಠ_ಠ"));
            defaultKeys.add(new UserKey("haha"));
            defaultKeys.add(new UserKey("¯\\_(ツ)_/¯"));
            defaultKeys.add(new UserKey("/exec!dt(!),e($0)"));
            defaultKeys.add(new UserKey("\uD83D\uDE12"));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                defaultKeys.add(new UserKey("/🅰🅱🅲!ds,fancy(darksquare)"));
                defaultKeys.add(new UserKey("/🄰🄱🄲!ds,fancy(square)"));
                defaultKeys.add(new UserKey("/🅐🅑🅒!ds,fancy(darkcircle)"));
                defaultKeys.add(new UserKey("/ⓐⓑⓒ!ds,fancy(circle)"));
                defaultKeys.add(new UserKey("/𝚊𝚋𝚌!ds,fancy(monospace)"));
                defaultKeys.add(new UserKey("/𝕒𝕓𝕔!ds,fancy(double)"));
                defaultKeys.add(new UserKey("/𝔞𝔟𝔠!ds,fancy(fancy)"));
                defaultKeys.add(new UserKey("/𝖆𝖇𝖈!ds,fancy(fancybold)"));
            }

            defaultKeys.add(new UserKey("/catfact!curl(https://kboard-api.glitch.me/catfact"));
            defaultKeys.add(new UserKey("ಥ_ಥ"));
            defaultKeys.add(new UserKey("thank you"));
            defaultKeys.add(new UserKey("sorry"));
            defaultKeys.add(new UserKey("( ͡° \u035Cʖ ͡°)"));
            defaultKeys.add(new UserKey("(╯°□°）╯︵ ┻━┻"));
            defaultKeys.add(new UserKey("hey!"));
            defaultKeys.add(new UserKey("cool"));

            defaultKeys.add(new UserKey("yes"));
            defaultKeys.add(new UserKey("no"));
            defaultKeys.add(new UserKey("maybe"));
            defaultKeys.add(new UserKey("don't mind"));
            defaultKeys.add(new UserKey("sure, whatever"));
            defaultKeys.add(new UserKey("xxx"));
            defaultKeys.add(new UserKey("Can't talk now. Speak later."));
            defaultKeys.add(new UserKey("I'll be late"));
            defaultKeys.add(new UserKey("okay"));

            defaultKeys.add(new UserKey("/Italicise Previous!dw,i(_$0_)"));
            defaultKeys.add(new UserKey("/Italicise Next!i(__),j"));
            defaultKeys.add(new UserKey("/Bolden Previous!dw,i(*$0*)"));
            defaultKeys.add(new UserKey("/Bolden Next!i(**),j"));
            defaultKeys.add(new UserKey("/Copy All!yy"));
            defaultKeys.add(new UserKey("/Paste!p"));
            defaultKeys.add(new UserKey("/-1w!b"));
            defaultKeys.add(new UserKey("/+1w!w"));
            defaultKeys.add(new UserKey("/Delete Word!dw"));
            return defaultKeys;
        }

        public static final String STORAGE_KEY = "userKeys-defaults";
    }
}

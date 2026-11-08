package com.adgad.kboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;


import java.util.List;
import java.util.Objects;

/**
 * Created by arjun on 12/06/16.
 */
public class KboardView extends KeyboardView {


    private final SharedPreferences sharedPref;
    private Canvas mCanvas;
    private final Paint mPaint = new Paint();
    private final Paint mBackground = new Paint();
    private final Paint mKey = new Paint();
    float[] hsv = new float[3];


    public KboardView(Context context, AttributeSet attrs) {

        super(context, attrs);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

    }


    private String ellipsize(String input, int maxChars) {
        String ellip = "...";
        if (input == null || input.length() <= maxChars
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, maxChars - ellip.length()).concat(ellip);
    }

    private boolean isLuckyKey(Keyboard.Key key) {
        return key.codes[0] == -99;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int height = getHeight();
        int width = getWidth();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(36);
        mPaint.setAntiAlias(true);
        mPaint.setSubpixelText(true);
        int bgColor = sharedPref.getInt("bgcolor", R.color.md_teal200);
        int borderColor = sharedPref.getInt("bgcolor", R.color.md_teal200);
        int pressedColor = sharedPref.getInt("pressedcolor", R.color.md_teal500);
        int textColor = sharedPref.getInt("textcolor", R.color.material_black);
        String keySize = sharedPref.getString(KeySize.PREF, KeySize.MEDIUM);
        float textScale = KeySize.textScale(keySize);
        float heightScale = KeySize.heightScale(keySize);
        int maxLabelChars = KeySize.maxLabelChars(keySize);
        int baseTextSize = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("fontsize", "36")));
        int textSize = Math.max(1, Math.round(baseTextSize * textScale));
        boolean spacing = sharedPref.getBoolean("spacing", false);
        int radius = 5;
        boolean isBold = sharedPref.getBoolean("textBold", true);

        //darken the border color
        Color.colorToHSV(borderColor, hsv);
        hsv[2] *= 0.85f; // value component
        borderColor = Color.HSVToColor(hsv);



        mPaint.setColor(textColor);

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            Integer customColor = null;
            if (key instanceof KBoard.LatinKey) {
                customColor = ((KBoard.LatinKey) key).backgroundColor;
            }
            boolean pressed = (key.pressed && !isLuckyKey(key)) || (isLuckyKey(key) && !key.pressed);
            int fill;
            if (customColor != null) {
                fill = pressed ? ColorContrast.darken(customColor, 0.78f) : customColor;
                mBackground.setColor(ColorContrast.darken(customColor, 0.85f));
            } else if (pressed) {
                fill = pressedColor;
                mBackground.setColor(borderColor);
            } else {
                fill = bgColor;
                mBackground.setColor(borderColor);
            }
            mKey.setColor(fill);
            int labelColor = customColor != null ? ColorContrast.textColorFor(fill) : textColor;

            int edgeGap = Math.round(20 * heightScale);
            int innerGap = Math.round(6 * heightScale);
            int sideGap = Math.round(5 * heightScale);
            int marginBottom = spacing ? (key.y + key.height == height) ? edgeGap : innerGap : 0;
            int marginTop = spacing ? (key.y == 0) ? edgeGap : innerGap : 0;
            int marginLeft = spacing ? (key.x == 0) ? edgeGap : sideGap : 0;
            int marginRight = spacing ?(key.x + key.width == width) ? edgeGap : sideGap : 0;

            canvas.drawRect(key.x, key.y, key.x + key.width, key.y + key.height, mBackground);

            if (spacing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(key.x + marginLeft, key.y + marginTop, key.x + key.width - marginRight, key.y + key.height - marginBottom, radius, radius, mKey);
            } else {
                canvas.drawRect(key.x + marginLeft, key.y + marginTop, key.x + key.width - marginRight, key.y + key.height - marginBottom, mKey);

            }
            mPaint.setColor(labelColor);
            if (key.icon != null) {
                int iconHalf = Math.round(30 * textScale);
                int iconAbove = Math.round(40 * textScale);
                int iconBelow = Math.round(20 * textScale);
                key.icon.setBounds(key.x + (key.width/2) - iconHalf, key.y + (key.height/2) - iconAbove, key.x + (key.width/2) + iconHalf, key.y + (key.height/2) + iconBelow);
                key.icon.setColorFilter(labelColor, PorterDuff.Mode.MULTIPLY);
                key.icon.draw(canvas);
            } else if(key.label != null) {
                String label = key.popupCharacters != null ? key.popupCharacters.toString() : key.label.toString();
                boolean isCommandKey = label.length() > 2 && label.charAt(0) == '/' && label.indexOf("!") > 0;
                if(key.codes[0] == 10) {
                    mPaint.setTextSize(Math.round((baseTextSize + 32) * textScale)); //enter icon is small so make it bigger
                } else {
                    mPaint.setTextSize(textSize);
                }
                if (isCommandKey) {
                    label = label.substring(1, label.indexOf("!"));
                    mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, isBold ? Typeface.ITALIC : Typeface.BOLD_ITALIC));
                    mPaint.setUnderlineText(true);
                } else {
                    mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, isBold ? Typeface.BOLD : Typeface.NORMAL));
                    mPaint.setUnderlineText(false);
                }
                canvas.drawText(ellipsize(label, maxLabelChars), key.x + ((key.width - marginRight) / 2) + marginLeft, key.y + ((key.height - marginBottom) / 2) + marginTop, mPaint);
            }
        }
    }

}

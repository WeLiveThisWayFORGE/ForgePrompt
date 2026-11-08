package com.adgad.kboard;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Offline macro guide. The text is docs/macro-help.md, packaged with the app.
 */
public class MacroHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.macro_help);
        setTitle(R.string.title_activity_macro_help);
        TextView body = findViewById(R.id.macro_help_body);
        body.setMovementMethod(LinkMovementMethod.getInstance());
        String markdown = readGuide();
        if (markdown == null) {
            body.setText(R.string.macro_help_missing);
            return;
        }
        body.setText(Html.fromHtml(MacroHelpHtml.toHtml(markdown), Html.FROM_HTML_MODE_LEGACY));
    }

    private String readGuide() {
        try (InputStream input = getAssets().open("macro-help.md");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }
            return text.toString();
        } catch (IOException e) {
            return null;
        }
    }
}

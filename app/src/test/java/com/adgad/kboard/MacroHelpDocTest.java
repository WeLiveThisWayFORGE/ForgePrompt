package com.adgad.kboard;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class MacroHelpDocTest {

    @Test
    public void guideDocumentsArbitraryMinuteOffsetsAndAttribution() throws Exception {
        Path guide = Paths.get("../docs/macro-help.md");
        String text = new String(Files.readAllBytes(guide), StandardCharsets.UTF_8);
        String html = MacroHelpHtml.toHtml(text);

        assertTrue(text.contains("ForgePrompt"));
        assertTrue(text.contains("https://github.com/adgad/kboard"));
        assertTrue(text.contains("GPL-3.0"));
        assertTrue(text.contains("com.adgad.kboard.plus"));
        assertTrue(!text.contains("We Live This Way"));
        assertTrue(text.contains("{time+5m}"));
        assertTrue(text.contains("{time+10m}"));
        assertTrue(text.contains("{time+20m}"));
        assertTrue(text.contains("{time+30m}"));
        assertTrue(text.contains("100000 minutes"));
        assertTrue(text.contains("{time+100001m}"));
        assertTrue(text.contains("does not run those hosts"));
        assertTrue(html.contains("I'm unable to access your property."));
        assertTrue(html.contains("adgad/kboard"));
    }
}
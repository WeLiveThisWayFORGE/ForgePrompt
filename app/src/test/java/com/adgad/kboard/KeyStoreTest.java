package com.adgad.kboard;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class KeyStoreTest {

    @Test
    public void loadsLegacyStringArrayWithoutColors() {
        String json = "[\"thanks\",\"lol!\",\"/exec!dt(!),e($0)\",\"¯\\\\_(ツ)_/¯\"]";
        List<UserKey> keys = KeyStore.fromJson(json);

        assertEquals(4, keys.size());
        assertEquals("thanks", keys.get(0).text);
        assertNull(keys.get(0).color);
        assertEquals("lol!", keys.get(1).text);
        assertEquals("/exec!dt(!),e($0)", keys.get(2).text);
        assertEquals("¯\\_(ツ)_/¯", keys.get(3).text);
        assertNull(keys.get(3).color);
    }

    @Test
    public void legacyKeysRoundTripWithoutInventingColors() {
        String legacy = "[\"k\",\"Good thanks, yourself?\",\"👍\"]";
        List<UserKey> once = KeyStore.fromJson(legacy);
        List<UserKey> twice = KeyStore.fromJson(KeyStore.toJson(once));

        assertEquals(3, twice.size());
        assertEquals("k", twice.get(0).text);
        assertEquals("Good thanks, yourself?", twice.get(1).text);
        assertEquals("👍", twice.get(2).text);
        assertNull(twice.get(0).color);
        assertNull(twice.get(1).color);
        assertNull(twice.get(2).color);
    }

    @Test
    public void roundTripsColorAndOmitsUnsetColor() {
        UserKey colored = new UserKey("thanks", 0xFF009688);
        UserKey plain = new UserKey("lol!", null);
        String json = KeyStore.toJson(Arrays.asList(colored, plain));
        List<UserKey> keys = KeyStore.fromJson(json);

        assertEquals(Integer.valueOf(0xFF009688), keys.get(0).color);
        assertEquals("thanks", keys.get(0).text);
        assertEquals("lol!", keys.get(1).text);
        assertNull(keys.get(1).color);
        assertFalse(json.contains("\"color\":null"));
        assertFalse(json.substring(json.indexOf("lol!")).contains("\"color\""));
    }

    @Test
    public void readsHexColorMixedWithLegacyStrings() {
        String json = "[\"plain\",{\"text\":\"blue\",\"color\":\"#FF2196F3\"},{\"text\":\"short\",\"color\":\"#F44336\"},{\"text\":\"nocolor\"}]";
        List<UserKey> keys = KeyStore.fromJson(json);

        assertEquals(4, keys.size());
        assertEquals("plain", keys.get(0).text);
        assertNull(keys.get(0).color);
        assertEquals(Integer.valueOf(0xFF2196F3), keys.get(1).color);
        assertEquals(Integer.valueOf(0xFFF44336), keys.get(2).color);
        assertEquals("nocolor", keys.get(3).text);
        assertNull(keys.get(3).color);
    }

    @Test
    public void keepsQuotesAndBackslashesInKeyText() {
        UserKey key = new UserKey("say \"hi\" \\ ok", 0xFF000000);
        List<UserKey> keys = KeyStore.fromJson(KeyStore.toJson(Arrays.asList(key)));

        assertEquals("say \"hi\" \\ ok", keys.get(0).text);
        assertEquals(Integer.valueOf(0xFF000000), keys.get(0).color);
    }

    @Test
    public void skipsUnknownEntriesAndIgnoresBadColors() {
        String json = "[1, {\"text\":\"kept\",\"color\":\"nope\"}, {\"color\":-16777216}]";
        List<UserKey> keys = KeyStore.fromJson(json);

        assertEquals(2, keys.size());
        assertEquals("kept", keys.get(0).text);
        assertNull(keys.get(0).color);
        assertEquals("", keys.get(1).text);
        assertEquals(Integer.valueOf(-16777216), keys.get(1).color);
    }

    @Test
    public void rejectsNonArray() {
        try {
            KeyStore.fromJson("{\"text\":\"x\"}");
            fail("object root should be rejected");
        } catch (IllegalArgumentException expected) {
            // import must not wipe keys when the file is the wrong shape
        }
    }

    @Test
    public void emptyInputIsAnEmptyList() {
        assertEquals(0, KeyStore.fromJson("").size());
        assertEquals(0, KeyStore.fromJson("   ").size());
        assertEquals(0, KeyStore.fromJson("[]").size());
        assertEquals(0, KeyStore.fromJson(null).size());
    }
}

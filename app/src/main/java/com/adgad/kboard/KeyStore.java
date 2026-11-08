package com.adgad.kboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the key list stored in SharedPreferences and in import/export files.
 *
 * <p>Older kboard versions saved a JSON array of strings. kboard Plus saves a JSON array of
 * objects, {@code {"text":"...","color":123}}, and still accepts the string form. A missing
 * color means the key uses the keyboard background.
 */
public final class KeyStore {

    private KeyStore() {
    }

    public static ArrayList<UserKey> fromJson(String json) {
        ArrayList<UserKey> keys = new ArrayList<UserKey>();
        if (json == null || json.trim().length() == 0) {
            return keys;
        }
        JsonElement root;
        try {
            root = new JsonParser().parse(json);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Keys file is not valid JSON", e);
        }
        if (root == null || !root.isJsonArray()) {
            throw new IllegalArgumentException("Keys file must be a JSON array");
        }
        for (JsonElement element : root.getAsJsonArray()) {
            if (element == null || element.isJsonNull()) {
                continue;
            }
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                keys.add(new UserKey(element.getAsString(), null));
            } else if (element.isJsonObject()) {
                keys.add(fromObject(element.getAsJsonObject()));
            }
        }
        return keys;
    }

    public static String toJson(List<UserKey> keys) {
        JsonArray array = new JsonArray();
        if (keys != null) {
            for (UserKey key : keys) {
                JsonObject object = new JsonObject();
                object.addProperty("text", key == null || key.text == null ? "" : key.text);
                if (key != null && key.color != null) {
                    object.addProperty("color", key.color);
                }
                array.add(object);
            }
        }
        return array.toString();
    }

    private static UserKey fromObject(JsonObject object) {
        String text = "";
        if (object.has("text") && !object.get("text").isJsonNull()) {
            text = object.get("text").getAsString();
        }
        Integer color = null;
        if (object.has("color") && !object.get("color").isJsonNull()) {
            color = readColor(object.get("color"));
        }
        return new UserKey(text, color);
    }

    private static Integer readColor(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        try {
            if (primitive.isNumber()) {
                return primitive.getAsInt();
            }
            if (primitive.isString()) {
                return parseColorString(primitive.getAsString());
            }
        } catch (RuntimeException ignored) {
            return null;
        }
        return null;
    }

    static Integer parseColorString(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        try {
            if (value.startsWith("#")) {
                if (value.length() == 7) {
                    return (int) (Long.parseLong(value.substring(1), 16) | 0xFF000000L);
                }
                if (value.length() == 9) {
                    return (int) Long.parseLong(value.substring(1), 16);
                }
                return null;
            }
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

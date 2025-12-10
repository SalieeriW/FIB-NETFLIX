package util;

import model.Video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON parser for practica5 video streaming platform.
 * Parses video API responses without external JSON libraries.
 */
public class JsonParser {

    public static Map<String, String> parseJsonObject(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null) {
            return result;
        }
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        StringBuilder token = new StringBuilder();
        String currentKey = null;
        boolean inQuotes = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                token.append(c);
            } else if (!inQuotes && c == ':') {
                currentKey = stripQuotes(token.toString().trim());
                token.setLength(0);
            } else if (!inQuotes && c == ',') {
                String value = token.toString().trim();
                if (currentKey != null) {
                    result.put(currentKey, stripQuotes(value));
                }
                token.setLength(0);
                currentKey = null;
            } else {
                token.append(c);
            }
        }
        if (currentKey != null) {
            result.put(currentKey, stripQuotes(token.toString().trim()));
        }
        return result;
    }

    public static Video parseVideo(String json) {
        Map<String, String> data = parseJsonObject(json);
        if (data.isEmpty()) {
            return null;
        }
        return new Video(
                parseInt(data.get("id")),
                data.getOrDefault("title", ""),
                data.getOrDefault("description", ""),
                data.getOrDefault("uploader", ""),
                data.getOrDefault("uploadDate", ""),
                parseIntegerOrNull(data.get("duration")),
                data.getOrDefault("originalFilename", ""),
                data.getOrDefault("filePath", ""),
                data.getOrDefault("processedPath", ""),
                data.getOrDefault("status", "UPLOADING"),
                parseInt(data.get("views"))
        );
    }

    public static List<Video> parseVideoArray(String json) {
        List<Video> videos = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return videos;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            Video video = parseVideo(trimmed);
            if (video != null) {
                videos.add(video);
            }
            return videos;
        }
        String content = trimmed.substring(1, trimmed.length() - 1);
        List<String> objects = splitObjects(content);
        for (String obj : objects) {
            Video video = parseVideo(obj);
            if (video != null) {
                videos.add(video);
            }
        }
        return videos;
    }

    private static List<String> splitObjects(String data) {
        List<String> objects = new ArrayList<>();
        if (data.isBlank()) {
            return objects;
        }
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuotes = false;

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == '"' && (i == 0 || data.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
            current.append(c);
            if (!inQuotes && depth == 0 && c == '}') {
                objects.add(current.toString());
                current.setLength(0);
                // skip comma and spaces
                while (i + 1 < data.length() && (data.charAt(i + 1) == ',' || Character.isWhitespace(data.charAt(i + 1)))) {
                    i++;
                }
            }
        }
        return objects;
    }

    public static String extractValue(String json, String key) {
        return parseJsonObject(json).get(key);
    }

    public static String extractString(String json, String key) {
        String value = extractValue(json, key);
        return value != null ? value : "";
    }

    public static boolean extractBoolean(String json, String key) {
        String value = extractValue(json, key);
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private static String stripQuotes(String text) {
        if (text == null) {
            return null;
        }
        text = text.trim();
        if (text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
            text = text.substring(1, text.length() - 1);
        }
        return text.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
    }

    private static int parseInt(String value) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static Integer parseIntegerOrNull(String value) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

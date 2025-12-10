package util;

import database.VideoDAO;

import java.util.List;

/**
 * JSON serializer for practica5 video streaming platform.
 * Provides manual JSON encoding to avoid relying on JSON-B automatic binding.
 */
public class JsonSerializer {

    public static String videoToJson(VideoDAO.Video video) {
        if (video == null) {
            return "null";
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(video.id).append(",");
        json.append("\"title\":").append(quote(video.title)).append(",");
        json.append("\"description\":").append(quote(video.description)).append(",");
        json.append("\"uploader\":").append(quote(video.uploader)).append(",");
        json.append("\"uploadDate\":").append(quote(video.uploadDate)).append(",");
        json.append("\"duration\":").append(video.duration != null ? video.duration : "null").append(",");
        json.append("\"originalFilename\":").append(quote(video.originalFilename)).append(",");
        json.append("\"filePath\":").append(quote(video.filePath)).append(",");
        json.append("\"processedPath\":").append(quote(video.processedPath)).append(",");
        json.append("\"status\":").append(quote(video.status)).append(",");
        json.append("\"views\":").append(video.views);
        json.append("}");
        return json.toString();
    }

    public static String videosToJson(List<VideoDAO.Video> videos) {
        if (videos == null) {
            return "[]";
        }
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < videos.size(); i++) {
            json.append(videoToJson(videos.get(i)));
            if (i < videos.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    public static String successResponse(String message) {
        return "{\"success\":true,\"message\":" + quote(message) + "}";
    }

    public static String loginResponse(boolean valid, String username) {
        return "{\"valid\":" + valid + ",\"username\":" + quote(username) + "}";
    }

    public static String loginResponseWithToken(String token, String username) {
        return "{\"token\":" + quote(token) + ",\"username\":" + quote(username) + "}";
    }

    public static String errorResponse(String error, String message, int status) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"error\":").append(quote(error)).append(",");
        json.append("\"message\":").append(quote(message)).append(",");
        json.append("\"status\":").append(status);
        json.append("}");
        return json.toString();
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        String hex = Integer.toHexString(c);
                        escaped.append("\\u");
                        for (int i = hex.length(); i < 4; i++) {
                            escaped.append('0');
                        }
                        escaped.append(hex);
                    } else {
                        escaped.append(c);
                    }
            }
        }
        escaped.append("\"");
        return escaped.toString();
    }
}

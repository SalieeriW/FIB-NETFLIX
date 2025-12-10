package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Simple HTTP client used by the JSP application to talk to the REST service.
 */
public class RestClient {

    private static final String BASE_URL = System.getenv("REST_API_URL") != null
        ? System.getenv("REST_API_URL")
        : "http://localhost:8080/practica5-rest-service/resources";

    public record RestResponse(int statusCode, String body) {}

    public static RestResponse postForm(String endpoint, Map<String, String> params) throws IOException {
        return postForm(endpoint, params, null);
    }

    public static RestResponse postForm(String endpoint, Map<String, String> params, String jwtToken) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");

        if (jwtToken != null && !jwtToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        conn.setDoOutput(true);

        StringBuilder form = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (form.length() > 0) form.append('&');
            form.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            form.append('=');
            form.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(form.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        String body = readBody(conn, status);
        conn.disconnect();
        return new RestResponse(status, body);
    }

    public static RestResponse get(String endpoint) throws IOException {
        return get(endpoint, null);
    }

    public static RestResponse get(String endpoint, String jwtToken) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (jwtToken != null && !jwtToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        int status = conn.getResponseCode();
        String body = readBody(conn, status);
        conn.disconnect();
        return new RestResponse(status, body);
    }

    private static String readBody(HttpURLConnection conn, int status) throws IOException {
        InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}

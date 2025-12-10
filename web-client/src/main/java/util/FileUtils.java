package util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static final String UPLOAD_DIR = "uploads";

    public static String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return "";
        }
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }

    public static String generateUniqueFilename(String username, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String prefix = username != null ? username : "user";
        return prefix + "_" + System.currentTimeMillis() + extension;
    }

    public static String getUploadDirectory(ServletContext context) {
        String configured = context != null ? context.getInitParameter("uploadsRoot") : null;
        String basePath = !isBlank(configured) ? configured : System.getenv("UPLOADS_ROOT");
        if (isBlank(basePath) && context != null) {
            basePath = context.getRealPath("/" + UPLOAD_DIR);
        }
        if (isBlank(basePath)) {
            basePath = System.getProperty("java.io.tmpdir") + File.separator + "video_uploads";
        }
        createDirectories(basePath);
        return basePath;
    }

    private static void createDirectories(String basePath) {
        if (isBlank(basePath)) return;
        try {
            Files.createDirectories(Path.of(basePath));
        } catch (Exception e) {
            System.err.println("Could not create upload directory " + basePath + ": " + e.getMessage());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

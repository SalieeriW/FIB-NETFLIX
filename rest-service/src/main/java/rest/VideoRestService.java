package rest;

import database.VideoDAO;
import service.TranscodingService;
import util.JsonSerializer;
import java.io.File;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

/**
 * REST service for video management.
 * Provides endpoints for video CRUD operations and streaming.
 */
@Path("/video")
@Produces(MediaType.APPLICATION_JSON)
public class VideoRestService {

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registerVideo(@FormParam("title") String title,
                                  @FormParam("description") String description,
                                  @FormParam("filename") String filename,
                                  @FormParam("filePath") String filePath,
                                  @FormParam("duration") String durationStr,
                                  @Context HttpServletRequest request) {

        String uploader = (String) request.getAttribute("jwtUser");
        if (isBlank(uploader)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        if (isBlank(title)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Title is required", 400))
                    .build();
        }

        String uploadDate = LocalDate.now().toString();
        String safeDescription = description != null ? description : "";
        String safeFilename = filename != null ? filename : "";
        String safeFilePath = filePath != null ? filePath : "";
        Integer duration = parseDuration(durationStr);

        VideoDAO.Video video = VideoDAO.insertVideo(
                title,
                safeDescription,
                uploader,
                uploadDate,
                safeFilename,
                duration,
                safeFilePath,
                "UPLOADING"
        );

        if (video == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to register video", 500))
                    .build();
        }

        String fullInputPath = safeFilePath;
        String outputDir = "/tmp/vidstream/videos/processed/" + video.id;

        TranscodingService.transcodeVideoAsync(video.id, fullInputPath, outputDir);

        return Response.status(Response.Status.CREATED)
                .entity(JsonSerializer.videoToJson(video))
                .build();
    }

    @POST
    @Path("/modify")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response modifyVideo(@FormParam("id") String idStr,
                                @FormParam("title") String title,
                                @FormParam("description") String description,
                                @Context HttpServletRequest request) {

        String uploader = (String) request.getAttribute("jwtUser");
        if (isBlank(uploader)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        int id = parseId(idStr);
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Valid id is required", 400))
                    .build();
        }

        VideoDAO.Video existing = VideoDAO.getVideoById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video does not exist", 404))
                    .build();
        }
        if (!uploader.equals(existing.uploader)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(JsonSerializer.errorResponse("Forbidden", "Only owner can modify the video", 403))
                    .build();
        }

        if (isBlank(title)) title = existing.title;
        if (isBlank(description)) description = existing.description;

        VideoDAO.Video updated = VideoDAO.updateVideo(id, uploader, title, description);

        if (updated == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to modify video", 500))
                    .build();
        }
        return Response.ok(JsonSerializer.videoToJson(updated)).build();
    }

    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteVideo(@FormParam("id") String idStr,
                                @Context HttpServletRequest request) {
        String uploader = (String) request.getAttribute("jwtUser");
        if (isBlank(uploader)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        int id = parseId(idStr);
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Valid id is required", 400))
                    .build();
        }

        VideoDAO.Video existing = VideoDAO.getVideoById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video does not exist", 404))
                    .build();
        }
        if (!uploader.equals(existing.uploader)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(JsonSerializer.errorResponse("Forbidden", "Only owner can delete the video", 403))
                    .build();
        }

        boolean deleted = VideoDAO.deleteVideo(id, uploader);
        if (!deleted) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to delete video", 500))
                    .build();
        }
        return Response.ok(JsonSerializer.successResponse("Video deleted")).build();
    }

    @GET
    @Path("/search/{id}")
    public Response searchById(@PathParam("id") int id) {
        VideoDAO.Video video = VideoDAO.getVideoById(id);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not found", 404))
                    .build();
        }
        return Response.ok(JsonSerializer.videoToJson(video)).build();
    }

    @GET
    @Path("/status/{id}")
    public Response getStatus(@PathParam("id") int id) {
        VideoDAO.Video video = VideoDAO.getVideoById(id);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not found", 404))
                    .build();
        }
        String json = String.format("{\"id\":%d,\"status\":\"%s\",\"duration\":%s}",
                video.id, video.status, video.duration != null ? video.duration : "null");
        return Response.ok(json).build();
    }

    @GET
    @Path("/searchTitle/{title}")
    public Response searchByTitle(@PathParam("title") String title) {
        List<VideoDAO.Video> videos = VideoDAO.searchByTitle(title);
        return Response.ok(JsonSerializer.videosToJson(videos)).build();
    }

    @GET
    @Path("/searchUploader/{uploader}")
    public Response searchByUploader(@PathParam("uploader") String uploader) {
        List<VideoDAO.Video> videos = VideoDAO.searchByUploader(uploader);
        return Response.ok(JsonSerializer.videosToJson(videos)).build();
    }

    @GET
    @Path("/searchQuery/{query}")
    public Response search(@PathParam("query") String query) {
        List<VideoDAO.Video> videos = VideoDAO.searchByQuery(query);
        return Response.ok(JsonSerializer.videosToJson(videos)).build();
    }

    @GET
    @Path("/list")
    public Response listAll() {
        List<VideoDAO.Video> videos = VideoDAO.searchAll();
        return Response.ok(JsonSerializer.videosToJson(videos)).build();
    }

    @GET
    @Path("/thumbnail/{id}")
    @Produces("image/jpeg")
    public Response getThumbnail(@PathParam("id") int id) {
        VideoDAO.Video video = VideoDAO.getVideoById(id);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not found", 404))
                    .build();
        }

        // Check if processed path exists
        if (video.processedPath == null || video.processedPath.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not processed yet", 404))
                    .build();
        }

        String thumbnailPath = video.processedPath + java.io.File.separator + "thumbnail.jpg";
        java.io.File thumbnailFile = new java.io.File(thumbnailPath);

        if (!thumbnailFile.exists()) {
            // Try to generate thumbnail if it doesn't exist
            if (video.filePath != null && !video.filePath.isEmpty()) {
                String outputDir = video.processedPath;
                TranscodingService.generateThumbnail(video.filePath, outputDir, id);
                // Check again after generation
                if (!thumbnailFile.exists()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(JsonSerializer.errorResponse("Not Found", "Thumbnail not available", 404))
                            .build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(JsonSerializer.errorResponse("Not Found", "Thumbnail not available", 404))
                        .build();
            }
        }

        try {
            java.nio.file.Path path = java.nio.file.Paths.get(thumbnailPath);
            byte[] imageBytes = java.nio.file.Files.readAllBytes(path);
            
            return Response.ok(imageBytes)
                    .type("image/jpeg")
                    .header("Cache-Control", "public, max-age=3600")
                    .header("Content-Length", String.valueOf(imageBytes.length))
                    .build();
        } catch (java.io.IOException e) {
            System.err.println("Error reading thumbnail: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Error reading thumbnail", 500))
                    .build();
        }
    }

    @POST
    @Path("/updateStatus")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateStatus(@FormParam("id") String idStr,
                                 @FormParam("status") String status,
                                 @Context HttpServletRequest request) {
        String uploader = (String) request.getAttribute("jwtUser");
        if (isBlank(uploader)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        int id = parseId(idStr);
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Valid id is required", 400))
                    .build();
        }

        if (isBlank(status)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Status is required", 400))
                    .build();
        }

        // Validate status value
        if (!isValidStatus(status)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request",
                        "Invalid status. Must be one of: UPLOADING, PROCESSING, READY, ERROR", 400))
                    .build();
        }

        // Verify ownership before allowing status change
        VideoDAO.Video existingVideo = VideoDAO.getVideoById(id);
        if (existingVideo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not found", 404))
                    .build();
        }

        if (!uploader.equals(existingVideo.uploader)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(JsonSerializer.errorResponse("Forbidden", "You can only modify your own videos", 403))
                    .build();
        }

        boolean updated = VideoDAO.updateStatus(id, status);
        if (!updated) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to update status", 500))
                    .build();
        }

        VideoDAO.Video video = VideoDAO.getVideoById(id);

        if ("UPLOADING".equals(status) && video != null && video.filePath != null) {
            String fullInputPath = video.filePath;
            String outputDir = "/tmp/vidstream/videos/processed/" + video.id;
            TranscodingService.transcodeVideoAsync(video.id, fullInputPath, outputDir);
        }

        return Response.ok(JsonSerializer.videoToJson(video)).build();
    }

    @POST
    @Path("/incrementViews/{id}")
    public Response incrementViews(@PathParam("id") int id) {
        VideoDAO.Video video = VideoDAO.getVideoById(id);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not found", 404))
                    .build();
        }

        boolean updated = VideoDAO.incrementViews(id);
        if (!updated) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to increment views", 500))
                    .build();
        }

        VideoDAO.Video updatedVideo = VideoDAO.getVideoById(id);
        return Response.ok(JsonSerializer.videoToJson(updatedVideo)).build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int parseId(String idStr) {
        if (isBlank(idStr)) {
            return -1;
        }
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private Integer parseDuration(String durationStr) {
        if (isBlank(durationStr)) {
            return null;
        }
        try {
            return Integer.parseInt(durationStr);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isValidStatus(String status) {
        if (status == null) return false;
        return status.equals("UPLOADING") ||
               status.equals("PROCESSING") ||
               status.equals("READY") ||
               status.equals("ERROR");
    }

    @GET
    @Path("/stream/{id}")
    @Produces("application/dash+xml")
    public Response streamVideo(@PathParam("id") int id, @Context HttpServletRequest request) {
        VideoDAO.Video video = VideoDAO.getVideoById(id);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Video not found", 404))
                    .build();
        }

        if (!"READY".equals(video.status)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Ready", "Video is still processing", 404))
                    .build();
        }

        if (video.processedPath == null || video.processedPath.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Processed video path not found", 404))
                    .build();
        }

        String manifestPath = video.processedPath + "/manifest.mpd";
        java.io.File manifestFile = new java.io.File(manifestPath);

        if (!manifestFile.exists()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Manifest file not found", 404))
                    .build();
        }

        try {
            // Read manifest file
            String manifestContent = java.nio.file.Files.readString(manifestFile.toPath());

            // Get base URL for segments
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            String baseUrl = scheme + "://" + serverName;
            if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
                baseUrl += ":" + serverPort;
            }
            baseUrl += contextPath + "/resources/video/segment/" + id + "/";

            // Insert BaseURL into manifest after <Period> tag
            String modifiedManifest = manifestContent.replaceFirst(
                "(<Period[^>]*>)",
                "$1\n    <BaseURL>" + baseUrl + "</BaseURL>"
            );

            return Response.ok(modifiedManifest)
                    .type("application/dash+xml")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (java.io.IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Error reading manifest", 500))
                    .build();
        }
    }

    @GET
    @Path("/segment/{id}/{filename}")
    public Response getSegment(@PathParam("id") int id,
                              @PathParam("filename") String filename,
                              @Context HttpServletRequest request) {
        VideoDAO.Video video = VideoDAO.getVideoById(id);
        if (video == null || video.processedPath == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String segmentPath = video.processedPath + "/" + filename;
        java.io.File segmentFile = new java.io.File(segmentPath);

        if (!segmentFile.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String mimeType = filename.endsWith(".m4s") ? "video/iso.segment" : "application/octet-stream";

        return Response.ok(segmentFile)
                .type(mimeType)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
}

package rest;

import database.CourseDAO;
import database.CourseNotesDAO;
import database.TranscriptDAO;
import database.VideoDAO;
import service.CourseProcessingService;
import util.JsonSerializer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * REST service for multilingual course management.
 * Integrates with Python microservices for RAG functionality.
 */
@Path("/course")
@Produces(MediaType.APPLICATION_JSON)
public class CourseRestService {

    private static final String PYTHON_SERVICE_URL =
        System.getenv("PYTHON_SERVICE_URL") != null ?
        System.getenv("PYTHON_SERVICE_URL") : "http://localhost:5001";

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createCourse(@FormParam("title") String title,
                                @FormParam("language") String language,
                                @FormParam("videoId") String videoIdStr,
                                @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        if (isBlank(title)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Title is required", 400))
                    .build();
        }

        Integer videoId = null;
        if (!isBlank(videoIdStr)) {
            try {
                videoId = Integer.parseInt(videoIdStr);
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(JsonSerializer.errorResponse("Bad Request", "Invalid video ID", 400))
                        .build();
            }
        }

        String primaryLang = language != null ? language : "en";
        CourseDAO.Course course = CourseDAO.insertCourse(title, primaryLang, videoId, "CREATED");

        if (course == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to create course", 500))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(courseToJson(course))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getCourse(@PathParam("id") int courseId,
                             @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        CourseDAO.Course course = CourseDAO.getCourseById(courseId);

        if (course == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Course not found", 404))
                    .build();
        }

        return Response.ok(courseToJson(course)).build();
    }

    @GET
    @Path("/list")
    public Response listCourses(@Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        var courses = CourseDAO.getAllCourses();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            json.append(courseToJson(courses.get(i)));
            if (i < courses.size() - 1) json.append(",");
        }
        json.append("]");

        return Response.ok(json.toString()).build();
    }

    @GET
    @Path("/{id}/notes")
    public Response getCourseNotes(@PathParam("id") int courseId,
                                  @QueryParam("lang") String language,
                                  @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        String lang = language != null ? language : "en";
        CourseNotesDAO.CourseNotes notes = CourseNotesDAO.getNotesByCourseId(courseId);

        String content = "";
        String generatedAt = "";

        if (notes != null) {
            content = switch(lang) {
                case "es" -> notes.notesEs;
                case "ca" -> notes.notesCa;
                default -> notes.notesEn;
            };

            // Handle null content
            if (content == null) {
                content = "";
            }

            // Handle null generatedAt - convert Timestamp to String
            generatedAt = notes.generatedAt != null ? notes.generatedAt.toString() : "";
        }

        String json = String.format(
            "{\"courseId\":%d,\"language\":\"%s\",\"notes\":\"%s\",\"generatedAt\":\"%s\"}",
            courseId, lang, escapeJson(content), escapeJson(generatedAt)
        );

        return Response.ok(json).build();
    }

    @POST
    @Path("/{id}/generate-notes")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response generateNotes(@PathParam("id") int courseId,
                                 @FormParam("language") String language,
                                 @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        String lang = language != null ? language : "en";

        try {
            String pythonResponse = callPythonService(
                "/api/rag/generate_notes",
                String.format("{\"course_id\":%d,\"language\":\"%s\",\"include_sources\":true}", courseId, lang)
            );

            // Parse the response and save notes to database
            String notes = extractNotesFromResponse(pythonResponse);
            if (notes != null) {
                // Check if notes record exists
                CourseNotesDAO.CourseNotes existingNotes = CourseNotesDAO.getNotesByCourseId(courseId);
                
                if (existingNotes == null) {
                    // Create new notes record
                    String notesEn = lang.equals("en") ? notes : null;
                    String notesEs = lang.equals("es") ? notes : null;
                    String notesCa = lang.equals("ca") ? notes : null;
                    CourseNotesDAO.insertNotes(courseId, notesEn, notesEs, notesCa);
                } else {
                    // Update existing notes record
                    CourseNotesDAO.updateNotes(courseId, lang, notes);
                }
            }

            return Response.ok(pythonResponse).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", e.getMessage(), 500))
                    .build();
        }
    }

    @POST
    @Path("/{id}/process")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response processCourse(@PathParam("id") int courseId,
                                 @FormParam("pdfPath") String pdfPath,
                                 @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        CourseDAO.Course course = CourseDAO.getCourseById(courseId);
        if (course == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Course not found", 404))
                    .build();
        }

        if (course.videoId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Course has no associated video", 400))
                    .build();
        }

        VideoDAO.Video video = VideoDAO.getVideoById(course.videoId);
        if (video == null || video.filePath == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Video file not found", 400))
                    .build();
        }

        CourseProcessingService.processCourseAsync(
            courseId,
            video.filePath,
            pdfPath,
            course.primaryLanguage
        );

        return Response.ok("{\"message\":\"Course processing started\",\"courseId\":" + courseId + "}").build();
    }

    @POST
    @Path("/{id}/chat")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response chat(@PathParam("id") int courseId,
                        @FormParam("question") String question,
                        @FormParam("language") String language,
                        @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        if (isBlank(question)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Question is required", 400))
                    .build();
        }

        try {
            String langParam = language != null ? String.format(",\"language\":\"%s\"", language) : "";
            String pythonResponse = callPythonService(
                "/api/chat",
                String.format("{\"course_id\":%d,\"question\":\"%s\"%s}",
                    courseId, escapeJson(question), langParam)
            );

            return Response.ok(pythonResponse).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", e.getMessage(), 500))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCourse(@PathParam("id") int courseId,
                                @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        CourseDAO.Course course = CourseDAO.getCourseById(courseId);
        if (course == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(JsonSerializer.errorResponse("Not Found", "Course not found", 404))
                    .build();
        }

        boolean deleted = CourseDAO.deleteCourse(courseId);
        if (!deleted) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", "Unable to delete course", 500))
                    .build();
        }

        return Response.ok(JsonSerializer.successResponse("Course deleted successfully")).build();
    }

    @GET
    @Path("/{id}/search")
    public Response searchCourseContent(@PathParam("id") int courseId,
                                       @QueryParam("q") String query,
                                       @QueryParam("n") String nResultsStr,
                                       @QueryParam("lang") String languageFilter,
                                       @Context HttpServletRequest request) {

        String user = (String) request.getAttribute("jwtUser");
        if (isBlank(user)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(JsonSerializer.errorResponse("Unauthorized", "Valid token required", 401))
                    .build();
        }

        if (isBlank(query)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(JsonSerializer.errorResponse("Bad Request", "Query parameter 'q' is required", 400))
                    .build();
        }

        try {
            int nResults = 5;
            if (nResultsStr != null && !nResultsStr.isEmpty()) {
                try {
                    nResults = Integer.parseInt(nResultsStr);
                } catch (NumberFormatException e) {
                    // Use default
                }
            }

            String urlStr = PYTHON_SERVICE_URL + "/api/embedding/search?course_id=" + courseId +
                    "&query=" + java.net.URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&n_results=" + nResults;
            if (languageFilter != null && !languageFilter.isEmpty()) {
                urlStr += "&language_filter=" + languageFilter;
            }

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Python service returned error: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(JsonSerializer.errorResponse("Internal Server Error", e.getMessage(), 500))
                    .build();
        }
    }

    private String callPythonService(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(PYTHON_SERVICE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Python service returned error: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }

        return response.toString();
    }

    private String courseToJson(CourseDAO.Course course) {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"primaryLanguage\":\"%s\",\"detectedLanguages\":\"%s\"," +
            "\"videoId\":%s,\"createdAt\":\"%s\",\"status\":\"%s\"}",
            course.id, escapeJson(course.title), course.primaryLanguage,
            course.detectedLanguages != null ? escapeJson(course.detectedLanguages) : "",
            course.videoId != null ? course.videoId : "null",
            course.createdAt, course.status
        );
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r");
    }

    private String extractNotesFromResponse(String jsonResponse) {
        try {
            // Simple JSON parsing to extract "notes" field
            // Expected format: {"success":true,"notes":"...","language":"en",...}
            int notesStart = jsonResponse.indexOf("\"notes\":\"");
            if (notesStart == -1) {
                return null;
            }
            
            notesStart += 9; // Skip past "notes":"
            int notesEnd = notesStart;
            boolean escaped = false;
            
            while (notesEnd < jsonResponse.length()) {
                char c = jsonResponse.charAt(notesEnd);
                if (escaped) {
                    escaped = false;
                    notesEnd++;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    notesEnd++;
                    continue;
                }
                if (c == '"') {
                    break;
                }
                notesEnd++;
            }
            
            if (notesEnd >= jsonResponse.length()) {
                return null;
            }
            
            String notes = jsonResponse.substring(notesStart, notesEnd);
            // Unescape JSON string
            notes = notes.replace("\\\"", "\"")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                        .replace("\\\\", "\\");
            
            return notes;
        } catch (Exception e) {
            System.err.println("Error extracting notes from response: " + e.getMessage());
            return null;
        }
    }
}

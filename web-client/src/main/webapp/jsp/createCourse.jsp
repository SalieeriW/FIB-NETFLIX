<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="util.JwtSessionHelper" %>
<%
    if (!JwtSessionHelper.isLoggedIn(session)) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    String videosJson = (String) request.getAttribute("videosJson");
    if (videosJson == null) videosJson = "[]";
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Course - VidStream</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <style>
        .form-group {
            margin-bottom: var(--space-6);
        }
        
        .form-group label {
            display: block;
            margin-bottom: var(--space-2);
            color: var(--text-primary);
            font-weight: 500;
        }
        
        .form-group input[type="text"],
        .form-group select {
            width: 100%;
            padding: var(--space-3) var(--space-4);
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: 6px;
            color: var(--text-primary);
            font-size: 1rem;
            transition: var(--transition-fast);
        }
        
        .form-group input[type="text"]:focus,
        .form-group select:focus {
            outline: none;
            border-color: var(--accent);
        }
        
        .form-actions {
            display: flex;
            gap: var(--space-4);
            margin-top: var(--space-8);
        }
        
        .form-actions .btn {
            flex: 1;
        }
    </style>
</head>
<body>
    <a href="${pageContext.request.contextPath}/listCourses" class="back-button" aria-label="Back to courses">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
    </a>
    <jsp:include page="navbar.jsp" />

    <div class="main-content">
        <div class="container container-narrow">
            <h1 class="text-center mb-8">Create New Course</h1>

            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    <% if (error != null && !error.isEmpty()) { %>
                        createToast('<%= error.replace("'", "\\'") %>', 'error');
                    <% } %>
                });
            </script>

            <form action="${pageContext.request.contextPath}/createCourse" method="post" id="createCourseForm">
                <div class="form-group">
                    <label for="title">Course Title *</label>
                    <input type="text"
                           id="title"
                           name="title"
                           required
                           maxlength="255"
                           placeholder="Enter course title">
                </div>

                <div class="form-group">
                    <label for="language">Primary Language *</label>
                    <select id="language" name="language" required>
                        <option value="en">English</option>
                        <option value="es">Español</option>
                        <option value="ca">Català</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="videoId">Associated Video (Optional)</label>
                    <select id="videoId" name="videoId">
                        <option value="">-- No video --</option>
                    </select>
                    <small style="color: var(--text-secondary); margin-top: var(--space-2); display: block;">
                        Select a video to associate with this course. The video will be processed for transcription and note generation.
                    </small>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/listCourses" class="btn btn-secondary">Cancel</a>
                    <button type="submit" class="btn btn-primary">Create Course</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        // Populate video dropdown
        const videos = <%= videosJson %>;
        const videoSelect = document.getElementById('videoId');
        
        videos.forEach(video => {
            const option = document.createElement('option');
            option.value = video.id;
            option.textContent = video.title + (video.uploader ? ' - ' + video.uploader : '');
            videoSelect.appendChild(option);
        });
    </script>
</body>
</html>


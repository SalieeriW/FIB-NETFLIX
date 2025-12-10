<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="util.JwtSessionHelper" %>
<%
    if (!JwtSessionHelper.isLoggedIn(session)) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Courses - VidStream</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <style>
        .courses-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-8);
        }
        
        .course-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: var(--space-6);
            margin-top: var(--space-8);
        }
        
        .course-card {
            background: var(--bg-card);
            border-radius: 8px;
            padding: var(--space-6);
            border: 1px solid var(--border);
            transition: var(--transition);
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }
        
        .course-card:hover {
            transform: translateY(-4px);
            border-color: var(--border-hover);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
        }
        
        .course-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: var(--space-3);
            line-height: 1.3;
        }
        
        .course-meta {
            display: flex;
            flex-direction: column;
            gap: var(--space-2);
            margin-bottom: var(--space-4);
        }
        
        .course-meta-item {
            font-size: 0.875rem;
            color: var(--text-secondary);
            display: flex;
            align-items: center;
            gap: var(--space-2);
        }
        
        .course-status {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            border-radius: 12px;
            font-size: 0.75rem;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .status-READY {
            background: rgba(16, 185, 129, 0.2);
            color: #10b981;
            border: 1px solid rgba(16, 185, 129, 0.3);
        }
        
        .status-PROCESSING {
            background: rgba(245, 158, 11, 0.2);
            color: #f59e0b;
            border: 1px solid rgba(245, 158, 11, 0.3);
        }
        
        .status-CREATED {
            background: rgba(59, 130, 246, 0.2);
            color: #3b82f6;
            border: 1px solid rgba(59, 130, 246, 0.3);
        }
        
        .status-ERROR {
            background: rgba(239, 68, 68, 0.2);
            color: #ef4444;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }
        
        .course-actions {
            display: flex;
            gap: var(--space-2);
            margin-top: var(--space-4);
            flex-wrap: wrap;
        }
        
        .btn-course {
            flex: 1;
            min-width: 100px;
            padding: var(--space-2) var(--space-3);
            font-size: 0.875rem;
        }
        
        .empty-state {
            text-align: center;
            padding: var(--space-16);
            color: var(--text-secondary);
        }
        
        .empty-state h3 {
            margin-bottom: var(--space-4);
            color: var(--text-primary);
        }
    </style>
</head>
<body>
    <jsp:include page="navbar.jsp" />

    <div class="main-content">
    <div class="container">
            <div class="courses-header">
            <h1>My Courses</h1>
                <a href="${pageContext.request.contextPath}/createCourse" class="btn btn-primary">
                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    New Course
                </a>
        </div>

        <div class="course-grid" id="courseGrid">
                <div class="empty-state">
            <p>Loading courses...</p>
                </div>
            </div>
        </div>
    </div>

    <script>
        const apiBaseUrl = '<%= System.getenv("REST_API_URL") != null ? System.getenv("REST_API_URL") : "http://localhost:8080/practica5-rest-service/resources" %>';
        const token = '<%= JwtSessionHelper.getToken(session) != null ? JwtSessionHelper.getToken(session) : "" %>';
        const contextPath = '<%= request.getContextPath() %>';

        async function loadCourses() {
            try {
                const response = await fetch(apiBaseUrl + '/course/list', {
                    headers: { 'Authorization': 'Bearer ' + token }
                });

                if (!response.ok) {
                    throw new Error('Failed to load courses');
                }

                const courses = await response.json();
                console.log('Loaded courses:', courses);
                displayCourses(courses);

            } catch (error) {
                console.error('Error:', error);
                document.getElementById('courseGrid').innerHTML = 
                    '<div class="empty-state"><h3>Error loading courses</h3><p>' + escapeHtml(error.message) + '</p></div>';
            }
        }

        function displayCourses(courses) {
            const grid = document.getElementById('courseGrid');

            if (courses.length === 0) {
                grid.innerHTML = `
                    <div class="empty-state">
                        <h3>No courses yet</h3>
                        <p>Create your first course to get started with AI-powered learning!</p>
                    </div>
                `;
                return;
            }

            grid.innerHTML = courses.map((course, index) => {
                let actionsHtml = '';
                const courseId = course.id || course.courseId || null;
                const courseStatus = course.status || 'CREATED';
                
                console.log(`Course ${index}:`, course, 'ID:', courseId, 'Status:', courseStatus);
                
                if (!courseId) {
                    console.error('Course missing ID:', course);
                    return '';
                }
                
                // Ensure courseId is a string for URL construction
                const courseIdStr = String(courseId);
                
                let deleteBtn = '<button class="btn btn-danger btn-course delete-course-btn" data-course-id="' + String(courseId) + '" title="Delete course">' +
                    '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<polyline points="3 6 5 6 21 6"></polyline>' +
                    '<path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>' +
                    '</svg></button>';
                
                if (courseStatus === 'READY') {
                    // Validate courseIdStr before using it
                    if (!courseIdStr || courseIdStr === 'null' || courseIdStr === 'undefined') {
                        console.error('Invalid courseId for course:', course);
                        return '';
                    }
                    
                    actionsHtml = '<a href="' + contextPath + '/viewNotes?id=' + encodeURIComponent(courseIdStr) + '" class="btn btn-primary btn-course">' +
                        '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                        '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>' +
                        '<polyline points="14 2 14 8 20 8"></polyline>' +
                        '<line x1="16" y1="13" x2="8" y2="13"></line>' +
                        '<line x1="16" y1="17" x2="8" y2="17"></line>' +
                        '<polyline points="10 9 9 9 8 9"></polyline>' +
                        '</svg>Notes</a>' +
                        '<a href="' + contextPath + '/chat?id=' + encodeURIComponent(courseIdStr) + '" class="btn btn-secondary btn-course">' +
                        '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                        '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>' +
                        '</svg>Chat</a>' +
                        deleteBtn;
                } else if (courseStatus === 'CREATED') {
                    console.log('Creating process button for course ID:', courseId);
                    actionsHtml = '<button class="btn btn-primary btn-course process-course-btn" style="width: 100%;" data-course-id="' + courseIdStr + '">' +
                        '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                        '<polygon points="5 3 19 12 5 21 5 3"></polygon>' +
                        '</svg>Process</button>' +
                        deleteBtn;
                } else if (courseStatus === 'PROCESSING') {
                    actionsHtml = '<div class="course-meta-item" style="width: 100%; justify-content: center;">' +
                        '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="animation: spin 1s linear infinite;">' +
                        '<line x1="12" y1="2" x2="12" y2="6"></line>' +
                        '<line x1="12" y1="18" x2="12" y2="22"></line>' +
                        '<line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>' +
                        '<line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>' +
                        '<line x1="2" y1="12" x2="6" y2="12"></line>' +
                        '<line x1="18" y1="12" x2="22" y2="12"></line>' +
                        '<line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>' +
                        '<line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>' +
                        '</svg>Processing...</div>' +
                        deleteBtn;
                } else {
                    // ERROR or other status
                    actionsHtml = '<div class="course-meta-item" style="width: 100%; justify-content: center; color: var(--error);">' +
                        '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                        '<circle cx="12" cy="12" r="10"></circle>' +
                        '<line x1="12" y1="8" x2="12" y2="12"></line>' +
                        '<line x1="12" y1="16" x2="12.01" y2="16"></line>' +
                        '</svg>Error: ' + escapeHtml(courseStatus) + '</div>' +
                        deleteBtn;
                }
                
                return '<div class="course-card" style="animation: fadeInUp 0.4s ease ' + (index * 0.05) + 's both;">' +
                    '<div class="course-title">' + escapeHtml(course.title) + '</div>' +
                    '<div class="course-meta">' +
                    '<div class="course-meta-item">' +
                    '<span class="course-status status-' + courseStatus + '">' + courseStatus + '</span>' +
                    '</div>' +
                    '<div class="course-meta-item">' +
                    '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<circle cx="12" cy="12" r="10"></circle>' +
                    '<polyline points="12 6 12 12 16 14"></polyline>' +
                    '</svg>' + getLanguageName(course.primaryLanguage) +
                    '</div>' +
                    '<div class="course-meta-item">' +
                    '<svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>' +
                    '<line x1="16" y1="2" x2="16" y2="6"></line>' +
                    '<line x1="8" y1="2" x2="8" y2="6"></line>' +
                    '<line x1="3" y1="10" x2="21" y2="10"></line>' +
                    '</svg>' + formatDate(course.createdAt) +
                    '</div>' +
                    '</div>' +
                    '<div class="course-actions">' + actionsHtml + '</div>' +
                    '</div>';
            }).join('');
            
            // Attach event listeners to process buttons
            setTimeout(() => {
                const processButtons = document.querySelectorAll('.process-course-btn');
                console.log('Found process buttons:', processButtons.length);
                processButtons.forEach((btn, index) => {
                    const btnCourseId = btn.getAttribute('data-course-id');
                    console.log(`Button ${index} - data-course-id:`, btnCourseId, 'Type:', typeof btnCourseId);
                    if (btnCourseId) {
                        btn.addEventListener('click', (e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            const id = parseInt(btnCourseId, 10);
                            console.log('Button clicked, parsed ID:', id, 'Original:', btnCourseId);
                            if (!isNaN(id) && id > 0) {
                                processCourse(id);
                            } else {
                                console.error('Invalid course ID from button:', btnCourseId, 'Parsed:', id);
                                createToast('Invalid course ID: ' + btnCourseId, 'error');
                            }
                        });
                    } else {
                        console.error('Button missing data-course-id attribute:', btn);
                    }
                });
                
                // Attach event listeners to delete buttons
                const deleteButtons = document.querySelectorAll('.delete-course-btn');
                console.log('Found delete buttons:', deleteButtons.length);
                deleteButtons.forEach((btn) => {
                    const btnCourseId = btn.getAttribute('data-course-id');
                    if (btnCourseId) {
                        btn.addEventListener('click', (e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            const id = parseInt(btnCourseId, 10);
                            if (!isNaN(id) && id > 0) {
                                deleteCourse(id);
                            } else {
                                createToast('Invalid course ID', 'error');
                            }
                        });
                    }
                });
            }, 100);
        }

        async function processCourse(courseId) {
            console.log('processCourse called with courseId:', courseId, typeof courseId);
            if (!courseId || isNaN(courseId) || courseId <= 0) {
                console.error('Invalid courseId:', courseId);
                createToast('Invalid course ID', 'error');
                return;
            }
            
            if (!token || token.trim() === '') {
                console.error('Missing JWT token');
                createToast('Authentication required. Please log in again.', 'error');
                return;
            }
            
            if (!confirm('Start processing this course? This may take several minutes.')) {
                return;
            }

            try {
                const url = apiBaseUrl + '/course/' + String(courseId) + '/process';
                console.log('Fetching URL:', url);
                console.log('apiBaseUrl:', apiBaseUrl);
                console.log('courseId:', courseId, 'as string:', String(courseId));
                console.log('token length:', token ? token.length : 0);
                
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                });

                if (!response.ok) {
                    let errorMsg = 'Failed to start processing';
                    try {
                        const errorText = await response.text();
                        const errorJson = JSON.parse(errorText);
                        if (errorJson.message) {
                            errorMsg = errorJson.message;
                        } else if (errorJson.error) {
                            errorMsg = errorJson.error;
                        }
                    } catch (e) {
                        // Use default error message
                    }
                    throw new Error(errorMsg);
                }
                
                const result = await response.json();
                createToast('Course processing started! This may take several minutes.', 'info');
                setTimeout(() => loadCourses(), 1000);
                
            } catch (error) {
                console.error('Error:', error);
                createToast('Failed to start processing: ' + error.message, 'error');
            }
        }
        
        async function deleteCourse(courseId) {
            if (!courseId || isNaN(courseId) || courseId <= 0) {
                createToast('Invalid course ID', 'error');
                return;
            }
            
            if (!confirm('Are you sure you want to delete this course? This action cannot be undone.')) {
                return;
            }
            
            try {
                const response = await fetch(apiBaseUrl + '/course/' + courseId, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });
                
                if (!response.ok) {
                    let errorMsg = 'Failed to delete course';
                    try {
                        const errorText = await response.text();
                        const errorJson = JSON.parse(errorText);
                        if (errorJson.message) {
                            errorMsg = errorJson.message;
                        } else if (errorJson.error) {
                            errorMsg = errorJson.error;
                        }
                    } catch (e) {
                        // Use default error message
                    }
                    throw new Error(errorMsg);
                }
                
                const result = await response.json();
                createToast('Course deleted successfully', 'success');
                setTimeout(() => loadCourses(), 500);

            } catch (error) {
                console.error('Error:', error);
                createToast('Failed to delete course: ' + error.message, 'error');
            }
        }

        function getLanguageName(code) {
            const names = { 'en': 'English', 'es': 'Español', 'ca': 'Català' };
            return names[code] || code;
        }

        function formatDate(dateStr) {
            if (!dateStr) return 'N/A';
            try {
                const date = new Date(dateStr);
                if (isNaN(date.getTime())) {
                    return dateStr;
                }
                return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
            } catch (e) {
                return dateStr;
            }
        }

        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        // Auto-refresh for processing courses (but not ERROR status)
        setInterval(() => {
            const processingCards = document.querySelectorAll('.status-PROCESSING');
            const errorCards = document.querySelectorAll('.status-ERROR');
            // Only refresh if there are processing courses and no errors
            if (processingCards.length > 0 && errorCards.length === 0) {
                console.log('Auto-refreshing courses (processing detected)');
                loadCourses();
            }
        }, 10000); // Refresh every 10 seconds if there are processing courses

        loadCourses();
    </script>
</body>
</html>

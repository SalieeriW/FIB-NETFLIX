<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="util.JwtSessionHelper" %>
<%
    if (!JwtSessionHelper.isLoggedIn(session)) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    String courseId = request.getParameter("id");
    if (courseId == null || courseId.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/listCourses");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Course Notes - VidStream</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/style.css">
    <script src="<%= request.getContextPath() %>/js/toast.js"></script>
    <style>
        .notes-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-8);
            flex-wrap: wrap;
            gap: var(--space-4);
        }
        
        .header-controls {
            display: flex;
            gap: var(--space-4);
            align-items: center;
            flex-wrap: wrap;
        }
        
        .search-box {
            position: relative;
            display: flex;
            align-items: center;
        }
        
        .search-input {
            padding: var(--space-3) var(--space-4);
            padding-right: 2.5rem;
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: 6px;
            color: var(--text-primary);
            font-size: 0.875rem;
            width: 250px;
            transition: var(--transition-fast);
        }
        
        .search-input:focus {
            outline: none;
            border-color: var(--accent);
            width: 300px;
        }
        
        .search-icon {
            position: absolute;
            right: var(--space-3);
            width: 18px;
            height: 18px;
            color: var(--text-secondary);
            pointer-events: none;
        }
        
        .search-results {
            margin-top: var(--space-6);
            display: none;
        }
        
        .search-results.active {
            display: block;
        }
        
        .search-result-item {
            background: var(--bg-tertiary);
            border: 1px solid var(--border);
            border-radius: 6px;
            padding: var(--space-4);
            margin-bottom: var(--space-3);
            transition: var(--transition-fast);
        }
        
        .search-result-item:hover {
            border-color: var(--border-hover);
            background: var(--bg-card);
        }
        
        .search-result-text {
            color: var(--text-secondary);
            line-height: 1.6;
            margin-bottom: var(--space-2);
        }
        
        .search-result-meta {
            font-size: 0.75rem;
            color: var(--text-tertiary);
            display: flex;
            gap: var(--space-4);
        }
        
        .language-selector {
            display: flex;
            gap: var(--space-2);
            background: var(--bg-card);
            padding: var(--space-1);
            border-radius: 6px;
            border: 1px solid var(--border);
        }
        
        .lang-btn {
            padding: var(--space-2) var(--space-4);
            background: transparent;
            border: none;
            color: var(--text-secondary);
            cursor: pointer;
            border-radius: 4px;
            font-size: 0.875rem;
            font-weight: 500;
            transition: var(--transition-fast);
            font-family: inherit;
        }
        
        .lang-btn:hover {
            color: var(--text-primary);
            background: rgba(255, 255, 255, 0.05);
        }
        
        .lang-btn.active {
            background: var(--accent);
            color: var(--text-primary);
        }
        
        .notes-container {
            background: var(--bg-card);
            border-radius: 8px;
            padding: var(--space-8);
            border: 1px solid var(--border);
            min-height: 400px;
        }
        
        .notes-content {
            color: var(--text-secondary);
            line-height: 1.8;
            font-size: 1rem;
        }
        
        .notes-content h1,
        .notes-content h2,
        .notes-content h3 {
            color: var(--text-primary);
            margin-top: var(--space-6);
            margin-bottom: var(--space-4);
        }
        
        .notes-content h1 {
            font-size: 2rem;
            border-bottom: 2px solid var(--border);
            padding-bottom: var(--space-3);
        }
        
        .notes-content h2 {
            font-size: 1.5rem;
        }
        
        .notes-content h3 {
            font-size: 1.25rem;
        }
        
        .notes-content ul,
        .notes-content ol {
            margin-left: var(--space-6);
            margin-bottom: var(--space-4);
        }
        
        .notes-content li {
            margin-bottom: var(--space-2);
        }
        
        .notes-content code {
            background: var(--bg-tertiary);
            padding: 0.125rem 0.375rem;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
            font-size: 0.9em;
        }
        
        .notes-content pre {
            background: var(--bg-tertiary);
            padding: var(--space-4);
            border-radius: 6px;
            overflow-x: auto;
            margin: var(--space-4) 0;
        }
        
        .loading-state {
            text-align: center;
            padding: var(--space-16);
            color: var(--text-secondary);
        }
        
        .error-state {
            text-align: center;
            padding: var(--space-16);
            color: var(--error);
        }
    </style>
</head>
<body>
    <jsp:include page="navbar.jsp" />
    
    <div class="main-content">
        <div class="container" style="max-width: 1000px;">
            <a href="<%= request.getContextPath() %>/listCourses" class="back-button" aria-label="Back to courses">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
            </a>
            
            <div class="notes-header">
                <h1>Course Notes</h1>
                <div class="header-controls">
                    <div class="search-box">
                        <input type="text" 
                               class="search-input" 
                               id="searchInput" 
                               placeholder="Search in course content..."
                               onkeydown="handleSearchKeyDown(event)">
                        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <path d="m21 21-4.35-4.35"></path>
                        </svg>
                    </div>
                    <div class="language-selector">
                        <button class="lang-btn active" data-lang="en" onclick="loadNotes('en')">English</button>
                        <button class="lang-btn" data-lang="es" onclick="loadNotes('es')">Español</button>
                        <button class="lang-btn" data-lang="ca" onclick="loadNotes('ca')">Català</button>
                    </div>
                </div>
            </div>
            
            <div class="search-results" id="searchResults"></div>
            
            <div class="notes-container" id="notesContainer">
                <div class="loading-state">
                    <p>Loading notes...</p>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const apiBaseUrl = '<%= System.getenv("REST_API_URL") != null ? System.getenv("REST_API_URL") : "http://localhost:8080/practica5-rest-service/resources" %>';
        const token = '<%= JwtSessionHelper.getToken(session) != null ? JwtSessionHelper.getToken(session) : "" %>';
        const courseId = '<%= courseId != null ? courseId : "" %>';

        if (!courseId || courseId === '') {
            console.error('Course ID is missing');
            createToast('Error: Course ID is missing', 'error');
            setTimeout(() => {
                window.location.href = '<%= request.getContextPath() %>/listCourses';
            }, 2000);
        }

        let currentLang = 'en';
        
        function loadNotes(lang) {
            if (!courseId || courseId === '') {
                console.error('Course ID is missing');
                createToast('Error: Course ID is missing', 'error');
                setTimeout(() => {
                    window.location.href = '<%= request.getContextPath() %>/listCourses';
                }, 2000);
                return;
            }
            
            currentLang = lang;
            const container = document.getElementById('notesContainer');
            container.innerHTML = '<div class="loading-state"><p>Loading notes...</p></div>';
            
            document.querySelectorAll('.lang-btn').forEach(btn => {
                btn.classList.toggle('active', btn.dataset.lang === lang);
            });
            
            fetch(apiBaseUrl + '/course/' + courseId + '/notes?lang=' + lang, {
                headers: { 'Authorization': 'Bearer ' + token }
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        let errorMsg = 'Failed to load notes';
                        try {
                            const errorJson = JSON.parse(text);
                            errorMsg = errorJson.message || errorJson.error || errorMsg;
                        } catch (e) {
                            errorMsg = text || errorMsg;
                        }
                        throw new Error(errorMsg);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.notes && data.notes.trim() !== '') {
                    container.innerHTML = '<div class="notes-content">' + formatMarkdown(data.notes) + '</div>';
                } else {
                    container.innerHTML = '<div class="error-state"><p>No notes available for this language yet.</p></div>';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                container.innerHTML = '<div class="error-state"><p>Error loading notes: ' + escapeHtml(error.message) + '</p></div>';
                createToast('Error loading notes: ' + error.message, 'error');
            });
        }
        
        function formatMarkdown(text) {
            let html = escapeHtml(text);
            
            html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
            html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
            html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
            
            html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
            html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
            
            html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
            html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
            
            html = html.replace(/^\- (.*$)/gim, '<li>$1</li>');
            html = html.replace(/^(\d+)\. (.*$)/gim, '<li>$2</li>');
            
            html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>');
            
            html = html.split('\n\n').map(para => {
                if (!para.trim()) return '';
                if (para.startsWith('<h') || para.startsWith('<ul') || para.startsWith('<pre')) {
                    return para;
                }
                return '<p>' + para + '</p>';
            }).join('');
            
            return html;
        }
        
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        let searchTimeout;
        
        function handleSearchKeyDown(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                performSearch();
            }
        }
        
        document.getElementById('searchInput').addEventListener('input', function() {
            const query = this.value.trim();
            const resultsDiv = document.getElementById('searchResults');
            
            clearTimeout(searchTimeout);
            
            if (query.length < 2) {
                resultsDiv.classList.remove('active');
                resultsDiv.innerHTML = '';
                return;
            }
            
            searchTimeout = setTimeout(() => {
                performSearch();
            }, 500);
        });
        
        async function performSearch() {
            if (!courseId || courseId === '') {
                console.error('Course ID is missing');
                return;
            }
            
            const query = document.getElementById('searchInput').value.trim();
            const resultsDiv = document.getElementById('searchResults');
            
            if (query.length < 2) {
                resultsDiv.classList.remove('active');
                return;
            }
            
            resultsDiv.classList.add('active');
            resultsDiv.innerHTML = '<div class="loading-state"><p>Searching...</p></div>';
            
            try {
                const response = await fetch(apiBaseUrl + '/course/' + courseId + '/search?q=' + encodeURIComponent(query) + '&n=10', {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                
                if (!response.ok) {
                    throw new Error('Search failed');
                }
                
                const data = await response.json();
                
                if (data.success && data.documents && data.documents.length > 0) {
                    let html = '<h3 style="margin-bottom: var(--space-4); color: var(--text-primary);">Search Results (' + data.documents.length + ')</h3>';
                    
                    data.documents.forEach((doc, index) => {
                        const metadata = data.metadatas && data.metadatas[index] ? data.metadatas[index] : {};
                        const distance = data.distances && data.distances[index] ? data.distances[index] : null;
                        const relevance = distance !== null ? Math.round((1 - distance) * 100) : null;
                        
                        html += '<div class="search-result-item">' +
                            '<div class="search-result-text">' + escapeHtml(doc) + '</div>' +
                            '<div class="search-result-meta">' +
                            (metadata.lang ? '<span>Language: ' + metadata.lang.toUpperCase() + '</span>' : '') +
                            (metadata.timestamp ? '<span>Time: ' + escapeHtml(metadata.timestamp) + '</span>' : '') +
                            (metadata.chunk_id !== undefined ? '<span>Chunk #' + metadata.chunk_id + '</span>' : '') +
                            (relevance !== null ? '<span>Relevance: ' + relevance + '%</span>' : '') +
                            '</div>' +
                            '</div>';
                    });
                    
                    resultsDiv.innerHTML = html;
                } else {
                    resultsDiv.innerHTML = '<div class="empty-state"><p>No results found for "' + escapeHtml(query) + '"</p></div>';
                }
                
            } catch (error) {
                console.error('Search error:', error);
                resultsDiv.innerHTML = '<div class="error-state"><p>Error searching: ' + escapeHtml(error.message) + '</p></div>';
            }
        }
        
        loadNotes('en');
    </script>
</body>
</html>

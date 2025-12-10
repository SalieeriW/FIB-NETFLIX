<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Video" %>
<%@ page import="util.JwtSessionHelper" %>
<%
    if (!JwtSessionHelper.isLoggedIn(session)) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    String currentUser = JwtSessionHelper.getUsername(session);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VidStream - Your Video Library</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
    <script>
        // Navbar scroll effect
        window.addEventListener('scroll', function() {
            const navbar = document.querySelector('.navbar');
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
        
        // Modern search bar
        document.addEventListener('DOMContentLoaded', function() {
            const searchToggle = document.getElementById('searchToggle');
            const searchWrapper = document.getElementById('searchWrapper');
            const searchInput = document.getElementById('searchInput');
            const searchForm = document.getElementById('searchForm');
            
            // Check if there's a search query, open search bar
            <% if (request.getAttribute("searchQuery") != null && !((String)request.getAttribute("searchQuery")).isEmpty()) { %>
                searchWrapper.classList.add('active');
                searchToggle.classList.add('active');
            <% } %>
            
            // Toggle search on icon click
            searchToggle.addEventListener('click', function(e) {
                e.stopPropagation();
                const isActive = searchWrapper.classList.contains('active');
                
                if (!isActive) {
                    searchWrapper.classList.add('active');
                    searchToggle.classList.add('active');
                    setTimeout(() => searchInput.focus(), 300);
                } else {
                    if (searchInput.value === '') {
                        searchWrapper.classList.remove('active');
                        searchToggle.classList.remove('active');
                    } else {
                        searchInput.focus();
                    }
                }
            });
            
            // Close search on outside click
            document.addEventListener('click', function(e) {
                if (!searchWrapper.contains(e.target) && !searchToggle.contains(e.target)) {
                    if (searchInput.value === '') {
                        searchWrapper.classList.remove('active');
                        searchToggle.classList.remove('active');
                    }
                }
            });
            
            // Keep search open if it has a value
            if (searchInput.value && searchInput.value.trim() !== '') {
                searchWrapper.classList.add('active');
                searchToggle.classList.add('active');
            }
            
            // Prevent form submission if input is empty
            searchForm.addEventListener('submit', function(e) {
                if (searchInput.value.trim() === '') {
                    e.preventDefault();
                }
            });
            
            // Show toasts on page load
            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null && !error.isEmpty()) { %>
                createToast('<%= error.replace("'", "\\'") %>', 'error');
            <% } %>
            
            // Poll video status if uploading
            <% String uploadingVideoId = request.getParameter("uploading"); %>
            <% if (uploadingVideoId != null && !uploadingVideoId.isEmpty()) { %>
                pollVideoStatus(<%= uploadingVideoId %>);
            <% } %>
        });
        
        // Poll video status until ready
        function pollVideoStatus(videoId) {
            const API_BASE_URL = '<%= System.getenv("REST_API_URL") != null ? System.getenv("REST_API_URL") : "http://localhost:8080/practica5-rest-service/resources" %>';
            let pollCount = 0;
            const maxPolls = 300; // 5 minutes max (2 second intervals = 10 minutes total)
            let processingToast = null;
            let isPolling = true;
            
            const poll = function() {
                if (!isPolling) return;
                
                if (pollCount >= maxPolls) {
                    if (processingToast) {
                        processingToast.remove();
                    }
                    createToast('Video processing is taking longer than expected. Please check back later.', 'info');
                    isPolling = false;
                    return;
                }
                
                pollCount++;
                
                fetch(API_BASE_URL + '/video/status/' + videoId)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Failed to check video status');
                        }
                        return response.json();
                    })
                    .then(data => {
                        const status = data.status;
                        
                        if (status === 'READY' || status === 'PARTIAL_READY') {
                            isPolling = false;
                            // Remove processing toast if it exists
                            if (processingToast) {
                                processingToast.classList.add('toast-exit');
                                setTimeout(() => processingToast.remove(), 300);
                            }
                            createToast('Video uploaded and processed successfully!', 'success');
                            // Refresh the page to show the new video
                            setTimeout(() => {
                                window.location.href = window.location.pathname;
                            }, 1500);
                        } else if (status === 'ERROR') {
                            isPolling = false;
                            // Remove processing toast if it exists
                            if (processingToast) {
                                processingToast.classList.add('toast-exit');
                                setTimeout(() => processingToast.remove(), 300);
                            }
                            createToast('Video processing failed. Please try uploading again.', 'error');
                        } else if (status === 'UPLOADING' || status === 'PROCESSING') {
                            // Show processing toast only once
                            if (!processingToast) {
                                const container = document.getElementById('toastContainer') || createToastContainer();
                                processingToast = document.createElement('div');
                                processingToast.className = 'toast toast-info';
                                processingToast.innerHTML = `
                                    <svg class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="12" y1="16" x2="12" y2="12"></line>
                                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                    </svg>
                                    <span class="toast-message">Video is being processed. This may take a few minutes...</span>
                                `;
                                container.appendChild(processingToast);
                                // Don't auto-dismiss processing toast
                            }
                            // Continue polling
                            setTimeout(poll, 2000); // Poll every 2 seconds
                        } else {
                            // Unknown status, continue polling
                            setTimeout(poll, 2000);
                        }
                    })
                    .catch(error => {
                        console.error('Error polling video status:', error);
                        // Retry after a delay
                        setTimeout(poll, 3000);
                    });
            };
            
            // Start polling after a short delay
            setTimeout(poll, 1000);
        }
        
        // Smooth fade in for video cards
        document.addEventListener('DOMContentLoaded', function() {
            const cards = document.querySelectorAll('.video-card');
            cards.forEach((card, index) => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                setTimeout(() => {
                    card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, index * 50);
            });
        });
    </script>
</head>
<body>
    <jsp:include page="navbar.jsp" />

    <div class="main-content">
        <%
            List<Video> videos = (List<Video>) request.getAttribute("videos");
        %>


    <div class="container">
            <!-- Modern Search Bar -->
            <div class="search-container">
                <div class="search-wrapper">
                    <button class="search-icon-btn" id="searchToggle" type="button" aria-label="Search">
                        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <path d="m21 21-4.35-4.35"></path>
                        </svg>
                    </button>
                    <div class="search-input-wrapper" id="searchWrapper">
                        <form method="get" action="${pageContext.request.contextPath}/listVideo" id="searchForm">
                            <input type="text" name="q" id="searchInput" placeholder="Search videos by title or uploader..."
                                   value="<%= request.getAttribute("searchQuery") != null ? request.getAttribute("searchQuery") : "" %>"
                                   autocomplete="off">
                            <% if (request.getAttribute("searchQuery") != null && !((String)request.getAttribute("searchQuery")).isEmpty()) { %>
                                <a href="${pageContext.request.contextPath}/listVideo" class="search-clear" aria-label="Clear search">
                                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <line x1="18" y1="6" x2="6" y2="18"></line>
                                        <line x1="6" y1="6" x2="18" y2="18"></line>
                                    </svg>
                                </a>
                            <% } %>
                            <button type="submit" class="search-submit" aria-label="Submit search">
                                <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="11" cy="11" r="8"></circle>
                                    <path d="m21 21-4.35-4.35"></path>
                                </svg>
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Toast container will be added by JavaScript -->

            <!-- Video Section -->
        <% if (videos == null || videos.isEmpty()) { %>
                <div class="hero-section">
                    <div class="hero-content">
                        <h1 class="hero-title">Your Library</h1>
                        <p class="hero-subtitle">Upload and manage your video collection</p>
                        <a href="${pageContext.request.contextPath}/uploadVideo" class="btn btn-primary btn--large">
                            <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                <polyline points="17 8 12 3 7 8"></polyline>
                                <line x1="12" y1="3" x2="12" y2="15"></line>
                            </svg>
                            Upload Video
                        </a>
                    </div>
            </div>
        <% } else { %>
                <div class="video-section">
                    <div class="section-header">
                    <h2 class="section-title">
                        <% if (request.getAttribute("searchQuery") != null && !((String)request.getAttribute("searchQuery")).isEmpty()) { %>
                            Search Results
                        <% } else { %>
                            My List
                        <% } %>
                    </h2>
                    <a href="${pageContext.request.contextPath}/uploadVideo" class="btn btn-primary">
                        <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                            <polyline points="17 8 12 3 7 8"></polyline>
                            <line x1="12" y1="3" x2="12" y2="15"></line>
                        </svg>
                        Upload Video
                    </a>
                    </div>
            <div class="video-grid">
                <% for (Video video : videos) { %>
                            <div class="video-card" data-video-id="<%= video.id %>">
                                <div class="video-thumbnail">
                                    <%
                                        String apiBaseUrl = System.getenv("REST_API_URL");
                                        if (apiBaseUrl == null || apiBaseUrl.isEmpty()) {
                                            apiBaseUrl = "http://localhost:8080/practica5-rest-service/resources";
                                        }
                                    %>
                                    <img src="<%= apiBaseUrl %>/video/thumbnail/<%= video.id %>" 
                                         alt="<%= video.title %>"
                                         onerror="this.style.display='none';">
                                    <div class="video-overlay">
                                        <% if ("READY".equals(video.status)) { %>
                                            <a href="${pageContext.request.contextPath}/playerVideo?id=<%= video.id %>" 
                                               class="btn btn-primary">
                                                <svg class="icon icon-lg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                                </svg>
                                                Play
                                            </a>
                                        <% } else if ("PARTIAL_READY".equals(video.status)) { %>
                                            <a href="${pageContext.request.contextPath}/playerVideo?id=<%= video.id %>" 
                                               class="btn btn-primary">
                                                <svg class="icon icon-lg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                                </svg>
                                                Play (360p)
                                            </a>
                                        <% } %>
                                    </div>
                                </div>
                        <div class="video-info">
                            <h3 class="video-title" title="<%= video.title %>"><%= video.title %></h3>

                            <div class="video-meta">
                                        <span><%= video.uploader %></span>
                            </div>

                            <div class="video-meta-row">
                                        <span><%= video.getDurationFormatted() %></span>
                                        <span><%= video.views %> views</span>
                            </div>

                                    <div class="video-status">
                                <%
                                    String statusClass = "status-uploading";
                                    String status = video.status;
                                    String statusDisplay = status;
                                    int progress = 0;
                                    String progressText = "";

                                    if ("READY".equals(status)) {
                                        statusClass = "status-ready";
                                        progress = 100;
                                                statusDisplay = "Ready";
                                    } else if ("PARTIAL_READY".equals(status)) {
                                        statusClass = "status-partial-ready";
                                        progress = 70;
                                        statusDisplay = "360p Available";
                                        progressText = "Higher qualities transcoding...";
                                    } else if ("PROCESSING".equals(status)) {
                                        statusClass = "status-processing";
                                        progress = 50;
                                        progressText = "Transcoding video...";
                                    } else if ("UPLOADING".equals(status)) {
                                        progress = 20;
                                        progressText = "Upload complete, waiting for transcoding...";
                                    } else if ("ERROR".equals(status)) {
                                        statusClass = "status-error";
                                        progress = 0;
                                    }
                                %>
                                <span class="status-badge <%= statusClass %>"><%= statusDisplay %></span>
                            </div>

                            <% if ("UPLOADING".equals(video.status) || "PROCESSING".equals(video.status) || "PARTIAL_READY".equals(video.status)) { %>
                                <div class="progress-bar-container">
                                    <div class="progress-bar <%= "PROCESSING".equals(video.status) || "PARTIAL_READY".equals(video.status) ? "processing" : "" %>"
                                         style="width: <%= progress %>%;"></div>
                                </div>
                                        <% if (!progressText.isEmpty()) { %>
                                <div class="progress-text"><%= progressText %></div>
                                        <% } %>
                            <% } %>

                            <div class="video-actions">
                                <% if ("READY".equals(video.status)) { %>
                                    <a href="${pageContext.request.contextPath}/playerVideo?id=<%= video.id %>"
                                               class="btn btn-primary">
                                                <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                                </svg>
                                                Play
                                            </a>
                                <% } else if ("PARTIAL_READY".equals(video.status)) { %>
                                    <a href="${pageContext.request.contextPath}/playerVideo?id=<%= video.id %>"
                                               class="btn btn-primary">
                                                <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                    <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                                </svg>
                                                Play (360p)
                                            </a>
                                <% } else if ("PROCESSING".equals(video.status)) { %>
                                            <button class="btn btn-primary" disabled>
                                                <span class="spinner"></span> Processing
                                    </button>
                                <% } else if ("ERROR".equals(video.status) && currentUser != null && currentUser.equals(video.uploader)) { %>
                                            <form action="${pageContext.request.contextPath}/retryTranscode" method="post" class="video-action-form">
                                        <input type="hidden" name="id" value="<%= video.id %>">
                                                <button type="submit" class="btn btn-secondary">
                                                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                        <polyline points="23 4 23 10 17 10"></polyline>
                                                        <polyline points="1 20 1 14 7 14"></polyline>
                                                        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                                                    </svg>
                                                    Retry
                                                </button>
                                    </form>
                                <% } %>

                                <% if (currentUser != null && currentUser.equals(video.uploader)) { %>
                                    <form action="${pageContext.request.contextPath}/deleteVideo"
                                          method="post"
                                                  class="video-action-form"
                                          onsubmit="return confirm('Are you sure you want to delete this video?');">
                                        <input type="hidden" name="id" value="<%= video.id %>">
                                                <button type="submit" class="btn btn-danger">
                                                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                        <polyline points="3 6 5 6 21 6"></polyline>
                                                        <path d="m19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                        <line x1="10" y1="11" x2="10" y2="17"></line>
                                                        <line x1="14" y1="11" x2="14" y2="17"></line>
                                                    </svg>
                                                    Delete
                                                </button>
                                    </form>
                                <% } %>
                            </div>
                        </div>
                    </div>
                <% } %>
                    </div>
            </div>
        <% } %>
        </div>
    </div>

    <script>
        const API_BASE_URL = '<%= System.getenv("REST_API_URL") != null ? System.getenv("REST_API_URL") : "http://localhost:8080/practica5-rest-service/resources" %>';

        const processingVideos = [
            <%
                if (videos != null) {
                    boolean first = true;
                    for (Video v : videos) {
                        if ("UPLOADING".equals(v.status) || "PROCESSING".equals(v.status) || "PARTIAL_READY".equals(v.status)) {
                            if (!first) out.print(",");
                            out.print(v.id);
                            first = false;
                        }
                    }
                }
            %>
        ];

        function updateVideoStatus(videoId) {
            fetch(API_BASE_URL + '/video/status/' + videoId)
                .then(response => response.json())
                .then(data => {
                    const card = document.querySelector('[data-video-id="' + videoId + '"]');
                    if (!card) return;

                    const statusBadge = card.querySelector('.status-badge');
                    const progressContainer = card.querySelector('.progress-bar-container');
                    const progressBar = card.querySelector('.progress-bar');
                    const progressText = card.querySelector('.progress-text');
                    const actionsDiv = card.querySelector('.video-actions');

                    statusBadge.className = 'status-badge';
                    statusBadge.textContent = data.status;

                    if (data.status === 'READY') {
                        statusBadge.classList.add('status-ready');
                        statusBadge.textContent = 'Ready';
                        if (progressContainer) progressContainer.style.display = 'none';
                        window.location.reload();
                    } else if (data.status === 'PARTIAL_READY') {
                        statusBadge.classList.add('status-partial-ready');
                        statusBadge.textContent = '360p Available';
                        if (progressBar) {
                            progressBar.style.width = '70%';
                            progressBar.classList.add('processing');
                        }
                        if (progressText) progressText.textContent = 'Higher qualities transcoding...';
                        window.location.reload();
                    } else if (data.status === 'PROCESSING') {
                        statusBadge.classList.add('status-processing');
                        if (progressBar) {
                            progressBar.style.width = '50%';
                            progressBar.classList.add('processing');
                        }
                        if (progressText) progressText.textContent = 'Transcoding video...';
                    } else if (data.status === 'UPLOADING') {
                        statusBadge.classList.add('status-uploading');
                        if (progressBar) {
                            progressBar.style.width = '20%';
                            progressBar.classList.remove('processing');
                        }
                        if (progressText) progressText.textContent = 'Upload complete, waiting for transcoding...';
                    } else if (data.status === 'ERROR') {
                        statusBadge.classList.add('status-error');
                        if (progressContainer) progressContainer.style.display = 'none';
                        window.location.reload();
                    }
                })
                .catch(err => console.error('Error fetching video status:', err));
        }

        if (processingVideos.length > 0) {
            setInterval(function() {
                processingVideos.forEach(videoId => updateVideoStatus(videoId));
            }, 5000);
        }
    </script>
</body>
</html>

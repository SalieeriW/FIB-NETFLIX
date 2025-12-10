<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Video - VidStream</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
</head>
<body>
    <a href="${pageContext.request.contextPath}/listVideo" class="back-button" aria-label="Back to library">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
    </a>
    <jsp:include page="navbar.jsp" />

    <div class="main-content upload-page">
        <div class="container container-narrow">
            <h1 class="text-center mb-8">Upload Video</h1>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            <% String error = request.getParameter("error"); %>
            <% if (error != null && !error.isEmpty()) { %>
                createToast('<%= error.replace("'", "\\'") %>', 'error');
            <% } %>
        });
    </script>

        <div class="requirements">
            <strong>Requirements</strong>
            <ul>
                <li>Supported formats: MP4, WebM, AVI</li>
                <li>Maximum file size: 500 MB</li>
                <li>Recommended: H.264 codec for best compatibility</li>
            </ul>
        </div>

        <form action="${pageContext.request.contextPath}/uploadVideo"
              method="post"
              enctype="multipart/form-data"
              id="uploadForm"
              onsubmit="return validateForm()">

            <div class="form-group">
                <label for="title">Title *</label>
                <input type="text"
                       id="title"
                       name="title"
                       required
                       maxlength="255"
                       placeholder="Enter video title">
            </div>

            <div class="form-group">
                <label for="description">Description</label>
                <textarea id="description"
                          name="description"
                          maxlength="2000"
                          placeholder="Enter video description (optional)"></textarea>
            </div>

            <div class="form-group">
                <label for="duration">Duration (seconds)</label>
                <input type="number"
                       id="duration"
                       name="duration"
                       min="1"
                       placeholder="Optional - will be auto-detected later">
            </div>

            <div class="form-group">
                <label>Video File *</label>
                <div class="file-input-wrapper">
                    <input type="file"
                           id="videoFile"
                           name="videoFile"
                           accept="video/mp4,video/webm,video/x-msvideo"
                           required
                           onchange="handleFileSelect(this)">
                    <label for="videoFile" class="file-input-label">
                        <span id="fileLabel">Click to select video file</span>
                    </label>
                </div>
                <div class="video-preview" id="videoPreview" style="display: none;">
                    <img id="thumbnailPreview" alt="Video thumbnail preview">
                </div>
                <div class="file-info" id="fileInfo"></div>
            </div>

            <div class="form-group">
                <button type="submit" class="btn btn-primary btn--block" id="submitBtn">
                    Upload Video
                </button>
            </div>
        </form>
        </div>
    </div>

    <script>
        const MAX_FILE_SIZE = 524288000; // 500 MB

        function handleFileSelect(input) {
            const file = input.files[0];
            const fileInfo = document.getElementById('fileInfo');
            const fileLabel = document.getElementById('fileLabel');
            const submitBtn = document.getElementById('submitBtn');
            const videoPreview = document.getElementById('videoPreview');
            const thumbnailPreview = document.getElementById('thumbnailPreview');

            if (!file) {
                fileInfo.classList.remove('show');
                videoPreview.style.display = 'none';
                fileLabel.textContent = 'Click to select video file';
                submitBtn.disabled = true;
                return;
            }

            // Validate file size
            if (file.size > MAX_FILE_SIZE) {
                alert('File size exceeds 500MB limit. Please select a smaller file.');
                input.value = '';
                fileInfo.classList.remove('show');
                videoPreview.style.display = 'none';
                fileLabel.textContent = 'Click to select video file';
                submitBtn.disabled = true;
                return;
            }

            // Validate file type
            const validTypes = ['video/mp4', 'video/webm', 'video/x-msvideo'];
            if (!validTypes.includes(file.type)) {
                alert('Invalid file type. Please select MP4, WebM, or AVI file.');
                input.value = '';
                fileInfo.classList.remove('show');
                videoPreview.style.display = 'none';
                fileLabel.textContent = 'Click to select video file';
                submitBtn.disabled = true;
                return;
            }

            // Generate thumbnail preview
            generateThumbnailPreview(file);

            // Display file info
            const fileName = file.name || 'Unknown';
            const fileSizeBytes = file.size || 0;
            const fileSizeMB = (fileSizeBytes / (1024 * 1024)).toFixed(2);
            let fileType = file.type || '';
            
            // If type is empty, try to detect from extension
            if (!fileType && fileName) {
                const ext = fileName.split('.').pop().toLowerCase();
                const typeMap = {
                    'mp4': 'video/mp4',
                    'webm': 'video/webm',
                    'avi': 'video/x-msvideo',
                    'mov': 'video/quicktime',
                    'mkv': 'video/x-matroska'
                };
                fileType = typeMap[ext] || 'video/' + ext;
            }
            
            if (!fileType) {
                fileType = 'Unknown';
            }
            
            fileLabel.textContent = 'File selected';
            fileInfo.innerHTML = 
                '<strong>' + escapeHtml(fileName) + '</strong><br>' +
                'Size: ' + fileSizeMB + ' MB<br>' +
                'Type: ' + escapeHtml(fileType);
            fileInfo.classList.add('show');
            submitBtn.disabled = false;
        }
        
        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        function generateThumbnailPreview(file) {
            const videoPreview = document.getElementById('videoPreview');
            const thumbnailPreview = document.getElementById('thumbnailPreview');
            
            // Create video element to extract frame
            const video = document.createElement('video');
            video.preload = 'auto';
            video.muted = true;
            video.playsInline = true;
            video.setAttribute('playsinline', '');
            video.setAttribute('webkit-playsinline', '');
            
            const url = URL.createObjectURL(file);
            video.src = url;
            
            let frameCaptured = false;
            
            function captureFrame() {
                if (frameCaptured) return;
                frameCaptured = true;
                
                try {
                    // Ensure video dimensions are valid
                    if (video.videoWidth === 0 || video.videoHeight === 0) {
                        videoPreview.style.display = 'none';
                        URL.revokeObjectURL(url);
                        video.remove();
                        return;
                    }
                    
                    // Create canvas to capture frame
                    const canvas = document.createElement('canvas');
                    const maxWidth = 640;
                    const aspectRatio = video.videoHeight / video.videoWidth;
                    
                    canvas.width = Math.min(video.videoWidth, maxWidth);
                    canvas.height = canvas.width * aspectRatio;
                    
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
                    
                    // Check if canvas has content (not all black)
                    const imageData = ctx.getImageData(0, 0, Math.min(canvas.width, 100), Math.min(canvas.height, 100));
                    let hasContent = false;
                    for (let i = 0; i < imageData.data.length; i += 4) {
                        const r = imageData.data[i];
                        const g = imageData.data[i + 1];
                        const b = imageData.data[i + 2];
                        if (r > 10 || g > 10 || b > 10) {
                            hasContent = true;
                            break;
                        }
                    }
                    
                    if (!hasContent) {
                        // Canvas is all black, try again at different time
                        if (video.duration > 2) {
                            video.currentTime = 2;
                            frameCaptured = false;
                            return;
                        }
                        videoPreview.style.display = 'none';
                        URL.revokeObjectURL(url);
                        video.remove();
                        return;
                    }
                    
                    // Convert to image
                    canvas.toBlob(function(blob) {
                        if (blob && blob.size > 0) {
                            const thumbnailUrl = URL.createObjectURL(blob);
                            thumbnailPreview.src = thumbnailUrl;
                            videoPreview.style.display = 'block';
                        } else {
                            videoPreview.style.display = 'none';
                        }
                        // Cleanup
                        URL.revokeObjectURL(url);
                        video.remove();
                    }, 'image/jpeg', 0.85);
                } catch (e) {
                    console.error('Error capturing frame:', e);
                    videoPreview.style.display = 'none';
                    URL.revokeObjectURL(url);
                    video.remove();
                }
            }
            
            video.addEventListener('loadedmetadata', function() {
                if (video.duration && video.duration > 0 && !isNaN(video.duration)) {
                    // Seek to 1 second or 10% of video, whichever is smaller
                    const seekTime = Math.min(1, video.duration * 0.1);
                    video.currentTime = seekTime;
                } else {
                    // If duration is not available, try at 0.5 seconds
                    video.currentTime = 0.5;
                }
            });
            
            video.addEventListener('seeked', function() {
                // Wait a bit to ensure frame is rendered
                setTimeout(function() {
                    if (video.readyState >= 2) { // HAVE_CURRENT_DATA
                        captureFrame();
                    }
                }, 50);
            });
            
            video.addEventListener('loadeddata', function() {
                if (video.readyState >= 2 && !frameCaptured) {
                    if (video.currentTime === 0) {
                        video.currentTime = 0.1;
                    }
                }
            });
            
            video.addEventListener('canplay', function() {
                if (!frameCaptured && video.videoWidth > 0 && video.videoHeight > 0) {
                    if (video.currentTime === 0 || video.currentTime < 0.1) {
                        video.currentTime = 0.1;
                    }
                }
            });
            
            video.addEventListener('error', function(e) {
                console.error('Video load error:', e);
                videoPreview.style.display = 'none';
                URL.revokeObjectURL(url);
                video.remove();
            });
            
            video.load();
        }

        function validateForm() {
            const title = document.getElementById('title').value.trim();
            const fileInput = document.getElementById('videoFile');

            if (!title) {
                alert('Please enter a title');
                return false;
            }

            if (!fileInput.files || !fileInput.files[0]) {
                alert('Please select a video file');
                return false;
            }

            const file = fileInput.files[0];
            if (file.size > MAX_FILE_SIZE) {
                alert('File size exceeds 500MB limit');
                return false;
            }

            // Disable submit button to prevent double submission
            document.getElementById('submitBtn').disabled = true;
            document.getElementById('submitBtn').innerHTML = '<span class="spinner"></span> Uploading...';

            return true;
        }

        // Initialize button state
        document.getElementById('submitBtn').disabled = true;
    </script>
</body>
</html>

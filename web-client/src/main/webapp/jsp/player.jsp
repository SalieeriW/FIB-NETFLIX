<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Video" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Video Player - VidStream</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.dashjs.org/latest/dash.all.min.js"></script>
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

<div class="main-content">
    <div class="player-container">
        <%
            String error = (String) request.getAttribute("error");
            Video video = (Video) request.getAttribute("video");
        %>

        <% if (error != null) { %>
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                createToast('<%= error.replace("'", "\\'") %>', 'error');
            });
        </script>
        <% } else if (video != null) { %>
        <div class="video-wrapper">
            <video id="videoPlayer" controls style="width: 100%; max-width: 1280px;"></video>
        </div>

        <div class="video-info">
            <h1 class="video-title"><%= video.title %></h1>

            <% if (video.description != null && !video.description.isEmpty()) { %>
                <p><%= video.description %></p>
            <% } %>

            <div class="video-meta">
                <div class="meta-item">
                    <span class="meta-label">Uploader</span>
                    <span class="meta-value"><%= video.uploader %></span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Upload Date</span>
                    <span class="meta-value"><%= video.uploadDate %></span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Duration</span>
                    <span class="meta-value"><%= video.getDurationFormatted() %></span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Views</span>
                    <span class="meta-value"><%= video.views %></span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Status</span>
                    <span class="meta-value"><%= video.status %></span>
                </div>
            </div>

            <div class="quality-controls">
                <div class="quality-selector-group">
                    <label for="videoQualitySelect" class="quality-label">Video Quality</label>
                    <select id="videoQualitySelect" class="quality-select">
                        <option value="auto">Auto</option>
                    </select>
                </div>
                <div class="quality-selector-group">
                    <label for="audioQualitySelect" class="quality-label">Audio Quality</label>
                    <select id="audioQualitySelect" class="quality-select">
                        <option value="auto">Auto</option>
                    </select>
                </div>
            </div>

            <div class="stats-panel">
                <div class="stats-title">Streaming Stats</div>
                <div class="stats-grid">
                    <div class="stat-item">Current Quality: <span class="stat-value" id="currentQuality">-</span></div>
                    <div class="stat-item">Bitrate: <span class="stat-value" id="currentBitrate">-</span></div>
                    <div class="stat-item">Buffer Length: <span class="stat-value" id="bufferLength">-</span></div>
                    <div class="stat-item">Dropped Frames: <span class="stat-value" id="droppedFrames">0</span></div>
                </div>
            </div>
        </div>

        <script>
            const videoId = <%= video.id %>;
            <%
                String apiUrl = System.getenv("REST_API_URL");
                if (apiUrl == null || apiUrl.isEmpty()) {
                    apiUrl = "http://localhost:8080/practica5-rest-service/resources";
                }
            %>
            const apiBaseUrl = '<%= apiUrl %>';
            const manifestUrl = apiBaseUrl + '/video/stream/' + videoId;

            console.log('Manifest URL:', manifestUrl);

            const videoElement = document.getElementById('videoPlayer');

            let player = null;
            let videoQualitySelect = document.getElementById('videoQualitySelect');
            let audioQualitySelect = document.getElementById('audioQualitySelect');
            let availableVideoQualities = [];
            let availableAudioQualities = [];

            // Estado para re-inicializar manteniendo posición
            let lastTime = 0;
            let lastWasPaused = true;
            let forcedVideoRepId = null;   // null = auto
            let forcedAudioRepId = null;   // opcional

            // --------- FUNCIÓN CENTRAL: crear / recrear player ----------
            function setupPlayer() {
                // Si ya había player, lo reseteamos
                if (player) {
                    try {
                        player.reset();
                    } catch (e) {
                        console.error('Error resetting player:', e);
                    }
                }

                player = dashjs.MediaPlayer().create();

                // Settings básicos
                player.updateSettings({
                    streaming: {
                        abr: {
                            autoSwitchBitrate: {
                                video: forcedVideoRepId === null, // si hay rep forzada → false
                                audio: forcedAudioRepId === null
                            }
                        },
                        buffer: {
                            fastSwitchEnabled: true,
                            bufferToKeep: 5
                        }
                    }
                });

                // Eventos
                player.on(dashjs.MediaPlayer.events.ERROR, function(e) {
                    console.error('Dash.js error:', e);
                    alert('Error loading video: ' + (e.error ? e.error.message : 'Unknown error'));
                });

                player.on(dashjs.MediaPlayer.events.MANIFEST_LOADED, function(e) {
                    console.log('Manifest loaded');
                });

                player.on(dashjs.MediaPlayer.events.STREAM_INITIALIZED, function() {
                    console.log('Stream initialized');

                    // Rellena selectores
                    populateQualitySelectors();

                    // Si hay rep forzada, la aplicamos
                    if (forcedVideoRepId) {
                        try {
                            if (typeof player.setRepresentationForTypeById === 'function') {
                                player.setRepresentationForTypeById('video', forcedVideoRepId);
                                console.log('Applied forced video repId after init:', forcedVideoRepId);
                            }
                        } catch (e) {
                            console.error('Error applying forced video rep:', e);
                        }
                    }
                    if (forcedAudioRepId) {
                        try {
                            if (typeof player.setRepresentationForTypeById === 'function') {
                                player.setRepresentationForTypeById('audio', forcedAudioRepId);
                                console.log('Applied forced audio repId after init:', forcedAudioRepId);
                            }
                        } catch (e) {
                            console.error('Error applying forced audio rep:', e);
                        }
                    }

                    // Volver a la posición previa aproximadamente
                    if (!isNaN(lastTime) && lastTime > 0 && videoElement.duration && lastTime < videoElement.duration) {
                        try {
                            videoElement.currentTime = Math.min(lastTime, videoElement.duration - 0.2);
                        } catch (e) {
                            console.error('Error setting currentTime after reinit:', e);
                        }
                    }

                    if (!lastWasPaused) {
                        videoElement.play().catch(err => console.warn('Autoplay blocked:', err));
                    }

                    setTimeout(updateQualityInfo, 500);
                });

                player.on(dashjs.MediaPlayer.events.PLAYBACK_STARTED, function() {
                    console.log('Playback started');
                    setTimeout(updateQualityInfo, 500);
                });

                player.on(dashjs.MediaPlayer.events.QUALITY_CHANGE_RENDERED, function(e) {
                    console.log('Quality change rendered:', e);
                    updateQualityInfo();
                });

                // Inicializar (no autoPlay aquí, controlamos nosotros)
                player.initialize(videoElement, manifestUrl, true);
            }

            // --------- Rellenar selectores de calidad ----------
            function populateQualitySelectors() {
                console.log('Populating quality selectors (hard reload version)...');

                availableVideoQualities = [];
                availableAudioQualities = [];

                videoQualitySelect.innerHTML = '<option value="auto">Auto</option>';
                audioQualitySelect.innerHTML = '<option value="auto">Auto</option>';

                // VIDEO
                let videoReps = [];
                if (typeof player.getRepresentationsByType === 'function') {
                    videoReps = player.getRepresentationsByType('video') || [];
                } else if (typeof player.getRepresentationsForType === 'function') {
                    videoReps = player.getRepresentationsForType('video') || [];
                }

                console.log('Video representations:', videoReps);

                videoReps.forEach((rep, index) => {
                    const height = rep.height || (rep.attributes && rep.attributes.height);
                    const bandwidth = rep.bandwidth || (rep.attributes && rep.attributes.bandwidth);
                    let label = '';

                    if (height) label += height + 'p';
                    if (bandwidth) {
                        label += (label ? ' ' : '') + '(' + Math.round(bandwidth / 1000) + ' kbps)';
                    }
                    if (!label) label = 'Quality ' + (index + 1);

                    const opt = document.createElement('option');
                    opt.value = index;
                    opt.textContent = label;
                    videoQualitySelect.appendChild(opt);

                    availableVideoQualities.push({
                        index,
                        id: rep.id,
                        height: height || 0,
                        bitrate: bandwidth || 0
                    });

                    // Si tenemos forcedVideoRepId, seleccionamos el option correspondiente
                    if (forcedVideoRepId && rep.id === forcedVideoRepId) {
                        videoQualitySelect.value = String(index);
                    }
                });

                // AUDIO (similar, pero sin reset
                let audioReps = [];
                if (typeof player.getRepresentationsByType === 'function') {
                    audioReps = player.getRepresentationsByType('audio') || [];
                } else if (typeof player.getRepresentationsForType === 'function') {
                    audioReps = player.getRepresentationsForType('audio') || [];
                }

                console.log('Audio representations:', audioReps);

                audioReps.forEach((rep, index) => {
                    const bandwidth = rep.bandwidth || (rep.attributes && rep.attributes.bandwidth);
                    let label = 'Audio ' + (index + 1);
                    if (bandwidth) {
                        label += ' (' + Math.round(bandwidth / 1000) + ' kbps)';
                    }

                    const opt = document.createElement('option');
                    opt.value = index;
                    opt.textContent = label;
                    audioQualitySelect.appendChild(opt);

                    availableAudioQualities.push({
                        index,
                        id: rep.id,
                        bitrate: bandwidth || 0
                    });

                    if (forcedAudioRepId && rep.id === forcedAudioRepId) {
                        audioQualitySelect.value = String(index);
                    }
                });
            }

            // --------- Cambio de calidad: VIDEO (opción C: hard reload) ----------
            videoQualitySelect.addEventListener('change', function() {
                const selectedValue = this.value;

                // Guardamos estado actual
                lastTime = videoElement.currentTime || 0;
                lastWasPaused = videoElement.paused;

                if (selectedValue === 'auto') {
                    console.log('Video quality set to AUTO (reloading player)');
                    forcedVideoRepId = null;
                    // Re-crear player en modo auto
                    setupPlayer();
                } else {
                    const qualityIndex = parseInt(selectedValue, 10);
                    if (isNaN(qualityIndex)) return;

                    const selected = availableVideoQualities.find(q => q.index === qualityIndex);
                    if (!selected) {
                        console.warn('Selected video quality not found in list:', qualityIndex);
                        return;
                    }

                    console.log('Changing video quality via hard reload to:', selected);

                    // Forzamos esta representación
                    forcedVideoRepId = selected.id;

                    // Re-crear player con esta calidad
                    setupPlayer();
                }
            });

            // --------- Cambio de calidad: AUDIO (sin hard reload, opcional) ----------
            audioQualitySelect.addEventListener('change', function() {
                const selectedValue = this.value;

                if (selectedValue === 'auto') {
                    console.log('Audio quality set to AUTO');
                    forcedAudioRepId = null;
                    player.updateSettings({
                        streaming: {
                            abr: {
                                autoSwitchBitrate: {
                                    audio: true
                                }
                            }
                        }
                    });
                } else {
                    const qualityIndex = parseInt(selectedValue, 10);
                    if (isNaN(qualityIndex)) return;

                    const selected = availableAudioQualities.find(q => q.index === qualityIndex);
                    if (!selected) {
                        console.warn('Selected audio quality not found in list:', qualityIndex);
                        return;
                    }

                    console.log('Setting audio quality (no hard reload):', selected);

                    forcedAudioRepId = selected.id;
                    player.updateSettings({
                        streaming: {
                            abr: {
                                autoSwitchBitrate: {
                                    audio: false
                                }
                            }
                        }
                    });

                    try {
                        if (typeof player.setRepresentationForTypeById === 'function') {
                            player.setRepresentationForTypeById('audio', selected.id);
                        } else if (typeof player.setRepresentationForTypeByIndex === 'function') {
                            player.setRepresentationForTypeByIndex('audio', qualityIndex);
                        }
                    } catch (e) {
                        console.error('Error setting audio quality:', e);
                    }
                }
            });

            // --------- Stats ----------
            function updateQualityInfo() {
                try {
                    const currentRepr = player && player.getCurrentRepresentationForType
                        ? player.getCurrentRepresentationForType('video')
                        : null;
                    if (currentRepr) {
                        const bitrate = currentRepr.bandwidth || (currentRepr.attributes && currentRepr.attributes.bandwidth);
                        const height = currentRepr.height || (currentRepr.attributes && currentRepr.attributes.height);

                        if (height) {
                            document.getElementById('currentQuality').textContent = height + 'p';
                        }
                        if (bitrate) {
                            document.getElementById('currentBitrate').textContent = Math.round(bitrate / 1000) + ' kbps';
                        }
                    }
                } catch (e) {
                    console.error('Error updating quality info:', e);
                }
            }

            setInterval(function() {
                try {
                    updateQualityInfo();

                    if (player && player.getDashMetrics) {
                        const dashMetrics = player.getDashMetrics();
                        if (dashMetrics && dashMetrics.getCurrentBufferLevel) {
                            const bufferLevel = dashMetrics.getCurrentBufferLevel('video');
                            if (typeof bufferLevel === 'number' && !isNaN(bufferLevel)) {
                                document.getElementById('bufferLength').textContent = bufferLevel.toFixed(2) + 's';
                            }
                        }
                    }

                    const qualityMetrics = videoElement.getVideoPlaybackQuality
                        ? videoElement.getVideoPlaybackQuality()
                        : null;

                    if (qualityMetrics && qualityMetrics.droppedVideoFrames !== undefined) {
                        document.getElementById('droppedFrames').textContent = qualityMetrics.droppedVideoFrames;
                    } else if (videoElement.webkitDroppedFrameCount !== undefined) {
                        document.getElementById('droppedFrames').textContent = videoElement.webkitDroppedFrameCount || 0;
                    }
                } catch (e) {
                    console.error('Error updating stats:', e);
                }
            }, 1000);

            // --------- Primera inicialización ----------
            setupPlayer();
        </script>
        <% } else { %>
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                createToast('Video not found', 'error');
            });
        </script>
        <% } %>
    </div>
</div>
</body>
</html>

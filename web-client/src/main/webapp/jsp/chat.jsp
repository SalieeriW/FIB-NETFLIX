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
    <title>Course Chat - VidStream</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/style.css">
    <script src="<%= request.getContextPath() %>/js/toast.js"></script>
    <style>
        .chat-container {
            max-width: 900px;
            margin: 0 auto;
            display: flex;
            flex-direction: column;
            height: calc(100vh - 200px);
            background: var(--bg-card);
            border-radius: 8px;
            border: 1px solid var(--border);
            overflow: hidden;
            transition: var(--transition);
            box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
        }
        .chat-header {
            padding: var(--space-6);
            border-bottom: 1px solid var(--border);
            background: var(--bg-tertiary);
            transition: var(--transition);
        }
        .chat-header h2 {
            margin: 0;
            font-size: 1.5rem;
            font-weight: 600;
            color: var(--text-primary);
        }
        .chat-header p {
            color: var(--text-secondary);
            font-size: 0.875rem;
            margin-top: var(--space-2);
            margin-bottom: 0;
        }
        .chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: var(--space-6);
            display: flex;
            flex-direction: column;
            gap: var(--space-4);
            scroll-behavior: smooth;
        }
        .chat-messages::-webkit-scrollbar {
            width: 8px;
        }
        .chat-messages::-webkit-scrollbar-track {
            background: var(--bg-primary);
        }
        .chat-messages::-webkit-scrollbar-thumb {
            background: var(--bg-tertiary);
            border-radius: 4px;
        }
        .chat-messages::-webkit-scrollbar-thumb:hover {
            background: var(--border-hover);
        }
        .message {
            display: flex;
            gap: var(--space-3);
            animation: fadeInUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            opacity: 0;
            animation-fill-mode: forwards;
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .message.user { flex-direction: row-reverse; }
        .message-avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            font-weight: 600;
            font-size: 0.875rem;
            transition: var(--transition);
        }
        .message.user .message-avatar {
            background: var(--accent);
            color: var(--text-primary);
        }
        .message.assistant .message-avatar {
            background: var(--bg-tertiary);
            color: var(--text-primary);
            border: 1px solid var(--border);
        }
        .message-content {
            flex: 1;
            max-width: 70%;
            transition: var(--transition);
        }
        .message.user .message-content { text-align: right; }
        .message-bubble {
            padding: var(--space-4);
            border-radius: 12px;
            line-height: 1.6;
            word-wrap: break-word;
            transition: var(--transition);
        }
        .message.user .message-bubble {
            background: var(--accent);
            color: var(--text-primary);
            border-bottom-right-radius: 4px;
        }
        .message.user .message-bubble:hover {
            background: var(--accent-hover);
        }
        .message.assistant .message-bubble {
            background: var(--bg-tertiary);
            color: var(--text-secondary);
            border: 1px solid var(--border);
            border-bottom-left-radius: 4px;
        }
        .message.assistant .message-bubble:hover {
            border-color: var(--border-hover);
            background: rgba(255, 255, 255, 0.03);
        }
        .message-sources {
            margin-top: var(--space-2);
            font-size: 0.75rem;
            color: var(--text-tertiary);
            font-style: italic;
            transition: var(--transition);
        }
        .chat-input-container {
            padding: var(--space-6);
            border-top: 1px solid var(--border);
            background: var(--bg-tertiary);
            transition: var(--transition);
        }
        .chat-input-form {
            display: flex;
            gap: var(--space-3);
            align-items: flex-end;
        }
        .chat-input {
            flex: 1;
            padding: var(--space-4);
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: 6px;
            color: var(--text-primary);
            font-family: inherit;
            font-size: 0.875rem;
            resize: none;
            min-height: 50px;
            max-height: 150px;
            transition: var(--transition);
        }
        .chat-input:hover {
            border-color: var(--border-hover);
            background: rgba(255, 255, 255, 0.03);
        }
        .chat-input:focus {
            outline: none;
            border-color: var(--accent);
            background: rgba(255, 255, 255, 0.05);
            box-shadow: 0 0 0 3px rgba(229, 9, 20, 0.1);
        }
        .chat-submit {
            padding: var(--space-4) var(--space-6);
            background: var(--accent);
            color: var(--text-primary);
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 500;
            transition: var(--transition);
            display: flex;
            align-items: center;
            gap: var(--space-2);
            min-height: 50px;
        }
        .chat-submit:hover:not(:disabled) {
            background: var(--accent-hover);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(229, 9, 20, 0.3);
        }
        .chat-submit:active:not(:disabled) { transform: translateY(0); }
        .chat-submit:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none;
        }
        .loading-indicator {
            display: flex;
            gap: var(--space-2);
            align-items: center;
            color: var(--text-secondary);
            font-size: 0.875rem;
        }
        .loading-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: var(--text-secondary);
            animation: bounce 1.4s infinite ease-in-out both;
        }
        .loading-dot:nth-child(1) { animation-delay: -0.32s; }
        .loading-dot:nth-child(2) { animation-delay: -0.16s; }
        @keyframes bounce {
            0%, 80%, 100% { transform: scale(0); }
            40% { transform: scale(1); }
        }
        .empty-chat {
            text-align: center;
            color: var(--text-secondary);
            padding: var(--space-16);
            animation: fadeIn 0.5s ease;
        }
        .empty-chat h3 {
            margin-bottom: var(--space-4);
            color: var(--text-primary);
            font-size: 1.25rem;
        }
        .empty-chat p { font-size: 0.9375rem; }
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        @keyframes fadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }
    </style>
</head>
<body>
    <jsp:include page="navbar.jsp" />
    
    <div class="main-content">
        <div class="container">
            <a href="<%= request.getContextPath() %>/listCourses" class="back-button" aria-label="Back to courses">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
            </a>
            
            <div class="chat-container">
                <div class="chat-header">
                    <h2>Course Assistant</h2>
                    <p>Ask questions about the course content. I'll help you understand the material.</p>
                </div>
                
                <div class="chat-messages" id="chatMessages">
                    <div class="empty-chat">
                        <h3>Start a conversation</h3>
                        <p>Ask me anything about the course content!</p>
                    </div>
                </div>
                
                <div class="chat-input-container">
                    <form class="chat-input-form" id="chatForm" onsubmit="sendMessage(event)">
                        <textarea 
                            class="chat-input" 
                            id="chatInput" 
                            placeholder="Ask a question about the course..."
                            rows="1"
                            onkeydown="handleKeyDown(event)"></textarea>
                        <button type="submit" class="chat-submit" id="submitBtn">
                            <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="22" y1="2" x2="11" y2="13"></line>
                                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                            </svg>
                            Send
                        </button>
                    </form>
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

        const messagesContainer = document.getElementById('chatMessages');
        const chatForm = document.getElementById('chatForm');
        const chatInput = document.getElementById('chatInput');
        const submitBtn = document.getElementById('submitBtn');
        
        function handleKeyDown(event) {
            if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault();
                sendMessage(event);
            }
        }
        
        function autoResize(textarea) {
            textarea.style.height = 'auto';
            textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
        }
        
        chatInput.addEventListener('input', () => autoResize(chatInput));
        
        async function sendMessage(event) {
            event.preventDefault();
            
            if (!courseId || courseId === '') {
                createToast('Error: Course ID is missing', 'error');
                setTimeout(() => {
                    window.location.href = '<%= request.getContextPath() %>/listCourses';
                }, 2000);
                return;
            }
            
            const question = chatInput.value.trim();
            if (!question) return;
            
            chatInput.value = '';
            autoResize(chatInput);
            
            addMessage('user', question);
            
            const loadingId = addMessage('assistant', '', true);
            
            chatInput.disabled = true;
            submitBtn.disabled = true;

            try {
                const formData = new URLSearchParams();
                formData.append('question', question);
                
                const response = await fetch(apiBaseUrl + '/course/' + courseId + '/chat', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: formData
                });
                
                if (!response.ok) {
                    const errorText = await response.text();
                    let errorMsg = 'Failed to get response';
                    try {
                        const errorJson = JSON.parse(errorText);
                        errorMsg = errorJson.message || errorJson.error || errorMsg;
                    } catch (e) {
                        errorMsg = errorText || errorMsg;
                    }
                    throw new Error(errorMsg);
                }
                
                const data = await response.json();
                
                document.getElementById(loadingId).remove();
                addMessage('assistant', data.answer || data.error || 'No response received', false, data.sources);
                
            } catch (error) {
                console.error('Error:', error);
                const loadingEl = document.getElementById(loadingId);
                if (loadingEl) loadingEl.remove();
                addMessage('assistant', 'Sorry, I encountered an error. Please try again.', false);
                createToast('Error sending message: ' + error.message, 'error');
            } finally {
                chatInput.disabled = false;
                submitBtn.disabled = false;
                chatInput.focus();
            }
        }
        
        function addMessage(role, content, isLoading = false, sources = null) {
            const emptyChat = messagesContainer.querySelector('.empty-chat');
            if (emptyChat) {
                emptyChat.style.animation = 'fadeOut 0.3s ease';
                setTimeout(() => emptyChat.remove(), 300);
            }
            
            const messageId = 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
            const message = document.createElement('div');
            message.className = 'message ' + role;
            message.id = messageId;
            
            const delay = messagesContainer.children.length * 0.05;
            message.style.animationDelay = delay + 's';
            
            const avatar = document.createElement('div');
            avatar.className = 'message-avatar';
            avatar.textContent = role === 'user' ? 'U' : 'AI';
            
            const messageContent = document.createElement('div');
            messageContent.className = 'message-content';
            
            const bubble = document.createElement('div');
            bubble.className = 'message-bubble';
            
            if (isLoading) {
                bubble.innerHTML = '<div class="loading-indicator"><div class="loading-dot"></div><div class="loading-dot"></div><div class="loading-dot"></div></div>';
            } else {
                bubble.textContent = content;
            }
            
            messageContent.appendChild(bubble);
            
            if (sources && sources.length > 0) {
                const sourcesDiv = document.createElement('div');
                sourcesDiv.className = 'message-sources';
                sourcesDiv.textContent = 'Sources: ' + sources.length + ' reference(s)';
                messageContent.appendChild(sourcesDiv);
            }
            
            message.appendChild(avatar);
            message.appendChild(messageContent);
            messagesContainer.appendChild(message);
            
            setTimeout(() => {
                messagesContainer.scrollTo({
                    top: messagesContainer.scrollHeight,
                    behavior: 'smooth'
                });
            }, 100);
            
            return messageId;
        }
    </script>
</body>
</html>

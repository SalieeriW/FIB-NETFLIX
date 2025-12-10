# ============================================
# STEP 1.3: Web Client Dockerfile
# ============================================
# This serves the static web files (HTML, CSS, JS, JSP)

# Use Nginx - lightweight web server
# Alpine = minimal Linux distribution (very small image)
FROM nginx:alpine

# Copy web files to Nginx html directory
# Nginx serves files from /usr/share/nginx/html by default
COPY target/practica5-web-client.war /tmp/webapp.war

# Install unzip to extract WAR file
RUN apk add --no-cache unzip && \
    unzip -q /tmp/webapp.war -d /usr/share/nginx/html/ && \
    rm /tmp/webapp.war && \
    apk del unzip

# Copy custom Nginx configuration
# This handles routing and proxy settings
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80 (HTTP)
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]


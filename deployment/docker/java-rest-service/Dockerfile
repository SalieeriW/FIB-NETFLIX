# ============================================
# STEP 1.2: Java REST Service Dockerfile
# ============================================
# This container runs GlassFish with our REST API
# Using Payara Server (fork of GlassFish) as it's actively maintained

# Use Payara Server (compatible with GlassFish)
# Payara is a fork of GlassFish and is actively maintained
FROM payara/server-full:6.2024.1-jdk17

# Install FFmpeg for video transcoding
# FFmpeg is required for video processing (transcoding, thumbnails, etc.)
# Payara uses Ubuntu-based image, so we use apt-get
# Run as root to install packages
USER root
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/* && \
    ffmpeg -version | head -1

# Set working directory
WORKDIR /opt/payara

# Copy both WAR files (REST service and Web client)
# Payara can serve both - REST API and web pages with JSP
# The WAR files should be in rest-service/target/ and web-client/target/ from the build context root
COPY rest-service/target/practica5-rest-service.war /opt/payara/appserver/glassfish/domains/domain1/autodeploy/
COPY web-client/target/practica5-web-client.war /opt/payara/appserver/glassfish/domains/domain1/autodeploy/
# Also copy to the alternative path (symlink or different Payara versions)
RUN cp /opt/payara/appserver/glassfish/domains/domain1/autodeploy/*.war /opt/payara/glassfish/domains/domain1/autodeploy/ 2>/dev/null || true

# Copy post-boot configuration for large file uploads (2GB)
COPY deployment/docker/java-rest-service/postboot.asadmin /opt/payara/postboot.asadmin

# Copy initialization script for upload directories
COPY deployment/docker/java-rest-service/init-uploads.sh /opt/payara/init-uploads.sh
RUN chmod +x /opt/payara/init-uploads.sh

# Expose Payara ports
# 8080 = HTTP, 4848 = Admin console
EXPOSE 8080 4848

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/practica5-rest-service/resources/ || exit 1

# Start Payara Server with post-boot commands for large file uploads
# Initialize upload directories first (as root), then switch to payara user and start Payara
# Use full path to asadmin since PATH may not be set correctly for payara user
CMD ["/bin/bash", "-c", "/opt/payara/init-uploads.sh && exec su payara -s /bin/bash -c '/opt/payara/appserver/bin/asadmin start-domain --verbose --postbootcommandfile /opt/payara/postboot.asadmin'"]

# ============================================
# STEP 1.2: Java REST Service Dockerfile
# ============================================
# This container runs GlassFish with our REST API

# Use GlassFish official image
# GlassFish is a Java EE application server
FROM glassfish/server-full:latest

# Set working directory
WORKDIR /glassfish5

# Copy the WAR file
# WAR = Web Application Archive (Java web app package)
COPY target/practica5-rest-service.war /glassfish5/glassfish/domains/domain1/autodeploy/

# Expose GlassFish ports
# 8080 = HTTP, 4848 = Admin console
EXPOSE 8080 4848

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/practica5-rest-service/resources/ || exit 1

# Start GlassFish
# asadmin is the GlassFish admin tool
# start-domain starts the application server
CMD ["asadmin", "start-domain", "--verbose"]


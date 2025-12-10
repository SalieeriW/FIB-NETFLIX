# Dockerfile for Apache Derby Network Server
FROM eclipse-temurin:17-jre

# Install Derby
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://archive.apache.org/dist/db/derby/db-derby-10.16.1.1/db-derby-10.16.1.1-bin.zip && \
    unzip db-derby-10.16.1.1-bin.zip && \
    mv db-derby-10.16.1.1-bin /opt/derby && \
    rm db-derby-10.16.1.1-bin.zip && \
    apt-get remove -y wget unzip && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# Set Derby home
ENV DERBY_HOME=/opt/derby
ENV PATH=$PATH:$DERBY_HOME/bin

# Create databases directory
RUN mkdir -p /opt/derby/databases

# Expose Derby network server port
EXPOSE 1527

# Start Derby Network Server
# Use shell form to allow environment variable expansion
CMD $DERBY_HOME/bin/startNetworkServer -h 0.0.0.0 -p 1527


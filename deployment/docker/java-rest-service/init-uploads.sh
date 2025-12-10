#!/bin/bash
# Initialize upload directories with correct permissions

# Create directories if they don't exist
mkdir -p /tmp/vidstream/uploads/videos/original
mkdir -p /tmp/vidstream/videos/processed
mkdir -p /tmp/vidstream/audio

# Set permissions so Payara (running as payara user) can write
chmod -R 777 /tmp/vidstream/uploads
chmod -R 777 /tmp/vidstream/videos
chmod -R 777 /tmp/vidstream/audio

echo "Upload directories initialized with correct permissions"


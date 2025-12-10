# Learning: Docker Concepts

## Key Concepts Explained

### 1. **Dockerfile Stages**
We use **multi-stage builds** to keep images small:
- **Stage 1 (base)**: Minimal Python image
- **Stage 2 (dependencies)**: Install system packages and Python libs
- **Stage 3 (app)**: Copy application code

**Why?** Final image only contains what's needed, not build tools.

### 2. **Layers and Caching**
Docker builds images in layers. Each instruction creates a layer:
```dockerfile
COPY requirements.txt .     # Layer 1
RUN pip install ...         # Layer 2
COPY . .                    # Layer 3
```

**Caching**: If `requirements.txt` doesn't change, Docker reuses Layer 2 (saves time!).

### 3. **Ports**
```yaml
ports:
  - "5001:5001"  # host:container
```
- Left side (5001): Port on your machine
- Right side (5001): Port inside container
- Access via: `http://localhost:5001`

### 4. **Volumes**
Persistent storage that survives container restarts:
```yaml
volumes:
  - python-data:/tmp/vidstream/rag_knowledge
```
- Data stored in `/tmp/vidstream/rag_knowledge` persists
- Even if container is deleted, data remains

### 5. **Networks**
Containers on same network can communicate by service name:
- `python-service:5001` (not `localhost:5001`!)
- Docker DNS resolves service names automatically

### 6. **Health Checks**
Kubernetes uses these to know if container is ready:
```dockerfile
HEALTHCHECK --interval=30s CMD curl http://localhost:5001/api/health
```
- Checks every 30 seconds
- If fails 3 times, container marked unhealthy

## Commands to Try

```bash
# Build an image
docker build -t vidstream-python -f deployment/docker/python-service/Dockerfile python-services/

# Run a container
docker run -p 5001:5001 vidstream-python

# View running containers
docker ps

# View logs
docker logs vidstream-python

# Stop container
docker stop vidstream-python

# Run with Docker Compose
cd deployment/docker
docker-compose up -d  # -d = detached (background)

# View all services
docker-compose ps

# Stop all services
docker-compose down
```

## Next Steps

Once you understand Docker, we'll move to Kubernetes - which orchestrates multiple containers across multiple machines!


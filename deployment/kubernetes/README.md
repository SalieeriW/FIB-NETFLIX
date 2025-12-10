# Step 2: Kubernetes Orchestration

## What is Kubernetes?

Kubernetes (K8s) is a **container orchestrator** - it manages containers across multiple machines.

**Why Kubernetes?**
- **Scaling**: Automatically add/remove containers based on load
- **High Availability**: If a container crashes, K8s starts a new one
- **Load Balancing**: Distributes traffic across multiple containers
- **Rolling Updates**: Update app without downtime

## Key Concepts

### 1. **Pod**
- Smallest deployable unit
- Contains one or more containers
- Containers in a pod share network and storage

### 2. **Deployment**
- Manages pods
- Ensures desired number of pods are running
- Handles updates and rollbacks

### 3. **Service**
- Exposes pods to network
- Load balances traffic
- Provides stable IP address

### 4. **ConfigMap**
- Stores configuration data
- Environment variables, config files
- Can be updated without rebuilding image

### 5. **Secret**
- Stores sensitive data (passwords, API keys)
- Encrypted at rest
- Base64 encoded (not truly secure, but better than plain text)

### 6. **Ingress**
- Routes external traffic to services
- Handles SSL/TLS termination
- URL-based routing

Let's create manifests for each service!


# Learning: Kubernetes Concepts

## Understanding the YAML

### Deployment Structure
```yaml
apiVersion: apps/v1        # K8s API version
kind: Deployment           # Type of resource
metadata:                  # Metadata (name, labels)
spec:                      # Desired state
  replicas: 2             # Run 2 copies
  template:               # Pod template
    spec:
      containers:         # Container definition
```

### Labels and Selectors
**Labels** = tags for organizing resources
```yaml
labels:
  app: python-service
  component: ml-service
```

**Selectors** = find resources by labels
```yaml
selector:
  matchLabels:
    app: python-service
```

### Resources (CPU/Memory)
```yaml
resources:
  requests:    # Minimum guaranteed
    memory: "4Gi"
    cpu: "2"
  limits:      # Maximum allowed
    memory: "8Gi"
    cpu: "4"
```

**Why important?**
- Prevents one pod from using all resources
- Helps scheduler place pods on right nodes
- ML services need more resources!

### Probes
**Liveness**: Is container alive? If not, restart it.
**Readiness**: Is container ready? If not, don't send traffic.

```yaml
livenessProbe:
  httpGet:
    path: /api/health
    port: 5001
  initialDelaySeconds: 120  # Wait 2 min (models loading)
```

### Services
Services provide stable IP and load balancing:
- **ClusterIP**: Internal only
- **NodePort**: Expose on node IP
- **LoadBalancer**: Cloud load balancer
- **Ingress**: HTTP/HTTPS routing

### Persistent Volumes
Data that survives pod restarts:
```yaml
volumeMounts:
- name: python-data
  mountPath: /tmp/vidstream/rag_knowledge
```

## Commands to Try

```bash
# Apply all manifests
kubectl apply -f deployment/kubernetes/

# View deployments
kubectl get deployments

# View pods
kubectl get pods

# View services
kubectl get services

# View logs
kubectl logs -f deployment/python-service

# Describe a pod (debugging)
kubectl describe pod python-service-xxxxx

# Scale deployment
kubectl scale deployment python-service --replicas=3

# Delete everything
kubectl delete -f deployment/kubernetes/
```

## Next: Terraform for Infrastructure


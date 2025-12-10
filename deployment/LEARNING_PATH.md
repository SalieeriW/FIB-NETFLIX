# Complete Learning Path

## 🎯 What You'll Learn

This deployment teaches you:
1. **Docker**: Containerization fundamentals
2. **Kubernetes**: Container orchestration
3. **Terraform**: Infrastructure as Code
4. **Prometheus**: Metrics collection
5. **Grafana**: Visualization and monitoring

## 📚 Learning Order

### Step 1: Docker (Week 1)
**Goal**: Understand containers

**What to do:**
1. Read `deployment/docker/README.md`
2. Study each Dockerfile (understand each line)
3. Build images locally
4. Run with Docker Compose
5. Experiment: modify Dockerfiles, rebuild

**Key concepts:**
- Layers and caching
- Multi-stage builds
- Volumes
- Networks
- Health checks

**Practice:**
```bash
docker build -t my-app .
docker run -p 8080:8080 my-app
docker-compose up
```

### Step 2: Kubernetes (Week 2)
**Goal**: Understand orchestration

**What to do:**
1. Read `deployment/kubernetes/README.md`
2. Study each YAML file
3. Deploy to local cluster (minikube/kind)
4. Understand Pods, Services, Deployments
5. Practice: scale, update, rollback

**Key concepts:**
- Pods vs Containers
- Services and networking
- Deployments and replicas
- ConfigMaps and Secrets
- Persistent Volumes

**Practice:**
```bash
kubectl apply -f deployment.yaml
kubectl get pods
kubectl scale deployment my-app --replicas=3
kubectl rollout undo deployment/my-app
```

### Step 3: Terraform (Week 3)
**Goal**: Understand Infrastructure as Code

**What to do:**
1. Read `deployment/terraform/README.md`
2. Study each .tf file
3. Understand OCI concepts
4. Practice: plan, apply, destroy
5. Modify infrastructure

**Key concepts:**
- Resources and providers
- Variables and outputs
- State management
- Dependencies
- Modules (advanced)

**Practice:**
```bash
terraform init
terraform plan
terraform apply
terraform destroy
```

### Step 4: Monitoring (Week 4)
**Goal**: Understand observability

**What to do:**
1. Read `deployment/monitoring/README.md`
2. Add metrics to your apps
3. Deploy Prometheus
4. Create Grafana dashboards
5. Set up alerts

**Key concepts:**
- Metrics types (Counter, Gauge, Histogram)
- PromQL queries
- Service discovery
- Dashboards
- Alerting

**Practice:**
```bash
# Add metrics endpoint
@app.get("/metrics")
def metrics():
    return generate_latest()

# Query in Prometheus
rate(http_requests_total[5m])
```

## 🏗️ Architecture Understanding

### How It All Fits Together

```
┌─────────────────────────────────────────┐
│  Terraform (Infrastructure)              │
│  - Creates VCN, Subnets                  │
│  - Provisions Kubernetes Cluster         │
│  - Sets up networking                    │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│  Kubernetes (Orchestration)              │
│  - Manages Pods (containers)            │
│  - Load balances traffic                 │
│  - Handles scaling                      │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│  Docker (Containers)                      │
│  - Python Service                        │
│  - Java REST Service                     │
│  - Web Client                            │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│  Prometheus (Metrics)                    │
│  - Scrapes metrics from apps            │
│  - Stores time-series data              │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│  Grafana (Visualization)                 │
│  - Queries Prometheus                    │
│  - Creates dashboards                   │
│  - Sends alerts                         │
└─────────────────────────────────────────┘
```

## 🎓 Learning Tips

### 1. **Start Small**
- Don't try to deploy everything at once
- Test each component separately
- Use local environments first (Docker Compose, minikube)

### 2. **Break Things**
- Intentionally break things to learn
- Delete a pod, see K8s recreate it
- Stop a service, see how others react

### 3. **Read Logs**
- `kubectl logs` is your friend
- `docker logs` for containers
- Prometheus logs for scraping issues

### 4. **Use Documentation**
- Kubernetes: https://kubernetes.io/docs/
- Terraform: https://www.terraform.io/docs/
- Prometheus: https://prometheus.io/docs/

### 5. **Practice Queries**
- Learn PromQL by experimenting
- Start simple, get complex
- Use Grafana's Explore feature

## 🐛 Common Mistakes

1. **Forgetting to expose ports**
   - Container port vs Service port vs Ingress port

2. **Wrong image names**
   - Local: `vidstream-python:latest`
   - Registry: `region.ocir.io/namespace/vidstream-python:latest`

3. **Resource limits too low**
   - ML services need memory!
   - Check with `kubectl top pods`

4. **Not waiting for readiness**
   - Pods need time to start
   - Use `kubectl wait`

5. **Forgetting persistent storage**
   - Data lost on pod restart
   - Use PVCs for important data

## ✅ Checklist

### Docker
- [ ] Understand Dockerfile syntax
- [ ] Build images successfully
- [ ] Run containers locally
- [ ] Use Docker Compose
- [ ] Understand volumes and networks

### Kubernetes
- [ ] Understand Pods, Services, Deployments
- [ ] Deploy applications
- [ ] Scale applications
- [ ] Update applications
- [ ] Debug failed pods

### Terraform
- [ ] Understand HCL syntax
- [ ] Create infrastructure
- [ ] Modify infrastructure
- [ ] Destroy infrastructure
- [ ] Understand state

### Monitoring
- [ ] Add metrics to applications
- [ ] Deploy Prometheus
- [ ] Query metrics (PromQL)
- [ ] Create Grafana dashboards
- [ ] Set up alerts

## 🚀 Next Level

Once you master the basics:

1. **CI/CD Pipeline**
   - GitHub Actions / GitLab CI
   - Automated builds and deployments

2. **Advanced Kubernetes**
   - Helm charts
   - Operators
   - Service mesh (Istio)

3. **Advanced Monitoring**
   - Distributed tracing (Jaeger)
   - Log aggregation (ELK stack)
   - APM tools

4. **Security**
   - Network policies
   - Pod security policies
   - Secrets management (Vault)

5. **Cost Optimization**
   - Auto-scaling
   - Spot instances
   - Resource optimization

## 📖 Resources

### Official Docs
- [Docker](https://docs.docker.com/)
- [Kubernetes](https://kubernetes.io/docs/)
- [Terraform](https://www.terraform.io/docs/)
- [Prometheus](https://prometheus.io/docs/)
- [Grafana](https://grafana.com/docs/)

### Courses
- Kubernetes: https://kubernetes.io/training/
- Terraform: https://learn.hashicorp.com/terraform

### Practice
- Play with Kubernetes: https://labs.play-with-k8s.com/
- Katacoda: https://www.katacoda.com/

## 💡 Final Thoughts

**Take your time** - This is a lot to learn, but each piece builds on the previous one.

**Practice regularly** - Set up and tear down environments frequently.

**Ask questions** - Use Stack Overflow, Reddit (r/kubernetes, r/devops), Discord communities.

**Build projects** - Apply what you learn to real projects.

**You've got this!** 🎉


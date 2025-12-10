# Complete Deployment Guide

## Overview

This guide walks you through deploying VidStream on Oracle Cloud using:
- **Terraform**: Infrastructure
- **Docker**: Containerization  
- **Kubernetes**: Orchestration
- **Prometheus + Grafana**: Monitoring

## Prerequisites

1. **Oracle Cloud Account**
   - Sign up at https://cloud.oracle.com
   - Get Always Free tier

2. **Install Tools**
   ```bash
   # Docker
   curl -fsSL https://get.docker.com | sh
   
   # kubectl
   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
   sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
   
   # Terraform
   wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
   unzip terraform_1.6.0_linux_amd64.zip
   sudo mv terraform /usr/local/bin/
   
   # OCI CLI
   bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
   ```

3. **OCI API Keys**
   ```bash
   # Generate API key in OCI Console
   # User Settings → API Keys → Add API Key
   # Save private key to ~/.oci/oci_api_key.pem
   # Note the fingerprint
   ```

## Step-by-Step Deployment

### Phase 1: Build Docker Images

```bash
# 1. Build Python service
cd python-services
docker build -t vidstream-python:latest -f ../deployment/docker/python-service/Dockerfile .

# 2. Build Java REST service
cd ../rest-service
mvn clean package  # Build WAR file first
docker build -t vidstream-rest:latest -f ../deployment/docker/java-rest-service/Dockerfile .

# 3. Build Web client
cd ../web-client
mvn clean package  # Build WAR file first
docker build -t vidstream-web:latest -f ../deployment/docker/web-client/Dockerfile .

# 4. Test locally with Docker Compose
cd ../deployment/docker
docker-compose up -d
# Test: http://localhost
```

### Phase 2: Push Images to Registry

```bash
# Option A: OCI Container Registry (recommended)
# 1. Create registry in OCI Console
# 2. Login
docker login <region>.ocir.io
# Username: <tenancy-namespace>/<username>
# Password: <auth-token>

# 3. Tag images
docker tag vidstream-python:latest <region>.ocir.io/<namespace>/vidstream-python:latest
docker tag vidstream-rest:latest <region>.ocir.io/<namespace>/vidstream-rest:latest
docker tag vidstream-web:latest <region>.ocir.io/<namespace>/vidstream-web:latest

# 4. Push
docker push <region>.ocir.io/<namespace>/vidstream-python:latest
docker push <region>.ocir.io/<namespace>/vidstream-rest:latest
docker push <region>.ocir.io/<namespace>/vidstream-web:latest

# Option B: Docker Hub (simpler, but public)
docker tag vidstream-python:latest yourusername/vidstream-python:latest
docker push yourusername/vidstream-python:latest
```

### Phase 3: Create Infrastructure with Terraform

```bash
cd deployment/terraform

# 1. Configure variables
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your OCI details

# 2. Initialize Terraform
terraform init

# 3. Plan (review what will be created)
terraform plan

# 4. Apply (create infrastructure)
terraform apply
# Type 'yes' when prompted

# 5. Get kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id $(terraform output -raw cluster_id) \
  --file $HOME/.kube/config \
  --region $(terraform output -raw region) \
  --token-version 2.0.0

# 6. Verify cluster
kubectl get nodes
```

### Phase 4: Deploy to Kubernetes

```bash
# 1. Update image names in Kubernetes manifests
# Edit deployment/kubernetes/*/deployment.yaml
# Change image: vidstream-python:latest to your registry URL

# 2. Create storage
kubectl apply -f deployment/kubernetes/storage/

# 3. Deploy services
kubectl apply -f deployment/kubernetes/python-service/
kubectl apply -f deployment/kubernetes/java-rest-service/
kubectl apply -f deployment/kubernetes/web-client/

# 4. Deploy monitoring
kubectl create configmap prometheus-config \
  --from-file=deployment/monitoring/prometheus/prometheus.yml
kubectl apply -f deployment/kubernetes/monitoring/

# 5. Check status
kubectl get pods
kubectl get services
```

### Phase 5: Configure Ingress

```bash
# 1. Install Nginx Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

# 2. Wait for controller to be ready
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s

# 3. Apply ingress
kubectl apply -f deployment/kubernetes/ingress/

# 4. Get external IP
kubectl get ingress
```

### Phase 6: Access Services

```bash
# Get service URLs
kubectl get ingress vidstream-ingress

# Access:
# - Web: http://<external-ip>/
# - Prometheus: http://<external-ip>:9090 (port-forward)
# - Grafana: http://<external-ip>:3000 (port-forward)

# Port forward for monitoring
kubectl port-forward svc/prometheus 9090:9090
kubectl port-forward svc/grafana 3000:3000
```

## Monitoring Setup

### 1. Add Metrics to Applications

See `deployment/monitoring/python-metrics.py` for examples.

### 2. Create Grafana Dashboards

1. Login to Grafana (admin/admin)
2. Go to Dashboards → Import
3. Use dashboard JSON files (create based on your metrics)

### 3. Set Up Alerts

1. Configure Alertmanager
2. Create alert rules
3. Test alerts

## Troubleshooting

### Pods not starting
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

### Services not accessible
```bash
kubectl get endpoints
kubectl get services
```

### Resource issues
```bash
kubectl top nodes
kubectl top pods
```

## Cost Optimization

1. **Use ARM instances** (free tier)
2. **Right-size resources** (don't over-allocate)
3. **Use spot instances** (if available)
4. **Monitor usage** (set up billing alerts)

## Next Steps

1. Set up CI/CD pipeline
2. Add more monitoring
3. Implement auto-scaling
4. Set up backups
5. Configure SSL/TLS

## Resources

- [Oracle Cloud Documentation](https://docs.oracle.com/en-us/iaas/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Terraform OCI Provider](https://registry.terraform.io/providers/oracle/oci/latest/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)


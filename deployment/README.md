# Deployment Guide: Oracle Cloud with Kubernetes

This guide walks you through deploying the VidStream application on Oracle Cloud using:
- **Terraform**: Infrastructure as Code
- **Docker**: Containerization
- **Kubernetes**: Container orchestration
- **Prometheus + Grafana**: Monitoring

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Oracle Cloud (OCI)                     │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │         Kubernetes Cluster (OKE)                 │   │
│  │                                                   │   │
│  │  ┌──────────────┐  ┌──────────────┐            │   │
│  │  │  Web Client  │  │  REST API    │            │   │
│  │  │  (Nginx)     │  │  (GlassFish) │            │   │
│  │  └──────────────┘  └──────────────┘            │   │
│  │                                                   │   │
│  │  ┌──────────────┐  ┌──────────────┐            │   │
│  │  │ Python ML    │  │  Database   │            │   │
│  │  │ (FastAPI)    │  │  (Derby/    │            │   │
│  │  │              │  │   MySQL)    │            │   │
│  │  └──────────────┘  └──────────────┘            │   │
│  │                                                   │   │
│  │  ┌──────────────┐  ┌──────────────┐            │   │
│  │  │ Prometheus   │  │  Grafana     │            │   │
│  │  │ (Metrics)    │  │  (Dashboards)│            │   │
│  │  └──────────────┘  └──────────────┘            │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## Step-by-Step Learning Path

### Phase 1: Containerization (Docker)
1. Create Dockerfiles for each service
2. Build and test containers locally
3. Understand multi-stage builds
4. Learn Docker networking

### Phase 2: Orchestration (Kubernetes)
1. Create Kubernetes manifests
2. Understand Pods, Services, Deployments
3. Configure ConfigMaps and Secrets
4. Set up Ingress for external access

### Phase 3: Infrastructure (Terraform)
1. Set up OCI provider
2. Create VCN and networking
3. Provision Kubernetes cluster (OKE)
4. Configure security groups

### Phase 4: Monitoring (Prometheus + Grafana)
1. Deploy Prometheus
2. Configure service discovery
3. Set up Grafana dashboards
4. Create alerts

## Prerequisites

Before starting, ensure you have:
- [ ] Oracle Cloud account (free tier)
- [ ] Docker installed locally
- [ ] kubectl installed
- [ ] Terraform installed
- [ ] OCI CLI configured
- [ ] Basic understanding of YAML

### 🚀 Quick Install

**Automated installation (Linux/macOS):**
```bash
cd deployment
./install-prerequisites.sh
```

**Manual installation:**
See [INSTALL.md](INSTALL.md) for detailed instructions for all platforms.

Let's begin!


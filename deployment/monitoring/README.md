# Step 4: Monitoring with Prometheus & Grafana

## What is Prometheus?

Prometheus is a **time-series database** that collects metrics from your applications.

**How it works:**
1. Applications expose metrics (via HTTP endpoint)
2. Prometheus **scrapes** (polls) these endpoints periodically
3. Stores metrics as time-series data
4. Grafana queries Prometheus to create dashboards

## What is Grafana?

Grafana is a **visualization tool** that creates beautiful dashboards from metrics.

**Features:**
- Real-time graphs
- Alerts (email, Slack, etc.)
- Multiple data sources
- Custom dashboards

## Architecture

```
Applications → Metrics Endpoints → Prometheus → Grafana → Dashboards
     ↓              ↓                  ↓           ↓
  /metrics      /metrics          Time-Series   Visual
```

## What We'll Monitor

1. **Application Metrics**
   - Request rate (requests/second)
   - Response time (latency)
   - Error rate (4xx, 5xx)
   - Active users

2. **ML Service Metrics**
   - Transcription time (Whisper)
   - Embedding generation time
   - LLM inference time
   - Vector DB query time

3. **Infrastructure Metrics**
   - CPU usage
   - Memory usage
   - Disk I/O
   - Network traffic

4. **Kubernetes Metrics**
   - Pod status
   - Resource usage per pod
   - Node health

Let's set it up!


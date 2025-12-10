# Learning: Monitoring Concepts

## Prometheus Metrics Types

### 1. **Counter**
- Only goes up (monotonically increasing)
- Example: Total requests, errors
```python
REQUEST_COUNT.inc()  # Increment by 1
REQUEST_COUNT.inc(5)  # Increment by 5
```

### 2. **Gauge**
- Can go up or down
- Example: CPU usage, memory, active users
```python
ACTIVE_USERS.set(42)  # Set to 42
ACTIVE_USERS.inc()    # Increment
ACTIVE_USERS.dec()    # Decrement
```

### 3. **Histogram**
- Tracks distribution of values
- Example: Request duration, response size
```python
REQUEST_DURATION.observe(0.5)  # Record 0.5 seconds
```

### 4. **Summary**
- Like histogram, but with quantiles
- Less common

## PromQL (Prometheus Query Language)

### Basic Queries
```promql
# Request rate (requests per second)
rate(http_requests_total[5m])

# Average response time
rate(http_request_duration_seconds_sum[5m]) / 
rate(http_request_duration_seconds_count[5m])

# Error rate
rate(http_requests_total{status=~"5.."}[5m])

# Memory usage
container_memory_usage_bytes{pod="python-service-xxx"}
```

### Aggregations
```promql
# Sum across all pods
sum(rate(http_requests_total[5m]))

# Average
avg(cpu_usage)

# Max
max(memory_usage)

# Group by label
sum by (pod) (http_requests_total)
```

## Grafana Dashboards

### Panel Types
- **Graph**: Time-series line charts
- **Stat**: Single number (like current CPU)
- **Table**: Tabular data
- **Gauge**: Circular gauge
- **Heatmap**: 2D visualization

### Variables
Make dashboards dynamic:
```json
{
  "name": "pod",
  "type": "query",
  "query": "label_values(pod_name)"
}
```

## Alerts

### Alert Rules
```yaml
groups:
- name: vidstream_alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate detected"
```

### Alertmanager
- Routes alerts to channels (email, Slack, PagerDuty)
- Groups similar alerts
- Suppresses duplicate alerts

## Best Practices

1. **Label Cardinality**
   - Don't use high-cardinality labels (like user_id)
   - Use low-cardinality labels (like endpoint, status)

2. **Scrape Intervals**
   - 15s for most metrics
   - 1m for expensive metrics

3. **Retention**
   - 30 days for detailed data
   - Longer for aggregated data

4. **Dashboards**
   - One dashboard per service
   - Include key metrics at top
   - Use consistent colors

## Next Steps

1. Add metrics to your applications
2. Deploy Prometheus
3. Create Grafana dashboards
4. Set up alerts
5. Monitor and optimize!


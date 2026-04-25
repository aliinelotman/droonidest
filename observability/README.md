# Observability Setup

This project now includes Loki for log aggregation, Prometheus for metrics, and Grafana for visualization.

## Starting Services

```bash
cd c:\dev\droonidest
docker-compose up -d
```

## Accessing Dashboards

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Loki**: http://localhost:3100 (API only)

## Grafana Configuration

1. Log in to Grafana (admin/admin)
2. Add Loki data source:
   - URL: http://loki:3100
   - Save & Test
3. Add Prometheus data source:
   - URL: http://prometheus:9090
   - Save & Test
4. Create dashboards for logs and metrics

## Log Queries

In Grafana Explore (Loki):
- `{job="spring-boot"}` - All backend logs
- `{job="spring-boot"} |= "ERROR"` - Error logs only

## Metrics

Spring Boot metrics available at: http://localhost:8080/actuator/prometheus
# Observability Setup

This project includes:

- Prometheus for Spring Boot metrics
- Loki for centralized logs
- Promtail for collecting backend container stdout
- Grafana for dashboards and exploration
- Alertmanager for Prometheus alert delivery routing

## Starting Services

```bash
cd c:\dev\droonidest
docker-compose up -d
```

## Starting Production Stack

```bash
cd c:\dev\droonidest
docker compose -f docker-compose.prod.yaml up -d
```

Required `.env` values for production:

- `GRAFANA_ADMIN_USER`
- `GRAFANA_ADMIN_PASSWORD`

## Access Points

- Grafana: http://localhost:3000 (development compose only)
- Prometheus: http://localhost:9090
- Loki: http://localhost:3100
- Spring Boot metrics endpoint: http://localhost:8080/actuator/prometheus

In production compose, observability services are internal-only (`expose`), not published with host `ports`.

## Grafana Provisioning

Grafana is already provisioned with:

- `Prometheus` datasource
- `Loki` datasource
- `DrooniDest System Overview` dashboard

After the stack starts, open Grafana and the dashboard should be available automatically.

## Overview Dashboard

The provisioned dashboard focuses on the backend health and request flow:

- Uptime, request rate, 5xx error rate, and p95 latency
- Traffic by HTTP status
- JVM heap, CPU, threads, and database connection pressure
- Top endpoints by traffic
- Slowest endpoints by p95 latency
- Application logs (INFO/WARN/ERROR) and recent request completion logs

This is a strong backend overview, but it is not yet a full infrastructure dashboard because Postgres, MinIO, and frontend-specific exporters are not configured in Prometheus.

## Production Defaults Implemented

- Loki multi-tenant auth enabled with tenant `droonidest`
- Persistent Loki and Promtail state on named volumes
- Loki retention enabled (30 days)
- Prometheus TSDB retention configured (30d, 8GB cap)
- Baseline alerting rules and Alertmanager wiring
- Pinned image versions for Grafana/Loki/Promtail/Prometheus

## Useful Log Queries

In Grafana Explore with the `Loki` datasource:

- `{job="spring-boot"}` for all backend logs
- `{job="spring-boot",compose_service="backend"} |~ "\\b(INFO|WARN|ERROR)\\b"` for application logs by level
- `{job="spring-boot",compose_service="backend"} |= "Request completed"` for request completion logs
- `{job="spring-boot"} |= "corr="` to inspect correlation IDs in log lines

## Useful Metrics

In Grafana Explore or Prometheus:

- `sum(rate(http_server_requests_seconds_count{job="spring-boot"}[5m]))` for total request throughput
- `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="spring-boot"}[5m])))` for p95 latency
- `sum(rate(http_server_requests_seconds_count{job="spring-boot",status=~"5.."}[5m]))` for 5xx traffic
- `jvm_memory_used_bytes{job="spring-boot",area="heap"}` for heap usage

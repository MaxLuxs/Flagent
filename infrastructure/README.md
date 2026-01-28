# Flagent Infrastructure

This directory contains infrastructure-related tools and configurations for Flagent project.

## Contents

### 📊 Grafana
Location: `grafana/`

Grafana dashboards and configurations for monitoring Flagent metrics:
- **Metrics Overview Dashboard** - evaluation metrics, success/error rates, latency
- **Anomaly Alerts Dashboard** - anomaly detection alerts and actions
- **Provisioning** - automatic data source and dashboard setup

See [grafana/README.md](grafana/README.md) for setup instructions.

### 🧪 Load Tests
Location: `load-tests/`

K6 load testing scripts for performance validation:
- **Metrics Load Test** - test metrics API under load
- **Anomaly Detection Load Test** - test anomaly detection system

See [load-tests/README.md](load-tests/README.md) for usage.

### 🔧 Scripts
Location: `scripts/`

Operational scripts for database management:
- **backup-database.sh** - automated PostgreSQL backup with S3 upload
- **restore-database.sh** - restore database from backup

## Quick Start

### Grafana Monitoring
```bash
cd infrastructure/grafana
docker-compose -f docker-compose.grafana.yml up -d
open http://localhost:3000  # admin/admin
```

### Load Testing
```bash
cd infrastructure/load-tests
k6 run metrics-load-test.js
```

### Database Backup
```bash
cd infrastructure/scripts
./backup-database.sh
```

## CI/CD Integration

These infrastructure components are integrated with GitHub Actions:
- `.github/workflows/load-test.yml` - automated load testing
- `.github/workflows/security-scan.yml` - security scanning

## Documentation

For detailed documentation, see:
- [Grafana Setup](grafana/README.md)
- [Load Testing Guide](load-tests/README.md)
- [Backup & Recovery](../docs/operations/backup-recovery.md)

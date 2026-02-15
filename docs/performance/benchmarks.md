# Evaluation Benchmarks

> [English](benchmarks.md) | [Русский](benchmarks.ru.md)

Performance benchmarks for the evaluation API (`POST /api/v1/evaluation`).

## Target Metrics

| Metric | Target | CI Threshold |
|--------|--------|--------------|
| Throughput | ~2000 req/s | — |
| Mean latency | < 1 ms | — |
| p50 | < 5 ms | < 5 ms |
| p95 | < 50 ms | < 50 ms |
| p99 | < 100 ms | < 100 ms |
| Error rate | < 1% | < 1% |

## Running Benchmarks Locally

### Prerequisites

1. **Flagent server** running with test data:

```bash
# Start backend (from repo root)
./gradlew :backend:run

# Or with Docker
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

2. **k6** installed:

```bash
# macOS
brew install k6

# Linux
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update && sudo apt-get install k6
```

### k6 Evaluation Load Test

```bash
cd infrastructure/load-tests

# Default: 200 VUs, 30s, localhost:8000
k6 run evaluation-load-test.js

# High load: 2000 VUs, 60s
k6 run -e EVAL_VUS=2000 -e EVAL_DURATION=60s evaluation-load-test.js

# Custom base URL
k6 run -e BASE_URL=http://localhost:18000 evaluation-load-test.js

# Export results
k6 run --out json=eval-results.json --summary-export=eval-summary.json evaluation-load-test.js
```

### Vegeta Burst Test

```bash
# Requires vegeta: go install github.com/tsenart/vegeta/v12/cmd/vegeta@latest

./evaluation-vegeta.sh                          # 500 req/s, 10s, localhost:8000
./evaluation-vegeta.sh http://localhost:18000 500 30s   # Custom URL, rate, duration
```

## Recommended Hardware (Reproducibility)

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 4 cores | 8+ cores |
| RAM | 4 GB | 8 GB |
| Database | PostgreSQL 15 on same host | Dedicated DB server |

For CI (GitHub Actions): `ubuntu-latest` (2 cores). Use EVAL_VUS=200 for stable CI runs. The [load-test workflow](../../.github/workflows/load-test.yml) starts the server with `PORT=8000` and k6 uses `BASE_URL=http://localhost:8000`.

## Interpreting Results

- **http_reqs (rate)** — requests per second (throughput)
- **http_req_duration p(95)** — 95% of requests complete within this time
- **http_req_failed (rate)** — fraction of failed requests (target < 0.01)

See [tuning-guide.md](tuning-guide.md) for optimization recommendations.

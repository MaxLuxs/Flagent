# Evaluation Benchmarks

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

## Example results (reference baselines)

The following table shows **example** evaluation API results. Numbers depend on hardware, database, and load; use them as a reference only.

| Environment | VUs | Duration | Throughput (req/s) | p50 (ms) | p95 (ms) | p99 (ms) | Error rate |
|-------------|-----|---------|--------------------|----------|----------|----------|------------|
| GitHub Actions (2 vCPU) | 200 | 30s | ~400–800 | 2–4 | 15–40 | 30–80 | < 1% |
| Local (8 vCPU, PostgreSQL) | 200 | 30s | ~800–1500 | 1–3 | 8–25 | 20–60 | < 1% |
| Local (8 vCPU, PostgreSQL) | 2000 | 30s | ~1500–2500 | 2–5 | 20–50 | 50–100 | < 1% |

Results will vary with DB choice (SQLite vs PostgreSQL), network, and background load. CI uses 200 VUs and PostgreSQL; see [load-test workflow](https://github.com/MaxLuxs/Flagent/blob/main/.github/workflows/load-test.yml).

## Reproduce these numbers

1. **Start Flagent**  
   From repo root, either:
   - `./gradlew :backend:run` (default port 18000), or  
   - Docker: `docker run -d -p 18000:18000 -e FLAGENT_ADMIN_EMAIL=admin@local -e FLAGENT_ADMIN_PASSWORD=admin -e FLAGENT_JWT_AUTH_SECRET=dev-secret-at-least-32-characters-long ghcr.io/maxluxs/flagent`

2. **Optional: seed one flag**  
   The evaluation load test script creates 5 flags in `setup()`. If you run the server without auth for local testing, the script will create them. For CI, the [workflow](https://github.com/MaxLuxs/Flagent/blob/main/.github/workflows/load-test.yml) creates flags via `curl` before k6. For a quick local run with default backend port 8000: ensure the server is on port 8000, or set `BASE_URL` in step 3.

3. **Run the evaluation load test**  
   From repo root:
   ```bash
   k6 run -e BASE_URL=http://localhost:18000 -e EVAL_VUS=200 -e EVAL_DURATION=30s infrastructure/load-tests/evaluation-load-test.js
   ```
   If Flagent runs on port 8000 (e.g. `PORT=8000 ./gradlew :backend:run`):
   ```bash
   k6 run -e BASE_URL=http://localhost:8000 -e EVAL_VUS=200 -e EVAL_DURATION=30s infrastructure/load-tests/evaluation-load-test.js
   ```

4. **Inspect output**  
   k6 prints a summary: `http_reqs` (rate = throughput), `http_req_duration` (p50, p95, p99), `http_req_failed` (error rate). For more options (e.g. higher VUs, JSON export), see [infrastructure/load-tests/README.md](../../infrastructure/load-tests/README.md).

## Interpreting Results

- **http_reqs (rate)** — requests per second (throughput)
- **http_req_duration p(95)** — 95% of requests complete within this time
- **http_req_failed (rate)** — fraction of failed requests (target < 0.01)

See [tuning-guide.md](tuning-guide.md) for optimization recommendations.

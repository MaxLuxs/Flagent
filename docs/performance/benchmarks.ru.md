# Бенчмарки evaluation

Бенчмарки производительности для API evaluation (`POST /api/v1/evaluation`).

## Целевые метрики

| Метрика | Цель | Порог в CI |
|--------|------|------------|
| Пропускная способность | ~2000 req/s | — |
| Средняя задержка | < 1 ms | — |
| p50 | < 5 ms | < 5 ms |
| p95 | < 50 ms | < 50 ms |
| p99 | < 100 ms | < 100 ms |
| Доля ошибок | < 1% | < 1% |

## Запуск бенчмарков локально

### Требования

1. **Сервер Flagent** запущен с тестовыми данными:

```bash
# Запуск backend (из корня репозитория)
./gradlew :backend:run

# Или через Docker
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

2. **k6** установлен:

```bash
# macOS
brew install k6

# Linux
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update && sudo apt-get install k6
```

### Нагрузочный тест k6

```bash
cd infrastructure/load-tests

# По умолчанию: 200 VU, 30 с, localhost:8000
k6 run evaluation-load-test.js

# Высокая нагрузка: 2000 VU, 60 с
k6 run -e EVAL_VUS=2000 -e EVAL_DURATION=60s evaluation-load-test.js

# Свой base URL
k6 run -e BASE_URL=http://localhost:18000 evaluation-load-test.js

# Экспорт результатов
k6 run --out json=eval-results.json --summary-export=eval-summary.json evaluation-load-test.js
```

### Burst-тест Vegeta

```bash
# Нужен vegeta: go install github.com/tsenart/vegeta/v12/cmd/vegeta@latest

./evaluation-vegeta.sh                          # 500 req/s, 10 с, localhost:8000
./evaluation-vegeta.sh http://localhost:18000 500 30s   # Свой URL, rate, длительность
```

## Рекомендуемое железо (воспроизводимость)

| Компонент | Минимум | Рекомендуется |
|-----------|---------|---------------|
| CPU | 4 ядра | 8+ ядер |
| RAM | 4 GB | 8 GB |
| БД | PostgreSQL 15 на том же хосте | Отдельный сервер БД |

Для CI (GitHub Actions): `ubuntu-latest` (2 ядра). Используйте EVAL_VUS=200 для стабильных прогонов. [Workflow load-test](../../.github/workflows/load-test.yml) запускает сервер с `PORT=8000`, k6 использует `BASE_URL=http://localhost:8000`.

## Интерпретация результатов

- **http_reqs (rate)** — запросов в секунду (пропускная способность)
- **http_req_duration p(95)** — 95% запросов выполняются быстрее этого времени
- **http_req_failed (rate)** — доля неуспешных запросов (цель < 0.01)

Рекомендации по настройке см. в [tuning-guide.ru.md](tuning-guide.ru.md).

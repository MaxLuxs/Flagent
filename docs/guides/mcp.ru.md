# MCP (Model Context Protocol) для Flagent

> [English](mcp.md) | Русский

Flagent предоставляет **MCP-сервер**, чтобы AI-ассистенты (Cursor, Claude, GigaChat и др.) могли читать и управлять feature flags, выполнять evaluation и анализировать эксперименты.

## Включение MCP

```bash
FLAGENT_MCP_ENABLED=true
FLAGENT_MCP_PATH=/mcp
```

В режиме **eval-only** (`FLAGENT_EVAL_ONLY_MODE=true`) доступны только инструменты чтения и evaluation; `create_flag` и `update_flag` не регистрируются.

## Инструменты

### Чтение и evaluation

| Инструмент | Описание |
|------------|----------|
| **evaluate_flag** | Оценить флаг по key для entity. Возвращает variant, attachment, debug. |
| **list_flags** | Список включённых флагов (опционально limit, tags). |
| **get_flag** | Полная конфигурация флага по key: сегменты, варианты, распределения, ограничения, теги. |
| **analyze_flags** | Сводка по всем включённым флагам: количество, сегменты/варианты. |
| **suggest_segments** | Сегменты флага (ограничения, rollout %, распределения). |
| **optimize_experiment** | Распределение вариантов и rollout для флага (A/B анализ). |

### Запись (только когда не eval-only)

| Инструмент | Описание |
|------------|----------|
| **create_flag** | Создать флаг. Обязательно: `description`. Опционально: `key`, `template`. |
| **update_flag** | Обновить флаг по key. Опционально: `description`, `notes`, `entityType`, `dataRecordsEnabled`, `enabled`. |

Ответы в JSON. Ошибки: `"error": "message"`, `isError: true`.

## Ресурсы

| URI | Описание |
|-----|----------|
| **flagent://flags** | JSON со списком всех включённых флагов (полная конфигурация). |
| **flagent://config/snapshot** | Полный снапшот eval cache (все флаги в JSON). |

Ресурсы можно загружать в контекст AI до вызова инструментов.

## Интеграция

### Cursor

1. Настройки Cursor → MCP.
2. Добавить MCP-сервер: Transport — HTTP (Streamable HTTP), URL — `http://localhost:18000/mcp` (или ваш base URL + `/mcp`).
3. Сохранить; в Cursor появятся инструменты и ресурсы Flagent.

Можно просить: «покажи мои feature flags», «evaluate флаг X для user 123», «создай флаг для checkout experiment».

### Claude / MCP Inspector

```bash
npx -y @modelcontextprotocol/inspector --connect http://localhost:18000/mcp
```

### Примеры промптов

- «Какие feature flags включены?»
- «Evaluate флаг `new_payment_flow` для entity `user_42` с контекстом `{"region":"EU"}`.»
- «Покажи полную конфигурацию флага `checkout_ab_test`.»
- «Создай флаг с key `dark_mode_v2` и описанием "Dark mode experiment".»
- «Обнови описание флага `dark_mode_v2` на "Dark mode A/B test".»
- «Проанализируй флаги и суммируй количество сегментов и вариантов.»
- «Предложи сегменты для флага `checkout_ab_test».»
- «Покажи оптимизацию эксперимента для флага `checkout_ab_test`.»

## Рекомендации

1. **Auth:** MCP использует тот же Ktor-сервер; при защите `/api` защищайте `/mcp` или запускайте MCP только в доверенной сети.
2. **Eval-only:** В read-only или edge-развёртываниях используйте `FLAGENT_EVAL_ONLY_MODE=true`.
3. **Rate limits:** При интенсивном использовании AI поставьте перед Flagent reverse proxy с rate limiting.
4. **Контекст:** Используйте `flagent://config/snapshot` или `flagent://flags`, чтобы у AI был полный список флагов перед предложениями.

Конфигурация: [Configuration → MCP](configuration.md#mcp-model-context-protocol).

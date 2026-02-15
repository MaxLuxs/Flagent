# Flagent Python SDK

Async Python клиент для Flagent feature flags и экспериментов.

## Возможности

- Async/Await (на `httpx`)
- Типы и автодополнение (Pydantic модели)
- Авто-retry на сетевых ошибках
- Connection pooling
- Python 3.8+

## Установка

```bash
pip install flagent-python-client
```

## Быстрый старт

Рекомендуемая точка входа — `create_client()` (как в Kotlin/JS/Go SDK):

```python
import asyncio
from flagent import create_client

async def main():
    client = create_client("http://localhost:18000/api/v1")
    result = await client.evaluate(
        flag_key="new_payment_flow",
        entity_id="user123",
        entity_context={"tier": "premium", "region": "RU"}
    )
    if result.is_enabled():
        print("enabled", result.variant_key)
    # или: enabled = await client.is_enabled("new_feature", entity_id="user123")
    await client.close()

asyncio.run(main())
```

## Ссылки

- Полный (англ.) README: [`README.md`](./README.md)
- OpenAPI: [OpenAPI spec](https://maxluxs.github.io/Flagent/api/openapi.yaml)


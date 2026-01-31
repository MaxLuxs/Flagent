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

```python
import asyncio
from flagent import FlagentClient

async def main():
    async with FlagentClient(base_url="http://localhost:18000/api/v1") as client:
        result = await client.evaluate(
            flag_key="new_payment_flow",
            entity_id="user123",
            entity_context={"tier": "premium", "region": "RU"}
        )

        if result.is_enabled():
            print("enabled", result.variant_key)
        else:
            print("disabled")

asyncio.run(main())
```

## Ссылки

- Полный (англ.) README: [`README.md`](./README.md)
- OpenAPI (RU): [OpenAPI (RU)](https://maxluxs.github.io/Flagent/api/openapi.ru.yaml)


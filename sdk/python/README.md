# Flagent Python SDK

Async Python client for Flagent feature flags and experimentation platform.

## Features

- ✅ **Async/Await** - Built on httpx for modern async Python
- ✅ **Type-Safe** - Full type hints with Pydantic models
- ✅ **Auto-Retry** - Automatic retry on network errors
- ✅ **Connection Pooling** - Efficient HTTP connection management
- ✅ **Python 3.8+** - Support for Python 3.8 through 3.12

## Installation

```bash
pip install flagent-python-client
```

## Quick Start

### Basic Usage

```python
import asyncio
from flagent import FlagentClient

async def main():
    # Create client
    client = FlagentClient(base_url="http://localhost:18000/api/v1")
    
    # Evaluate a flag
    result = await client.evaluate(
        flag_key="new_payment_flow",
        entity_id="user123"
    )
    
    if result.is_enabled():
        print("Feature is enabled")
        print(f"Variant: {result.variant_key}")
    else:
        print("Feature is disabled")
    
    # Close client
    await client.close()

asyncio.run(main())
```

### Using Context Manager

```python
async def main():
    async with FlagentClient(base_url="http://localhost:18000/api/v1") as client:
        result = await client.evaluate(
            flag_key="new_feature",
            entity_id="user123",
            entity_context={"tier": "premium", "region": "US"}
        )
        print(f"Enabled: {result.is_enabled()}")

asyncio.run(main())
```

## API Reference

### Client Initialization

```python
client = FlagentClient(
    base_url="http://localhost:18000/api/v1",  # Required
    api_key="your-api-key",                    # Optional
    timeout=30.0,                              # Optional (seconds)
    max_retries=3                              # Optional
)
```

### Evaluate Flag

```python
result = await client.evaluate(
    flag_key="feature_name",        # Flag key (required if flag_id not provided)
    flag_id=123,                    # Flag ID (required if flag_key not provided)
    entity_id="user123",            # Entity ID for bucketing
    entity_type="user",             # Entity type (optional)
    entity_context={                # Context for constraint matching
        "tier": "premium",
        "region": "US",
        "age": 25
    },
    enable_debug=False              # Enable debug logs
)

# Check if enabled
if result.is_enabled():
    # Feature is enabled
    variant_key = result.variant_key
    
    # Access variant attachment
    color = result.get_attachment_value("color", default="#000")
```

### Batch Evaluation

```python
results = await client.evaluate_batch(
    entities=[
        {
            "entityID": "user1",
            "entityType": "user",
            "entityContext": {"tier": "free"}
        },
        {
            "entityID": "user2",
            "entityType": "user",
            "entityContext": {"tier": "premium"}
        }
    ],
    flag_keys=["feature_a", "feature_b"],
    enable_debug=False
)

for result in results:
    print(f"{result.flag_key} for {result.entity_id}: {result.variant_key}")
```

### Get Flag Details

```python
flag = await client.get_flag(flag_id=123)

print(f"Key: {flag.key}")
print(f"Enabled: {flag.enabled}")
print(f"Segments: {len(flag.segments)}")
print(f"Variants: {len(flag.variants)}")
```

### List Flags

```python
flags = await client.list_flags(
    limit=50,
    offset=0,
    enabled=True,     # Filter by enabled status
    preload=True      # Preload segments, variants, etc.
)

for flag in flags:
    print(f"{flag.key}: {flag.enabled}")
```

### Health Check

```python
health = await client.health_check()
print(f"Status: {health['status']}")
```

## Advanced Usage

### Custom Timeout

```python
client = FlagentClient(
    base_url="http://localhost:18000/api/v1",
    timeout=60.0  # 60 seconds
)
```

### Authentication

```python
client = FlagentClient(
    base_url="http://localhost:18000/api/v1",
    api_key="your-api-key"
)
```

### Error Handling

```python
from flagent import (
    FlagentClient,
    FlagNotFoundError,
    EvaluationError,
    NetworkError
)

async def safe_evaluate():
    try:
        result = await client.evaluate(
            flag_key="nonexistent_flag",
            entity_id="user123"
        )
    except FlagNotFoundError:
        print("Flag not found")
        # Use default behavior
    except EvaluationError as e:
        print(f"Evaluation failed: {e}")
        # Use default behavior
    except NetworkError as e:
        print(f"Network error: {e}")
        # Retry or use cached value
```

### Integration with FastAPI

```python
from fastapi import FastAPI, Depends
from flagent import FlagentClient

app = FastAPI()

# Create global client
flagent_client = FlagentClient(base_url="http://localhost:18000/api/v1")

@app.on_event("shutdown")
async def shutdown_event():
    await flagent_client.close()

@app.get("/features/{feature_name}")
async def check_feature(feature_name: str, user_id: str):
    result = await flagent_client.evaluate(
        flag_key=feature_name,
        entity_id=user_id
    )
    return {
        "feature": feature_name,
        "enabled": result.is_enabled(),
        "variant": result.variant_key
    }
```

### Integration with Django (Async Views)

```python
from django.http import JsonResponse
from asgiref.sync import async_to_sync
from flagent import FlagentClient

# Initialize client (singleton)
flagent_client = FlagentClient(base_url="http://localhost:18000/api/v1")

async def check_feature_view(request, feature_name):
    user_id = request.user.id
    
    result = await flagent_client.evaluate(
        flag_key=feature_name,
        entity_id=str(user_id),
        entity_context={
            "tier": request.user.tier,
            "region": request.user.region
        }
    )
    
    return JsonResponse({
        "feature": feature_name,
        "enabled": result.is_enabled(),
        "variant": result.variant_key
    })
```

### Data Science / ML Workflows

```python
import asyncio
import pandas as pd
from flagent import FlagentClient

async def evaluate_for_users(user_ids: list, flag_key: str):
    """Evaluate a flag for a list of users."""
    async with FlagentClient(base_url="http://localhost:18000/api/v1") as client:
        results = await client.evaluate_batch(
            entities=[{"entityID": uid} for uid in user_ids],
            flag_keys=[flag_key]
        )
    
    return pd.DataFrame([
        {
            "user_id": r.entity_id,
            "variant": r.variant_key,
            "enabled": r.is_enabled()
        }
        for r in results
    ])

# Usage
user_ids = ["user1", "user2", "user3", "user4", "user5"]
df = asyncio.run(evaluate_for_users(user_ids, "experiment_checkout"))
print(df)

# Output:
#   user_id    variant  enabled
# 0   user1    control     True
# 1   user2  treatment     True
# 2   user3    control     True
# 3   user4  treatment     True
# 4   user5    control     True
```

### Jupyter Notebook Integration

```python
# In Jupyter notebook
import nest_asyncio
nest_asyncio.apply()  # Allow nested event loops

from flagent import FlagentClient

client = FlagentClient(base_url="http://localhost:18000/api/v1")

# Evaluate flag
result = await client.evaluate(flag_key="my_experiment", entity_id="user123")
print(f"Variant: {result.variant_key}")

# List all flags
flags = await client.list_flags()
for flag in flags:
    print(f"{flag.key}: {flag.enabled}")
```

## Exception Handling

### Exception Hierarchy

```
FlagentError (base)
├── FlagNotFoundError
├── EvaluationError
├── NetworkError
└── InvalidConfigError
```

### Example

```python
from flagent.exceptions import FlagentError

try:
    result = await client.evaluate(flag_key="test", entity_id="user123")
except FlagentError as e:
    # Handle any Flagent error
    print(f"Error: {e}")
```

## Development

### Install Development Dependencies

```bash
pip install -e ".[dev]"
```

### Run Tests

```bash
pytest
```

### Type Checking

```bash
mypy src/flagent
```

### Code Formatting

```bash
black src/flagent
isort src/flagent
```

## Best Practices

### 1. Reuse Client Instances

```python
# Good: Create once, reuse
client = FlagentClient(base_url="http://localhost:18000/api/v1")

async def evaluate_feature_a():
    return await client.evaluate(flag_key="feature_a", entity_id="user123")

async def evaluate_feature_b():
    return await client.evaluate(flag_key="feature_b", entity_id="user123")

# Bad: Creating new client for each request
async def bad_evaluate():
    client = FlagentClient(base_url="http://localhost:18000/api/v1")
    result = await client.evaluate(flag_key="feature", entity_id="user123")
    await client.close()
```

### 2. Use Context Managers

```python
# Ensures proper cleanup
async with FlagentClient(base_url="http://localhost:18000/api/v1") as client:
    result = await client.evaluate(flag_key="feature", entity_id="user123")
```

### 3. Handle Errors Gracefully

```python
async def safe_feature_check(flag_key: str, user_id: str, default: bool = False) -> bool:
    try:
        result = await client.evaluate(flag_key=flag_key, entity_id=user_id)
        return result.is_enabled()
    except FlagentError:
        return default  # Fallback to default on error
```

### 4. Use Batch Evaluation for Multiple Users

```python
# More efficient than multiple single evaluations
results = await client.evaluate_batch(
    entities=[{"entityID": uid} for uid in user_ids],
    flag_keys=["feature_a"]
)
```

## Performance

### Connection Pooling

The SDK uses httpx with connection pooling by default, which reuses TCP connections for better performance.

### Timeouts

Configure appropriate timeouts based on your latency requirements:

```python
client = FlagentClient(
    base_url="http://localhost:18000/api/v1",
    timeout=5.0  # 5 seconds for low-latency environments
)
```

### Batch Evaluation

Use batch evaluation when evaluating multiple flags for multiple users:

```python
# Evaluate 100 flags for 1000 users in a single request
results = await client.evaluate_batch(
    entities=[{"entityID": f"user{i}"} for i in range(1000)],
    flag_keys=[f"feature_{i}" for i in range(100)]
)
```

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history.

## License

Apache 2.0

## Links

- **Documentation**: https://maxluxs.github.io/Flagent
- **GitHub**: https://github.com/MaxLuxs/Flagent
- **PyPI**: https://pypi.org/project/flagent-python-client

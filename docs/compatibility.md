# API Compatibility

Flagent evaluation API follows industry-standard patterns for feature flag services. This document describes the supported endpoints and request/response formats for easy migration from existing solutions.

## Supported Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/evaluation` | POST | Single flag evaluation |
| `/api/v1/evaluation/batch` | POST | Batch evaluation for multiple flags and entities |
| `/api/v1/flags` | GET, POST | List and create flags |
| `/api/v1/flags/{flagID}` | GET, PUT, DELETE | Flag CRUD operations |
| `/api/v1/flags/{flagID}/segments` | GET, POST | Segment CRUD |
| `/api/v1/flags/{flagID}/segments/{segmentID}` | PUT, DELETE | Segment update/delete |
| `/api/v1/flags/{flagID}/segments/{segmentID}/constraints` | GET, POST | Constraint CRUD |
| `/api/v1/flags/{flagID}/segments/{segmentID}/distributions` | GET, PUT | Distribution management |
| `/api/v1/flags/{flagID}/variants` | GET, POST | Variant CRUD |
| `/api/v1/tags` | GET | List tags |
| `/api/v1/flags/{flagID}/tags` | GET, POST, DELETE | Flag tags |
| `/api/v1/health` | GET | Health check |
| `/api/v1/info` | GET | Version information |

## Evaluation Request Format

### Single Evaluation (`POST /api/v1/evaluation`)

```json
{
  "flagID": 1,
  "flagKey": "new_feature",
  "entityID": "user123",
  "entityType": "user",
  "entityContext": {
    "country": "US",
    "tier": "premium",
    "region": "eu-west"
  },
  "enableDebug": false
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `flagID` | integer | No* | Flag ID. Either `flagID` or `flagKey` must be provided |
| `flagKey` | string | No* | Flag key |
| `entityID` | string | No | Used for deterministic bucketing. If empty, random ID is generated |
| `entityType` | string | No | Entity type (e.g., "user", "session") |
| `entityContext` | object | No | Key-value attributes for constraint matching (country, tier, etc.) |
| `enableDebug` | boolean | No | Include debug info in response (default: false) |

### Batch Evaluation (`POST /api/v1/evaluation/batch`)

```json
{
  "entities": [
    {
      "entityID": "user1",
      "entityType": "user",
      "entityContext": {"country": "US", "tier": "premium"}
    },
    {
      "entityID": "user2",
      "entityType": "user",
      "entityContext": {"country": "CA"}
    }
  ],
  "flagIDs": [1, 2],
  "flagKeys": ["new_feature", "experiment_1"],
  "enableDebug": false
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `entities` | array | Yes | List of entity contexts |
| `flagIDs` | array | No* | Flag IDs to evaluate |
| `flagKeys` | array | No* | Flag keys to evaluate |
| `flagTags` | array | No | Evaluate flags by tags |
| `flagTagsOperator` | string | No | "ANY" or "ALL" for tag matching |
| `enableDebug` | boolean | No | Include debug info |

## Evaluation Response Format

```json
{
  "flagID": 1,
  "flagKey": "new_feature",
  "flagSnapshotID": 1,
  "flagTags": [],
  "segmentID": 1,
  "variantID": 1,
  "variantKey": "control",
  "variantAttachment": {},
  "evalContext": {
    "entityID": "user123",
    "entityType": "user",
    "entityContext": {"country": "US", "tier": "premium"}
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `flagID` | integer | Flag identifier |
| `flagKey` | string | Flag key |
| `segmentID` | integer | Matched segment (null if no match) |
| `variantID` | integer | Assigned variant ID |
| `variantKey` | string | Assigned variant key (e.g., "control", "treatment") |
| `variantAttachment` | object | Additional variant metadata |
| `evalContext` | object | Echo of request context |
| `timestamp` | string | Evaluation timestamp |

## Constraint Operators

Supported operators for segment constraints:

| Operator | Description | Example |
|----------|-------------|---------|
| `EQ` | Equals | `country` EQ `US` |
| `NEQ` | Not equals | `tier` NEQ `free` |
| `LT`, `LTE`, `GT`, `GTE` | Numeric comparison | `age` GT `18` |
| `IN` | In list | `country` IN `US,CA,UK` |
| `NOTIN` | Not in list | `region` NOTIN `blocked` |
| `EREG`, `NEREG` | Regex match | `email` EREG `@company\.com` |
| `CONTAINS`, `NOTCONTAINS` | String contains | `features` CONTAINS `beta` |

## Migration Guide

### From Another Feature Flag Service

1. **Export your flags** – Most services support export. Map your flag structure to Flagent:
   - Flag key/ID
   - Segments and rollout percentages
   - Variants and distributions
   - Constraints (property, operator, value)

2. **Update evaluation calls** – Replace your current SDK calls with Flagent API:
   - Map `entityID` and `entityType` from your user/session identifiers
   - Map `entityContext` from your user attributes (country, tier, etc.)

3. **Batch evaluation** – Use `POST /api/v1/evaluation/batch` when evaluating multiple flags for multiple entities to reduce latency.

4. **SDK integration** – Use Flagent SDKs (Kotlin, Java, JavaScript, Swift, Python, Go) or direct HTTP calls.

### Example: cURL Evaluation

```bash
curl -X POST http://localhost:18000/api/v1/evaluation \
  -H "Content-Type: application/json" \
  -d '{
    "flagKey": "new_feature",
    "entityID": "user123",
    "entityType": "user",
    "entityContext": {"country": "US", "tier": "premium"}
  }'
```

## See Also

- [API Reference](api/endpoints.md)
- [OpenAPI Specification](api/openapi.yaml)
- [Getting Started](getting-started.md)
- [Examples](examples.md)

# Flagent JavaScript/TypeScript Enhanced SDK

Enhanced TypeScript/JavaScript client library for Flagent API with caching and management.

## Installation

```bash
npm install @flagent/enhanced-client
```

**Note**: This library depends on the base Flagent SDK (`@flagent/client`).

## Usage (recommended)

Create a client once with `Flagent.create()`, then use `evaluate`, `isEnabled`, and `evaluateBatch`:

```typescript
import { Flagent } from '@flagent/enhanced-client';

const client = Flagent.create({
  basePath: 'https://api.example.com/api/v1',
  enableCache: true,
  cacheTtlMs: 5 * 60 * 1000, // 5 minutes
});

// Evaluate a flag
const result = await client.evaluate({
  flagKey: 'new_feature',
  entityID: 'user123',
  entityType: 'user',
  entityContext: { region: 'US', tier: 'premium' },
});
console.log('Variant:', result.variantKey);

// Boolean check: true when variant is not "control"
const on = await client.isEnabled({
  flagKey: 'new_feature',
  entityID: 'user123',
});

// Batch evaluation
const results = await client.evaluateBatch({
  flagKeys: ['flag1', 'flag2'],
  entities: [{ entityID: 'user123', entityType: 'user' }],
});
```

Optional auth (Bearer or Basic):

```typescript
const client = Flagent.create({
  basePath: 'https://api.example.com/api/v1',
  accessToken: 'your-bearer-token',
  // or: apiKey: 'key', username: 'u', password: 'p',
});
```

## Advanced: direct FlagentManager

If you need full control (e.g. custom Configuration or reuse of an existing one), use `FlagentManager`:

```typescript
import { FlagentManager, defaultFlagentConfig } from '@flagent/enhanced-client';
import { Configuration } from '@flagent/client';

const configuration = new Configuration({
  basePath: 'https://api.example.com/api/v1',
});
const manager = new FlagentManager(configuration, {
  ...defaultFlagentConfig,
  cacheTtlMs: 5 * 60 * 1000,
});
const result = await manager.evaluate({ flagKey: 'new_feature', entityID: 'user123' });
```

## Features

- **Single entry point**: `Flagent.create(options)` — no need to use EvaluationApi or internal types
- Caching with configurable TTL
- `evaluate`, `isEnabled`, `evaluateBatch` on the client
- Batch evaluation support
- Cache management (`clearCache`, `destroy`)
# Flagent JavaScript/TypeScript Enhanced SDK

Enhanced TypeScript/JavaScript client library for Flagent API with caching and management.

## Installation

```bash
npm install @flagent/enhanced-client
```

**Note**: This library depends on the base Flagent SDK (`@flagent/client`).

## Usage

```typescript
import { FlagentManager, defaultFlagentConfig } from '@flagent/enhanced-client';
import { Configuration } from '@flagent/client';

const config = new Configuration({
  basePath: 'https://api.example.com/api/v1',
});

const manager = new FlagentManager(config, {
  ...defaultFlagentConfig,
  cacheTtlMs: 5 * 60 * 1000, // 5 minutes
});

// Evaluate a flag
const result = await manager.evaluate({
  flagKey: 'new_feature',
  entityID: 'user123',
  entityType: 'user',
  entityContext: { region: 'US', tier: 'premium' },
});

console.log('Variant:', result.variantKey);
```

## Features

- Caching with configurable TTL
- Convenient API
- Batch evaluation support
- Cache management
# Flagent JavaScript Debug UI

Debug UI library for Flagent Enhanced SDK using React.

## Installation

```bash
npm install @flagent/debug-ui
```

## Declarative FeatureFlag

```tsx
import { FeatureFlag, FlagentProvider, useFlag } from '@flagent/debug-ui';
import { FlagentManager } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);

<FlagentProvider manager={manager}>
  <FeatureFlag flagKey="new_checkout" fallback={<OldCheckout />}>
    <NewCheckout />
  </FeatureFlag>
</FlagentProvider>
```

Or use the `useFlag` hook:

```tsx
const { enabled, loading } = useFlag(manager, 'new_checkout', 'user123');
if (loading) return <Spinner />;
return enabled ? <NewCheckout /> : <OldCheckout />;
```

## Usage (Debug UI)

```typescript
import { FlagentDebugUI } from '@flagent/debug-ui';
import { FlagentManager } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);
<FlagentDebugUI manager={manager} />
```

## Features

- List all flags
- View flag details with evaluation logs
- Local overrides
- Evaluation logs viewer
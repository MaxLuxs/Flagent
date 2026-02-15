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

```tsx
import { FlagentDebugPanel } from '@flagent/debug-ui';
import { FlagentManager } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);
<FlagentProvider manager={manager}>
  <FlagentDebugPanel flagKeys={['feature_a', 'feature_b']} />
</FlagentProvider>
```

With **list of all flags** (e.g. from FlagApi or ExportApi):

```tsx
import { FlagentDebugPanel, FlagRow } from '@flagent/debug-ui';
import { FlagApi } from '@flagent/client';

const flagApi = new FlagApi(configuration, basePath, axios);
const flagsProvider = async (): Promise<FlagRow[]> => {
  const res = await flagApi.findFlags(1000);
  return res.data.map((f) => ({
    key: f.key,
    id: f.id,
    enabled: f.enabled,
    variantKeys: f.variants?.map((v) => v.key) ?? [],
  }));
};

<FlagentDebugPanel flagsProvider={flagsProvider} position="bottom-right" />
```

## Features

- **Flags list** — when `flagsProvider` is set, loads and shows all flags with overrides column; Refresh and Clear all overrides
- **Local overrides** — set variant (or disabled) per flag; overrides apply to Evaluate and list
- **Evaluate** — flag key, entity ID/type, Debug checkbox, result and evalDebugLog
- **Cache** — Clear cache, Evict expired
- **Last evaluations** — recent evaluate results
- Optional `flagKeys` for simple on/off list when no flagsProvider
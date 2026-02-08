# Declarative UI

Компоненты `FeatureFlag` и `FlagentProvider` для Compose и React.

## React (`@flagent/debug-ui`)

```tsx
import { FlagentProvider, FeatureFlag } from '@flagent/debug-ui';
import { FlagentManager } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);

<FlagentProvider manager={manager}>
  <FeatureFlag flagKey="new_checkout" fallback={<OldCheckout />}>
    <NewCheckout />
  </FeatureFlag>
</FlagentProvider>
```

### useFlag

```tsx
import { useFlag } from '@flagent/debug-ui';

const { enabled, loading } = useFlag(manager, 'new_feature', 'user123', { tier: 'premium' });
```

## Kotlin Compose (`sdk/kotlin-debug-ui`)

```kotlin
FlagentProvider(manager = manager) {
    FeatureFlag(
        key = "new_checkout",
        fallback = { OldCheckout() }
    ) {
        NewCheckout()
    }
}
```

## См. также

- [Preview environments](preview-environments.md) — передача `entityContext` в preview

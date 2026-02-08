# Flagent SDK для React Native

React Native использует JavaScript runtime, поэтому **можно напрямую использовать существующие npm-пакеты** без отдельного SDK.

## Установка

```bash
npm install @flagent/client @flagent/enhanced-client
# или
yarn add @flagent/client @flagent/enhanced-client
```

## Использование

### Базовый вариант (с кэшем)

```tsx
import { Configuration } from '@flagent/client';
import { FlagentManager } from '@flagent/enhanced-client';

// Один раз при старте приложения
const configuration = new Configuration({
  basePath: 'https://api.example.com/api/v1',
  // Добавьте API key если нужна аутентификация:
  // accessToken: 'your-api-key',
});

const manager = new FlagentManager(configuration, {
  cacheTtlMs: 5 * 60 * 1000, // 5 минут
  enableCache: true,
});

// В компоненте или хуке
const MyComponent = () => {
  const [variant, setVariant] = useState<string | null>(null);

  useEffect(() => {
    manager
      .evaluate({
        flagKey: 'new_feature',
        entityID: 'user123',
        entityContext: { region: 'US' },
      })
      .then((result) => setVariant(result.variantKey ?? null))
      .catch(console.error);
  }, []);

  return variant === 'enabled' ? <NewFeature /> : <LegacyFeature />;
};
```

### С React Query / SWR

```tsx
import { useQuery } from '@tanstack/react-query';

const useFeatureFlag = (flagKey: string, entityID?: string) => {
  return useQuery({
    queryKey: ['flag', flagKey, entityID],
    queryFn: () =>
      manager.evaluate({
        flagKey,
        entityID: entityID ?? 'anonymous',
      }),
    staleTime: 5 * 60 * 1000, // 5 min
  });
};

// Использование
const { data } = useFeatureFlag('new_feature', 'user123');
const isEnabled = data?.variantKey != null;
```

### Опционально: Persistent Cache (AsyncStorage)

Для кэширования между сессиями можно обернуть менеджер:

```tsx
import AsyncStorage from '@react-native-async-storage/async-storage';

// Custom cache implementation using AsyncStorage
const createPersistentCache = () => {
  const CACHE_PREFIX = 'flagent_cache_';
  const CACHE_TTL_KEY = 'flagent_cache_ttl';
  const ttlMs = 5 * 60 * 1000;

  return {
    async get(key: string) {
      const cached = await AsyncStorage.getItem(CACHE_PREFIX + key);
      if (!cached) return null;
      const { value, expiresAt } = JSON.parse(cached);
      if (Date.now() > expiresAt) return null;
      return value;
    },
    async set(key: string, value: any) {
      await AsyncStorage.setItem(
        CACHE_PREFIX + key,
        JSON.stringify({ value, expiresAt: Date.now() + ttlMs })
      );
    },
  };
};
```

> **Примечание:** `@flagent/enhanced-client` использует in-memory кэш. Для persistent cache между перезапусками приложения нужна кастомная реализация (см. выше) или расширение enhanced-client.

## Ссылки

- [JavaScript SDK](../javascript/README.md) — base client
- [JavaScript Enhanced](../javascript-enhanced/README.md) — caching, manager API

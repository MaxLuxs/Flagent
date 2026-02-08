# Preview Environments

> [English](preview-environments.md) | [Русский](preview-environments.ru.md)

Настройка feature flags для preview/PR окружений (Vercel, Netlify, Cloudflare Pages).

## Резервированные ключи entityContext

| Ключ | Описание |
|------|----------|
| `_preview_pr` | Номер PR (string) |
| `_preview_env` | Имя окружения (например `pr-123`) |
| `_branch` | Ветка |

## getPreviewContext() (JavaScript/TypeScript)

В `@flagent/enhanced-client`:

```ts
import { FlagentManager, getPreviewContext } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);

// В preview-среде возвращает { _preview_pr, _branch, _preview_env }
// В production — null
const ctx = getPreviewContext();

const result = await manager.evaluate({
  flagKey: 'new_feature',
  entityContext: ctx ?? undefined,
});
```

## Переменные окружения по платформам

| Платформа | PR | Branch |
|----------|----|----|
| Vercel | `VERCEL_GIT_PULL_REQUEST_ID` | `VERCEL_GIT_COMMIT_REF` |
| Netlify | `REVIEW_ID` | `BRANCH` |
| Cloudflare Pages | `CF_PAGES_PULL_REQUEST` | `CF_PAGES_BRANCH` |

## Таргетинг в Flagent UI

1. Создайте сегмент «Preview PR #123»
2. Добавьте constraint: `_preview_pr` EQ `123`
3. Или: `_preview_env` EQ `pr-123`
4. Или: `_branch` EQ `feature/new-checkout`

## Пример: Vercel

```ts
// app/layout.tsx или root
const previewCtx = getPreviewContext();

<FlagentProvider manager={manager}>
  <FeatureFlag
    flagKey="new_checkout"
    entityContext={previewCtx ?? undefined}
  >
    <NewCheckout />
  </FeatureFlag>
</FlagentProvider>
```

## См. также

- [Trunk-based development](trunk-based-development.md)
- [GitOps](gitops.md)

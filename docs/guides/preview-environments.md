# Preview Environments

> [English](preview-environments.md) | [Русский](preview-environments.ru.md)

Configure feature flags for preview/PR environments (Vercel, Netlify, Cloudflare Pages).

## Reserved entityContext keys

| Key | Description |
|-----|--------------|
| `_preview_pr` | PR number (string) |
| `_preview_env` | Environment name (e.g. `pr-123`) |
| `_branch` | Branch name |

## getPreviewContext() (JavaScript/TypeScript)

In `@flagent/enhanced-client`:

```ts
import { FlagentManager, getPreviewContext } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);

// In preview env returns { _preview_pr, _branch, _preview_env }
// In production — null
const ctx = getPreviewContext();

const result = await manager.evaluate({
  flagKey: 'new_feature',
  entityContext: ctx ?? undefined,
});
```

## Platform environment variables

| Platform | PR | Branch |
|----------|----|----|
| Vercel | `VERCEL_GIT_PULL_REQUEST_ID` | `VERCEL_GIT_COMMIT_REF` |
| Netlify | `REVIEW_ID` | `BRANCH` |
| Cloudflare Pages | `CF_PAGES_PULL_REQUEST` | `CF_PAGES_BRANCH` |

## Targeting in Flagent UI

1. Create segment "Preview PR #123"
2. Add constraint: `_preview_pr` EQ `123`
3. Or: `_preview_env` EQ `pr-123`
4. Or: `_branch` EQ `feature/new-checkout`

## Example: Vercel

```ts
// app/layout.tsx or root
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

## See also

- [Trunk-based development](trunk-based-development.md)
- [GitOps](gitops.md)

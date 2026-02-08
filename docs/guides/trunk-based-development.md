# Trunk-Based Development with Flagent

> [English](trunk-based-development.md) | [Русский](trunk-based-development.ru.md)

Guidelines for using Flagent in trunk-based workflow: feature branches → feature flags.

## Convention: branch → flag key

| Branch | Flag key |
|--------|----------|
| `feature/new-payment` | `feature_new-payment` |
| `fix/FLAG-123` | `fix_flag-123` |
| `refs/heads/feature/foo` | `feature_foo` |

Conversion rules:

- Strip prefix `refs/heads/`
- Replace `/` with `_`
- Replace invalid chars with `_`
- Lowercase

## CLI: create flag from branch

```bash
# From current git branch
./scripts/flagent-cli.sh flag create --from-branch --url https://flagent.example.com --api-key sk-xxx

# From specified branch
./scripts/flagent-cli.sh flag create --from-branch feature/new-payment --url https://flagent.example.com --api-key sk-xxx
```

## GitHub Webhook

On PR open, flag is created automatically. See [GitOps guide](gitops.md#github-webhook).

## Workflow

1. Create branch `feature/new-checkout`
2. Write code wrapped in `FeatureFlag(key="feature_new-checkout")`
3. Open PR → webhook creates flag `feature_new-checkout`
4. In preview env pass `entityContext: { _branch: "feature/new-checkout" }` for targeting
5. After merge — enable flag in production and remove dead code

## Reserved entityContext keys

For preview/PR environments:

| Key | Description |
|-----|-------------|
| `_preview_pr` | PR number (string) |
| `_preview_env` | Environment name (e.g. `pr-123`) |
| `_branch` | Branch name |

See [Preview environments](preview-environments.md).

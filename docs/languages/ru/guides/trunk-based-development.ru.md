# Trunk-Based Development с Flagent

Рекомендации по использованию Flagent в trunk-based workflow: feature ветки → feature flags.

## Конвенция: ветка → ключ флага

| Ветка | Ключ флага |
|-------|------------|
| `feature/new-payment` | `feature_new-payment` |
| `fix/FLAG-123` | `fix_flag-123` |
| `refs/heads/feature/foo` | `feature_foo` |

Правила преобразования:

- Убирается префикс `refs/heads/`
- `/` заменяется на `_`
- Недопустимые символы заменяются на `_`
- Регистр приводится к нижнему

## CLI: создание флага из ветки

```bash
# Из текущей git-ветки
./scripts/flagent-cli.sh flag create --from-branch --url https://flagent.example.com --api-key sk-xxx

# Из указанной ветки
./scripts/flagent-cli.sh flag create --from-branch feature/new-payment --url https://flagent.example.com --api-key sk-xxx
```

## GitHub Webhook

При открытии PR флаг создаётся автоматически. См. [GitOps guide](gitops.md#github-webhook).

## Workflow

1. Создаёте ветку `feature/new-checkout`
2. Пишете код, обёрнутый в `FeatureFlag(key="feature_new-checkout")`
3. Открываете PR → webhook создаёт флаг `feature_new-checkout`
4. В preview-среде передаёте `entityContext: { _branch: "feature/new-checkout" }` для таргетинга
5. После merge — включаете флаг в production и удаляете dead code

## Резервированные ключи entityContext

Для preview/PR окружений:

| Ключ | Описание |
|------|----------|
| `_preview_pr` | Номер PR (string) |
| `_preview_env` | Имя окружения (например `pr-123`) |
| `_branch` | Ветка |

См. [Preview environments](preview-environments.md).

# GitOps с Flagent

> [English](gitops.md) | [Русский](gitops.ru.md)

Flagent поддерживает GitOps-подход: конфигурация флагов хранится в репозитории, синхронизируется через CI/CD.

## Обзор

- **Export**: экспорт флагов в YAML/JSON
- **Import**: импорт из файла в Flagent
- **GitHub Action**: автоматический sync при push в `main`
- **GitHub Webhook**: автоподсказка/создание флага при открытии PR

## Quick Start

### 1. Настройка secrets в GitHub

В настройках репозитория: **Settings → Secrets and variables → Actions** добавьте:

| Secret | Описание |
|--------|----------|
| `FLAGENT_URL` | URL инстанса Flagent (например `https://flagent.example.com`) |
| `FLAGENT_API_KEY` | API ключ для импорта (X-API-Key) |

### 2. Добавьте workflow

Скопируйте [`.github/workflows/flagent-sync.yml`](../../.github/workflows/flagent-sync.yml) в свой репозиторий или создайте:

```yaml
name: Flagent GitOps Sync
on:
  push:
    branches: [main]
    paths: ['flags.yaml', 'flags/*.yaml']
  workflow_dispatch:
jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Sync flags to Flagent
        run: |
          chmod +x scripts/flagent-cli.sh
          ./scripts/flagent-cli.sh import --url ${{ secrets.FLAGENT_URL }} --file flags.yaml --api-key ${{ secrets.FLAGENT_API_KEY }}
```

### 3. Формат flags.yaml

Пример:

```yaml
flags:
  - key: new_checkout
    description: "New checkout flow"
    enabled: false
  - key: dark_mode
    description: "Dark mode UI"
    enabled: true
```

Полный формат в [Import API](../api/endpoints.md#import).

## CLI

### Export

```bash
./scripts/flagent-cli.sh export --url https://flagent.example.com --output flags.yaml --api-key sk-xxx
```

### Import

```bash
./scripts/flagent-cli.sh import --url https://flagent.example.com --file flags.yaml --api-key sk-xxx
```

### Создание флага из ветки (Trunk-based)

```bash
# Из текущей git-ветки
./scripts/flagent-cli.sh flag create --from-branch --url https://flagent.example.com --api-key sk-xxx

# Из указанной ветки
./scripts/flagent-cli.sh flag create --from-branch feature/new-payment --url https://flagent.example.com --api-key sk-xxx
```

Имя ветки преобразуется в ключ флага: `feature/new-payment` → `feature_new-payment`.

---

## GitHub Webhook (автосоздание флага при PR) {#github-webhook}

При открытии Pull Request Flagent может автоматически создать флаг по имени ветки.

### Настройка в GitHub

1. **Settings → Webhooks → Add webhook**

2. **Payload URL**:
   ```
   https://your-flagent.com/api/v1/integrations/github/webhook
   ```

3. **Content type**: `application/json`

4. **Secret**: сгенерируйте случайную строку и укажите её. Эту же строку задайте в переменной окружения Flagent `FLAGENT_GITHUB_WEBHOOK_SECRET`.

5. **Which events**: выберите **Let me select individual events** → **Pull requests**

6. Сохраните webhook.

### Настройка Flagent

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `FLAGENT_GITHUB_WEBHOOK_SECRET` | Секрет для проверки подписи (обязательно для production) | — |
| `FLAGENT_GITHUB_AUTO_CREATE_FLAG` | Включить автоподсказку/создание флага при PR | `true` |

### Поведение

- При событии **pull_request** с `action: opened` или `synchronize`
- Берётся ветка PR: `pull_request.head.ref` (например `feature/new-payment`)
- Преобразуется в ключ: `feature_new-payment`
- Если флаг с таким ключом **не существует** — создаётся с описанием `Auto from PR #<number> branch: <branch>`
- Если флаг **уже существует** — возвращается 200 без изменений

### Безопасность

- **Обязательно** задайте `FLAGENT_GITHUB_WEBHOOK_SECRET` в production
- Webhook проверяет заголовок `X-Hub-Signature-256` (HMAC SHA256)
- Без корректного секрета запросы отклоняются с 401

### Тестирование webhook

```bash
# Проверка (без секрета, если FLAGENT_GITHUB_WEBHOOK_SECRET пуст)
curl -X POST https://your-flagent.com/api/v1/integrations/github/webhook \
  -H "Content-Type: application/json" \
  -d '{"action":"opened","pull_request":{"number":1,"head":{"ref":"feature/test"}}}'
```

Юнит-тест с моком для этого сценария отключён (в testApplication может возвращаться 500); сценарий «создание флага из PR» проверяется на живом сервере Flagent или в интеграционных тестах.

---

## См. также

- [Trunk-based development](trunk-based-development.md)
- [Preview environments](preview-environments.md)

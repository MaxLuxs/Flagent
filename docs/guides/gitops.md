# GitOps with Flagent

> [English](gitops.md) | [Русский](gitops.ru.md)

Flagent supports GitOps: flag configuration lives in the repository and syncs via CI/CD.

## Overview

- **Export**: export flags to YAML/JSON
- **Import**: import from file into Flagent
- **GitHub Action**: automatic sync on push to `main`
- **GitHub Webhook**: auto-create flag when PR is opened

## Quick Start

### 1. Configure GitHub secrets

In repository **Settings → Secrets and variables → Actions** add:

| Secret | Description |
|--------|-------------|
| `FLAGENT_URL` | Flagent instance URL (e.g. `https://flagent.example.com`) |
| `FLAGENT_API_KEY` | API key for import (X-API-Key) |

### 2. Add workflow

Copy [`.github/workflows/flagent-sync.yml`](../../.github/workflows/flagent-sync.yml) or create:

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

### 3. flags.yaml format

Example:

```yaml
flags:
  - key: new_checkout
    description: "New checkout flow"
    enabled: false
  - key: dark_mode
    description: "Dark mode UI"
    enabled: true
```

Full format: [Import API](../api/endpoints.md#import).

## CLI

### Export

```bash
./scripts/flagent-cli.sh export --url https://flagent.example.com --output flags.yaml --api-key sk-xxx
```

### Import

```bash
./scripts/flagent-cli.sh import --url https://flagent.example.com --file flags.yaml --api-key sk-xxx
```

### Create flag from branch (Trunk-based)

```bash
# From current git branch
./scripts/flagent-cli.sh flag create --from-branch --url https://flagent.example.com --api-key sk-xxx

# From specified branch
./scripts/flagent-cli.sh flag create --from-branch feature/new-payment --url https://flagent.example.com --api-key sk-xxx
```

Branch name is converted to flag key: `feature/new-payment` → `feature_new-payment`.

---

## GitHub Webhook (auto-create flag on PR) {#github-webhook}

When a Pull Request is opened, Flagent can create a flag from the branch name.

### GitHub setup

1. **Settings → Webhooks → Add webhook**

2. **Payload URL**:
   ```
   https://your-flagent.com/api/v1/integrations/github/webhook
   ```

3. **Content type**: `application/json`

4. **Secret**: generate a random string and set it. Use the same value for Flagent env var `FLAGENT_GITHUB_WEBHOOK_SECRET`.

5. **Which events**: **Let me select individual events** → **Pull requests**

6. Save webhook.

### Flagent configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `FLAGENT_GITHUB_WEBHOOK_SECRET` | Secret for signature verification (required in production) | — |
| `FLAGENT_GITHUB_AUTO_CREATE_FLAG` | Enable auto-create flag on PR | `true` |

### Behavior

- On **pull_request** with `action: opened` or `synchronize`
- Uses branch from `pull_request.head.ref` (e.g. `feature/new-payment`)
- Converts to key: `feature_new-payment`
- If flag **does not exist** — creates with description `Auto from PR #<number> branch: <branch>`
- If flag **already exists** — returns 200 with no changes

### Security

- **Always** set `FLAGENT_GITHUB_WEBHOOK_SECRET` in production
- Webhook validates `X-Hub-Signature-256` (HMAC SHA256)
- Without valid secret, requests are rejected with 401

### Testing webhook

```bash
# Without secret (only if FLAGENT_GITHUB_WEBHOOK_SECRET is empty)
curl -X POST https://your-flagent.com/api/v1/integrations/github/webhook \
  -H "Content-Type: application/json" \
  -d '{"action":"opened","pull_request":{"number":1,"head":{"ref":"feature/test"}}}'
```

---

## See also

- [Trunk-based development](trunk-based-development.md)
- [Preview environments](preview-environments.md)

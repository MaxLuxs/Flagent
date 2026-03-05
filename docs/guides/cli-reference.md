# CLI Reference

The Flagent CLI is a bash script that uses `curl` and `jq`. Run it from the repo root: `./scripts/flagent-cli.sh`.

**Requirements:** `curl`, `jq` (for commands that create flags or evaluate).

## Common options

| Option | Description |
|--------|-------------|
| `--url <base>` | Flagent base URL (e.g. `http://localhost:18000` or `https://flagent.example.com`). Required for all commands. |
| `--api-key <key>` | Optional. Tenant API key (sent as `X-API-Key`). Omit in OSS mode without tenants. |

---

## Commands

### Export (GitOps)

Export flags to a file.

```bash
./scripts/flagent-cli.sh export --url https://flagent.example.com [--output flags.yaml] [--api-key sk-xxx]
```

- `--output` — output file path (default: `flags.yaml`). Use `.json` for JSON.

### Import (GitOps)

Import flags from a file.

```bash
./scripts/flagent-cli.sh import --url https://flagent.example.com --file flags.yaml [--api-key sk-xxx]
```

- `--file` — path to YAML or JSON file (GitOps format).

### Sync (alias for import)

```bash
./scripts/flagent-cli.sh sync --url https://flagent.example.com --file flags.yaml [--api-key sk-xxx]
```

### Flags list

List flags.

```bash
./scripts/flagent-cli.sh flags list --url http://localhost:18000 [--limit N] [--offset N] [--key KEY] [--output json] [--api-key sk-xxx]
```

- `--limit` — max number of flags.
- `--offset` — pagination offset.
- `--key` — filter by flag key (exact or partial, depending on API).
- `--output json` — print raw JSON instead of a table.

**Example:**

```bash
./scripts/flagent-cli.sh flags list --url http://localhost:18000
./scripts/flagent-cli.sh flags list --url http://localhost:18000 --key payment --output json
```

### Flags create

Create a flag by key.

```bash
./scripts/flagent-cli.sh flags create --key KEY --description "..." [--enabled] --url http://localhost:18000 [--api-key sk-xxx]
```

- `--key` — flag key (required).
- `--description` — description text.
- `--enabled` — create the flag as enabled.

**Example:**

```bash
./scripts/flagent-cli.sh flags create --key my_feature --description "My feature flag" --enabled --url http://localhost:18000
```

### Eval

Evaluate a flag for an entity.

```bash
./scripts/flagent-cli.sh eval --flag-key KEY [--entity-id ID] [--entity-type TYPE] [--context JSON] --url http://localhost:18000 [--output json] [--api-key sk-xxx]
```

- `--flag-key` — flag key (required).
- `--entity-id` — entity ID (default: `user1`).
- `--entity-type` — entity type (default: `user`).
- `--context` — JSON object for targeting context (e.g. `'{"tier":"premium"}'`).
- `--output json` — print raw evaluation response.

**Example:**

```bash
./scripts/flagent-cli.sh eval --flag-key new_payment_flow --entity-id user1 --url http://localhost:18000
./scripts/flagent-cli.sh eval --flag-key new_payment_flow --entity-id user2 --context '{"tier":"premium"}' --url http://localhost:18000 --output json
```

### Flag create (from branch)

Create a flag whose key is derived from the current (or given) git branch. See [Trunk-Based Development](trunk-based-development.md).

```bash
./scripts/flagent-cli.sh flag create --from-branch [branch] --url https://flagent.example.com [--api-key sk-xxx]
```

---

## See also

- [GitOps](gitops.md) — export/import workflow and GitHub Action.
- [Trunk-Based Development](trunk-based-development.md) — branch-based flag creation.
- [API Endpoints](../api/endpoints.md) — HTTP API reference.

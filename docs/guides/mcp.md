# MCP (Model Context Protocol) for Flagent

> [English](mcp.md) | [Русский](mcp.ru.md)

Flagent exposes a **Model Context Protocol (MCP)** server so AI assistants (Cursor, Claude, GigaChat, etc.) can read and manage feature flags, evaluate them, and analyze experiments.

## Enable MCP

```bash
FLAGENT_MCP_ENABLED=true
FLAGENT_MCP_PATH=/mcp
```

When running in **eval-only mode** (`FLAGENT_EVAL_ONLY_MODE=true`), the server only exposes read and evaluate tools; `create_flag` and `update_flag` are not registered.

## Tools

### Read & Evaluate

| Tool | Description |
|------|-------------|
| **evaluate_flag** | Evaluate a flag by key for an entity. Returns variant, attachment, debug message. Use for "what would user X get for flag Y?" |
| **list_flags** | List enabled flags with optional `limit` and `tags` filter. Returns id, key, description, enabled. |
| **get_flag** | Full flag configuration by key: segments, variants, distributions, constraints, tags. |
| **analyze_flags** | Summary of all enabled flags: total count, list with segments/variants counts. Useful for AI to see what exists. |
| **suggest_segments** | List segments for a flag (constraints, rollout percent, distributions). Use to understand targeting and suggest changes. |
| **optimize_experiment** | Variant distribution and rollout summary for a flag. Use to analyze A/B setup and suggest balance. |

### Write (only when not in eval-only mode)

| Tool | Description |
|------|-------------|
| **create_flag** | Create a new flag. Required: `description`. Optional: `key`, `template` (e.g. `simple_boolean_flag`). Returns id, key, description, enabled. |
| **update_flag** | Update a flag by key. Optional: `description`, `notes`, `entityType`, `dataRecordsEnabled`, `enabled` (turn flag on/off). |

All tools return JSON. Errors are returned with `"error": "message"` and `isError: true`.

## Resources

| URI | Description |
|-----|-------------|
| **flagent://flags** | JSON list of all enabled flags (full config). |
| **flagent://config/snapshot** | Full eval cache snapshot (all flags as JSON). |

Resources can be used by AI clients to load context before calling tools.

## Integration Examples

### Cursor

1. Open Cursor Settings → MCP.
2. Add a new MCP server:
   - **Transport:** HTTP (Streamable HTTP).
   - **URL:** `http://localhost:18000/mcp` (or your Flagent base URL + `/mcp`).
3. Save; Cursor will list Flagent tools and resources.

You can then ask Cursor to "list my feature flags", "evaluate flag X for user 123", or "create a new flag for checkout experiment".

### Claude / MCP Inspector

Test with MCP Inspector:

```bash
npx -y @modelcontextprotocol/inspector --connect http://localhost:18000/mcp
```

Use the Inspector to call tools and read resources.

### Example prompts (for any MCP client)

- "What feature flags are enabled?"
- "Evaluate flag `new_payment_flow` for entity id `user_42` with context `{\"region\":\"EU\"}`."
- "Show me the full config for flag `checkout_ab_test`."
- "Create a new flag with key `dark_mode_v2` and description 'Dark mode experiment'."
- "Update the description of flag `dark_mode_v2` to 'Dark mode A/B test'."
- "Analyze flags and summarize how many segments and variants each has."
- "Suggest segments for flag `checkout_ab_test`."
- "Show experiment optimization info for flag `checkout_ab_test`."

## Best practices

1. **Auth:** MCP endpoint uses the same Ktor app; if you protect `/api` with auth, consider protecting `/mcp` or running MCP only in trusted networks.
2. **Eval-only:** In read-only or edge deployments, use `FLAGENT_EVAL_ONLY_MODE=true` so create/update tools are not exposed.
3. **Rate limits:** For heavy AI usage, put Flagent behind a reverse proxy with rate limiting.
4. **Context:** Use `flagent://config/snapshot` or `flagent://flags` so the AI has full flag list before suggesting changes.

## Configuration reference

See [Configuration → MCP](configuration.md#mcp-model-context-protocol) for `FLAGENT_MCP_ENABLED` and `FLAGENT_MCP_PATH`.

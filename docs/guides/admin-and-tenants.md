# Admin and Tenants: Who Is Who and How It Works

This guide explains the difference between **admin** and **tenant** in Flagent, why the UI asks you to "choose a tenant" even when you are admin, and how to work with this model.

---

## What is a Tenant?

A **tenant** is a **customer or workspace**: one isolated set of data (flags, segments, variants, experiments, metrics). Think of it as "one company" or "one project" in a multi-tenant setup.

- **Data isolation** — Tenant A cannot see or change Tenant B's flags. Each tenant has its own schema (or tenant-scoped tables) in the database.
- **API key** — Each tenant has one or more API keys. Your mobile app, backend, or SDK use `X-API-Key: <tenant-api-key>` so the server knows which tenant's data to read/write.
- **Usage & billing** — Evaluations, API calls, and crash reports are counted per tenant (for limits and billing in SaaS).

So: **tenant = whose data we are working with**. Every request that touches flags/segments/experiments must answer "which tenant?" — and that comes from the API key (or from a JWT that already contains `tenant_id`).

---

## What is Admin?

**Admin** is the **platform operator**: the person who manages the Flagent instance (or the SaaS platform). Admin is **not** "see and do everything inside every tenant" in the UI.

- **Admin can:**
  - Log in to the **admin UI** (email/password or SSO, depending on config).
  - Create and list **tenants** (`POST /admin/tenants`, `GET /admin/tenants`).
  - Manage billing, SSO, smart rollout, anomaly config (enterprise).
  - Access **admin-only routes** protected by `X-Admin-Key` or JWT with `admin: true`.

- **Admin does not automatically get:**
  - Access to tenant data (flags, segments, etc.) **without** a tenant context. The APIs that return flags (`/api/v1/flags`, dashboard stats, etc.) are **tenant-scoped**. The server must know *which tenant* to answer for; that comes from `X-API-Key` or from a JWT that has `tenant_id` (e.g. SSO users of that tenant).

So: **admin = who can manage the platform and create tenants**. It is not "I am admin, so I see all tenants’ data in one view." The UI is built to work **inside one tenant at a time** (the one whose API key you are using).

---

## Why Does the UI Ask Me to Choose a Tenant Even When I'm Admin?

Because the **UI is tenant-scoped**:

1. When you open Dashboard, Flags, Experiments, etc., the frontend calls APIs like `/api/v1/flags`, `/api/v1/stats`, etc.
2. Those APIs require a **tenant context**: they need to know *which tenant’s* flags and stats to return.
3. Tenant context is determined by:
   - **`X-API-Key`** — the tenant’s API key (what the browser stores after you create a tenant or "Use" a tenant you created in this browser), or
   - **Bearer JWT with `tenant_id`** — used when users log in via **SSO into a specific tenant** (then the JWT already says which tenant they belong to).

When you log in as **admin** (email/password), you get a JWT that says "this person is admin." It does **not** contain a `tenant_id`, because admin is a global role, not tied to one tenant. So the server cannot infer "which tenant’s data to show." The UI therefore needs an API key (or equivalent) to send with every request — and that API key is tied to **one tenant**. So you must either:

- **Create a tenant** (you get its API key and the UI stores it), or  
- **Use an existing tenant** for which you already have the API key in this browser (e.g. you created it here earlier).

That’s why you are asked to "choose" or create a tenant: not to limit admin rights, but to answer "**in which tenant’s context** should the UI work right now?"

---

## How to Work With This in Practice

### Single-tenant (one company, one Flagent instance)

1. Log in as admin.
2. Create **one** tenant (e.g. "My Company") — you get its API key.
3. The UI stores that API key and uses it for all requests. You work only in that tenant.
4. Your apps/SDKs use the same API key in `X-API-Key` for evaluations.

You don’t need to "switch" tenants unless you add more later.

### Multi-tenant (SaaS: many customers)

1. Log in as admin.
2. Create a tenant per customer (e.g. "Acme Corp", "Beta Inc"). Each has its own API key.
3. In the admin UI, you **switch** which tenant you’re acting in: choose "Use" for a tenant whose API key is stored in this browser (usually one you just created or used before). The UI then sends that tenant’s API key with every request.
4. End users of a customer can log in via **SSO** into *that* tenant; their JWT contains `tenant_id`, so they don’t need to pick a tenant — they’re already scoped.

So:

- **Admin** = platform role (create tenants, manage billing, etc.).
- **Tenant** = data scope (whose flags/experiments we see and edit).
- **Choosing a tenant** in the UI = choosing which tenant’s API key to send so that the UI can load that tenant’s data.

---

## API Key: Save Once, Use Everywhere

Flagent follows the same **display-once** pattern used by Stripe, AWS, and other platforms: the tenant API key is shown **only once** when the tenant is created. After that it cannot be viewed again in the UI (only regenerated or new keys created, depending on product).

### When you create a tenant

1. **Right after creation** you see a success step with your API key and a **Copy** button.
2. **Save the key immediately** to a password manager, `.env` file, or secure notes. Use it for:
   - **SDK / app config** — set `X-API-Key` (or equivalent) in your backend, mobile app, or frontend so evaluations and API calls are tenant-scoped.
   - **Another browser or device** — on the Tenants page use **“Use API key from another device”**, paste the key, and continue. The key is then stored in that browser for that tenant.
3. The UI reminds you: *“Save your API key now — it won’t be shown again.”* A short hint explains that the key is used in the app (X-API-Key) or on the Tenants page in another browser.

### If you didn’t save the key

- **Same browser:** The key is already stored for the tenant you just created; you can keep working. Copy it later only if you need it for another device or for config (e.g. from DevTools → Application → Local Storage → `api_key`). Prefer not to rely on that; save it at creation time.
- **Other browser / new device:** Use a key that was saved at creation (from a teammate, password manager, or env). Paste it in **“Use API key from another device”** on the Tenants page.
- **Lost key (no copy anywhere):** The **same** key cannot be restored — it is stored only as a hash. You can get a **new** key:
  - **Admin / support:** Create a new API key for the tenant via **POST /admin/tenants/{tenantId}/api-keys** (Enterprise). Body: `{ "name": "Recovery", "scopes": [] }` (empty scopes = full default). The response contains the new key **once**; hand it to the user through a secure channel (e.g. password manager share, secure link). The user then pastes it in “Use API key from another device” or uses it in SDK config.
  - **If you still have one valid key or SSO:** In that tenant context you can create additional keys via **POST /tenants/me/api-keys** (e.g. from Settings or API). So you only need admin recovery when **all** keys were lost.

### Best practices (like in “cool” companies)

- **One-time display** — Key is shown only at creation; the UI warns you to copy it.
- **Copy button** — One click copies the key to the clipboard; store it in a secure place.
- **Don’t commit keys** — Keep keys in env vars, secret managers, or CI secrets, not in source code.
- **Per-environment keys** — Use different keys for dev/staging/prod when the product supports it.

---

## Short Summary

| Concept | Meaning |
|--------|--------|
| **Tenant** | One customer/workspace: isolated flags, segments, API keys, usage. |
| **Admin** | Platform operator: can create tenants and use admin APIs; does not automatically have "one key to see all tenants" in the UI. |
| **Why choose a tenant?** | The app and API are tenant-scoped; the server needs an API key (or JWT with `tenant_id`) to know which tenant’s data to return. |
| **How to "see" a tenant** | Create it (you get the API key) or "Use" a tenant you created in this browser; the UI then uses that tenant’s API key for all requests. |

Once you have one tenant and its API key stored in the browser, you work as admin **inside that tenant** until you switch to another tenant (if you have multiple and have their keys stored).

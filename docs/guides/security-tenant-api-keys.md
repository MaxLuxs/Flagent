# Security: Tenant API Keys and Admin Endpoints

This document describes secure development practices for tenant provisioning and API key handling in Flagent (including Enterprise).

---

## API key lifecycle

### Creation and storage (backend)

- **API keys are generated** with cryptographically secure random data (`SecureRandom`, Base64URL).
- **Only the key hash** (SHA-256) is stored in the database. The raw key cannot be recovered from the hash.
- **Keys are returned once** in the HTTP response body at creation time. They are never logged or included in error responses.

### Logging rules

- **Do not log** the raw API key or any secret in application or audit logs.
- **Allowed:** tenant ID, key name, action type (e.g. "Admin created API key for tenant 1, key name=Recovery").
- **Not allowed:** `apiKey` value, key hash in user-facing messages, or any variable that might hold the key in stack traces. In `TenantRoutes`, error logging uses only `e.message` (no full exception with stack) for create-api-key failures.

### Error responses

- **4xx/5xx** must never include the API key or key hash in the response body.
- Use generic messages: `"Tenant not found"`, `"Failed to create API key"`, `"Invalid scope"`. Do not echo request parameters that could contain sensitive data (e.g. scope list in error text was reduced to `"Invalid scope"`).

---

## Admin endpoints

### Protection

- All routes under **`/admin`** are protected by **AdminAuthMiddleware** when `FLAGENT_ADMIN_AUTH_ENABLED=true` or `FLAGENT_ADMIN_API_KEY` is set.
- Valid auth is either:
  - **Header** `X-Admin-Key: <FLAGENT_ADMIN_API_KEY>`, or
  - **Bearer JWT** with claim `admin: true` (signed with `FLAGENT_JWT_AUTH_SECRET`).
- Requests to `/admin/*` without valid auth receive **401** and a generic message (`"Admin authentication required"`).

### POST /admin/tenants/{id}/api-keys

- **Purpose:** Allow admin (or support) to create a **new** API key for an existing tenant (e.g. key recovery).
- **Auth:** Admin only (see above).
- **Validation:** `id` must be a valid tenant ID; tenant must exist. Invalid or non-existent tenant returns **404** with body `{"error":"Tenant not found"}`.
- **Response:** **201** with body `{"apiKey":"<secret>","apiKeyInfo":{...}}`. The key is shown once; the client (UI or script) must store or hand it off securely.
- **Tests:** Unit tests assert 201 with `apiKey` and `apiKeyInfo`, 404 for missing tenant, 400 for invalid scope or invalid id, and that **error** responses do not contain the key or prefix `fla_`. Middleware tests assert that POST to this path **without** admin auth returns **401**.

---

## Frontend and client

- **Storage:** API key is stored in `localStorage` under a fixed key (e.g. `api_key`). This is acceptable for admin UI in a trusted environment; for production, consider HTTP-only cookies or short-lived tokens where applicable.
- **Transmission:** Key is sent only in the **`X-API-Key`** header for API requests, never in URL or query parameters.
- **Display:** Key is shown only in the “create tenant” and “create API key” success steps, with a one-time copy option and a warning that it will not be shown again.
- **Errors:** User-facing error messages are derived from server `error`/`message` fields only; the server must not return the key in those fields.

---

## Checklist for changes

When adding or changing tenant or API-key behaviour:

1. **Backend**
   - [ ] No raw API key or secret in logs (only identifiers and key names).
   - [ ] Error responses do not include the key or hash; use generic messages.
   - [ ] Admin-only routes are under `/admin` and protected by AdminAuthMiddleware (or equivalent).
   - [ ] Tenant ID is validated (tenant exists) before creating a key or returning tenant-specific data.

2. **Tests**
   - [ ] Success path returns key only in 2xx body; error path (4xx/5xx) does not return key or key prefix.
   - [ ] Admin endpoint returns 401 when auth is missing or invalid (middleware or integration test).
   - [ ] Invalid tenant id or scope yields 400/404 with safe error body.

3. **Frontend**
   - [ ] Key is not sent in URL or query params.
   - [ ] Key is not included in error toasts or logs (only generic or server message).
   - [ ] One-time display and copy for new keys, with clear warning.

---

## References

- [Admin and Tenants](admin-and-tenants.md) — roles, tenant context, and API key usage.
- Enterprise: `TenantContextMiddleware`, `AdminAuthMiddleware`, `TenantRoutes`, `TenantProvisioningService`.

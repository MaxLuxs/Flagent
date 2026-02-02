/**
 * E2E API helpers for backend setup (tenant, auth).
 * Uses BACKEND_URL from env (default http://localhost:18000).
 */

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18000';
const ADMIN_EMAIL = process.env.FLAGENT_ADMIN_EMAIL || 'admin@local';
const ADMIN_PASSWORD = process.env.FLAGENT_ADMIN_PASSWORD || 'admin';
// Default admin key for local dev (matches FLAGENT_ADMIN_API_KEY in .env.example)
const ADMIN_API_KEY = process.env.FLAGENT_ADMIN_API_KEY || 'dev-admin-key';

export interface CreateTenantResult {
  apiKey: string;
  tenantKey: string;
}

/**
 * Create tenant via POST /admin/tenants.
 * Returns apiKey or null if backend doesn't support tenants (OSS) or request fails.
 * @param authToken - Optional JWT for Bearer auth (when admin auth enabled)
 */
export async function createTenant(
  request: import('@playwright/test').APIRequestContext,
  authToken?: string
): Promise<CreateTenantResult | null> {
  const tenantKey = `e2e-tenant-${Date.now()}`;
  try {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (ADMIN_API_KEY) {
      headers['X-Admin-Key'] = ADMIN_API_KEY;
    }
    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    }
    const response = await request.post(`${BACKEND_URL}/admin/tenants`, {
      headers,
      data: {
        key: tenantKey,
        name: 'E2E Test Tenant',
        plan: 'STARTER',
        ownerEmail: 'e2e@test.com',
      },
    });
    if (response.status() !== 201) {
      return null;
    }
    const body = await response.json();
    const apiKey = body.apiKey;
    if (!apiKey) return null;
    return { apiKey, tenantKey };
  } catch {
    return null;
  }
}

export interface LoginResult {
  token: string;
  user: { email: string };
}

/**
 * Login via POST /auth/login.
 * Returns token or null if auth is disabled or request fails.
 */
export async function login(
  request: import('@playwright/test').APIRequestContext
): Promise<LoginResult | null> {
  try {
    const response = await request.post(`${BACKEND_URL}/auth/login`, {
      headers: { 'Content-Type': 'application/json' },
      data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
    });
    if (response.status() !== 201) {
      return null;
    }
    const body = await response.json();
    const token = body.token;
    if (!token) return null;
    return { token, user: body.user };
  } catch {
    return null;
  }
}

export function getBackendUrl(): string {
  return BACKEND_URL;
}

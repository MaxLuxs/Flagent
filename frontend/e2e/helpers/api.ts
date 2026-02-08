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

export interface CreateFlagResult {
  id: number;
  key: string;
  apiKey?: string;
}

export interface CreateExperimentResult extends CreateFlagResult {
  variantIds: number[];
}

/**
 * Ensure at least one flag exists. Creates tenant (if enterprise) and flag via API.
 * Returns { id, key, apiKey? } or null. Caller should set localStorage api_key when apiKey is returned.
 */
export async function ensureFlagExists(
  request: import('@playwright/test').APIRequestContext
): Promise<CreateFlagResult | null> {
  let apiKey: string | undefined;
  const tenant = await createTenant(request);
  if (tenant) {
    apiKey = tenant.apiKey;
  }

  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (apiKey) headers['X-API-Key'] = apiKey;

  try {
    const key = `e2e_fixture_${Date.now()}`;
    const response = await request.post(`${BACKEND_URL}/api/v1/flags`, {
      headers,
      data: { description: 'E2E flag editor fixture', key },
    });
    if (response.status() !== 200) return null;
    const body = (await response.json()) as { id?: number; key?: string };
    const id = body.id;
    if (!id || typeof id !== 'number') return null;
    return { id, key: body.key || key, apiKey };
  } catch {
    return null;
  }
}

/**
 * Create flag with 2 variants (experiment) for E2E. Creates tenant, flag, segment, 2 variants, distribution.
 * Returns { id, key, apiKey?, variantIds } or null.
 */
export async function ensureExperimentExists(
  request: import('@playwright/test').APIRequestContext
): Promise<CreateExperimentResult | null> {
  const flagResult = await ensureFlagExists(request);
  if (!flagResult) return null;

  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (flagResult.apiKey) headers['X-API-Key'] = flagResult.apiKey;

  try {
    // Enable flag
    await request.put(`${BACKEND_URL}/api/v1/flags/${flagResult.id}/enabled`, {
      headers,
      data: { enabled: true },
    });

    // Create segment
    const segRes = await request.post(`${BACKEND_URL}/api/v1/flags/${flagResult.id}/segments`, {
      headers,
      data: { description: 'E2E segment', rolloutPercent: 100 },
    });
    if (segRes.status() !== 200) return flagResult;
    const segBody = (await segRes.json()) as { id?: number };
    const segmentId = segBody.id;
    if (!segmentId) return flagResult;

    // Create 2 variants
    const v1Res = await request.post(`${BACKEND_URL}/api/v1/flags/${flagResult.id}/variants`, {
      headers,
      data: { key: 'control' },
    });
    const v2Res = await request.post(`${BACKEND_URL}/api/v1/flags/${flagResult.id}/variants`, {
      headers,
      data: { key: 'variant_a' },
    });
    if (v1Res.status() !== 200 || v2Res.status() !== 200) return flagResult;

    const v1Body = (await v1Res.json()) as { id?: number };
    const v2Body = (await v2Res.json()) as { id?: number };
    const v1Id = v1Body.id;
    const v2Id = v2Body.id;
    if (!v1Id || !v2Id) return flagResult;

    // Set distribution
    await request.put(
      `${BACKEND_URL}/api/v1/flags/${flagResult.id}/segments/${segmentId}/distributions`,
      {
        headers,
        data: {
          distributions: [
            { variantID: v1Id, percent: 50 },
            { variantID: v2Id, percent: 50 },
          ],
        },
      }
    );

    return { ...flagResult, variantIds: [v1Id, v2Id] };
  } catch {
    return flagResult;
  }
}

export interface SeedAnalyticsFlag {
  id: number;
  key: string;
  expectedEvals: number;
}

export interface SeedAnalyticsResult {
  flags: SeedAnalyticsFlag[];
  apiKey?: string;
  totalEvaluations: number;
}

/**
 * Call POST /api/v1/evaluation for a single flag.
 * Returns true if response is 200.
 */
export async function callEvaluation(
  request: import('@playwright/test').APIRequestContext,
  flagId: number,
  entityId: string,
  apiKey?: string
): Promise<boolean> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (apiKey) headers['X-API-Key'] = apiKey;
  try {
    const response = await request.post(`${BACKEND_URL}/api/v1/evaluation`, {
      headers,
      data: { flagID: flagId, entityID: entityId, enableDebug: false },
    });
    return response.status() === 200;
  } catch {
    return false;
  }
}

/**
 * Enable flag with segment and variant so evaluation succeeds.
 * Must be called after flag creation; wait ~500ms before evaluation for cache refresh.
 */
export async function enableFlagForEval(
  request: import('@playwright/test').APIRequestContext,
  flagId: number,
  apiKey?: string
): Promise<boolean> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (apiKey) headers['X-API-Key'] = apiKey;
  try {
    await request.put(`${BACKEND_URL}/api/v1/flags/${flagId}/enabled`, {
      headers,
      data: { enabled: true },
    });

    const segRes = await request.post(`${BACKEND_URL}/api/v1/flags/${flagId}/segments`, {
      headers,
      data: { description: 'E2E analytics segment', rolloutPercent: 100 },
    });
    if (segRes.status() !== 200) return false;
    const segBody = (await segRes.json()) as { id?: number };
    const segmentId = segBody.id;
    if (!segmentId) return false;

    const vRes = await request.post(`${BACKEND_URL}/api/v1/flags/${flagId}/variants`, {
      headers,
      data: { key: 'control' },
    });
    if (vRes.status() !== 200) return false;
    const vBody = (await vRes.json()) as { id?: number };
    const variantId = vBody.id;
    if (!variantId) return false;

    await request.put(
      `${BACKEND_URL}/api/v1/flags/${flagId}/segments/${segmentId}/distributions`,
      {
        headers,
        data: {
          distributions: [{ variantID: variantId, percent: 100 }],
        },
      }
    );
    return true;
  } catch {
    return false;
  }
}

/**
 * Seed analytics data: create 5 flags, enable them, call evaluation API many times.
 * Returns flags with expected eval counts for assertions.
 * Evaluation events are recorded in evaluation_events (Core metrics).
 */
export async function seedAnalyticsData(
  request: import('@playwright/test').APIRequestContext
): Promise<SeedAnalyticsResult | null> {
  const tenant = await createTenant(request);
  const apiKey = tenant?.apiKey;

  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (apiKey) headers['X-API-Key'] = apiKey;

  const evalCounts = [50, 30, 20, 10, 5];
  const keys = ['analytics_flag_a', 'analytics_flag_b', 'analytics_flag_c', 'analytics_flag_d', 'analytics_flag_e'];
  const prefix = `e2e_analytics_${Date.now()}_`;
  const flags: SeedAnalyticsFlag[] = [];

  try {
    for (let i = 0; i < 5; i++) {
      const key = `${prefix}${keys[i]}`;
      const res = await request.post(`${BACKEND_URL}/api/v1/flags`, {
        headers,
        data: { description: `E2E analytics flag ${i + 1}`, key },
      });
      if (res.status() !== 200) return null;
      const body = (await res.json()) as { id?: number; key?: string };
      const id = body.id;
      if (!id || typeof id !== 'number') return null;

      const ok = await enableFlagForEval(request, id, apiKey);
      if (!ok) return null;
      flags.push({ id, key: body.key || key, expectedEvals: evalCounts[i] });
    }

    // Wait for EvalCache to pick up new flags (CI: 100ms refresh; local: 3s default)
    await new Promise((r) => setTimeout(r, 3500));

    let total = 0;
    for (const f of flags) {
      for (let j = 0; j < f.expectedEvals; j++) {
        const ok = await callEvaluation(request, f.id, `eval_entity_${f.id}_${j}`, apiKey);
        if (ok) total++;
      }
    }

    // EvaluationEventRecorder batches events and flushes after 1s; wait for DB persistence
    await new Promise((r) => setTimeout(r, 2500));

    return { flags, apiKey, totalEvaluations: total };
  } catch {
    return null;
  }
}

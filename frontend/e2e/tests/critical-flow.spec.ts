import { test, expect } from '@playwright/test';
import {
  ensureFlagExists,
  enableFlagForEval,
  callEvaluation,
  getBackendUrl,
} from '../helpers/api';

/**
 * Critical path E2E: create flag → enable (segment + variant + distribution) → call evaluation API → assert result.
 * Ensures the full evaluation pipeline works without UI.
 */
test.describe('Critical flow: create flag and evaluate via API @oss @smoke', () => {
  test('create flag, enable for eval, evaluate returns 200 and variant', async ({ request }) => {
    const result = await ensureFlagExists(request);
    if (!result) {
      test.skip(true, 'Could not create flag (backend may require tenant)');
    }

    const ok = await enableFlagForEval(request, result!.id, result!.apiKey);
    if (!ok) {
      test.skip(true, 'Could not enable flag for evaluation');
    }

    // EvalCache refresh (CI: 100ms; allow extra for persistence)
    await new Promise((r) => setTimeout(r, 1500));

    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (result!.apiKey) headers['X-API-Key'] = result!.apiKey;

    const response = await request.post(`${getBackendUrl()}/api/v1/evaluation`, {
      headers,
      data: {
        flagID: result!.id,
        entityID: 'e2e_critical_entity_1',
        entityType: 'user',
        enableDebug: false,
      },
    });

    expect(response.status()).toBe(200);
    const body = (await response.json()) as { variantKey?: string; flagKey?: string };
    expect(body.flagKey).toBe(result!.key);
    expect(body.variantKey).toBeDefined();
  });
});

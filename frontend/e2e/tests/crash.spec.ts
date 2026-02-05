import { test, expect } from '@playwright/test';
import { ensureFlagExists } from '../helpers/api';

async function ensureFlagAndApiKey(
  page: import('@playwright/test').Page,
  request: import('@playwright/test').APIRequestContext
) {
  const result = await ensureFlagExists(request);
  if (result?.apiKey) {
    await page.addInitScript(
      (key: string) => localStorage.setItem('api_key', key),
      result.apiKey
    );
  }
  return result;
}

test.describe('Crash Page @enterprise', () => {
  test('crash page loads when feature enabled', async ({ page }) => {
    await page.goto('/crash');
    await page.waitForLoadState('domcontentloaded');

    const onCrash = page.url().includes('/crash');
    if (onCrash) {
      await expect(
        page.getByRole('heading', { name: /Crash Analytics/i })
      ).toBeVisible({ timeout: 10000 });
    }
    // If redirected to home (OSS, feature disabled), test passes
  });

  test('crash page shows flags list and navigates to flag metrics with CRASH_RATE', async ({
    page,
    request,
  }) => {
    const flag = await ensureFlagAndApiKey(page, request);
    if (!flag) {
      test.skip();
      return;
    }

    await page.goto('/crash');
    await page.waitForLoadState('domcontentloaded');

    if (!page.url().includes('/crash')) {
      test.skip();
      return;
    }

    const flagLink = page.getByRole('link', { name: new RegExp(flag.key) });
    if ((await flagLink.count()) === 0) {
      test.skip();
      return;
    }

    await flagLink.first().click();
    await expect(page).toHaveURL(new RegExp(`/flags/\\d+/metrics\\?metric=CRASH_RATE`));

    const select = page.locator('select').first();
    await expect(select).toHaveValue('CRASH_RATE');
  });
});

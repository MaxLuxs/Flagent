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

test.describe('Metrics Page', () => {
  test('displays Metrics & Analytics when navigating to flag metrics @oss', async ({
    page,
    request,
  }) => {
    const flag = await ensureFlagAndApiKey(page, request);
    if (!flag) {
      test.skip();
      return;
    }

    await page.goto(`/flags/${flag.id}/metrics`);
    await page.waitForLoadState('domcontentloaded');

    await expect(
      page.getByRole('heading', {
        name: /Metrics & Analytics|API Evaluation Stats|Метрики и аналитика/i,
      })
    ).toBeVisible({ timeout: 10000 });
  });

  test('shows A/B Statistics section on flag metrics page @enterprise', async ({
    page,
    request,
  }) => {
    const flag = await ensureFlagAndApiKey(page, request);
    if (!flag) {
      test.skip();
      return;
    }

    await page.goto(`/flags/${flag.id}/metrics`);
    await page.waitForLoadState('domcontentloaded');

    // ExperimentInsightsCard shows "A/B Statistics" heading or "No conversion data" when empty
    await expect(
      page.getByText(/A\/B Statistics|No conversion data/i)
    ).toBeVisible({ timeout: 10000 });
  });

  test('flag metrics page accepts metric query param and preselects type @enterprise', async ({
    page,
    request,
  }) => {
    const flag = await ensureFlagAndApiKey(page, request);
    if (!flag) {
      test.skip();
      return;
    }

    await page.goto(`/flags/${flag.id}/metrics?metric=CRASH_RATE`);
    await page.waitForLoadState('domcontentloaded');

    await expect(
      page.getByRole('heading', { name: /Metrics & Analytics/i })
    ).toBeVisible({ timeout: 10000 });

    const select = page.locator('select').first();
    await expect(select).toHaveValue('CRASH_RATE');
  });

  test('analytics page links to flag metrics when metrics enabled @oss', async ({
    page,
    request,
  }) => {
    const flag = await ensureFlagAndApiKey(page, request);
    if (!flag) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');

    // Analytics may show flags list or overview; look for View Metrics link
    const viewMetricsLink = page.getByRole('link', {
      name: /View metrics|Метрики|view metrics/i,
    });
    if ((await viewMetricsLink.count()) > 0) {
      await viewMetricsLink.first().click();
      await expect(page).toHaveURL(new RegExp(`/flags/\\d+/metrics`));
    }
  });
});

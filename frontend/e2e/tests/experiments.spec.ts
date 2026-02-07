import { test, expect } from '@playwright/test';
import { ensureExperimentExists } from '../helpers/api';

async function ensureExperimentAndApiKey(
  page: import('@playwright/test').Page,
  request: import('@playwright/test').APIRequestContext
) {
  const result = await ensureExperimentExists(request);
  if (result?.apiKey) {
    await page.addInitScript(
      (key: string) => localStorage.setItem('api_key', key),
      result.apiKey
    );
  }
  return result;
}

test.describe('Experiments Page @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/experiments');
  });

  test('displays Experiments page heading', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('heading', { name: /Experiments|Эксперименты|A\/B/i })
    ).toBeVisible({ timeout: 10000 });
  });

  test('loads experiments list', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Loading|Загрузка|No experiments|Нет экспериментов|flags with variants/i)
    ).toBeVisible({ timeout: 10000 });
  });

  test('View Metrics button navigates to flag metrics page', async ({
    page,
    request,
  }) => {
    const experiment = await ensureExperimentAndApiKey(page, request);
    if (!experiment) {
      test.skip();
      return;
    }

    await page.goto('/experiments');
    await page.waitForLoadState('domcontentloaded');
    // Wait for API to finish: table with rows or empty state
    await Promise.race([
      page.locator('table tbody tr').first().waitFor({ state: 'visible', timeout: 15000 }),
      page.getByText(/No experiments|Нет экспериментов/i).waitFor({ state: 'visible', timeout: 15000 }),
    ]);

    const row = page.locator('table tbody tr').filter({ hasText: experiment.key }).first();
    const viewMetricsBtn = row.getByRole('button', {
      name: /View metrics|Метрики|view metrics/i,
    });
    if ((await viewMetricsBtn.count()) === 0) {
      test.skip(true, 'No experiments with View Metrics (experiments list empty or enableMetrics disabled)');
      return;
    }
    await expect(viewMetricsBtn).toBeVisible({ timeout: 5000 });
    await viewMetricsBtn.click();

    await expect(page).toHaveURL(new RegExp(`/flags/${experiment.id}/metrics`));
    await expect(
      page.getByRole('heading', {
        name: /Metrics & Analytics|API Evaluation Stats|Метрики и аналитика/i,
      })
    ).toBeVisible({ timeout: 10000 });
  });
});

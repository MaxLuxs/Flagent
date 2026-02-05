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

    const viewMetricsBtn = page.getByRole('button', {
      name: /View metrics|Метрики|view metrics/i,
    });
    await expect(viewMetricsBtn.first()).toBeVisible({ timeout: 15000 });
    await viewMetricsBtn.first().click();

    await expect(page).toHaveURL(new RegExp(`/flags/${experiment.id}/metrics`));
    await expect(
      page.getByRole('heading', {
        name: /Metrics & Analytics|API Evaluation Stats|Метрики и аналитика/i,
      })
    ).toBeVisible({ timeout: 10000 });
  });
});

import { test, expect } from '@playwright/test';

test.describe('Analytics Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/analytics');
  });

  test('displays Analytics page', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('heading', { name: /Analytics|Аналитика/i })
    ).toBeVisible({ timeout: 10000 });
  });

  test('loads analytics content', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Metrics|Метрики|Overview|Обзор|Evaluations|Вычисления/i).first()
    ).toBeVisible({ timeout: 10000 });
  });
});

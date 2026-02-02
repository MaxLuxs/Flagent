import { test, expect } from '@playwright/test';

test.describe('Experiments Page', () => {
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
});

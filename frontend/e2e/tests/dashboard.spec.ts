import { test, expect } from '@playwright/test';

test.describe('Dashboard @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dashboard');
  });

  test('displays Dashboard heading @smoke', async ({ page }) => {
    await page.waitForLoadState('networkidle').catch(() => {});
    await expect(
      page.getByRole('heading', { name: /Dashboard|Главная|Дашборд/i })
    ).toBeVisible({ timeout: 15000 });
  });

  test('loads and displays flag statistics', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Total Flags|Enabled|Включен|Disabled|Выключен/i).first()
    ).toBeVisible({ timeout: 20000 });
  });

  test('has navigation to other sections', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const navLink = page.getByRole('link', { name: /Flags|Флаги/i }).first();
    await expect(navLink).toBeVisible({ timeout: 5000 });
  });
});

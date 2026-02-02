import { test, expect } from '@playwright/test';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dashboard');
  });

  test('displays Dashboard heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
  });

  test('loads and displays flag statistics', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText('Total Flags').first()
    ).toBeVisible({ timeout: 10000 });
  });

  test('has navigation to other sections', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const navLink = page.getByRole('link', { name: /Flags|Флаги/i }).first();
    await expect(navLink).toBeVisible({ timeout: 5000 });
  });
});

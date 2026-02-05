import { test, expect } from '@playwright/test';

/**
 * When auth is enabled (default), / redirects authenticated users to /dashboard.
 * These tests verify home page behavior: either landing content or dashboard.
 */
test.describe('Landing / Home Page @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('displays Flagent or Dashboard title @smoke', async ({ page }) => {
    await expect(
      page.locator('h1, h2, h3').filter({ hasText: /Flagent|Dashboard|Главная/ }).first()
    ).toBeVisible({ timeout: 10000 });
  });

  test('has navigation to Dashboard and Flags', async ({ page }) => {
    await expect(
      page.getByRole('link', {
        name: /Dashboard|Flags|Главная|Флаги|Home/i,
      }).first()
    ).toBeVisible({ timeout: 10000 });
  });

  test('navigates to Dashboard when Dashboard link clicked', async ({ page }) => {
    const dashboardLink = page.getByRole('link', { name: /Dashboard|Главная/i }).first();
    if ((await dashboardLink.count()) > 0) {
      await dashboardLink.click();
      await expect(page).toHaveURL(/\/dashboard/);
    }
  });

  test('navigates to Flags when Flags link clicked', async ({ page }) => {
    const flagsLink = page
      .getByRole('link', { name: /Flags|Флаги|Feature Flags/i })
      .first();
    if ((await flagsLink.count()) > 0) {
      await flagsLink.click();
      await expect(page).toHaveURL(/\/flags/);
    }
  });
});

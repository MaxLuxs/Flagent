import { test, expect } from '@playwright/test';

test.describe('Navigation @oss', () => {
  test('navbar links work correctly @smoke', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('domcontentloaded');
    // When auth is on, / redirects to /dashboard for logged-in users
    await expect(page).toHaveURL(/\/(|dashboard|login)/, { timeout: 15000 });

    const homeLink = page.locator('a[href="/"]').first();
    if ((await homeLink.count()) > 0) {
      await homeLink.click();
      await expect(page).toHaveURL(/\/(|dashboard)/);
    }

    await page.goto('/flags');
    await expect(page).toHaveURL(/\/flags/);

    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('logo click from Dashboard navigates to home', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/dashboard/);

    const logoLink = page.locator('a[href="/"]').first();
    await logoLink.click();
    // When auth is on, / redirects to /dashboard; when off, stays at /
    await expect(page).toHaveURL(/\/(|dashboard)/);
    await expect(
      page.locator('h1:has-text("Flagent"), h1:has-text("Dashboard")')
    ).toBeVisible({ timeout: 5000 });
  });

  test('logo click from Flags navigates to home', async ({ page }) => {
    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/\/flags/);

    const logoLink = page.locator('a[href="/"]').first();
    await logoLink.click();
    await expect(page).toHaveURL(/\/(|dashboard)/);
    await expect(
      page.locator('h1:has-text("Flagent"), h1:has-text("Dashboard")')
    ).toBeVisible({ timeout: 5000 });
  });

  test('can navigate via URL directly', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();

    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Feature Flags|Create New Flag|Создать новый флаг|Flags/i).first()
    ).toBeVisible({ timeout: 10000 });

    await page.goto('/debug');
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('heading', { name: /Debug Console|Консоль/i })
    ).toBeVisible({ timeout: 10000 });
  });

  test('experiments page loads', async ({ page }) => {
    await page.goto('/experiments');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/\/experiments/);
  });

  test('analytics page loads', async ({ page }) => {
    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/\/analytics/);
  });

  test('settings page loads', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/\/settings/);
  });

  test('create flag page loads', async ({ page }) => {
    await page.goto('/flags/new');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/\/flags\/new/);
  });

  test('crash page loads when feature enabled', async ({ page }) => {
    await page.goto('/crash');
    await page.waitForLoadState('domcontentloaded');
    const onCrash = page.url().includes('/crash');
    if (onCrash) {
      await expect(
        page.getByRole('heading', { name: /Crash Analytics/i })
      ).toBeVisible({ timeout: 10000 });
    }
  });
});

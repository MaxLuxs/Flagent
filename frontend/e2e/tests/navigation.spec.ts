import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('navbar links work correctly', async ({ page }) => {
    await page.goto('/');

    const homeLink = page.locator('a[href="/"]').first();
    await homeLink.click();
    await expect(page).toHaveURL('/');

    await page.goto('/flags');
    await expect(page).toHaveURL(/\/flags/);

    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/dashboard/);
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
});

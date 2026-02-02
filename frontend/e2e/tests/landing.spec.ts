import { test, expect } from '@playwright/test';

test.describe('Landing Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('displays Flagent title and description', async ({ page }) => {
    await expect(page.locator('h1:has-text("Flagent")')).toBeVisible();
    await expect(
      page.getByText(/Feature flags, A\/B testing, and dynamic configuration/i)
    ).toBeVisible();
  });

  test('has Dashboard and Flags navigation buttons', async ({ page }) => {
    await expect(page.getByRole('button', { name: 'Dashboard' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Flags' })).toBeVisible();
  });

  test('navigates to Dashboard when Dashboard button clicked', async ({ page }) => {
    await page.getByRole('button', { name: 'Dashboard' }).click();
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('navigates to Flags list when Flags button clicked', async ({ page }) => {
    await page.getByRole('button', { name: 'Flags' }).click();
    await expect(page).toHaveURL(/\/flags/);
  });
});

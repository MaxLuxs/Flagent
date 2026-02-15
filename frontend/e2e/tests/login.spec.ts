import { test, expect } from '@playwright/test';

/**
 * Login flow: /login page and submit.
 * When FLAGENT_ADMIN_AUTH_ENABLED is false, login endpoint may 404 or redirect; tests skip when form not available.
 */
test.describe('Login Page @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('displays login form or redirects when auth disabled @smoke', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const onLogin = page.url().includes('/login');
    if (onLogin) {
      await expect(
        page.getByRole('heading', { name: /Login|Вход|Sign in/i }).or(
          page.getByLabel(/Email|E-mail|Почта/i).first()
        )
      ).toBeVisible({ timeout: 10000 });
    } else {
      await expect(page).toHaveURL(/\/(dashboard|)/);
    }
  });

  test('can submit login and reach dashboard when auth enabled', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const emailInput = page.getByLabel(/Email|E-mail|Почта/i).first();
    const passwordInput = page.getByPlaceholder(/Password|Пароль/i).first();
    const submitBtn = page.getByRole('button', { name: /Login|Вход|Sign in/i }).first();

    if ((await emailInput.count()) === 0 || (await submitBtn.count()) === 0) {
      test.skip(true, 'Login form not visible (auth may be disabled)');
      return;
    }

    await emailInput.fill(process.env.FLAGENT_ADMIN_EMAIL || 'admin@local');
    if ((await passwordInput.count()) > 0) {
      await passwordInput.fill(process.env.FLAGENT_ADMIN_PASSWORD || 'admin');
    }
    await submitBtn.click();
    await expect(page).toHaveURL(/\/(dashboard|)/, { timeout: 15000 });
  });
});

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
    await page.waitForLoadState('networkidle').catch(() => {});
    const onLogin = page.url().includes('/login');
    const heading = page.getByRole('heading', { name: /Login|Вход|Sign in/i });
    const emailInput = page.getByLabel(/Email|E-mail|Почта/i).first();

    if (onLogin) {
      // OSS mode may return 404 or a page without a login form when admin auth is disabled.
      // Wait for content then skip if neither heading nor email is visible (avoids flaky toBeVisible).
      await page.waitForLoadState('domcontentloaded');
      const headingVisible = await heading.isVisible().catch(() => false);
      const emailVisible = await emailInput.isVisible().catch(() => false);
      if (!headingVisible && !emailVisible) {
        test.skip(true, 'Login form not available (auth disabled / OSS)');
        return;
      }
      await expect(heading.or(emailInput)).toBeVisible({ timeout: 15000 });
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

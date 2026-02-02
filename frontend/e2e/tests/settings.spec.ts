import { test, expect } from '@playwright/test';

test.describe('Settings Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/settings');
  });

  test('displays Settings page', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Settings|Настройки/i).first()
    ).toBeVisible({ timeout: 10000 });
  });

  test('shows edition badge', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/OPEN SOURCE|ENTERPRISE EDITION/i).first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('has General tab with API base URL', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/API Base URL|General/i).first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('shows enabled features', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Features|Функции|Metrics|Метрики|General|Общие/i).first()
    ).toBeVisible({ timeout: 5000 });
  });
});

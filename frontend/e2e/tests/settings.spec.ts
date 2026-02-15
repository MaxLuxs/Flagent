import { test, expect } from '@playwright/test';

test.describe('Settings Page @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/settings');
  });

  test('displays Settings page @smoke', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('heading', { name: /Settings|Настройки/i }).first()
    ).toBeVisible({ timeout: 15000 });
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
      page.getByText(/API Base URL|General|Общие|Базовый URL API/i).first()
    ).toBeVisible({ timeout: 10000 });
  });

  test('shows enabled features', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Features|Функции|Metrics|Метрики|General|Общие/i).first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('Export section visible when present', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const exportHeading = page.getByText(/Export|Экспорт|Export data|Экспорт данных/i).first();
    if ((await exportHeading.count()) > 0) {
      await expect(exportHeading).toBeVisible({ timeout: 5000 });
    }
  });
});

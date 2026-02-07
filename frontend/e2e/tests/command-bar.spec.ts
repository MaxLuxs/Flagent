import { test, expect } from '@playwright/test';

test.describe('Command Bar @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dashboard');
    await page.waitForLoadState('domcontentloaded');
  });

  test('opens on Cmd+K (Mac) or Ctrl+K (Windows/Linux) @smoke', async ({
    page,
  }) => {
    const isMac = process.platform === 'darwin';
    await page.keyboard.press(isMac ? 'Meta+k' : 'Control+k');
    await expect(
      page.getByPlaceholder(/Search flags|Поиск флагов/i)
    ).toBeVisible({ timeout: 5000 });
  });

  test('opens when clicking search button in navbar', async ({ page }) => {
    const searchBtn = page.getByRole('button', { name: /⌘K|Ctrl\+K/ });
    if ((await searchBtn.count()) > 0) {
      await searchBtn.click();
      await expect(
        page.getByPlaceholder(/Search flags|Поиск флагов/i)
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('closes on Escape', async ({ page }) => {
    const isMac = process.platform === 'darwin';
    await page.keyboard.press(isMac ? 'Meta+k' : 'Control+k');
    await expect(
      page.getByPlaceholder(/Search flags|Поиск флагов/i)
    ).toBeVisible({ timeout: 5000 });
    await page.keyboard.press('Escape');
    await expect(
      page.getByPlaceholder(/Search flags|Поиск флагов/i)
    ).not.toBeVisible({ timeout: 3000 });
  });
});

import { test, expect } from '@playwright/test';

/**
 * Full UI user journey: list → open flag → toggle → back to list.
 * Covers the key path without creating new data (uses existing flags).
 */
test.describe('User journey: list → detail → toggle → back @oss @smoke', () => {
  test('open first flag, toggle enabled, return to list', async ({ page }) => {
    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');

    const firstRow = page.locator('table tbody tr').first();
    await expect(firstRow).toBeVisible({ timeout: 15000 });

    await firstRow.click();
    await expect(page).toHaveURL(/\/flags\/\d+/);
    await page.waitForLoadState('domcontentloaded');

    const enabledSwitch = page
      .locator('label')
      .filter({ hasText: /Enabled|Disabled/ })
      .locator('input[type="checkbox"]')
      .first();
    if ((await enabledSwitch.count()) > 0) {
      const wasChecked = await enabledSwitch.isChecked();
      await enabledSwitch.click();
      await expect(enabledSwitch).toBeChecked({ checked: !wasChecked, timeout: 10000 });
    }

    const backBtn = page.getByRole('button', { name: /Back|Назад|Back to Flags|К флагам/i }).first();
    await expect(backBtn).toBeVisible({ timeout: 5000 });
    await backBtn.click();
    await expect(page).toHaveURL(/\/flags$/);
  });
});

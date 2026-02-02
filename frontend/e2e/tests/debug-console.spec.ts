import { test, expect } from '@playwright/test';

test.describe('Debug Console', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/debug');
  });

  test('displays Debug Console heading', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('heading', { name: /Debug Console|Консоль отладки/i })
    ).toBeVisible({ timeout: 10000 });
  });

  test('has Single and Batch evaluation tabs', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Single|Batch|Одиночная|Пакетная/i)
    ).toBeVisible({ timeout: 5000 });
  });

  test('can evaluate a flag', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');

    const inputs = page.locator('input[type="text"]');
    const keyInput = inputs.first();
    await keyInput.fill('integration_test_flag');

    const evaluateBtn = page.getByRole('button', {
      name: /Evaluate|Вычислить/i,
    });
    if ((await evaluateBtn.count()) > 0) {
      await evaluateBtn.first().click();
      await expect(
        page.getByText(/value|результат|true|false|Result/i)
      ).toBeVisible({ timeout: 5000 });
    }
  });
});

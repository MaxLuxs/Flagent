import { test, expect } from '@playwright/test';

test.describe('Debug Console @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/debug');
  });

  test('displays Debug Console heading @smoke', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.locator('h2:has-text("Debug Console"), h2:has-text("Консоль отладки")')
    ).toBeVisible({ timeout: 15000 });
  });

  test('has Single and Batch evaluation tabs', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('button', { name: /Оценка|Evaluation/i }).first()
    ).toBeVisible({ timeout: 10000 });
    await expect(
      page.getByRole('button', { name: /Пакетная оценка|Batch Evaluation/i }).first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('can evaluate a flag', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');

    const inputs = page.locator('input[type="text"]');
    const keyInput = inputs.first();
    await keyInput.fill('integration_test_flag');

    const evaluateBtn = page.getByRole('button', {
      name: /Evaluate|Оценить/i,
    });
    if ((await evaluateBtn.count()) > 0) {
      await evaluateBtn.first().click();
      await expect(
        page.getByText(/value|результат|true|false|Result/i)
      ).toBeVisible({ timeout: 5000 });
    }
  });
});

import { test, expect } from '@playwright/test';

const E2E_FLAG_DESCRIPTION = `E2E test flag ${Date.now()}`;

test.describe('Flags List @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/flags');
  });

  test('displays Feature Flags page @smoke', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByText(/Feature Flags|Create New Flag|Создать новый флаг/i).first()
    ).toBeVisible({ timeout: 10000 });
  });

  test('can create a new flag', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');

    const input = page.getByPlaceholder(/Enter flag description|Введите описание флага/i).first();
    await expect(input).toBeVisible({ timeout: 5000 });
    await input.fill(E2E_FLAG_DESCRIPTION);

    const createBtn = page.getByRole('button', {
      name: /Create Flag|Создать флаг/i,
    }).first();
    await createBtn.click();

    await expect(page).toHaveURL(/\/flags\/\d+/, { timeout: 15000 });
    const descInput = page.getByLabel(/Description|Описание|Flag description|Описание флага/i).first();
    await expect(descInput).toHaveValue(E2E_FLAG_DESCRIPTION, { timeout: 10000 });
  });

  test('displays flags table when flags exist', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const table = page.locator('table');
    await expect(table).toBeVisible({ timeout: 10000 });
  });
});

test.describe('Flag Detail @oss', () => {
  test('can open flag detail from list', async ({ page }) => {
    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');

    const firstRow = page.locator('table tbody tr').first();
    if (await firstRow.count() > 0) {
      await firstRow.click();
      await expect(page).toHaveURL(/\/flags\/\d+/);
    }
  });
});

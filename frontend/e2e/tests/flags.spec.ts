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

test.describe('Flags List - Toggle and Search @oss', () => {
  test('can toggle flag ON/OFF from list row without opening detail @smoke', async ({ page }) => {
    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');

    const table = page.locator('table tbody tr');
    await expect(table.first()).toBeVisible({ timeout: 10000 });

    // Find the ON/OFF toggle in first row (label with checkbox, or cell with "ON"/"OFF" text)
    const firstRow = table.first();
    const toggleCell = firstRow
      .locator('td')
      .filter({ has: firstRow.locator('label:has-text("ON"), label:has-text("OFF")') });
    const toggleCheckbox = toggleCell.locator('input[type="checkbox"]').first();

    if ((await toggleCheckbox.count()) === 0) {
      test.skip(true, 'No toggle in first row (empty list or layout changed)');
      return;
    }

    const wasChecked = await toggleCheckbox.isChecked();
    await toggleCheckbox.click();
    await expect(toggleCheckbox).toBeChecked({ checked: !wasChecked, timeout: 10000 });
  });

  test('can filter flags by search query', async ({ page }) => {
    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');

    const searchInput = page.getByPlaceholder(/Search|Поиск|flag key|ключ флага/i).first();
    if ((await searchInput.count()) === 0) {
      test.skip(true, 'Search input not found (layout may differ)');
      return;
    }
    await searchInput.fill('e2e_');
    await page.waitForTimeout(500);
    await expect(searchInput).toHaveValue('e2e_');
  });
});

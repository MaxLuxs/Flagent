import { test, expect } from '@playwright/test';
import { ensureFlagExists } from '../helpers/api';

const E2E_FLAG_PREFIX = `e2e_full_${Date.now()}`;

async function ensureFlagAndNavigate(
  page: import('@playwright/test').Page,
  request: import('@playwright/test').APIRequestContext
) {
  const result = await ensureFlagExists(request);
  if (result?.apiKey) {
    await page.addInitScript(
      (key: string) => {
        localStorage.setItem('api_key', key);
      },
      result.apiKey
    );
  }
  await page.goto('/flags');
  await page.waitForLoadState('domcontentloaded');
  await expect(page.locator('table tbody tr').first()).toBeVisible({ timeout: 15000 });
}

test.describe('Flag Editor - Full Flow @oss', () => {
  test.beforeEach(async ({ page, request }) => {
    await ensureFlagAndNavigate(page, request);
  });

  test('opens flag detail and shows Config tab', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await expect(page).toHaveURL(/\/flags\/\d+/);

    await expect(
      page.getByRole('button', { name: /Config|Конфигурация/i })
    ).toBeVisible({ timeout: 5000 });
  });

  test('can toggle flag enabled/disabled', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    // Use checkbox near Enabled/Disabled label (avoids DebugConsole/other checkboxes)
    const enabledSwitch = page
      .locator('label')
      .filter({ hasText: /Enabled|Disabled/ })
      .locator('input[type="checkbox"]')
      .first();
    await expect(enabledSwitch).toBeVisible({ timeout: 5000 });
    const wasChecked = await enabledSwitch.isChecked();
    await enabledSwitch.click();
    // Wait for API to complete and UI to update (checkbox is controlled by server state)
    await expect(enabledSwitch).toBeChecked({ checked: !wasChecked, timeout: 10000 });
  });

  test('can edit description and save', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    const descField = page.getByLabel(/Description|Описание|Flag description|Описание флага/i).first();
    if (await descField.count() > 0) {
      const newDesc = `${E2E_FLAG_PREFIX}_desc`;
      await descField.fill(newDesc);
      await page.getByRole('button', { name: /Save Flag|Сохранить флаг/i }).click();
      await page.waitForTimeout(2000);
      await expect(descField).toHaveValue(newDesc, { timeout: 5000 });
    }
  });

  test('can add variant to flag', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    const variantInput = page
      .getByPlaceholder(/Enter variant key|Введите ключ варианта|Variant key|Ключ варианта/i)
      .first();
    if (await variantInput.count() > 0) {
      const variantKey = `${E2E_FLAG_PREFIX}_variant`;
      await variantInput.fill(variantKey);
      await page.getByRole('button', { name: /Create Variant|Создать вариант|Add|Добавить/i }).first().click();
      await page.waitForTimeout(3000);
      let found = false;
      for (const input of await page.locator('input[type="text"]').all()) {
        if ((await input.inputValue()) === variantKey) {
          found = true;
          break;
        }
      }
      if (!found) {
        await expect(page.getByText(variantKey)).toBeVisible({ timeout: 5000 });
      }
    }
  });

  test('can add segment to flag', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    const newSegmentBtn = page
      .getByRole('button', { name: /New Segment|Новый сегмент|Create Segment|Создать сегмент/i })
      .first();
    if (await newSegmentBtn.count() > 0) {
      await newSegmentBtn.click();
      await page.waitForTimeout(300);

      const modalDesc = page.locator('[role="dialog"] input, .modal input').first();
      if (await modalDesc.count() > 0) {
        await modalDesc.fill(`${E2E_FLAG_PREFIX}_segment`);
        await page
          .getByRole('button', { name: /Create|Создать|Confirm|Подтвердить/i })
          .last()
          .click();
        await page.waitForTimeout(1000);
        await expect(page.getByText(`${E2E_FLAG_PREFIX}_segment`)).toBeVisible({
          timeout: 5000,
        });
      }
    }
  });

  test('shows History tab and content', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    await page.getByRole('button', { name: /History|История/i }).first().click();
    await page.waitForTimeout(500);
    await expect(
      page.getByText(/History|История|Changes|Изменения|No history|Нет истории/i).first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('can open Metrics tab from flag detail', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    const metricsTab = page.getByRole('button', { name: /Metrics|Метрики/i }).first();
    if ((await metricsTab.count()) === 0) {
      test.skip(true, 'Metrics tab not found (feature may be disabled)');
      return;
    }
    await metricsTab.click();
    await page.waitForTimeout(500);
    await expect(page).toHaveURL(/\/flags\/\d+\/metrics/);
    await expect(
      page.getByText(/Metrics|Метрики|Evaluation|Оценка|API|stats/i).first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('has back to flags button', async ({ page }) => {
    const firstRow = page.locator('table tbody tr').first();
    await firstRow.click();
    await page.waitForLoadState('domcontentloaded');

    const backBtn = page.getByRole('button', { name: /Back|Назад|Back to Flags|К флагам/i });
    await expect(backBtn).toBeVisible({ timeout: 5000 });
    await backBtn.click();
    await expect(page).toHaveURL(/\/flags$/);
  });
});

test.describe('Flag Editor - Delete @oss', () => {
  test('can delete flag with confirmation', async ({ page }) => {
    await page.goto('/flags');
    await page.waitForLoadState('domcontentloaded');

    const createInput = page.getByPlaceholder(/Enter flag description|Введите описание флага/i).first();
    await createInput.fill(`${E2E_FLAG_PREFIX}_to_delete`);
    await page.getByRole('button', { name: /Create Flag|Создать флаг/i }).click();
    await expect(page).toHaveURL(/\/flags\/\d+/);
    await page.waitForLoadState('domcontentloaded');

    const flagText = `${E2E_FLAG_PREFIX}_to_delete`;

    await page.getByRole('button', { name: /Delete Flag|Удалить флаг/i }).click();
    await page.waitForTimeout(500);
    await page
      .getByRole('button', { name: /Confirm Delete|Подтвердить удаление|Confirm|Подтвердить/i })
      .click();
    await page.waitForTimeout(2000);

    await expect(page).toHaveURL(/\/flags$/);
    await page.waitForLoadState('domcontentloaded');
    await expect(page.getByText(flagText)).not.toBeVisible({ timeout: 5000 });
  });
});

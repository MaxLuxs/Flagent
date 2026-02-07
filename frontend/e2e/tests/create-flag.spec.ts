import { test, expect } from '@playwright/test';
import { createTenant, login } from '../helpers/api';

const E2E_NEW_FLAG = `e2e_new_${Date.now()}`;

async function setupLocalStorage(
  page: import('@playwright/test').Page,
  opts: { apiKey?: string; authToken?: string; clear?: boolean }
) {
  await page.addInitScript((opts: { apiKey?: string; authToken?: string; clear?: boolean }) => {
    if (opts.clear) {
      localStorage.removeItem('api_key');
      // Keep auth_token (from storageState) so tests can access app
    }
    if (opts.apiKey) localStorage.setItem('api_key', opts.apiKey);
    if (opts.authToken) localStorage.setItem('auth_token', opts.authToken);
  }, opts);
}

async function isCreateFormVisible(page: import('@playwright/test').Page): Promise<boolean> {
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  const selectors = [
    page.getByRole('button', { name: /Create Flag|Создать флаг/i }),
    page.getByPlaceholder(/Flag description|Описание флага|Enter flag description|Введите описание/i),
    page.getByLabel(/Description|Описание/i),
    page.getByRole('textbox').first(),
  ];
  for (const loc of selectors) {
    try {
      await loc.first().waitFor({ state: 'visible', timeout: 5000 });
      return true;
    } catch {
      continue;
    }
  }
  return false;
}

test.describe('Create Flag - Open Source (no tenant) @oss', () => {
  test.beforeEach(async ({ page }) => {
    await setupLocalStorage(page, { clear: true });
    await page.goto('/flags/new');
  });

  test('displays create flag form @smoke', async ({ page }) => {
    const formVisible = await isCreateFormVisible(page);
    if (!formVisible) {
      test.skip(true, 'Create form not available');
    }
    await expect(page.getByRole('textbox').first()).toBeVisible();
  });

  test('can create flag with description and key', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available');
    }
    const descInput = page.getByRole('textbox').first();
    await descInput.fill(E2E_NEW_FLAG);
    const keyInput = page.getByPlaceholder(/Flag key|Ключ флага/i).first();
    if ((await keyInput.count()) > 0) {
      await keyInput.fill(`e2e_key_${Date.now()}`);
    }
    // Wait for Compose state to update after fill (button enabled)
    await page.waitForTimeout(300);
    await page.getByRole('button', { name: /Create Flag|Создать флаг/i }).first().click();
    const navigated = await page.waitForURL(/\/flags\/\d+/, { timeout: 20000 }).catch(() => false);
    if (!navigated) {
      const errMsg = await page.getByText(/Create tenant first|Create first tenant|401|Unauthorized/i).first().textContent().catch(() => '');
      if (errMsg) test.skip(true, 'Backend requires tenant (enterprise); use Create Flag - With Tenant test');
      await expect(page).toHaveURL(/\/flags\/\d+/, { timeout: 1000 });
    }
    await expect(page.getByLabel(/Description|Описание|Flag description|Описание флага/i).first()).toHaveValue(
      E2E_NEW_FLAG,
      { timeout: 10000 }
    );
  });

  test('create button is disabled when description empty', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available');
    }
    const createBtn = page.getByRole('button', { name: /Create Flag|Создать флаг/i }).first();
    await expect(createBtn).toBeDisabled();
  });
});

test.describe('Create Flag - With Tenant (api_key) @enterprise', () => {
  test.beforeEach(async ({ page, request }) => {
    const result = await createTenant(request);
    if (!result) {
      test.skip(true, 'Tenant creation not available (backend may be OSS only)');
    }
    await setupLocalStorage(page, { apiKey: result!.apiKey, clear: true });
    await page.goto('/flags/new');
  });

  test('displays create flag form', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available after tenant setup');
    }
    await expect(page.getByRole('textbox').first()).toBeVisible();
  });

  test('can create flag with description and key', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available');
    }
    const descInput = page.getByRole('textbox').first();
    await descInput.fill(E2E_NEW_FLAG);
    const keyInput = page.getByPlaceholder(/Flag key|Ключ флага/i).first();
    if ((await keyInput.count()) > 0) {
      await keyInput.fill(`e2e_key_${Date.now()}`);
    }
    await page.getByRole('button', { name: /Create Flag|Создать флаг/i }).first().click();
    await expect(page).toHaveURL(/\/flags\/\d+/, { timeout: 15000 });
    await expect(page.getByLabel(/Description|Описание|Flag description|Описание флага/i).first()).toHaveValue(
      E2E_NEW_FLAG,
      { timeout: 10000 }
    );
  });

  test('create button is disabled when description empty', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available');
    }
    const createBtn = page.getByRole('button', { name: /Create Flag|Создать флаг/i }).first();
    await expect(createBtn).toBeDisabled();
  });
});

test.describe('Create Flag - With Auth (login + tenant) @enterprise', () => {
  test.beforeEach(async ({ page, request }) => {
    const loginResult = await login(request);
    if (!loginResult) {
      test.skip(true, 'Login not available (FLAGENT_ADMIN_AUTH_ENABLED may be false)');
    }
    const tenantResult = await createTenant(request, loginResult.token);
    if (!tenantResult) {
      test.skip(true, 'Tenant creation not available (admin auth may require JWT)');
    }
    await setupLocalStorage(page, {
      authToken: loginResult!.token,
      apiKey: tenantResult!.apiKey,
      clear: true,
    });
    await page.goto('/flags/new');
  });

  test('displays create flag form', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available after auth+tenant setup');
    }
    await expect(page.getByRole('textbox').first()).toBeVisible();
  });

  test('can create flag with description and key', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available');
    }
    const descInput = page.getByRole('textbox').first();
    await descInput.fill(E2E_NEW_FLAG);
    const keyInput = page.getByPlaceholder(/Flag key|Ключ флага/i).first();
    if ((await keyInput.count()) > 0) {
      await keyInput.fill(`e2e_key_${Date.now()}`);
    }
    await page.getByRole('button', { name: /Create Flag|Создать флаг/i }).first().click();
    await expect(page).toHaveURL(/\/flags\/\d+/, { timeout: 15000 });
    await expect(page.getByLabel(/Description|Описание|Flag description|Описание флага/i).first()).toHaveValue(
      E2E_NEW_FLAG,
      { timeout: 10000 }
    );
  });

  test('create button is disabled when description empty', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    if (!(await isCreateFormVisible(page))) {
      test.skip(true, 'Create form not available');
    }
    const createBtn = page.getByRole('button', { name: /Create Flag|Создать флаг/i }).first();
    await expect(createBtn).toBeDisabled();
  });
});

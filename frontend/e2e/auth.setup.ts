/**
 * Setup project: login once, create tenant, save storageState for all tests.
 * Runs before chromium/firefox/webkit projects.
 */
import { test as setup } from '@playwright/test';
import { createTenant, login } from './helpers/api';

const AUTH_FILE = 'playwright/.auth/user.json';

setup('authenticate @setup', async ({ page, request }) => {
  const loginResult = await login(request);
  if (!loginResult) {
    throw new Error('Login failed. Ensure backend is running: ./gradlew run');
  }

  const tenantResult = await createTenant(request, loginResult.token);
  const apiKey = tenantResult?.apiKey;

  await page.goto('/');
  await page.evaluate(
    ({ token, apiKey }: { token: string; apiKey?: string }) => {
      localStorage.setItem('auth_token', token);
      if (apiKey) {
        localStorage.setItem('api_key', apiKey);
      }
    },
    { token: loginResult.token, apiKey }
  );

  await page.context().storageState({ path: AUTH_FILE });
});

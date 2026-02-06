import { defineConfig, devices } from '@playwright/test';

const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:8080';
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18000';

/**
 * FLAGENT_EDITION: oss | enterprise
 * When set (e.g. in CI), only tests tagged @oss or @enterprise run.
 * E2E_MODE: smoke | full
 * When smoke, only @smoke tests run.
 */
const FLAGENT_EDITION = process.env.FLAGENT_EDITION;
const E2E_MODE = process.env.E2E_MODE;

const AUTH_FILE = 'playwright/.auth/user.json';

function getGrep(): RegExp | undefined {
  const setup = '@setup';
  if (FLAGENT_EDITION && E2E_MODE === 'smoke') {
    return new RegExp(`${setup}|@${FLAGENT_EDITION}.*@smoke|@smoke.*@${FLAGENT_EDITION}`);
  }
  if (E2E_MODE === 'smoke') {
    return new RegExp(`${setup}|@smoke`);
  }
  if (FLAGENT_EDITION) {
    return new RegExp(`${setup}|@${FLAGENT_EDITION}`);
  }
  return undefined;
}

export default defineConfig({
  globalSetup: './global-setup.ts',
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  grep: getGrep(),
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    process.env.CI ? ['github'] : ['list'],
  ],
  use: {
    baseURL: FRONTEND_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 15000,
  },
  expect: { timeout: 15000 },
  projects: [
    { name: 'setup', testDir: '.', testMatch: /auth\.setup\.ts/ },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], storageState: AUTH_FILE },
      dependencies: ['setup'],
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'], storageState: AUTH_FILE },
      dependencies: ['setup'],
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'], storageState: AUTH_FILE },
      dependencies: ['setup'],
    },
  ],
  webServer: process.env.CI
    ? {
        command: './gradlew :backend:runDev',
        cwd: '../..',
        url: `${BACKEND_URL}/api/v1/health`,
        timeout: 180_000,
        reuseExistingServer: false,
      }
    : undefined,
});

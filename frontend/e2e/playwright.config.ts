import { defineConfig, devices } from '@playwright/test';

const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:8080';
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18000';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    process.env.CI ? ['github'] : ['list'],
  ],
  use: {
    baseURL: FRONTEND_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
  ],
  webServer: process.env.CI
    ? {
        command: './gradlew run',
        cwd: '../..',
        url: FRONTEND_URL,
        timeout: 180_000,
        reuseExistingServer: false,
      }
    : undefined,
});

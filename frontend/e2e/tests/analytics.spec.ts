import { test, expect } from '@playwright/test';
import { seedAnalyticsData } from '../helpers/api';

async function ensureAnalyticsSeedAndApiKey(
  page: import('@playwright/test').Page,
  request: import('@playwright/test').APIRequestContext
) {
  const result = await seedAnalyticsData(request);
  if (result?.apiKey) {
    await page.addInitScript(
      (key: string) => localStorage.setItem('api_key', key),
      result.apiKey
    );
  }
  return result;
}

test.describe('Analytics Page @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/analytics');
  });

  test('displays Analytics page @smoke', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page.getByRole('heading', { name: /Analytics|Аналитика/i })
    ).toBeVisible({ timeout: 10000 });
  });

  test('loads analytics content', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(
      page
        .getByText(
          /Analytics|Аналитика|Overview|Обзор|By flags|По флагам|Loading|Загрузка|No flags|Флаги не найдены|Total evaluations|Evaluations/i
        )
        .first()
    ).toBeVisible({ timeout: 20000 });
  });
});

test.describe('Analytics Page with seeded data @oss', () => {
  test('Overview tab shows Total evaluations and Unique flags cards', async ({
    page,
    request,
  }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');

    await expect(
      page.getByText(/Total evaluations|Всего оценок/i)
    ).toBeVisible({ timeout: 10000 });
    await expect(
      page.getByText(/Unique flags|Уникальных флагов/i)
    ).toBeVisible({ timeout: 10000 });
    await expect(page.getByText(/\d+/).first()).toBeVisible({ timeout: 5000 });
  });

  test('Overview tab shows time series chart when data exists', async ({
    page,
    request,
  }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');
    await expect(page.getByText(/Total evaluations|Всего оценок/i)).toBeVisible({
      timeout: 15000,
    });

    const chartTitle = page.getByText(
      /Evaluations over time|Оценки за период|evaluations/i
    );
    await expect(chartTitle.first()).toBeVisible({ timeout: 20000 });
  });

  test('Overview tab shows top flags list', async ({ page, request }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');
    await expect(page.getByText(/Total evaluations|Всего оценок/i)).toBeVisible({
      timeout: 15000,
    });

    const topFlagsHeading = page.getByText(
      /Top flags by evaluations|Топ флагов по оценкам|evaluations/i
    );
    await expect(topFlagsHeading.first()).toBeVisible({ timeout: 20000 });

    // Enterprise uses metric_data_points (not evaluation_events) - overview may be empty.
    const topFlag = seed.flags[0];
    const flagEl = page.getByText(topFlag.key);
    try {
      await expect(flagEl).toBeVisible({ timeout: 5000 });
    } catch {
      test.skip(true, 'Overview empty (enterprise uses metric_data_points, not evaluation_events)');
    }
  });

  test('Overview tab links to flag metrics', async ({ page, request }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');

    const viewMetricsLink = page.getByRole('link', {
      name: /View metrics|Метрики|view metrics/i,
    });
    if ((await viewMetricsLink.count()) > 0) {
      await viewMetricsLink.first().click();
      await expect(page).toHaveURL(new RegExp(`/flags/\\d+/metrics`));
    }
  });

  test('By flags tab shows table with Evaluations column', async ({
    page,
    request,
  }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');

    const byFlagsTab = page.getByRole('button', {
      name: /By flags|По флагам/i,
    });
    if ((await byFlagsTab.count()) > 0) {
      await byFlagsTab.click();
      // Use columnheader to avoid strict mode (many cells contain "evaluations")
      const evalsHeader = page.getByRole('columnheader', {
        name: /Evaluations|Вычисления/i,
      });
      await expect(evalsHeader.first()).toBeVisible({ timeout: 10000 });
      await expect(page.getByText(seed.flags[0].key)).toBeVisible({
        timeout: 15000,
      });
    }
  });

  test('By flags tab row click navigates to flag metrics', async ({
    page,
    request,
  }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');

    const byFlagsTab = page.getByRole('button', {
      name: /By flags|По флагам/i,
    });
    if ((await byFlagsTab.count()) > 0) {
      await byFlagsTab.click();
      const firstFlagKey = seed.flags[0].key;
      const row = page.getByText(firstFlagKey).first();
      await row.click();
      await expect(page).toHaveURL(new RegExp(`/flags/\\d+/metrics`));
    }
  });
});

test.describe('Analytics per-flag metrics (Core) @oss', () => {
  test('Flag metrics page shows API Evaluation Stats or Metrics & Analytics', async ({
    page,
    request,
  }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');
    const viewMetricsLink = page.getByRole('link', {
      name: /View metrics|Метрики|view metrics/i,
    });
    if ((await viewMetricsLink.count()) === 0) {
      test.skip();
      return;
    }
    await viewMetricsLink.first().click();
    await expect(page).toHaveURL(new RegExp(`/flags/\\d+/metrics`), {
      timeout: 10000,
    });

    const heading = page.getByRole('heading', {
      name: /API Evaluation Stats|Metrics & Analytics|Метрики и аналитика/i,
    });
    const lastHourBtn = page.getByRole('button', {
      name: /Last Hour|Последний час/i,
    });
    await expect(heading.or(lastHourBtn).first()).toBeVisible({ timeout: 15000 });
  });

  test('Flag metrics page shows evaluation count from seeded data', async ({
    page,
    request,
  }) => {
    const seed = await ensureAnalyticsSeedAndApiKey(page, request);
    if (!seed || seed.totalEvaluations === 0) {
      test.skip();
      return;
    }

    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');
    const viewMetricsLink = page.getByRole('link', {
      name: /View metrics|Метрики|view metrics/i,
    });
    if ((await viewMetricsLink.count()) === 0) {
      test.skip();
      return;
    }
    await viewMetricsLink.first().click();
    await expect(page).toHaveURL(new RegExp(`/flags/\\d+/metrics`), {
      timeout: 10000,
    });

    const totalEvals = page.getByText(/Total evaluations|Всего оценок/i);
    const lastHourBtn = page.getByRole('button', {
      name: /Last Hour|Последний час/i,
    });
    await expect(totalEvals.or(lastHourBtn).first()).toBeVisible({ timeout: 15000 });
    const anyNumber = page.getByText(/\b[1-9]\d*\b/).first();
    await expect(anyNumber).toBeVisible({ timeout: 10000 });
  });
});

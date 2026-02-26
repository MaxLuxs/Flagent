import { test, expect } from '@playwright/test';

/**
 * Segments page: list of segments (per flag or global depending on edition).
 */
test.describe('Segments Page @oss', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/segments');
  });

  test('displays Segments page @smoke', async ({ page }) => {
    await page.waitForLoadState('networkidle').catch(() => {});
    await expect(page).toHaveURL(/\/segments/);
    await expect(
      page.getByRole('heading', { name: /Segments|Сегменты/i }).or(
        page.getByText(/Segments|Сегменты|flags with segments/i).first()
      )
    ).toBeVisible({ timeout: 15000 });
  });

  test('shows content: table or empty state', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    const table = page.locator('table');
    const emptyOrMessage = page.getByText(/No segments|Нет сегментов|flags with segments|Loading|Загрузка/i);
    await expect(table.or(emptyOrMessage).first()).toBeVisible({ timeout: 15000 });
  });
});

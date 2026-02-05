/**
 * Wait for backend to be reachable before running tests.
 * When not in CI, webServer doesn't start the backend - this gives it time if started externally.
 */
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:18000';
const healthUrl = `${BACKEND_URL}/api/v1/health`;
const maxWaitMs = 60000;
const intervalMs = 2000;

export default async function globalSetup() {
  if (process.env.CI) return; // webServer handles it

  const start = Date.now();
  while (Date.now() - start < maxWaitMs) {
    try {
      const res = await fetch(healthUrl);
      if (res.ok) return;
    } catch {
      // not ready
    }
    await new Promise((r) => setTimeout(r, intervalMs));
  }
  console.warn('Backend may not be ready. Run: ./gradlew run');
}

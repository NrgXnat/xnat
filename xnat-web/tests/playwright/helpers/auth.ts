import { Page } from '@playwright/test';

/**
 * Log in to XNAT via the web UI login form.
 */
export async function login(page: Page, user?: string, pass?: string): Promise<void> {
  const username = user || process.env.XNAT_ADMIN_USER || 'admin';
  const password = pass || process.env.XNAT_ADMIN_PASS || 'admin';

  // Navigate to a page that will redirect to login if not authenticated
  await page.goto('/');
  // Wait for login form or already-authenticated page
  try {
    await page.locator('#username').waitFor({ timeout: 5_000 });
  } catch {
    // Already logged in
    return;
  }
  await page.locator('#username').fill(username);
  await page.locator('#password').fill(password);
  await page.locator('#loginButton').click();
  // Wait for redirect away from login page
  await page.waitForURL(url => !url.pathname.includes('Login'), { timeout: 15_000 });
}

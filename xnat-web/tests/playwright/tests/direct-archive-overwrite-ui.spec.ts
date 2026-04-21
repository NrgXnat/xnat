import { test, expect } from '@playwright/test';
import { login } from '../helpers/auth';
import { XnatApi } from '../helpers/xnat-api';

/**
 * Helper to navigate to the DICOM SCP Receivers panel on the admin page.
 */
async function navigateToScpPanel(page: any): Promise<void> {
  await page.goto('/page/admin');
  await page.waitForTimeout(3000);
  // Click "DICOM SCP Receivers" in the sidebar
  await page.evaluate(() => {
    const link = Array.from(document.querySelectorAll('a'))
      .find(a => a.textContent!.trim() === 'DICOM SCP Receivers');
    if (link) (link as HTMLElement).click();
  });
  await page.waitForTimeout(5000);
}

/**
 * Helper to open the editor dialog for an existing receiver by AE title.
 */
async function openEditorForReceiver(page: any, aeTitle: string): Promise<void> {
  await page.evaluate((title: string) => {
    const links = Array.from(document.querySelectorAll('td.aeTitle a, #dicom-scp-manager a'));
    const link = links.find(a => a.textContent!.trim() === title);
    if (link) (link as HTMLElement).click();
  }, aeTitle);
  await page.waitForSelector('form[name="dicomScpEditor"]', { timeout: 10_000 });
}

/**
 * Helper to open the "New" receiver dialog.
 */
async function openNewReceiverDialog(page: any): Promise<void> {
  await page.locator('button:has-text("New DICOM SCP Receiver"):visible').click();
  await page.waitForSelector('form[name="dicomScpEditor"]', { timeout: 10_000 });
}

/**
 * Locator for the directArchiveOverwrite panel container (the div, not the input).
 */
function overwritePanel(page: any) {
  return page.locator('div[data-name="directArchiveOverwrite"]');
}

/**
 * Locator for the directArchiveOverwrite switchbox controller input.
 */
function overwriteInput(page: any) {
  return page.locator('div[data-name="directArchiveOverwrite"] input.controller');
}

test.describe('Direct Archive Overwrite - Admin UI', () => {

  let api: XnatApi;

  test.beforeEach(async ({ page, request }) => {
    api = new XnatApi(request);
    // Clean up any leftover test receivers from prior runs
    await api.deleteReceiversByPrefix('PWTEST_');
    await login(page);
  });

  test('append-only mode switchbox is visible and unchecked by default for new receivers', async ({ page }) => {
    await navigateToScpPanel(page);
    await openNewReceiverDialog(page);

    await expect(overwritePanel(page)).toBeVisible();
    // The controller checkbox is CSS-hidden inside the switchbox; check its state, not visibility
    await expect(overwriteInput(page)).not.toBeChecked();
  });

  test('append-only mode switchbox has correct label text', async ({ page }) => {
    await navigateToScpPanel(page);
    await openNewReceiverDialog(page);

    const panel = overwritePanel(page);
    await expect(panel).toBeVisible();
    await expect(panel.locator('.element-label')).toContainText('Direct Archive Append-Only Mode');
    await expect(panel).toContainText('Append-Only Mode');
  });

  test('append-only mode reflects saved value when editing receiver', async ({ page }) => {
    const receiver = await api.createReceiver({
      aeTitle: 'PWTEST_OW',
      port: 8119,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: true,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });

    try {
      await navigateToScpPanel(page);
      await openEditorForReceiver(page, 'PWTEST_OW');

      await expect(overwriteInput(page)).toBeChecked();
    } finally {
      await api.deleteReceiver(receiver.id);
    }
  });

  test('append-only mode toggle persists after saving', async ({ page }) => {
    const receiver = await api.createReceiver({
      aeTitle: 'PWTEST_SAV',
      port: 8121,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: false,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });

    try {
      await navigateToScpPanel(page);
      await openEditorForReceiver(page, 'PWTEST_SAV');

      await expect(overwriteInput(page)).not.toBeChecked();

      // Click the switchbox toggle to enable append mode
      await overwritePanel(page).locator('.switchbox-outer').click();

      // Save via the dialog button
      await page.locator('.xnat-dialog button.default:has-text("Save")').click();
      await page.waitForTimeout(5000);

      // Verify via API
      const updated = await api.getReceiver(receiver.id);
      expect(updated.directArchiveOverwrite).toBe(true);
    } finally {
      await api.deleteReceiver(receiver.id);
    }
  });

  test('receiver table shows overwrite mode in behavior column', async ({ page }) => {
    const receiver = await api.createReceiver({
      aeTitle: 'PWTEST_TBL',
      port: 8122,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: true,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });

    try {
      await navigateToScpPanel(page);

      const row = page.locator('tr', { has: page.locator(`a:has-text("PWTEST_TBL")`) });
      const behaviorCell = row.locator('td.behavior');

      await expect(behaviorCell).toContainText('Direct Archive Enabled (Append)');
    } finally {
      await api.deleteReceiver(receiver.id);
    }
  });

  test('receiver table shows Overwrite when append mode is off', async ({ page }) => {
    const receiver = await api.createReceiver({
      aeTitle: 'PWTEST_TB2',
      port: 8123,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: false,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });

    try {
      await navigateToScpPanel(page);

      const row = page.locator('tr', { has: page.locator(`a:has-text("PWTEST_TB2")`) });
      const behaviorCell = row.locator('td.behavior');

      await expect(behaviorCell).toContainText('Direct Archive Enabled (Overwrite)');
    } finally {
      await api.deleteReceiver(receiver.id);
    }
  });
});

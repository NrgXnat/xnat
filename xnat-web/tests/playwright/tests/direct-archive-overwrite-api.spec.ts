import { test, expect } from '@playwright/test';
import { XnatApi } from '../helpers/xnat-api';

test.describe('Direct Archive Overwrite - API', () => {

  let api: XnatApi;
  let createdIds: number[] = [];

  test.beforeEach(async ({ request }) => {
    api = new XnatApi(request);
    createdIds = [];
    // Clean up any leftover test receivers from prior runs
    await api.deleteReceiversByPrefix('APITEST_');
  });

  test.afterEach(async () => {
    for (const id of createdIds) {
      await api.deleteReceiver(id).catch(() => {});
    }
  });

  test('new receiver defaults directArchiveOverwrite to false (overwrite)', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_DEF',
      port: 8130,
      enabled: false,
      directArchive: true,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    expect(receiver.directArchiveOverwrite).toBe(false);
  });

  test('create receiver with append mode (true)', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_APP',
      port: 8131,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: true,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    expect(receiver.directArchiveOverwrite).toBe(true);

    // Verify via separate GET
    const fetched = await api.getReceiver(receiver.id);
    expect(fetched.directArchiveOverwrite).toBe(true);
  });

  test('create receiver with overwrite mode (false)', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_DEL',
      port: 8132,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: false,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    expect(receiver.directArchiveOverwrite).toBe(false);
  });

  test('update receiver from overwrite to append mode', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_UPD',
      port: 8133,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: false,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    await api.updateReceiver(receiver.id, {
      ...receiver,
      directArchiveOverwrite: true,
    });

    const updated = await api.getReceiver(receiver.id);
    expect(updated.directArchiveOverwrite).toBe(true);
  });

  test('update receiver from append to overwrite mode', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_UP2',
      port: 8134,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: true,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    await api.updateReceiver(receiver.id, {
      ...receiver,
      directArchiveOverwrite: false,
    });

    const updated = await api.getReceiver(receiver.id);
    expect(updated.directArchiveOverwrite).toBe(false);
  });

  test('legacy string "append" is accepted and returned as true', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_LEG',
      port: 8135,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: 'append' as any,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    expect(receiver.directArchiveOverwrite).toBe(true);
  });

  test('legacy string "delete" is accepted and returned as false', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_LG2',
      port: 8136,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: 'delete' as any,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    expect(receiver.directArchiveOverwrite).toBe(false);
  });

  test('overwrite mode persists across receiver list retrieval', async () => {
    const receiver = await api.createReceiver({
      aeTitle: 'APITEST_LST',
      port: 8137,
      enabled: false,
      directArchive: true,
      directArchiveOverwrite: true,
      anonymizationEnabled: true,
      identifier: 'dicomObjectIdentifier',
    });
    createdIds.push(receiver.id);

    const receivers = await api.getReceivers();
    const found = receivers.find((r: any) => r.id === receiver.id);
    expect(found).toBeDefined();
    expect(found.directArchiveOverwrite).toBe(true);
  });

  test('enableDirectArchiveAppend site config can be toggled', async () => {
    // Save original value to restore later
    const original = await api.getSiteConfig('enableDirectArchiveAppend');

    try {
      // Enable
      await api.setSiteConfig('enableDirectArchiveAppend', 'true');
      const enabled = await api.getSiteConfig('enableDirectArchiveAppend');
      expect(enabled).toBe('true');

      // Disable
      await api.setSiteConfig('enableDirectArchiveAppend', 'false');
      const disabled = await api.getSiteConfig('enableDirectArchiveAppend');
      expect(disabled).toBe('false');
    } finally {
      // Restore original
      await api.setSiteConfig('enableDirectArchiveAppend', original);
    }
  });
});

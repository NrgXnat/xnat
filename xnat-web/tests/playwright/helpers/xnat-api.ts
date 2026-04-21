import { APIRequestContext, expect } from '@playwright/test';

/**
 * Helper for XNAT REST API calls with admin authentication.
 */
export class XnatApi {
  private readonly baseUrl: string;
  private readonly user: string;
  private readonly pass: string;
  private request: APIRequestContext;

  constructor(request: APIRequestContext) {
    this.baseUrl = process.env.XNAT_URL || 'http://localhost:8080';
    this.user = process.env.XNAT_ADMIN_USER || 'admin';
    this.pass = process.env.XNAT_ADMIN_PASS || 'admin';
    this.request = request;
  }

  private authHeader(): Record<string, string> {
    const encoded = Buffer.from(`${this.user}:${this.pass}`).toString('base64');
    return { 'Authorization': `Basic ${encoded}` };
  }

  // --- Site Config ---

  async getSiteConfig(property: string): Promise<string> {
    const resp = await this.request.get(`${this.baseUrl}/xapi/siteConfig/${property}`, {
      headers: this.authHeader(),
    });
    return resp.text();
  }

  async setSiteConfig(property: string, value: string): Promise<void> {
    await this.request.post(`${this.baseUrl}/xapi/siteConfig/${property}`, {
      headers: { ...this.authHeader(), 'Content-Type': 'text/plain' },
      data: value,
    });
  }

  // --- DICOM SCP Receivers ---

  async getReceivers(): Promise<any[]> {
    const resp = await this.request.get(`${this.baseUrl}/xapi/dicomscp`, {
      headers: this.authHeader(),
    });
    const text = await resp.text();
    return JSON.parse(text);
  }

  async getReceiver(id: number): Promise<any> {
    const resp = await this.request.get(`${this.baseUrl}/xapi/dicomscp/${id}`, {
      headers: this.authHeader(),
    });
    const text = await resp.text();
    return JSON.parse(text);
  }

  async createReceiver(receiver: Record<string, any>): Promise<any> {
    const resp = await this.request.post(`${this.baseUrl}/xapi/dicomscp`, {
      headers: { ...this.authHeader(), 'Content-Type': 'application/json' },
      data: receiver,
    });
    const text = await resp.text();
    expect(resp.ok(), `createReceiver failed (${resp.status()}): ${text}`).toBeTruthy();
    return JSON.parse(text);
  }

  async updateReceiver(id: number, receiver: Record<string, any>): Promise<any> {
    const resp = await this.request.put(`${this.baseUrl}/xapi/dicomscp/${id}`, {
      headers: { ...this.authHeader(), 'Content-Type': 'application/json' },
      data: receiver,
    });
    const text = await resp.text();
    expect(resp.ok(), `updateReceiver failed (${resp.status()}): ${text}`).toBeTruthy();
    return JSON.parse(text);
  }

  async deleteReceiver(id: number): Promise<void> {
    await this.request.delete(`${this.baseUrl}/xapi/dicomscp/${id}`, {
      headers: this.authHeader(),
    });
  }

  /**
   * Delete all test receivers whose AE title starts with the given prefix.
   * Useful for cleanup before/after tests to avoid port conflicts.
   */
  async deleteReceiversByPrefix(prefix: string): Promise<void> {
    const receivers = await this.getReceivers();
    for (const r of receivers) {
      if (r.aeTitle && r.aeTitle.startsWith(prefix)) {
        await this.deleteReceiver(r.id).catch(() => {});
      }
    }
  }
}

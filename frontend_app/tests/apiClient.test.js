import { afterEach, describe, expect, it, vi } from 'vitest';
import apiClient from '../src/utils/apiClient';

describe('apiClient anonymous requests', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('does not attach a legacy persisted token to requests', async () => {
    vi.stubGlobal('localStorage', {
      getItem: () => JSON.stringify({ state: { token: 'test-token' } }),
    });

    let requestConfig;
    const response = await apiClient.get('/transactions', {
      adapter: async (config) => {
        requestConfig = config;
        return {
          data: [],
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
        };
      },
    });

    expect(response.data).toEqual([]);
    expect(requestConfig.headers.get('Authorization')).toBeUndefined();
  });
});

import type { AxiosInstance } from 'axios';
import { Configuration } from '../configuration';
import { ExportApi } from '../api/export-api';

describe('ExportApi', () => {
  let mockAxios: jest.Mocked<AxiosInstance>;

  beforeEach(() => {
    mockAxios = {
      request: jest.fn(),
      defaults: { baseURL: '' },
    } as unknown as jest.Mocked<AxiosInstance>;
  });

  it('should get export eval cache JSON', async () => {
    const config = new Configuration({ basePath: 'https://api.example.com/api/v1' });
    const api = new ExportApi(config, 'https://api.example.com/api/v1', mockAxios);

    const snapshot = { flags: [{ id: 1, key: 'f1' }] };
    (mockAxios.request as jest.Mock).mockResolvedValue({
      data: snapshot,
      status: 200,
    });

    const result = await api.getExportEvalCacheJSON();
    expect(result.data).toEqual(snapshot);
    expect(mockAxios.request).toHaveBeenCalledTimes(1);
  });
});

import type { AxiosInstance } from 'axios';
import { Configuration } from '../configuration';
import { HealthApi } from '../api/health-api';

describe('HealthApi', () => {
  let mockAxios: jest.Mocked<AxiosInstance>;

  beforeEach(() => {
    mockAxios = {
      request: jest.fn(),
      defaults: { baseURL: '' },
    } as unknown as jest.Mocked<AxiosInstance>;
  });

  it('should get health', async () => {
    const config = new Configuration({ basePath: 'https://api.example.com/api/v1' });
    const api = new HealthApi(config, 'https://api.example.com/api/v1', mockAxios);

    (mockAxios.request as jest.Mock).mockResolvedValue({
      data: { status: 'ok' },
      status: 200,
    });

    const result = await api.getHealth();
    expect(result.data).toEqual({ status: 'ok' });
    expect(mockAxios.request).toHaveBeenCalledTimes(1);
  });

  it('should get info', async () => {
    const config = new Configuration({ basePath: 'https://api.example.com/api/v1' });
    const api = new HealthApi(config, 'https://api.example.com/api/v1', mockAxios);

    (mockAxios.request as jest.Mock).mockResolvedValue({
      data: { version: '0.1.5' },
      status: 200,
    });

    const result = await api.getInfo();
    expect(result.data.version).toBe('0.1.5');
  });
});

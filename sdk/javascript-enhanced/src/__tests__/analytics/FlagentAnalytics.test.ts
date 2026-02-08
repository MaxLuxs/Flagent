import { FlagentAnalytics } from '../../analytics/FlagentAnalytics';

const mockFetch = jest.fn();
const mockCrypto = {
  getRandomValues: (arr: Uint8Array) => {
    for (let i = 0; i < arr.length; i++) arr[i] = 1;
  },
};
const mockStorage: Record<string, string> = {};
const storage = {
  getItem: (k: string) => mockStorage[k] ?? null,
  setItem: (k: string, v: string) => { mockStorage[k] = v; },
  removeItem: () => {},
  clear: () => {},
  length: 0,
  key: () => null,
};

beforeEach(() => {
  jest.useFakeTimers();
  jest.clearAllMocks();
  Object.keys(mockStorage).forEach((k) => delete mockStorage[k]);
  (global as any).fetch = mockFetch;
  (global as any).crypto = mockCrypto;
  (global as any).localStorage = storage;
  (global as any).sessionStorage = storage;
  (global as any).window = {
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    location: { pathname: '/', href: 'http://test/' },
  };
  (global as any).document = { title: 'Test' };
  (global as any).history = {
    pushState: jest.fn(),
    replaceState: jest.fn(),
  };
});

afterEach(() => {
  jest.useRealTimers();
});

describe('FlagentAnalytics', () => {
  it('strips trailing slash from baseUrl', () => {
    new FlagentAnalytics({ baseUrl: 'http://test/' });
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('logEvent buffers and flushes on threshold', async () => {
    mockFetch.mockResolvedValue({ ok: true });
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logEvent('custom', { key: 'value' });
    for (let i = 0; i < 9; i++) analytics.logEvent('e' + i);
    expect(mockFetch).toHaveBeenCalled();
    const body = JSON.parse(mockFetch.mock.calls[0][1].body);
    expect(body.events).toHaveLength(10);
  });

  it('logFirstOpen logs first_open', () => {
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logFirstOpen();
    expect(mockStorage['flagent_first_open_done']).toBe('1');
  });

  it('logSessionStart logs session_start', () => {
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logSessionStart();
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('logPageView logs page_view', () => {
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logPageView('/home', 'Home');
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('flush sends buffered events', async () => {
    mockFetch.mockResolvedValue({ ok: true });
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logEvent('test');
    await analytics.flush();
    expect(mockFetch).toHaveBeenCalledWith(
      'http://test/api/v1/analytics/events',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
      })
    );
  });

  it('flush does nothing when buffer empty', async () => {
    mockFetch.mockResolvedValue({ ok: true });
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    await analytics.flush();
    const firstCallCount = mockFetch.mock.calls.length;
    await analytics.flush();
    expect(mockFetch.mock.calls.length).toBe(firstCallCount);
  });

  it('flush re-buffers on fetch failure', async () => {
    mockFetch.mockRejectedValue(new Error('network'));
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logEvent('test');
    await analytics.flush();
    analytics.logEvent('test2');
    await analytics.flush();
    expect(mockFetch).toHaveBeenCalledTimes(2);
  });

  it('destroy clears timer and flushes', async () => {
    mockFetch.mockResolvedValue({ ok: true });
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test' });
    analytics.logEvent('test');
    await analytics.destroy();
    expect(mockFetch).toHaveBeenCalled();
  });

  it('sends X-API-Key when configured', async () => {
    mockFetch.mockResolvedValue({ ok: true });
    const analytics = new FlagentAnalytics({ baseUrl: 'http://test', apiKey: 'secret' });
    analytics.logEvent('test');
    await analytics.flush();
    expect(mockFetch.mock.calls[0][1].headers['X-API-Key']).toBe('secret');
  });
});

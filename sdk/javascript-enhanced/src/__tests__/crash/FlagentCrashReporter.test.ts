import { FlagentCrashReporter } from '../../crash/FlagentCrashReporter';

const mockFetch = jest.fn();
let errorHandler: ((e: ErrorEvent) => void) | undefined;
let rejectionHandler: ((e: PromiseRejectionEvent) => void) | undefined;

beforeEach(() => {
  jest.clearAllMocks();
  (global as any).fetch = mockFetch;
  (global as any).window = {
    location: {},
    addEventListener: (name: string, fn: (e: any) => void) => {
      if (name === 'error') errorHandler = fn;
      if (name === 'unhandledrejection') rejectionHandler = fn;
    },
    removeEventListener: jest.fn(),
  };
});

describe('FlagentCrashReporter', () => {
  it('strips trailing slash from baseUrl', () => {
    const reporter = new FlagentCrashReporter({ baseUrl: 'http://test/' });
    expect(reporter).toBeDefined();
  });

  it('install registers error handlers when window exists', () => {
    const reporter = new FlagentCrashReporter({ baseUrl: 'http://test' });
    reporter.install();
    expect(errorHandler).toBeDefined();
    expect(rejectionHandler).toBeDefined();
  });

  it('error handler sends crash report', () => {
    mockFetch.mockResolvedValue({ ok: true });
    const reporter = new FlagentCrashReporter({ baseUrl: 'http://test' });
    reporter.install();
    expect(errorHandler).toBeDefined();
    errorHandler!({
      message: 'Test error',
      filename: 'test.js',
      lineno: 10,
      colno: 5,
      error: new Error('Test'),
    } as ErrorEvent);
    expect(mockFetch).toHaveBeenCalledWith(
      'http://test/api/v1/crashes',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
      })
    );
  });

  it('rejection handler sends crash report', () => {
    mockFetch.mockResolvedValue({ ok: true });
    const reporter = new FlagentCrashReporter({ baseUrl: 'http://test' });
    reporter.install();
    expect(rejectionHandler).toBeDefined();
    rejectionHandler!({
      reason: new Error('Rejection'),
    } as PromiseRejectionEvent);
    expect(mockFetch).toHaveBeenCalled();
  });

  it('uninstall removes handlers', () => {
    const reporter = new FlagentCrashReporter({ baseUrl: 'http://test' });
    reporter.install();
    reporter.uninstall();
    expect((global as any).window.removeEventListener).toHaveBeenCalled();
  });

  it('sends X-API-Key when configured', () => {
    mockFetch.mockResolvedValue({ ok: true });
    const reporter = new FlagentCrashReporter({ baseUrl: 'http://test', apiKey: 'key' });
    reporter.install();
    errorHandler!({ message: 'err', error: new Error('e') } as ErrorEvent);
    expect(mockFetch.mock.calls[0][1].headers['X-API-Key']).toBe('key');
  });
});

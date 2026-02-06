/**
 * Firebase Crashlytics-level crash reporting for Flagent.
 * Captures window.onerror and unhandledrejection, sends to Flagent backend.
 */

export interface FlagentCrashReporterConfig {
  /** Base URL of Flagent server (e.g. "http://localhost:18000") */
  baseUrl: string;
  /** Optional X-API-Key for tenant-scoped crash reports */
  apiKey?: string;
  /** Platform identifier, default "web" */
  platform?: string;
  /** Optional app version string */
  appVersion?: string;
}

interface CrashPayload {
  stackTrace: string;
  message: string;
  platform: string;
  appVersion?: string;
  deviceInfo?: string;
  breadcrumbs?: string | null;
  customKeys?: string | null;
  timestamp: number;
}

export class FlagentCrashReporter {
  private readonly baseUrl: string;
  private readonly apiKey?: string;
  private readonly platform: string;
  private readonly appVersion?: string;
  private onErrorHandler?: (event: ErrorEvent) => void;
  private onRejectionHandler?: (event: PromiseRejectionEvent) => void;

  constructor(config: FlagentCrashReporterConfig) {
    this.baseUrl = config.baseUrl.replace(/\/$/, '');
    this.apiKey = config.apiKey;
    this.platform = config.platform ?? 'web';
    this.appVersion = config.appVersion;
  }

  /** Install crash reporter. Call early in app startup. */
  install(): void {
    this.onErrorHandler = (event: ErrorEvent) => {
      const message = event.message ?? String(event.error ?? 'Unknown error');
      const stackTrace = event.error?.stack ?? `${event.filename}:${event.lineno}:${event.colno}`;
      this.send({ stackTrace, message });
    };
    this.onRejectionHandler = (event: PromiseRejectionEvent) => {
      const err = event.reason;
      const message = err?.message ?? String(err ?? 'Unhandled rejection');
      const stackTrace = err?.stack ?? 'No stack trace';
      this.send({ stackTrace, message });
    };
    if (typeof window !== 'undefined') {
      window.addEventListener('error', this.onErrorHandler);
      window.addEventListener('unhandledrejection', this.onRejectionHandler);
    }
  }

  /** Uninstall crash reporter. */
  uninstall(): void {
    if (typeof window !== 'undefined') {
      if (this.onErrorHandler) window.removeEventListener('error', this.onErrorHandler);
      if (this.onRejectionHandler) window.removeEventListener('unhandledrejection', this.onRejectionHandler);
    }
  }

  private send(payload: Partial<CrashPayload>): void {
    const full: CrashPayload = {
      stackTrace: payload.stackTrace ?? 'No stack trace',
      message: payload.message ?? 'Unknown error',
      platform: this.platform,
      appVersion: this.appVersion,
      deviceInfo: typeof navigator !== 'undefined' ? navigator.userAgent : undefined,
      breadcrumbs: null,
      customKeys: null,
      timestamp: Date.now(),
      ...payload,
    };
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (this.apiKey) headers['X-API-Key'] = this.apiKey;
    fetch(`${this.baseUrl}/api/v1/crashes`, {
      method: 'POST',
      headers,
      body: JSON.stringify(full),
      keepalive: true,
    }).catch(() => {});
  }
}

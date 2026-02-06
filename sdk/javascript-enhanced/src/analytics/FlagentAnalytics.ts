/**
 * Firebase-level analytics client for Flagent.
 * Logs events (first_open, session_start, screen_view, custom) to Flagent backend.
 */
export interface FlagentAnalyticsConfig {
  /** Base URL of Flagent server (e.g. "http://localhost:18000") */
  baseUrl: string;
  /** Optional X-API-Key for tenant-scoped analytics */
  apiKey?: string;
  /** Platform identifier: "android", "ios", "web" */
  platform?: string;
  /** Optional app version string */
  appVersion?: string;
  /** Optional user ID */
  userId?: string;
}

interface AnalyticsEventPayload {
  eventName: string;
  eventParams?: string;
  userId?: string;
  sessionId: string;
  platform?: string;
  appVersion?: string;
  timestampMs: number;
}

export class FlagentAnalytics {
  private readonly baseUrl: string;
  private readonly apiKey?: string;
  private readonly platform: string;
  private readonly appVersion?: string;
  private readonly userId?: string;
  private sessionId: string;
  private eventBuffer: AnalyticsEventPayload[] = [];
  private readonly maxBufferSize = 10;
  private flushTimer?: ReturnType<typeof setTimeout>;

  constructor(config: FlagentAnalyticsConfig) {
    this.baseUrl = config.baseUrl.replace(/\/$/, '');
    this.apiKey = config.apiKey;
    this.platform = config.platform ?? 'web';
    this.appVersion = config.appVersion;
    this.userId = config.userId;
    this.sessionId = this.getOrCreateSessionId();
    this.scheduleFlush();
    this.logSessionStart();
    this.logFirstOpenIfNeeded();
    this.installPageViewTracking();
  }

  private logFirstOpenIfNeeded(): void {
    if (typeof localStorage === 'undefined') return;
    const key = 'flagent_first_open_done';
    if (localStorage.getItem(key)) return;
    localStorage.setItem(key, '1');
    this.logFirstOpen();
  }

  private installPageViewTracking(): void {
    if (typeof window === 'undefined' || typeof document === 'undefined') return;
    const logCurrentPage = () => {
      const path = window.location.pathname || '/';
      const title = document.title || '';
      this.logPageView(path, title || undefined);
    };
    logCurrentPage();
    const origPushState = history.pushState;
    const origReplaceState = history.replaceState;
    history.pushState = (...args) => {
      origPushState.apply(history, args);
      logCurrentPage();
    };
    history.replaceState = (...args) => {
      origReplaceState.apply(history, args);
      logCurrentPage();
    };
    window.addEventListener('popstate', logCurrentPage);
  }

  private getOrCreateSessionId(): string {
    if (typeof sessionStorage !== 'undefined') {
      let sid = sessionStorage.getItem('flagent_session_id');
      if (!sid) {
        sid = 'sess_' + Date.now() + '_' + Math.random().toString(36).slice(2);
        sessionStorage.setItem('flagent_session_id', sid);
      }
      return sid;
    }
    return 'sess_' + Date.now() + '_' + Math.random().toString(36).slice(2);
  }

  private scheduleFlush(): void {
    this.flushTimer = setInterval(() => this.flush(), 5000);
  }

  /**
   * Log an analytics event (Firebase-style: first_open, session_start, screen_view, custom).
   */
  logEvent(eventName: string, params?: Record<string, string>): void {
    const event: AnalyticsEventPayload = {
      eventName,
      eventParams: params ? JSON.stringify(params) : undefined,
      userId: this.userId,
      sessionId: this.sessionId,
      platform: this.platform,
      appVersion: this.appVersion,
      timestampMs: Date.now(),
    };
    this.eventBuffer.push(event);
    if (this.eventBuffer.length >= this.maxBufferSize) {
      this.flush();
    }
  }

  /** Log first_open (call on first app launch). */
  logFirstOpen(): void {
    this.logEvent('first_open');
  }

  /** Log session_start (call when app/session starts). */
  logSessionStart(): void {
    this.logEvent('session_start');
  }

  /** Log page_view (call on route/navigation change). */
  logPageView(path: string, title?: string): void {
    const params: Record<string, string> = { screen: path };
    if (title) params.screen_class = title;
    this.logEvent('page_view', params);
  }

  /** Flush buffered events to server. */
  async flush(): Promise<void> {
    if (this.eventBuffer.length === 0) return;
    const toSend = this.eventBuffer.splice(0, this.eventBuffer.length);
    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      if (this.apiKey) headers['X-API-Key'] = this.apiKey;
      await fetch(`${this.baseUrl}/api/v1/analytics/events`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ events: toSend }),
      });
    } catch {
      this.eventBuffer.unshift(...toSend);
    }
  }

  /** Stop the analytics client and flush remaining events. */
  async destroy(): Promise<void> {
    if (this.flushTimer) {
      clearInterval(this.flushTimer);
      this.flushTimer = undefined;
    }
    await this.flush();
  }
}

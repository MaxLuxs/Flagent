//
// FlagentAnalytics.swift
// Firebase-level analytics client for Flagent.
//

import Foundation

/// Logs events (first_open, session_start, screen_view, custom) to Flagent backend.
public class FlagentAnalytics {

    private let baseUrl: String
    private let apiKey: String?
    private let platform: String
    private let appVersion: String?
    private let userId: String?
    private let sessionId: String
    private var eventBuffer: [AnalyticsEventPayload] = []
    private let maxBufferSize = 10
    private let queue = DispatchQueue(label: "com.flagent.analytics")
    private var flushTimer: Timer?

    public init(
        baseUrl: String,
        apiKey: String? = nil,
        platform: String = "ios",
        appVersion: String? = nil,
        userId: String? = nil
    ) {
        self.baseUrl = baseUrl.replacingOccurrences(of: "/$", with: "", options: .regularExpression)
        self.apiKey = apiKey
        self.platform = platform
        self.appVersion = appVersion
        self.userId = userId
        self.sessionId = "sess_\(Int64(Date().timeIntervalSince1970 * 1000))_\(UUID().uuidString.prefix(8))"
        self.scheduleFlush()
        self.logSessionStart()
        self.logFirstOpenIfNeeded()
    }

    private func logFirstOpenIfNeeded() {
        let key = "flagent_first_open_done"
        if UserDefaults.standard.bool(forKey: key) { return }
        UserDefaults.standard.set(true, forKey: key)
        logFirstOpen()
    }

    private func scheduleFlush() {
        flushTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
            self?.flush()
        }
        RunLoop.current.add(flushTimer!, forMode: .common)
    }

    /// Log an analytics event (Firebase-style: first_open, session_start, screen_view, custom).
    public func logEvent(eventName: String, params: [String: String]? = nil) {
        let event = AnalyticsEventPayload(
            eventName: eventName,
            eventParams: params.flatMap { try? JSONSerialization.data(withJSONObject: $0) }.flatMap { String(data: $0, encoding: .utf8) },
            userId: userId,
            sessionId: sessionId,
            platform: platform,
            appVersion: appVersion,
            timestampMs: Int64(Date().timeIntervalSince1970 * 1000)
        )
        queue.sync {
            eventBuffer.append(event)
            if eventBuffer.count >= maxBufferSize {
                flush()
            }
        }
    }

    /// Log first_open (call on first app launch).
    public func logFirstOpen() {
        logEvent(eventName: "first_open")
    }

    /// Log session_start (call when app/session starts).
    public func logSessionStart() {
        logEvent(eventName: "session_start")
    }

    /// Log screen_view (call on screen/navigation change).
    public func logScreenView(screenName: String, screenClass: String? = nil) {
        var params: [String: String] = ["screen": screenName]
        if let sc = screenClass { params["screen_class"] = sc }
        logEvent(eventName: "screen_view", params: params)
    }

    /// Flush buffered events to server.
    public func flush() {
        let toSend: [AnalyticsEventPayload] = queue.sync {
            guard !eventBuffer.isEmpty else { return [] }
            let result = eventBuffer
            eventBuffer = []
            return result
        }
        guard !toSend.isEmpty else { return }
        let url = URL(string: "\(baseUrl)/api/v1/analytics/events")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let key = apiKey { request.setValue(key, forHTTPHeaderField: "X-API-Key") }
        let body: [String: Any] = ["events": toSend.map { $0.toDictionary() }]
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        URLSession.shared.dataTask(with: request) { [weak self] _, _, error in
            if error != nil {
                self?.queue.sync { self?.eventBuffer.insert(contentsOf: toSend, at: 0) }
            }
        }.resume()
    }

    /// Stop the analytics client and flush remaining events.
    public func destroy() {
        flush()
    }

    private struct AnalyticsEventPayload {
        let eventName: String
        let eventParams: String?
        let userId: String?
        let sessionId: String
        let platform: String?
        let appVersion: String?
        let timestampMs: Int64

        func toDictionary() -> [String: Any] {
            var d: [String: Any] = [
                "eventName": eventName,
                "sessionId": sessionId,
                "timestampMs": timestampMs,
            ]
            if let p = eventParams { d["eventParams"] = p }
            if let u = userId { d["userId"] = u }
            if let p = platform { d["platform"] = p }
            if let a = appVersion { d["appVersion"] = a }
            return d
        }
    }
}

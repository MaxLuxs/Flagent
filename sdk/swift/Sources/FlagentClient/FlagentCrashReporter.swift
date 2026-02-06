//
// FlagentCrashReporter.swift
// Firebase Crashlytics-level crash reporting for Flagent.
//

import Foundation

#if os(iOS) || os(macOS) || os(tvOS) || os(watchOS)

/// Crash reporter that sends uncaught exceptions to Flagent backend.
public class FlagentCrashReporter {

    private let baseUrl: String
    private let apiKey: String?
    private let platform: String
    private let appVersion: String?
    private let deviceInfo: String?
    private static var instance: FlagentCrashReporter?
    private static var previousHandler: (@convention(c) (NSException) -> Void)?

    public init(
        baseUrl: String,
        apiKey: String? = nil,
        platform: String = "ios",
        appVersion: String? = nil,
        deviceInfo: String? = nil
    ) {
        self.baseUrl = baseUrl.replacingOccurrences(of: "/$", with: "", options: .regularExpression)
        self.apiKey = apiKey
        self.platform = platform
        self.appVersion = appVersion
        self.deviceInfo = deviceInfo
    }

    /// Install crash reporter. Call early in app startup (e.g. AppDelegate didFinishLaunching).
    public func install() {
        FlagentCrashReporter.instance = self
        FlagentCrashReporter.previousHandler = NSGetUncaughtExceptionHandler()
        NSSetUncaughtExceptionHandler { exception in
            FlagentCrashReporter.instance?.report(exception: exception)
            FlagentCrashReporter.previousHandler?(exception)
        }
    }

    /// Uninstall and restore previous handler.
    public func uninstall() {
        NSSetUncaughtExceptionHandler(FlagentCrashReporter.previousHandler)
        FlagentCrashReporter.instance = nil
    }

    private func report(exception: NSException) {
        let stackTrace = exception.callStackSymbols.joined(separator: "\n")
        let message = exception.reason ?? exception.name.rawValue
        let payload: [String: Any] = [
            "stackTrace": stackTrace,
            "message": message,
            "platform": platform,
            "appVersion": appVersion as Any,
            "deviceInfo": deviceInfo as Any,
            "breadcrumbs": NSNull(),
            "customKeys": NSNull(),
            "timestamp": Int64(Date().timeIntervalSince1970 * 1000)
        ]
        guard let data = try? JSONSerialization.data(withJSONObject: payload) else { return }
        var request = URLRequest(url: URL(string: "\(baseUrl)/api/v1/crashes")!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let key = apiKey { request.setValue(key, forHTTPHeaderField: "X-API-Key") }
        request.httpBody = data
        URLSession.shared.dataTask(with: request) { _, _, _ in }.resume()
    }
}

#else

/// Stub for non-Apple platforms.
public class FlagentCrashReporter {
    public init(baseUrl: String, apiKey: String? = nil, platform: String = "unknown", appVersion: String? = nil, deviceInfo: String? = nil) {}
    public func install() {}
    public func uninstall() {}
}

#endif

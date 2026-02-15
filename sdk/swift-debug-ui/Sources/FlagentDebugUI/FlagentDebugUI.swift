import SwiftUI
import FlagentEnhanced
import FlagentClient
import AnyCodable

// MARK: - Flag row (for flags list)

@available(macOS 10.15, iOS 13.0, *)
public struct FlagRow: Identifiable {
    public let key: String
    public let id: Int64
    public let enabled: Bool
    public let variantKeys: [String]

    public init(key: String, id: Int64, enabled: Bool, variantKeys: [String] = []) {
        self.key = key
        self.id = id
        self.enabled = enabled
        self.variantKeys = variantKeys
    }
}

// MARK: - Entry point

@available(macOS 10.15, iOS 13.0, *)
public struct FlagentDebugUI {
    /// Presents the debug UI. On macOS, opens a new panel; on iOS, the app should present `DebugView(manager:flagsProvider:)` in a sheet.
    public static func show(manager: FlagentManager, flagsProvider: (() async throws -> [FlagRow])? = nil) {
        #if os(macOS)
        let panel = NSPanel(
            contentRect: NSRect(x: 0, y: 0, width: 480, height: 560),
            styleMask: [.titled, .closable, .resizable],
            backing: .buffered,
            defer: false
        )
        panel.title = "Flagent Debug"
        panel.minSize = NSSize(width: 400, height: 400)
        panel.contentViewController = NSHostingController(rootView: DebugView(manager: manager, flagsProvider: flagsProvider))
        panel.center()
        panel.makeKeyAndOrderFront(nil)
        #endif
    }
}

// MARK: - Debug view

@available(macOS 10.15, iOS 13.0, *)
public struct DebugView: View {
    let manager: FlagentManager
    let flagsProvider: (() async throws -> [FlagRow])?

    @State private var flagKey: String = ""
    @State private var flagIDText: String = ""
    @State private var entityID: String = ""
    @State private var entityType: String = ""
    @State private var enableDebug: Bool = false
    @State private var result: EvalResult?
    @State private var errorMessage: String?
    @State private var isEvaluating: Bool = false
    @State private var cacheClearedMessage: String?
    @State private var lastEvaluations: [EvalResult] = []
    @State private var flagsList: [FlagRow] = []
    @State private var flagsLoading: Bool = false
    @State private var overrides: [String: String] = [:]
    private let lastEvalLimit: Int = 10

    public init(manager: FlagentManager, flagsProvider: (() async throws -> [FlagRow])? = nil) {
        self.manager = manager
        self.flagsProvider = flagsProvider
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: FlagentTokens.Spacing._16) {
                if flagsProvider != nil {
                    flagsSection
                }
                evaluationFormSection
                resultSection
                cacheSection
                lastEvaluationsSection
            }
            .padding(FlagentTokens.Spacing._16)
        }
        .frame(minWidth: 380, minHeight: 400)
        .background(FlagentTokens.Colors.Dark.background)
        .onAppear {
            loadFlagsIfNeeded()
        }
    }

    private func loadFlagsIfNeeded() {
        guard let provider = flagsProvider else { return }
        flagsLoading = true
        Task {
            do {
                flagsList = try await provider()
            } catch {}
            await MainActor.run {
                flagsLoading = false
            }
        }
    }

    // MARK: - Flags list

    private var flagsSection: some View {
        Group {
            Text("Flags")
                .font(.headline)
                .foregroundColor(FlagentTokens.Colors.Dark.text)
            HStack(spacing: FlagentTokens.Spacing._12) {
                Button("Refresh") {
                    loadFlagsIfNeeded()
                }
                .buttonStyle(.bordered)
                if !overrides.isEmpty {
                    Button("Clear all overrides") {
                        overrides = [:]
                    }
                    .buttonStyle(.bordered)
                }
            }
            if flagsLoading && flagsList.isEmpty {
                Text("Loading…")
                    .font(.caption)
                    .foregroundColor(FlagentTokens.Colors.Dark.text_light)
            } else if flagsList.isEmpty {
                Text("No flags")
                    .font(.caption)
                    .foregroundColor(FlagentTokens.Colors.Dark.text_light)
            } else {
                VStack(alignment: .leading, spacing: FlagentTokens.Spacing._6) {
                    ForEach(flagsList) { row in
                        HStack(alignment: .center) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(row.key)
                                    .font(.body)
                                Text(verbatim: "enabled=\(row.enabled), variants=\(row.variantKeys.joined(separator: ", "))")
                                    .font(.caption)
                                    .foregroundColor(FlagentTokens.Colors.Dark.text_light)
                            }
                            Spacer()
                            Text(overrides[row.key] ?? "—")
                                .font(.caption)
                            Picker(overrides[row.key] != nil ? "Change" : "Override", selection: Binding(
                                get: { overrides[row.key] ?? "" },
                                set: { val in
                                    if val.isEmpty {
                                        overrides.removeValue(forKey: row.key)
                                    } else {
                                        overrides[row.key] = val
                                    }
                                }
                            )) {
                                Text("—").tag("")
                                Text("disabled").tag("disabled")
                                ForEach(row.variantKeys, id: \.self) { vk in
                                    Text(vk).tag(vk)
                                }
                            }
                            if overrides[row.key] != nil {
                                Button("Clear") {
                                    overrides.removeValue(forKey: row.key)
                                }
                                .buttonStyle(.bordered)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
        }
    }

    // MARK: - Evaluation form

    private var evaluationFormSection: some View {
        Group {
            Text("Evaluate")
                .font(.headline)
                .foregroundColor(FlagentTokens.Colors.Dark.text)
            HStack {
                TextField("Flag key", text: $flagKey)
                    .textFieldStyle(.roundedBorder)
                TextField("Flag ID", text: $flagIDText)
                    .textFieldStyle(.roundedBorder)
            }
            Text("Current entity (used for evaluate)")
                .font(.caption)
                .foregroundColor(FlagentTokens.Colors.Dark.text_light)
            HStack {
                TextField("Entity ID", text: $entityID)
                    .textFieldStyle(.roundedBorder)
                TextField("Entity type", text: $entityType)
                    .textFieldStyle(.roundedBorder)
            }
            Toggle("Enable debug", isOn: $enableDebug)
            Button(action: runEvaluate) {
                Group {
                    if isEvaluating {
                        Text("Evaluating…")
                            .frame(maxWidth: .infinity)
                    } else {
                        Text("Evaluate")
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .disabled(isEvaluating || (flagKey.isEmpty && flagIDText.isEmpty))
            .buttonStyle(.bordered)
        }
    }

    // MARK: - Result

    private var resultSection: some View {
        Group {
            if let err = errorMessage {
                Text(err)
                    .foregroundColor(FlagentTokens.Colors.error)
                    .font(.caption)
            }
            if let r = result {
                VStack(alignment: .leading, spacing: FlagentTokens.Spacing._6) {
                    Text("Result")
                        .font(.headline)
                        .foregroundColor(FlagentTokens.Colors.Dark.text)
                    resultRow("Flag key", r.flagKey)
                    resultRow("Flag ID", r.flagID.map { "\($0)" })
                    resultRow("Variant key", r.variantKey)
                    resultRow("Variant ID", r.variantID.map { "\($0)" })
                    resultRow("Segment ID", r.segmentID.map { "\($0)" })
                    if let att = r.variantAttachment, !att.isEmpty {
                        Text("Attachment: \(formatAttachment(att))")
                            .font(.caption)
                    }
                    if enableDebug, let log = r.evalDebugLog {
                        evalDebugLogView(log)
                    }
                }
                .padding(FlagentTokens.Spacing._8)
                .background(FlagentTokens.Colors.Dark.card_bg)
                .cornerRadius(FlagentTokens.Radius.lg)
            }
        }
    }

    private func resultRow(_ label: String, _ value: String?) -> some View {
        HStack {
            Text("\(label):")
                .fontWeight(.medium)
                .foregroundColor(FlagentTokens.Colors.Dark.text)
            Text(value ?? "—")
                .font(.caption)
                .foregroundColor(FlagentTokens.Colors.Dark.text_light)
        }
    }

    private func formatAttachment(_ att: [String: AnyCodable]) -> String {
        let pairs = att.map { "\($0.key): \($0.value)" }
        return pairs.joined(separator: ", ")
    }

    private func evalDebugLogView(_ log: EvalDebugLog) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            if let msg = log.msg {
                Text("Debug: \(msg)")
                    .font(.caption)
            }
            if let segments = log.segmentDebugLogs, !segments.isEmpty {
                ForEach(Array(segments.enumerated()), id: \.offset) { _, s in
                    Text(segmentDebugDescription(s))
                        .font(.caption)
                }
            }
        }
    }

    private func segmentDebugDescription(_ s: SegmentDebugLog) -> String {
        let sid = s.segmentID.map { "\($0)" } ?? "?"
        let msg = s.msg ?? ""
        return "Segment \(sid): \(msg)"
    }

    // MARK: - Cache

    private var cacheSection: some View {
        Group {
            Text("Cache")
                .font(.headline)
                .foregroundColor(FlagentTokens.Colors.Dark.text)
            HStack(spacing: FlagentTokens.Spacing._12) {
                Button("Clear cache") {
                    manager.clearCache()
                    cacheClearedMessage = "Cache cleared"
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        cacheClearedMessage = nil
                    }
                }
                .buttonStyle(.bordered)
                Button("Evict expired") {
                    manager.evictExpired()
                    cacheClearedMessage = "Expired entries evicted"
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        cacheClearedMessage = nil
                    }
                }
                .buttonStyle(.bordered)
                if let msg = cacheClearedMessage {
                    Text(msg)
                        .foregroundColor(FlagentTokens.Colors.Dark.text_light)
                        .font(.caption)
                }
            }
        }
    }

    // MARK: - Last evaluations

    private var lastEvaluationsSection: some View {
        Group {
            if !lastEvaluations.isEmpty {
                Text("Last evaluations")
                    .font(.headline)
                    .foregroundColor(FlagentTokens.Colors.Dark.text)
                ForEach(Array(lastEvaluations.enumerated()), id: \.offset) { idx, r in
                    HStack {
                        Text("\(idx + 1).")
                        Text(r.flagKey ?? "key?")
                            .lineLimit(1)
                        Text("→")
                        Text(r.variantKey ?? "—")
                            .lineLimit(1)
                        Spacer()
                    }
                    .font(.caption)
                    .foregroundColor(FlagentTokens.Colors.Dark.text_light)
                    .padding(.vertical, 2)
                }
            }
        }
    }

    // MARK: - Actions

    private func runEvaluate() {
        errorMessage = nil
        result = nil
        isEvaluating = true
        let key = flagKey.isEmpty ? nil : flagKey
        let fid = Int64(flagIDText.trimmingCharacters(in: .whitespaces))
        let eid = entityID.isEmpty ? nil : entityID
        let etype = entityType.isEmpty ? nil : entityType

        if let k = key, let overrideVariant = overrides[k] {
            let r = EvalResult(flagKey: k, variantKey: overrideVariant)
            result = r
            prependLastEvaluation(r)
            isEvaluating = false
            return
        }

        Task {
            do {
                let r = try await manager.evaluate(
                    flagKey: key,
                    flagID: fid,
                    entityID: eid,
                    entityType: etype,
                    entityContext: nil,
                    enableDebug: enableDebug
                )
                await MainActor.run {
                    result = r
                    prependLastEvaluation(r)
                    isEvaluating = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isEvaluating = false
                }
            }
        }
    }

    private func prependLastEvaluation(_ r: EvalResult) {
        lastEvaluations.insert(r, at: 0)
        if lastEvaluations.count > lastEvalLimit {
            lastEvaluations = Array(lastEvaluations.prefix(lastEvalLimit))
        }
    }
}

// MARK: - macOS panel support

#if os(macOS)
import AppKit
#endif

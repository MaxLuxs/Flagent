import SwiftUI
import FlagentEnhanced
import FlagentClient
import AnyCodable

// MARK: - Entry point

@available(macOS 10.15, iOS 13.0, *)
public struct FlagentDebugUI {
    /// Presents the debug UI. On macOS, opens a new panel; on iOS, the app should present `DebugView(manager:)` in a sheet.
    public static func show(manager: FlagentManager) {
        #if os(macOS)
        let panel = NSPanel(
            contentRect: NSRect(x: 0, y: 0, width: 480, height: 560),
            styleMask: [.titled, .closable, .resizable],
            backing: .buffered,
            defer: false
        )
        panel.title = "Flagent Debug"
        panel.minSize = NSSize(width: 400, height: 400)
        panel.contentViewController = NSHostingController(rootView: DebugView(manager: manager))
        panel.center()
        panel.makeKeyAndOrderFront(nil)
        #endif
    }
}

// MARK: - Debug view

@available(macOS 10.15, iOS 13.0, *)
public struct DebugView: View {
    let manager: FlagentManager

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
    private let lastEvalLimit: Int = 10

    public init(manager: FlagentManager) {
        self.manager = manager
    }

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                evaluationFormSection
                resultSection
                cacheSection
                lastEvaluationsSection
            }
            .padding()
        }
        .frame(minWidth: 380, minHeight: 400)
    }

    // MARK: - Evaluation form

    private var evaluationFormSection: some View {
        Group {
            Text("Evaluate")
                .font(.headline)
            HStack {
                TextField("Flag key", text: $flagKey)
                    .textFieldStyle(.roundedBorder)
                TextField("Flag ID", text: $flagIDText)
                    .textFieldStyle(.roundedBorder)
            }
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
                    .foregroundColor(.red)
                    .font(.caption)
            }
            if let r = result {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Result")
                        .font(.headline)
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
                .padding(8)
                .background(Color.secondary.opacity(0.15))
                .cornerRadius(8)
            }
        }
    }

    private func resultRow(_ label: String, _ value: String?) -> some View {
        HStack {
            Text("\(label):")
                .fontWeight(.medium)
            Text(value ?? "—")
                .font(.caption)
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
            HStack(spacing: 12) {
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
                        .foregroundColor(.secondary)
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

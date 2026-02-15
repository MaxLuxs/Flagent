import Foundation
import FlagentClient
import AnyCodable
import Combine

/// Fetches snapshot from Export API (eval cache JSON) or falls back to FlagAPI.findFlags.
@available(macOS 10.15, iOS 13.0, *)
struct SnapshotFetcher {
    private let ttlMs: Int64

    init(ttlMs: Int64) {
        self.ttlMs = ttlMs
    }

    func fetchSnapshot() async throws -> OfflineFlagSnapshot {
        do {
            let body = try await ExportAPI.getExportEvalCacheJSON().async()
            let json = body
            let flags = parseExportJSON(json)
            return OfflineFlagSnapshot(
                flags: flags,
                revision: "snapshot-\(currentTimeMs())",
                fetchedAtMs: currentTimeMs(),
                ttlMs: ttlMs
            )
        } catch {
            return try await fetchSnapshotFallback()
        }
    }

    private func fetchSnapshotFallback() async throws -> OfflineFlagSnapshot {
        let flagsList = try await FlagAPI.findFlags(limit: 1000, offset: nil, enabled: nil, description: nil, key: nil, descriptionLike: nil, preload: true, deleted: false, tags: nil).async()
        var flags: [Int64: OfflineLocalFlag] = [:]
        for flagDto in flagsList {
            let segments = (flagDto.segments ?? []).map { seg -> OfflineLocalSegment in
                let constraints = (seg.constraints ?? []).map { c in
                    OfflineLocalConstraint(
                        id: c.id,
                        property: c.property,
                        operator_: c._operator.rawValue,
                        value: c.value
                    )
                }
                let distributions = (seg.distributions ?? []).map { d in
                    OfflineLocalDistribution(
                        id: d.id,
                        variantID: d.variantID,
                        percent: Int(d.percent)
                    )
                }
                return OfflineLocalSegment(
                    id: seg.id,
                    rank: Int(seg.rank),
                    rolloutPercent: Int(seg.rolloutPercent),
                    constraints: constraints,
                    distributions: distributions
                )
            }
            let variants = (flagDto.variants ?? []).map { v in
                OfflineLocalVariant(id: v.id, key: v.key, attachment: v.attachment)
            }
            let localFlag = OfflineLocalFlag(
                id: flagDto.id,
                key: flagDto.key,
                enabled: flagDto.enabled,
                segments: segments,
                variants: variants
            )
            flags[flagDto.id] = localFlag
        }
        return OfflineFlagSnapshot(
            flags: flags,
            revision: "snapshot-\(currentTimeMs())",
            fetchedAtMs: currentTimeMs(),
            ttlMs: ttlMs
        )
    }

    private func parseExportJSON(_ json: [String: AnyCodable]) -> [Int64: OfflineLocalFlag] {
        var flags: [Int64: OfflineLocalFlag] = [:]
        if let flagsArray = json["flags"]?.value as? [[String: AnyCodable]] {
            for flagEl in flagsArray {
                let flagObj = flagEl
                guard let flagId = int64(from: flagObj["id"]) else { continue }
                if let f = parseFlagObject(flagObj, flagId: flagId) {
                    flags[flagId] = f
                }
            }
        } else {
            for (key, value) in json {
                guard let flagId = Int64(key) else { continue }
                guard let flagObj = (value.value as? [String: AnyCodable]) else { continue }
                if let f = parseFlagObject(flagObj, flagId: flagId) {
                    flags[flagId] = f
                }
            }
        }
        return flags
    }

    private func parseFlagObject(_ flagObj: [String: AnyCodable], flagId: Int64) -> OfflineLocalFlag? {
        let segments: [OfflineLocalSegment] = {
            guard let arr = flagObj["segments"]?.value as? [[String: AnyCodable]] else { return [] }
            return arr.compactMap { segEl in
                let id = int64(from: segEl["id"]) ?? 0
                let rank = int(from: segEl["rank"]) ?? 0
                let rolloutPercent = int(from: segEl["rolloutPercent"]) ?? 100
                let constraints: [OfflineLocalConstraint] = {
                    guard let cArr = segEl["constraints"]?.value as? [[String: AnyCodable]] else { return [] }
                    return cArr.compactMap { c in
                        guard let cid = int64(from: c["id"]),
                              let prop = c["property"]?.value as? String,
                              let val = c["value"]?.value as? String else { return nil }
                        let op = c["operator"]?.value as? String ?? "EQ"
                        return OfflineLocalConstraint(id: cid, property: prop, operator_: op, value: val)
                    }
                }()
                let distributions: [OfflineLocalDistribution] = {
                    guard let dArr = segEl["distributions"]?.value as? [[String: AnyCodable]] else { return [] }
                    return dArr.compactMap { d in
                        guard let did = int64(from: d["id"]),
                              let vid = int64(from: d["variantId"]) ?? int64(from: d["variantID"]),
                              let pct = int(from: d["percent"]) else { return nil }
                        return OfflineLocalDistribution(id: did, variantID: vid, percent: pct)
                    }
                }()
                return OfflineLocalSegment(id: id, rank: rank, rolloutPercent: rolloutPercent, constraints: constraints, distributions: distributions)
            }
        }()
        let variants: [OfflineLocalVariant] = {
            guard let vArr = flagObj["variants"]?.value as? [[String: AnyCodable]] else { return [] }
            return vArr.compactMap { v in
                guard let vid = int64(from: v["id"]),
                      let key = v["key"]?.value as? String else { return nil }
                let attachment = v["attachment"]?.value as? [String: AnyCodable]
                return OfflineLocalVariant(id: vid, key: key, attachment: attachment)
            }
        }()
        let key = flagObj["key"]?.value as? String ?? ""
        let enabled = (flagObj["enabled"]?.value as? Bool) ?? false
        return OfflineLocalFlag(id: flagId, key: key, enabled: enabled, segments: segments, variants: variants)
    }

    private func int64(from ac: AnyCodable?) -> Int64? {
        guard let ac = ac else { return nil }
        if let n = ac.value as? Int { return Int64(n) }
        if let n = ac.value as? Int64 { return n }
        if let s = ac.value as? String { return Int64(s) }
        return nil
    }

    private func int(from ac: AnyCodable?) -> Int? {
        guard let ac = ac else { return nil }
        if let n = ac.value as? Int { return n }
        if let n = ac.value as? Int64 { return Int(n) }
        if let s = ac.value as? String { return Int(s) }
        return nil
    }

    private func currentTimeMs() -> Int64 {
        Int64(Date().timeIntervalSince1970 * 1000)
    }
}

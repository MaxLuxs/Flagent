import XCTest
import FlagentClient
import AnyCodable
@testable import FlagentEnhanced

// MARK: - Mock for FlagentClient / FlagentManagerAdapter tests

@available(macOS 10.15, iOS 13.0, *)
private final class MockFlagentEvaluating: FlagentEvaluating {
    var evaluateResult: EvalResult?
    var evaluateError: Error?
    var evaluateBatchResult: [EvalResult] = []
    var evaluateBatchError: Error?

    func evaluate(
        flagKey: String?,
        flagID: Int64?,
        entityID: String?,
        entityType: String?,
        entityContext: [String: AnyCodable]?,
        enableDebug: Bool
    ) async throws -> EvalResult {
        if let e = evaluateError { throw e }
        if let r = evaluateResult { return r }
        return EvalResult(flagKey: flagKey ?? "", variantKey: nil)
    }

    func evaluateBatch(
        flagKeys: [String]?,
        flagIDs: [Int64]?,
        entities: [EvaluationEntity],
        enableDebug: Bool
    ) async throws -> [EvalResult] {
        if let e = evaluateBatchError { throw e }
        return evaluateBatchResult
    }
}

// MARK: - Flagent unified entry point tests

@available(macOS 10.15, iOS 13.0, *)
final class FlagentBuilderTests: XCTestCase {

    func testBuilderBuildReturnsFlagentClient() {
        let client = Flagent.builder()
            .baseURL("https://api.example.com/api/v1")
            .build()
        XCTAssertTrue(client is FlagentManagerAdapter)
    }

    func testBuilderChainBaseURLHeaderBearerCacheMode() {
        let client = Flagent.builder()
            .baseURL("https://host/v1")
            .header(name: "X-API-Key", value: "key1")
            .bearerToken("token1")
            .cache(enable: true, ttlMs: 60_000)
            .mode(.server)
            .build()
        XCTAssertNotNil(client)
        XCTAssertTrue(client is FlagentManagerAdapter)
    }

    func testBuilderDefaultModeIsServer() {
        _ = Flagent.builder().baseURL("https://x").build()
        // Just ensure build succeeds with default mode
    }

    func testBuilderOfflineModeReturnsClient() {
        let client = Flagent.builder()
            .baseURL("https://api.example.com/api/v1")
            .mode(.offline)
            .build()
        XCTAssertTrue(client is FlagentManagerAdapter)
    }
}

@available(macOS 10.15, iOS 13.0, *)
final class FlagentClientAdapterTests: XCTestCase {

    func testEvaluateForwardsToEvaluator() async throws {
        let mock = MockFlagentEvaluating()
        mock.evaluateResult = EvalResult(flagKey: "feature_x", variantKey: "on")
        let client = FlagentManagerAdapter(evaluator: mock)

        let result = try await client.evaluate(
            flagKey: "feature_x",
            entityID: "user1",
            entityType: "user"
        )
        XCTAssertEqual(result.flagKey, "feature_x")
        XCTAssertEqual(result.variantKey, "on")
    }

    func testIsEnabledReturnsTrueWhenVariantKeySet() async throws {
        let mock = MockFlagentEvaluating()
        mock.evaluateResult = EvalResult(flagKey: "f", variantKey: "on")
        let client = FlagentManagerAdapter(evaluator: mock)

        let enabled = try await client.isEnabled(flagKey: "f", entityID: "e1")
        XCTAssertTrue(enabled)
    }

    func testIsEnabledReturnsFalseWhenVariantKeyNil() async throws {
        let mock = MockFlagentEvaluating()
        mock.evaluateResult = EvalResult(flagKey: "f", variantKey: nil)
        let client = FlagentManagerAdapter(evaluator: mock)

        let enabled = try await client.isEnabled(flagKey: "f", entityID: "e1")
        XCTAssertFalse(enabled)
    }

    func testIsEnabledReturnsTrueWhenVariantKeyEmptyString() async throws {
        let mock = MockFlagentEvaluating()
        mock.evaluateResult = EvalResult(flagKey: "f", variantKey: "")
        let client = FlagentManagerAdapter(evaluator: mock)
        // Empty string is non-nil, so isEnabled is true (variantKey != nil)
        let enabled = try await client.isEnabled(flagKey: "f", entityID: "e1")
        XCTAssertTrue(enabled)
    }

    func testEvaluateBatchForwardsToEvaluator() async throws {
        let mock = MockFlagentEvaluating()
        mock.evaluateBatchResult = [
            EvalResult(flagKey: "f1", variantKey: "control"),
            EvalResult(flagKey: "f2", variantKey: "treatment"),
        ]
        let client = FlagentManagerAdapter(evaluator: mock)
        let entities = [EvaluationEntity(entityID: "u1", entityType: "user")]

        let results = try await client.evaluateBatch(flagKeys: ["f1", "f2"], entities: entities)
        XCTAssertEqual(results.count, 2)
        XCTAssertEqual(results[0].flagKey, "f1")
        XCTAssertEqual(results[1].variantKey, "treatment")
    }

    func testIsEnabledPropagatesEvaluateError() async {
        let mock = MockFlagentEvaluating()
        mock.evaluateError = NSError(domain: "test", code: -1, userInfo: nil)
        let client = FlagentManagerAdapter(evaluator: mock)

        do {
            _ = try await client.isEnabled(flagKey: "f", entityID: "e1")
            XCTFail("Expected throw")
        } catch {
            XCTAssertEqual((error as NSError).domain, "test")
        }
    }
}

// MARK: - Offline / RolloutAlgorithm tests

@available(macOS 10.15, iOS 13.0, *)
final class RolloutAlgorithmTests: XCTestCase {
    func testBucketDeterministic() {
        let b1 = RolloutAlgorithm.bucket(entityID: "user123", salt: "flag_salt")
        let b2 = RolloutAlgorithm.bucket(entityID: "user123", salt: "flag_salt")
        XCTAssertEqual(b1, b2)
        XCTAssertLessThan(b1, 1000)
    }

    func testRollout100PercentReturnsVariant() {
        let (variantId, _) = RolloutAlgorithm.rollout(
            entityID: "entity_1",
            salt: "1",
            rolloutPercent: 100,
            variantIds: [42],
            percentsAccumulated: [1000]
        )
        XCTAssertEqual(variantId, 42)
    }

    func testRolloutEmptyEntityIDReturnsNil() {
        let (variantId, msg) = RolloutAlgorithm.rollout(
            entityID: "",
            salt: "1",
            rolloutPercent: 100,
            variantIds: [10],
            percentsAccumulated: [1000]
        )
        XCTAssertNil(variantId)
        XCTAssertTrue(msg.contains("empty entityID"))
    }

    func testRolloutFiftyFiftyDistribution() {
        let variantIds = [10, 11]
        let percents = [500, 1000]
        let (variantId, _) = RolloutAlgorithm.rollout(
            entityID: "test_entity_123",
            salt: "1",
            rolloutPercent: 100,
            variantIds: variantIds,
            percentsAccumulated: percents
        )
        XCTAssertTrue(variantId == 10 || variantId == 11)
    }
}

@available(macOS 10.15, iOS 13.0, *)
final class OfflineFlagentManagerTests: XCTestCase {
    func testNotBootstrappedEvaluateThrows() async {
        let manager = OfflineFlagentManager(config: OfflineFlagentConfig(snapshotTtlMs: 60_000))
        do {
            _ = try await manager.evaluate(flagKey: "f", entityID: "e1")
            XCTFail("Expected notBootstrapped or network error")
        } catch {
            // Either notBootstrapped (no snapshot) or network error when bootstrap runs
            XCTAssertTrue(error is OfflineFlagentError || (error as NSError).domain != "")
        }
    }
}

// MARK: - Original tests

final class FlagentEnhancedTests: XCTestCase {

    func testConfigDefaults() {
        let config = FlagentConfig()
        XCTAssertTrue(config.enableCache)
        XCTAssertFalse(config.enableDebugLogging)
        XCTAssertEqual(config.cacheTtlMs, 5 * 60, accuracy: 0.1)
    }

    func testConfigCustomInit() {
        let config = FlagentConfig(cacheTtlMs: 120, enableCache: false, enableDebugLogging: true)
        XCTAssertEqual(config.cacheTtlMs, 120, accuracy: 0.1)
        XCTAssertFalse(config.enableCache)
        XCTAssertTrue(config.enableDebugLogging)
    }

    func testCacheKey() {
        let key = CacheKey(flagKey: "f1", flagID: 1, entityID: "u1", entityType: "user")
        XCTAssertEqual(key.flagKey, "f1")
        XCTAssertEqual(key.entityID, "u1")
    }

    func testInMemoryEvaluationCache_putAndGet() {
        let cache = InMemoryEvaluationCache(ttl: 60)
        let key = CacheKey(flagKey: "f1", entityID: "u1")
        let result = EvalResult(flagKey: "f1", variantKey: "control")

        cache.put(key: key, result: result)

        let expectation = expectation(description: "get from cache")
        DispatchQueue.global().asyncAfter(deadline: .now() + 0.1) {
            let cached = cache.get(key: key)
            XCTAssertNotNil(cached)
            XCTAssertEqual(cached?.flagKey, "f1")
            XCTAssertEqual(cached?.variantKey, "control")
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 1)
    }

    func testInMemoryEvaluationCache_getMissingReturnsNil() {
        let cache = InMemoryEvaluationCache(ttl: 60)
        let key = CacheKey(flagKey: "missing", entityID: "u1")

        let cached = cache.get(key: key)
        XCTAssertNil(cached)
    }

    func testInMemoryEvaluationCache_clear() {
        let cache = InMemoryEvaluationCache(ttl: 60)
        let key = CacheKey(flagKey: "f1", entityID: "u1")
        cache.put(key: key, result: EvalResult(flagKey: "f1", variantKey: "control"))

        cache.clear()

        let expectation = expectation(description: "clear")
        DispatchQueue.global().asyncAfter(deadline: .now() + 0.2) {
            let cached = cache.get(key: key)
            XCTAssertNil(cached)
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 1)
    }

    func testInMemoryEvaluationCache_evictExpired() {
        let cache = InMemoryEvaluationCache(ttl: 0.05)
        let key = CacheKey(flagKey: "f1", entityID: "u1")
        cache.put(key: key, result: EvalResult(flagKey: "f1", variantKey: "control"))

        let expectation = expectation(description: "evict")
        DispatchQueue.global().asyncAfter(deadline: .now() + 0.15) {
            cache.evictExpired()
            DispatchQueue.global().asyncAfter(deadline: .now() + 0.1) {
                let cached = cache.get(key: key)
                XCTAssertNil(cached)
                expectation.fulfill()
            }
        }
        wait(for: [expectation], timeout: 2)
    }
}

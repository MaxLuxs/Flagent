import XCTest
import FlagentClient
@testable import FlagentEnhanced

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

import XCTest
@testable import FlagentEnhanced

final class FlagentEnhancedTests: XCTestCase {

    func testConfigDefaults() {
        let config = FlagentConfig()
        XCTAssertTrue(config.enableCache)
        XCTAssertFalse(config.enableDebugLogging)
        XCTAssertEqual(config.cacheTtlMs, 5 * 60, accuracy: 0.1)
    }
}

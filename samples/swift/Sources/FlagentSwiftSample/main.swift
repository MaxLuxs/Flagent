import Foundation
import FlagentClient
import FlagentEnhanced

#if os(iOS) || os(macOS) || os(watchOS) || os(tvOS)
import os.log
let logger = Logger(subsystem: "FlagentSwiftSample", category: "main")
#else
// Fallback logger for Linux
struct Logger {
    func info(_ message: String) { print("[INFO] \(message)") }
    func error(_ message: String) { print("[ERROR] \(message)") }
}
let logger = Logger()
#endif

func main() async {
    logger.info("Flagent Swift Sample Application")
    
    // Configuration
    let basePath = ProcessInfo.processInfo.environment["FLAGENT_BASE_URL"] ?? "http://localhost:18000/api/v1"
    let configuration = Configuration(basePath: basePath)
    
    let evaluationAPI = EvaluationAPI(configuration: configuration)
    
    print(String(repeating: "=", count: 60))
    print("Flagent Swift SDK Sample")
    print(String(repeating: "=", count: 60))
    print("Base URL: \(basePath)")
    print()
    
    // Example 1: Single flag evaluation
    do {
        print("Example 1: Single Flag Evaluation")
        print(String(repeating: "-", count: 60))
        
        let evalContext = EvalContext(
            flagKey: "my_feature_flag",
            entityID: "user123",
            entityType: "user",
            entityContext: [
                "region": "US",
                "tier": "premium"
            ],
            enableDebug: true
        )
        
        let result = try await evaluationAPI.postEvaluation(evalContext: evalContext)
        print("Flag Key: \(result.flagKey ?? "N/A")")
        print("Variant Key: \(result.variantKey ?? "N/A")")
        print("Flag ID: \(result.flagID ?? 0)")
        print("Variant ID: \(result.variantID ?? 0)")
        
        if let debugLog = result.evalDebugLog {
            print("\nDebug Log:")
            print("  Segment ID: \(debugLog.segmentID ?? "N/A")")
        }
        
        print()
    } catch {
        logger.error("Error evaluating flag: \(error.localizedDescription)")
        print("Error: \(error.localizedDescription)")
        print()
    }
    
    // Example 2: Batch evaluation
    do {
        print("Example 2: Batch Evaluation")
        print(String(repeating: "-", count: 60))
        
        let batchRequest = EvaluationBatchRequest(
            entities: [
                EvaluationEntity(
                    entityID: "user1",
                    entityType: "user",
                    entityContext: ["region": "US"]
                ),
                EvaluationEntity(
                    entityID: "user2",
                    entityType: "user",
                    entityContext: ["region": "EU"]
                )
            ],
            flagKeys: ["flag1", "flag2", "flag3"],
            enableDebug: false
        )
        
        let batchResponse = try await evaluationAPI.postEvaluationBatch(evaluationBatchRequest: batchRequest)
        print("Total Results: \(batchResponse.evaluationResults?.count ?? 0)")
        
        if let results = batchResponse.evaluationResults {
            for (index, result) in results.enumerated() {
                print("\nResult \(index + 1):")
                print("  Flag Key: \(result.flagKey ?? "N/A")")
                print("  Variant Key: \(result.variantKey ?? "N/A")")
                print("  Entity ID: \(result.evalContext?.entityID ?? "N/A")")
            }
        }
        
        print()
    } catch {
        logger.error("Error in batch evaluation: \(error.localizedDescription)")
        print("Error: \(error.localizedDescription)")
        print()
    }
    
    // Example 3: Evaluation with flag ID
    do {
        print("Example 3: Evaluation with Flag ID")
        print(String(repeating: "-", count: 60))
        
        let evalContext = EvalContext(
            flagID: 1, // Use flag ID instead of key
            entityID: "session456",
            entityType: "session",
            enableDebug: false
        )
        
        let result = try await evaluationAPI.postEvaluation(evalContext: evalContext)
        print("Flag ID: \(result.flagID ?? 0)")
        print("Flag Key: \(result.flagKey ?? "N/A")")
        print("Variant Key: \(result.variantKey ?? "N/A")")
        print()
    } catch {
        logger.error("Error evaluating with flag ID: \(error.localizedDescription)")
        print("Error: \(error.localizedDescription)")
        print()
    }
    
    print(String(repeating: "=", count: 60))
    print("Sample completed")
    print(String(repeating: "=", count: 60))
}

// Run the async main function
if #available(macOS 12.0, iOS 15.0, *) {
    Task {
        await main()
        exit(0)
    }
    RunLoop.main.run()
} else {
    // Fallback for older platforms
    let semaphore = DispatchSemaphore(value: 0)
    Task {
        await main()
        semaphore.signal()
    }
    semaphore.wait()
}

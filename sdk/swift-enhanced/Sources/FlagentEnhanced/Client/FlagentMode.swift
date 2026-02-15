import Foundation

/// Client mode: server-side evaluation (default) or offline (client-side, reserved for future).
public enum FlagentMode {
    /// Evaluate flags via server API (EvaluationAPI).
    case server
    /// Offline/client-side evaluation (reserved for future implementation).
    case offline
}

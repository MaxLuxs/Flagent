import SwiftUI
import FlagentEnhanced

@available(macOS 10.15, iOS 13.0, *)
public struct FlagentDebugUI {
    public static func show(manager: FlagentManager) {
        // TODO: Implement SwiftUI debug interface
    }
    
    @available(macOS 10.15, iOS 13.0, *)
    public struct DebugView: View {
        let manager: FlagentManager
        
        public var body: some View {
            Text("Flagent Debug UI")
            // TODO: Implement flags list, details, overrides, logs
        }
    }
}
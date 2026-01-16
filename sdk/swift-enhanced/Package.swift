// swift-tools-version:5.1

import PackageDescription

let package = Package(
    name: "FlagentEnhanced",
    platforms: [
        .iOS(.v11),
        .macOS(.v10_13),
    ],
    products: [
        .library(
            name: "FlagentEnhanced",
            targets: ["FlagentEnhanced"]
        ),
    ],
    dependencies: [
        // Base SDK dependency - in production, use: .package(url: "...", from: "1.1.19")
        .package(path: "../swift"),
    ],
    targets: [
        .target(
            name: "FlagentEnhanced",
            dependencies: ["FlagentClient"],
            path: "Sources/FlagentEnhanced"
        ),
        .testTarget(
            name: "FlagentEnhancedTests",
            dependencies: ["FlagentEnhanced"],
            path: "Tests/FlagentEnhancedTests"
        ),
    ]
)
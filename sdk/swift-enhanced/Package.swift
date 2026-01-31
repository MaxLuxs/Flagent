// swift-tools-version:5.1

import PackageDescription

let package = Package(
    name: "FlagentEnhanced",
    platforms: [
        .iOS(.v11),
        .macOS(.v10_15),
    ],
    products: [
        .library(
            name: "FlagentEnhanced",
            targets: ["FlagentEnhanced"]
        ),
    ],
    dependencies: [
        .package(path: "../swift"),
        .package(url: "https://github.com/Flight-School/AnyCodable", .upToNextMajor(from: "0.6.1")),
    ],
    targets: [
        .target(
            name: "FlagentEnhanced",
            dependencies: ["FlagentClient", "AnyCodable"],
            path: "Sources/FlagentEnhanced"
        ),
        .testTarget(
            name: "FlagentEnhancedTests",
            dependencies: ["FlagentEnhanced"],
            path: "Tests/FlagentEnhancedTests"
        ),
    ]
)
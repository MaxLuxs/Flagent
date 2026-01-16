// swift-tools-version:5.1

import PackageDescription

let package = Package(
    name: "FlagentDebugUI",
    platforms: [
        .iOS(.v13),
        .macOS(.v10_15),
    ],
    products: [
        .library(
            name: "FlagentDebugUI",
            targets: ["FlagentDebugUI"]
        ),
    ],
    dependencies: [
        .package(path: "../swift-enhanced"),
    ],
    targets: [
        .target(
            name: "FlagentDebugUI",
            dependencies: ["FlagentEnhanced"],
            path: "Sources/FlagentDebugUI"
        ),
    ]
)
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
        .package(url: "https://github.com/Flight-School/AnyCodable", .upToNextMajor(from: "0.6.1")),
        .package(path: "../swift"),
        .package(path: "../swift-enhanced"),
    ],
    targets: [
        .target(
            name: "FlagentDebugUI",
            dependencies: ["AnyCodable", "FlagentClient", "FlagentEnhanced"],
            path: "Sources/FlagentDebugUI"
        ),
    ]
)
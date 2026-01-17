// swift-tools-version:5.1
import PackageDescription

let package = Package(
    name: "FlagentSwiftSample",
    platforms: [
        .iOS(.v11),
        .macOS(.v10_13),
        .tvOS(.v11),
        .watchOS(.v4),
    ],
    products: [
        .executable(
            name: "FlagentSwiftSample",
            targets: ["FlagentSwiftSample"]
        ),
    ],
    dependencies: [
        .package(path: "../../sdk/swift"),
        .package(path: "../../sdk/swift-enhanced"),
    ],
    targets: [
        .executableTarget(
            name: "FlagentSwiftSample",
            dependencies: [
                .product(name: "FlagentClient", package: "swift"),
                .product(name: "FlagentEnhanced", package: "swift-enhanced"),
            ]
        ),
    ]
)

# Internal / Enterprise Module

This directory can hold the **flagent-enterprise** module (multi-tenancy, billing, SSO, smart rollout, anomaly detection).

## Open-source build (no enterprise)

- If `internal/flagent-enterprise` is absent or empty, the build uses only core. No submodule init needed.
- Clone: `git clone https://github.com/MaxLuxs/Flagent.git`

## Enterprise build (with enterprise)

- If `internal/flagent-enterprise/build.gradle.kts` exists, the project includes `:flagent-enterprise` and the backend depends on it.
- Enterprise tables and routes are registered when the enterprise JAR is on the classpath (ServiceLoader).
- To use the **private repo** as submodule:  
  `git submodule add git@github.com:MaxLuxs/Flagent-Enterprise.git internal/flagent-enterprise`  
  (or `https://github.com/MaxLuxs/Flagent-Enterprise.git` over HTTPS)  
  Then clone with: `git clone --recursive <url>` or run `git submodule update --init --recursive`.

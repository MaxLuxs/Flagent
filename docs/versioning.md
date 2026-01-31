# Project version (single source)

**Single source of truth:** root file `VERSION` (one line, e.g. `0.1.0`).

- **Gradle:** root `build.gradle.kts` reads `VERSION`; all subprojects inherit this version (do not set `version` in subproject `build.gradle.kts`).
- **Other build systems** (npm, pip, Go, Swift, Helm, Java/Maven, backend, frontend): run after changing `VERSION`:
  ```bash
  ./scripts/sync-version.sh
  ```
  This updates: `package.json`, `setup.py`, `helm/flagent/Chart.yaml`, OpenAPI files, `sdk/java/pom.xml`, `Configuration.java`, backend `InfoRoutes.kt`, frontend `Navbar.kt`, Go `client.go`, Swift `project.yml` and `podspec`.

**Fallback:** If `VERSION` file is missing, Gradle uses `0.1.0`. The sync script requires `VERSION` to exist and exits with an error if it is missing or empty.

**To release a new version:**
1. Edit `VERSION` (e.g. set to `0.2.0`).
2. Run `./scripts/sync-version.sh`.
3. Commit and tag (e.g. `git tag v0.2.0`).

Documentation and README examples are not updated by the script; update version strings there manually or extend the script.

# Project version (single source)

> [English](versioning.md) | [Русский](versioning.ru.md)

**Single source of truth:** root file `VERSION` (one line, e.g. `0.1.0`).

- **Gradle:** root `build.gradle.kts` reads `VERSION`; all subprojects inherit this version (do not set `version` in subproject `build.gradle.kts`).
- **Other build systems** (npm, pip, Go, Swift, Helm, Java/Maven, backend, frontend): run after changing `VERSION`:
  ```bash
  ./scripts/sync-version.sh
  ```
  This updates: `package.json`, `setup.py`, `helm/flagent/Chart.yaml`, OpenAPI files, `sdk/java/pom.xml`, `Configuration.java`, backend `InfoRoutes.kt`, frontend `Navbar.kt`, Go `client.go`, Swift `project.yml` and `podspec`.

**Fallback:** If `VERSION` file is missing, Gradle uses `0.1.0`. The sync script requires `VERSION` to exist and exits with an error if it is missing or empty.

**To release a new version:**
```bash
# One-time after clone
./scripts/install-hooks.sh

# On release
echo "0.1.6" > VERSION
./scripts/sync-version.sh   # required
git add -A
git commit -m "chore: release 0.1.6"
git tag v0.1.6
git push origin main --tags
```

**Pre-commit hook:** Run `./scripts/install-hooks.sh` to install a hook that blocks commits when `VERSION` or synced files are staged but out of sync. Install once per clone.

The script also updates: README.md, docs/guides (getting-started, deployment), SDK READMEs, frontend ShellLayout/Navbar. CHANGELOG entries and historical references are not touched.

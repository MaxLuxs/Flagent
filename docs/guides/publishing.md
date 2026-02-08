# Publishing Flagent Extensions

> [English](publishing.md) | [Русский](publishing.ru.md)

How to publish Gradle plugin and VS Code extension.

---

## Gradle Plugin (Plugin Portal)

### Prerequisites

1. Create account at [plugins.gradle.org](https://plugins.gradle.org/user/register)
2. Generate API key in Profile → API Keys
3. Add to `~/.gradle/gradle.properties`:
   ```properties
   gradle.publish.key=YOUR_KEY
   gradle.publish.secret=YOUR_SECRET
   ```
   Or use env vars `GRADLE_PUBLISH_KEY`, `GRADLE_PUBLISH_SECRET` for CI.

### Publish

```bash
cd gradle-plugins/flagent-gradle-plugin
../../gradlew publishPlugins
```

### Usage by users

```kotlin
plugins {
    id("com.flagent.verify-flags") version "0.1.0"
}
```

---

## VS Code Extension (Marketplace)

### Package .vsix

```bash
cd extensions/vscode-flagent
npm install
npm run compile
npx vsce package --no-dependencies
```

Output: `flagent-0.1.0.vsix`

### Publish to Marketplace

1. Install [vsce](https://github.com/microsoft/vscode-vsce): `npm i -g @vscode/vsce`
2. Create publisher at [marketplace.visualstudio.com](https://marketplace.visualstudio.com/manage)
3. Get Personal Access Token
4. Run:
   ```bash
   vsce publish -p YOUR_TOKEN
   ```

### Install from .vsix (local)

In VS Code: Extensions → ⋮ → Install from VSIX → select `flagent-0.1.0.vsix`

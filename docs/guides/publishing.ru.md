# Публикация расширений Flagent

> [English](publishing.md) | [Русский](publishing.ru.md)

Публикация Gradle-плагина и VS Code расширения.

---

## Gradle Plugin (Plugin Portal)

### Подготовка

1. Создайте аккаунт на [plugins.gradle.org](https://plugins.gradle.org/user/register)
2. Сгенерируйте API key в Profile → API Keys
3. Добавьте в `~/.gradle/gradle.properties`:
   ```properties
   gradle.publish.key=YOUR_KEY
   gradle.publish.secret=YOUR_SECRET
   ```
   Или используйте env vars `GRADLE_PUBLISH_KEY`, `GRADLE_PUBLISH_SECRET` для CI.

### Публикация

```bash
cd gradle-plugins/flagent-gradle-plugin
../../gradlew publishPlugins
```

### Использование

```kotlin
plugins {
    id("com.flagent.verify-flags") version "0.1.0"
}
```

---

## VS Code Extension (Marketplace)

### Сборка .vsix

```bash
cd extensions/vscode-flagent
npm install
npm run compile
npx vsce package --no-dependencies
```

Результат: `flagent-0.1.0.vsix`

### Публикация в Marketplace

1. Установите [vsce](https://github.com/microsoft/vscode-vsce): `npm i -g @vscode/vsce`
2. Создайте publisher на [marketplace.visualstudio.com](https://marketplace.visualstudio.com/manage)
3. Получите Personal Access Token
4. Запустите:
   ```bash
   vsce publish -p YOUR_TOKEN
   ```

### Установка из .vsix (локально)

В VS Code: Extensions → ⋮ → Install from VSIX → выберите `flagent-0.1.0.vsix`

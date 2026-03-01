# Версия проекта (единый источник)

> [English](versioning.md) | Русский

**Единый источник истины:** корневой файл `VERSION` (одна строка, например `0.1.0`).

- **Gradle:** корневой `build.gradle.kts` читает `VERSION`; все подпроекты наследуют эту версию (не задавайте `version` в `build.gradle.kts` подпроектов).
- **Остальные системы сборки** (npm, pip, Go, Swift, Helm, Java/Maven, backend, frontend): после изменения `VERSION` выполните:
  ```bash
  ./scripts/sync-version.sh
  ```
  Скрипт обновит: `package.json`, `setup.py`, `helm/flagent/Chart.yaml`, OpenAPI-файлы, `sdk/java/pom.xml`, `Configuration.java`, backend `InfoRoutes.kt`, frontend `Navbar.kt`, Go `client.go`, Swift `project.yml` и `podspec`.

**Fallback:** если файл `VERSION` отсутствует, Gradle использует `0.1.0`. Скрипт sync требует наличия `VERSION` и завершается с ошибкой при отсутствии или пустом файле.

**Выпуск новой версии:**
```bash
# Один раз после клонирования
./scripts/install-hooks.sh

# При релизе (подставьте свою версию вместо 0.1.6)
echo "0.1.6" > VERSION
./scripts/sync-version.sh   # обязательно
git add -A
git commit -m "chore: release 0.1.6"
git tag v0.1.6
git push origin main --tags
```

**Pre-commit hook:** выполните `./scripts/install-hooks.sh`, чтобы установить хук, блокирующий коммиты при рассинхронизации `VERSION` и синхронизированных файлов. Установка один раз на клон.

Скрипт также обновляет: README.md, docs/guides (getting-started, deployment), README SDK, frontend ShellLayout/Navbar. Записи CHANGELOG и исторические ссылки не меняются.

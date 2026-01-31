# Настройка GitHub для проекта Flagent

Этот документ описывает шаги для настройки GitHub Pages, CI/CD и других функций GitHub для проекта.

## 1. Включение GitHub Pages

1. Перейдите в **Settings** → **Pages** вашего репозитория
2. В разделе **Source** выберите:
   - **Source**: `GitHub Actions` (рекомендуется)
   - Или альтернативно: **Deploy from a branch** → выберите ветку `main` и папку `/docs`

**Важно**: Если используете **GitHub Actions** (рекомендуется), workflow `pages.yml` автоматически будет деплоить документацию из папки `docs/`. Выбор ветки/папки в настройках не требуется - всё управляется через workflow.

После первого запуска workflow `pages.yml`, документация будет доступна по адресу:
`https://maxluxs.github.io/Flagent/`

## 2. Настройка Codecov

1. Зайдите на [codecov.io](https://codecov.io)
2. Войдите через GitHub
3. Добавьте репозиторий `MaxLuxs/Flagent`
4. Добавьте токен в **Settings** → **Secrets and variables** → **Actions** → **New repository secret**:
   - **Name**: `CODECOV_TOKEN`
   - **Value**: `1da897aa-9578-4842-8424-00cff4bdbcce`

Токен уже настроен в workflow `ci.yml`, после добавления секрета покрытие кода будет автоматически загружаться в Codecov.

## 3. Настройка GitHub Container Registry (для Docker образов)

GitHub Container Registry (GHCR) автоматически работает с `GITHUB_TOKEN`, никаких дополнительных настроек не требуется.

После создания релиза, Docker образ будет автоматически опубликован:
- `ghcr.io/maxluxs/flagent:latest`
- `ghcr.io/maxluxs/flagent:<version>`

### Использование образа:

```bash
docker pull ghcr.io/maxluxs/flagent:latest
docker run -it -p 18000:18000 ghcr.io/maxluxs/flagent:latest
```

## 4. Создание релизов

Для автоматической публикации Docker образа:

1. Создайте новый релиз в GitHub:
   - **Settings** → **Releases** → **Create a new release**
   - Или используйте тег: `git tag -a v0.1.0 -m "Release version 0.1.0"` и `git push --tags`
2. Workflow `cd_docker.yml` автоматически соберет и опубликует Docker образ

## 5. Проверка CI/CD

После пуша изменений в `main`:
- **CI workflow** (`ci.yml`) автоматически запустится на каждый push и PR
- **CodeQL workflow** (`ci_codeql.yml`) проверит безопасность кода
- **Pages workflow** (`pages.yml`) обновит документацию

Проверить статус можно в разделе **Actions** репозитория.

## 6. Настройка бейджей в README

В `README.md` уже есть ссылки на бейджи. Они будут работать автоматически после первого успешного запуска CI:

- Build Status: `https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml/badge.svg?branch=main`
- Code Coverage: нужно заменить `YOUR_TOKEN` на реальный токен codecov (или убрать этот бейдж)
- Release: автоматически покажет последний релиз
- Version badges: обновляются вручную при изменении версий

## 7. Настройка защиты веток (рекомендуется)

1. Перейдите в **Settings** → **Branches**
2. Добавьте правило для ветки `main`:
   - ✅ Require a pull request before merging
   - ✅ Require status checks to pass before merging
     - Выберите: `unit_test`, `integration_test`, `actionlint`
   - ✅ Require branches to be up to date before merging

## 8. Настройка секретов (если требуется)

Если понадобятся дополнительные секреты:

1. Перейдите в **Settings** → **Secrets and variables** → **Actions**
2. Нажмите **New repository secret**
3. Добавьте необходимые секреты (например, API ключи для интеграций)

## Структура workflows

- **`.github/workflows/ci.yml`** - Основной CI: тесты, сборка, покрытие кода
- **`.github/workflows/cd_docker.yml`** - CD: публикация Docker образов при релизе
- **`.github/workflows/ci_codeql.yml`** - Анализ безопасности кода (CodeQL)
- **`.github/workflows/load-test.yml`** - Load testing (k6: metrics, anomaly detection)
- **`.github/workflows/security-scan.yml`** - Security scan (dependency check, TruffleHog, Trivy, SAST)
- **`.github/workflows/pages.yml`** - Деплой документации на GitHub Pages
- **`.github/workflows/stale.yml`** - Автоматическое помечание неактивных issues/PR как stale

## Проверка работоспособности

После настройки проверьте:

1. ✅ CI запускается на push в `main`
2. ✅ GitHub Pages доступна по адресу `https://maxluxs.github.io/Flagent/`
3. ✅ CodeQL анализ запускается
4. ✅ При создании релиза публикуется Docker образ

## Troubleshooting

### GitHub Pages не обновляется
- Убедитесь, что в настройках Pages выбран источник `GitHub Actions`
- Проверьте логи workflow `pages.yml` в разделе Actions

### Docker образ не публикуется
- Убедитесь, что создан релиз (не просто тег)
- Проверьте права доступа к GitHub Container Registry

### CI не запускается
- Проверьте синтаксис YAML файлов workflows
- Убедитесь, что файлы находятся в `.github/workflows/` в корне репозитория

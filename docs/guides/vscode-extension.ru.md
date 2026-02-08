# Flagent VS Code Extension

> [English](vscode-extension.md) | [Русский](vscode-extension.ru.md)

Просмотр feature flags в боковой панели VS Code / Cursor. Использует сгенерированный из OpenAPI клиент `@flagent/client`.

## Установка

1. Собрать: `cd extensions/vscode-flagent && npm install && npm run compile`
2. В VS Code: Run Extension (F5) или упаковать `.vsix`

## Настройка

Settings → Flagent:

| Параметр | Описание |
|----------|----------|
| `flagent.baseUrl` | URL API (по умолчанию `http://localhost:18000`) |
| `flagent.apiKey` | API ключ (X-API-Key) |

## Использование

1. Откройте панель **Explorer** → **Feature Flags**
2. Нажмите Refresh для загрузки флагов
3. ПКМ по флагу → **Copy Key** или **Insert Key**

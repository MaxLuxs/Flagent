# Flagent VS Code Extension

> [English](vscode-extension.md) | [Русский](vscode-extension.ru.md)

View feature flags in VS Code / Cursor Explorer sidebar. Uses OpenAPI-generated client `@flagent/client`.

## Installation

1. Build: `cd extensions/vscode-flagent && npm install && npm run compile`
2. In VS Code: Run Extension (F5) or package as `.vsix`

## Configuration

Settings → Flagent:

| Setting | Description |
|---------|-------------|
| `flagent.baseUrl` | API URL (default `http://localhost:18000`) |
| `flagent.apiKey` | API key (X-API-Key) |

## Usage

1. Open **Explorer** → **Feature Flags**
2. Click Refresh to load flags
3. Right-click flag → **Copy Key** or **Insert Key**

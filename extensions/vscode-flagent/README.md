# Flagent VS Code Extension

View Flagent feature flags in VS Code Explorer sidebar.

## Setup

1. Install the extension
2. Settings → search "flagent"
3. Set **Flagent: Base Url** (default: `http://localhost:18000`)
4. Set **Flagent: Api Key** (optional, for authenticated API)

## Usage

- **Feature Flags** view appears in Explorer sidebar
- Click refresh icon to fetch flags
- Right-click flag → **Copy Key** or **Insert Key** (inserts into active editor)

## Configuration

| Setting | Description |
|---------|-------------|
| `flagent.baseUrl` | Flagent API base URL |
| `flagent.apiKey` | API key (X-API-Key header) |

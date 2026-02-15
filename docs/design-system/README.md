# Flagent Design System

Single source of truth for UI tokens (colors, spacing, radius, typography, shadow) used across the frontend and all SDK debug UIs.

## Structure

- **Source**: `design-system/tokens/tokens.json` — W3C-style design tokens (semantic colors, light/dark, spacing, radius, typography, shadow).
- **Codegen**: `design-system/build-tokens.js` reads the JSON and generates:
  - **CSS**: `design-system/build/css/flagent-tokens.css` — CSS custom properties for web (light/dark via `[data-theme]`).
  - **TypeScript**: `design-system/build/ts/FlagentTokens.ts` — object for JS/React (e.g. JS debug UI).
  - **Kotlin**: `design-system/build/kotlin/com/flagent/design/tokens/FlagentDesignTokens.kt` — Compose `Color` and constants for Kotlin/Android.
  - **Swift**: `design-system/build/swift/FlagentTokens.swift` — SwiftUI `Color` and constants for iOS/macOS.

Generated files are **not** committed; they are produced at build time.

## How to run codegen

From repo root:

```bash
# Generate all artifacts (CSS, TS, Kotlin, Swift)
./gradlew :flagent-design-tokens:generateDesignTokens
```

Or from `design-system/`:

```bash
node build-tokens.js
```

The Gradle task also copies artifacts to consumers:

- `flagent-tokens.css` → `frontend/src/jsMain/resources/`
- `FlagentTokens.ts` → `sdk/javascript-debug-ui/src/`
- `FlagentTokens.swift` → `sdk/swift-debug-ui/Sources/FlagentDebugUI/`

Kotlin tokens are consumed via the `:flagent-design-tokens` Gradle module (source set points to `design-system/build/kotlin`).

## Consumers

| Consumer | How tokens are used |
|----------|----------------------|
| **Frontend** (Compose for Web) | `flagent-tokens.css` linked in `index.html`; `Theme.kt` uses `var(--flagent-*)` for colors. Frontend build depends on `copyCssToFrontend`. |
| **Kotlin Debug UI** | Depends on `:flagent-design-tokens`; `FlagentDebugTheme` and screens use `FlagentDesignTokens` for `ColorScheme` and spacing/radius. |
| **Android sample** | Depends on `:flagent-design-tokens`; `Theme.kt` builds Material `lightColorScheme`/`darkColorScheme` from `FlagentDesignTokens`. |
| **JS Debug UI** | Uses copied `FlagentTokens.ts`; `FlagentDebugPanel.tsx` uses `FlagentTokens.color`, `FlagentTokens.spacing`, etc. |
| **Swift Debug UI** | Uses copied `FlagentTokens.swift`; `DebugView` uses `FlagentTokens.Colors.Dark.*`, `FlagentTokens.Spacing`, `FlagentTokens.Radius`. |

## Adding or changing tokens

1. Edit `design-system/tokens/tokens.json` (add/change keys under `color`, `spacing`, `radius`, `typography`, `shadow`, `gradient`).
2. Run codegen: `./gradlew :flagent-design-tokens:generateDesignTokens` (or `node design-system/build-tokens.js`).
3. If you added a token consumed by the frontend: add a corresponding property or helper in `frontend/.../theme/Theme.kt` (e.g. `val Overlay = Color(v("color-overlay"))` or `val ShadowCard = v("shadow-card")`).
4. Rebuild consumers; for Kotlin/Android, a normal Gradle build will regenerate and recompile. For JS/Swift, copy tasks run as part of `:flagent-design-tokens:compileKotlin`.

**Rule**: Do not hardcode Flagent palette (e.g. `#0EA5E9`, `#14B8A6`) in UI code. Use design tokens (or theme objects built from tokens) only. Run the hardcode check before PR (see below).

## Token catalog (summary)

| Category | Keys | Description |
|----------|------|-------------|
| **color** | `primary`, `primaryDark`, `primaryLight`, `secondary`, … | Semantic brand/status colors. |
| **color** | `light.*`, `dark.*` | Theme-specific: `background`, `text`, `textLight`, `textMuted`, `border`, `cardBg`, `cardBorder`, `inputBg`, `inputBorder`, `errorBg`, `successBg`, `infoBg`, … |
| **color** | `overlay`, `primaryGlow` | Modal backdrop, primary glow. |
| **color** | `codeBackground`, `codeText` | Code blocks. |
| **spacing** | `4`–`25` | Values like `4px`, `16px`. |
| **radius** | `sm`, `md`, `lg`, `card` | Border radius (px). |
| **typography** | `fontFamily`, `fontFamilyMono`, `fontSize12`–`fontSize24`, `fontWeight400`, `fontWeight700` | Font stack and sizes. |
| **shadow** | `default`, `hover` | Shadow color (rgba). |
| **shadow** | `card`, `modal` | Full box-shadow presets (e.g. `0 2px 8px var(--flagent-shadow-default)`). |
| **gradient** | `primary`, `secondary`, `hero` | CSS linear-gradient strings. |

## Using tokens in the frontend (Theme.kt and cssVar)

- **Colors in Compose**: use `FlagentTheme.Primary`, `FlagentTheme.cardBg(themeMode)`, `FlagentTheme.Overlay`, etc. All map to `var(--flagent-*)` under the hood.
- **Box-shadow**: use presets `FlagentTheme.ShadowCard` or `FlagentTheme.ShadowModal` in `property("box-shadow", FlagentTheme.ShadowCard)`.
- **Borders in `property()`**: Compose `border(1.px, LineStyle.Solid, FlagentTheme.Border)` works directly. When you need a string (e.g. inline style), use `FlagentTheme.cssVar("color-dark-cardBorder")` and build the string: `property("border", "1px solid ${FlagentTheme.cssVar("color-dark-cardBorder")}")`.
- **Gradients**: `property("background", FlagentTheme.GradientHero)` or use `FlagentTheme.GradientPrimary` / `GradientSecondary`.

## Token schema (codegen mapping)

- **CSS**: all tokens become `--flagent-<key>` (dots → dashes). `color.dark.*` are under `[data-theme="dark"]`.
- **Kotlin**: semantic `color.*` (excluding `light`/`dark`) → `FlagentDesignTokens.Primary`, etc.; `color.light.*` / `color.dark.*` → `Light` / `Dark` objects; `shadow.*` → `Shadow`, `ShadowHover`, `ShadowCard`, `ShadowModal`; `gradient.*` → `GradientPrimary`, etc.
- **Swift**: same structure; semantic colors keep camelCase (`primaryDark`); `Dark.*` use snake_case in generated code.
- **TypeScript**: nested object mirroring JSON; used as-is in JS debug UI.

## Checking for hardcoded colors

Run the design-system hardcode check before opening a PR so that UI stays on tokens:

```bash
./design-system/check-hardcode.sh
```

See script for scanned paths and allowed exceptions. CI runs this step automatically.

/**
 * Flagent Design Tokens — generated from tokens.json.
 * Do not edit by hand.
 */
export const FlagentTokens = {
  "color": {
    "primary": "#0EA5E9",
    "primaryDark": "#0284C7",
    "primaryLight": "#38BDF8",
    "secondary": "#14B8A6",
    "secondaryDark": "#0D9488",
    "secondaryLight": "#5EEAD4",
    "accent": "#F59E0B",
    "success": "#10B981",
    "error": "#EF4444",
    "warning": "#F59E0B",
    "info": "#3B82F6",
    "neutral": "#6B7280",
    "neutralLight": "#9CA3AF",
    "neutralLighter": "#D1D5DB",
    "light": {
      "background": "#FFFFFF",
      "backgroundAlt": "#F8FAFC",
      "backgroundDark": "#F1F5F9",
      "text": "#0F172A",
      "textLight": "#64748B",
      "textLighter": "#94A3B8",
      "border": "#E2E8F0",
      "inputBg": "#FFFFFF",
      "inputBorder": "#E2E8F0",
      "cardBg": "#FFFFFF",
      "cardBorder": "#E2E8F0",
      "errorBg": "#FEF2F2",
      "errorText": "#991B1B",
      "badgeBg": "#E5E7EB",
      "successBg": "#D1FAE5",
      "warningBg": "#FEF3C7",
      "infoBg": "#F0F9FF"
    },
    "dark": {
      "background": "#0f172a",
      "sidebarBg": "#1e293b",
      "contentBg": "#0f172a",
      "text": "#FFFFFF",
      "textLight": "rgba(255,255,255,0.7)",
      "border": "rgba(255,255,255,0.08)",
      "inputBg": "rgba(255,255,255,0.06)",
      "inputBorder": "rgba(255,255,255,0.12)",
      "cardBg": "rgba(255,255,255,0.04)",
      "cardBorder": "rgba(255,255,255,0.08)",
      "errorBg": "rgba(239,68,68,0.2)",
      "errorText": "#FCA5A5",
      "badgeBg": "rgba(255,255,255,0.12)",
      "successBg": "rgba(16,185,129,0.2)",
      "warningBg": "rgba(245,158,11,0.2)",
      "infoBg": "rgba(59,130,246,0.2)",
      "textMuted": "rgba(255,255,255,0.5)"
    },
    "codeBackground": "#1E293B",
    "codeText": "#CBD5E1",
    "overlay": "rgba(0,0,0,0.5)",
    "primaryGlow": "rgba(14,165,233,0.2)"
  },
  "spacing": {
    "4": "4px",
    "6": "6px",
    "8": "8px",
    "10": "10px",
    "12": "12px",
    "16": "16px",
    "20": "20px",
    "24": "24px",
    "25": "25px"
  },
  "radius": {
    "sm": "3px",
    "md": "6px",
    "lg": "8px",
    "card": "10px"
  },
  "typography": {
    "fontFamily": "'DM Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif",
    "fontFamilyMono": "monospace",
    "fontSize12": "12px",
    "fontSize14": "14px",
    "fontSize16": "16px",
    "fontSize24": "24px",
    "fontWeight400": "400",
    "fontWeight700": "700"
  },
  "shadow": {
    "default": "rgba(0, 0, 0, 0.08)",
    "hover": "rgba(0, 0, 0, 0.15)",
    "card": "0 2px 8px var(--flagent-shadow-default)",
    "modal": "0 10px 25px var(--flagent-shadow-hover)"
  },
  "gradient": {
    "primary": "linear-gradient(135deg, #0EA5E9 0%, #0284C7 100%)",
    "secondary": "linear-gradient(135deg, #14B8A6 0%, #0D9488 100%)",
    "hero": "linear-gradient(135deg, #0f172a 0%, #1e293b 20%, #0f172a 40%, #1e3a5f 60%, #0f172a 80%, #1e293b 100%)"
  }
} as const;
export type FlagentTokensType = typeof FlagentTokens;

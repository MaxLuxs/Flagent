#!/usr/bin/env node
/**
 * Flagent Design System — token codegen.
 * Reads design-system/tokens/tokens.json, outputs CSS, TypeScript, Kotlin, Swift.
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname);
const TOKENS_PATH = path.join(ROOT, 'tokens', 'tokens.json');
const BUILD_DIR = path.join(ROOT, 'build');

function loadTokens() {
  const raw = fs.readFileSync(TOKENS_PATH, 'utf8');
  return JSON.parse(raw);
}

function* walk(obj, prefix = '') {
  for (const [key, v] of Object.entries(obj)) {
    const name = prefix ? `${prefix}.${key}` : key;
    if (v && typeof v === 'object' && 'value' in v) {
      yield [name, v.value];
    } else if (v && typeof v === 'object' && !Array.isArray(v)) {
      yield* walk(v, name);
    }
  }
}

function hexToKotlin(hex) {
  if (hex.startsWith('#')) {
    const h = hex.slice(1);
    const a = h.length === 8 ? h.slice(0, 2) : 'FF';
    const r = h.length === 8 ? h.slice(2, 4) : h.slice(0, 2);
    const g = h.length === 8 ? h.slice(4, 6) : h.slice(2, 4);
    const b = h.length === 8 ? h.slice(6, 8) : h.slice(4, 6);
    return `0x${a}${r}${g}${b}`.toUpperCase();
  }
  const m = hex.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
  if (m) {
    const r = parseInt(m[1], 10);
    const g = parseInt(m[2], 10);
    const b = parseInt(m[3], 10);
    const a = m[4] != null ? Math.round(parseFloat(m[4]) * 255) : 255;
    const argb = (a << 24) | (r << 16) | (g << 8) | b;
    const hexStr = (argb >>> 0).toString(16).padStart(8, '0').toUpperCase();
    const suffix = a < 255 ? 'L' : '';
    return `0x${hexStr}${suffix}`;
  }
  return '0xFF000000';
}

function hexToSwift(hex) {
  if (hex.startsWith('#')) {
    const h = hex.slice(1);
    const r = parseInt(h.length === 6 ? h.slice(0, 2) : h.slice(2, 4), 16) / 255;
    const g = parseInt(h.length === 6 ? h.slice(2, 4) : h.slice(4, 6), 16) / 255;
    const b = parseInt(h.length === 6 ? h.slice(4, 6) : h.slice(6, 8), 16) / 255;
    const a = h.length === 8 ? parseInt(h.slice(0, 2), 16) / 255 : 1;
    return `Color(red: ${r}, green: ${g}, blue: ${b}, opacity: ${a})`;
  }
  const m = hex.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
  if (m) {
    const r = parseInt(m[1], 10) / 255;
    const g = parseInt(m[2], 10) / 255;
    const b = parseInt(m[3], 10) / 255;
    const a = m[4] != null ? parseFloat(m[4]) : 1;
    return `Color(red: ${r}, green: ${g}, blue: ${b}, opacity: ${a})`;
  }
  return 'Color.black';
}

function cssVarName(tokenKey) {
  return '--flagent-' + tokenKey.replace(/\./g, '-');
}

function emitCss(tokens) {
  const lines = ['/* Flagent Design Tokens — generated from tokens.json */', ''];
  const byTheme = { root: [], dark: [] };
  for (const [name, value] of walk(tokens)) {
    const varName = cssVarName(name);
    if (name.startsWith('color.dark.')) {
      byTheme.dark.push(`  ${varName}: ${value};`);
    } else {
      byTheme.root.push(`  ${varName}: ${value};`);
    }
  }
  lines.push(':root, [data-theme="light"] {');
  lines.push(byTheme.root.join('\n'));
  lines.push('}');
  lines.push('');
  lines.push('[data-theme="dark"] {');
  lines.push(byTheme.dark.join('\n'));
  lines.push('}');
  return lines.join('\n');
}

function setNested(obj, path, value) {
  const parts = path.split('.');
  let cur = obj;
  for (let i = 0; i < parts.length - 1; i++) {
    const p = parts[i];
    if (!(p in cur)) cur[p] = {};
    cur = cur[p];
  }
  cur[parts[parts.length - 1]] = value;
}

function emitTypeScript(tokens) {
  const flat = {};
  for (const [name, value] of walk(tokens)) {
    setNested(flat, name, value);
  }
  const content = `/**
 * Flagent Design Tokens — generated from tokens.json.
 * Do not edit by hand.
 */
export const FlagentTokens = ${JSON.stringify(flat, null, 2)} as const;
export type FlagentTokensType = typeof FlagentTokens;
`;
  return content;
}

function toKotlinName(segment) {
  return segment.charAt(0).toUpperCase() + segment.slice(1);
}

function emitKotlin(tokens) {
  const lines = [
    'package com.flagent.design.tokens',
    '',
    'import androidx.compose.ui.graphics.Color',
    '',
    '/**',
    ' * Flagent Design Tokens — generated from tokens.json. Do not edit by hand.',
    ' */',
    'object FlagentDesignTokens {',
    '',
    '    // Semantic (theme-independent) — from tokens.json',
  ];
  const semanticColorPrefix = 'color.';
  const skipColorNested = ['light', 'dark'];
  for (const [name, value] of walk(tokens)) {
    const parts = name.split('.');
    if (name.startsWith(semanticColorPrefix) && parts.length === 2 && !skipColorNested.includes(parts[1])) {
      const kotlinName = toKotlinName(parts[1]);
      if (typeof value === 'string' && (value.startsWith('#') || value.startsWith('rgba'))) {
        lines.push(`    val ${kotlinName} = Color(${hexToKotlin(value)})`);
      } else {
        lines.push(`    val ${kotlinName} = "${(value + '').replace(/"/g, '\\"')}"`);
      }
      continue;
    }
    if (name.startsWith('shadow.')) {
      const key = parts[1];
      const shadowName = key === 'default' ? 'Shadow' : key === 'hover' ? 'ShadowHover' : 'Shadow' + toKotlinName(key);
      lines.push(`    val ${shadowName} = "${(value + '').replace(/"/g, '\\"')}"`);
      continue;
    }
    if (name.startsWith('gradient.')) {
      const kotlinName = 'Gradient' + toKotlinName(parts[1]);
      lines.push(`    val ${kotlinName} = "${(value + '').replace(/"/g, '\\"')}"`);
      continue;
    }
  }
  lines.push('');
  lines.push('    object Light {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('color.light.')) continue;
    const prop = name.replace('color.light.', '').replace(/([A-Z])/g, '_$1').toUpperCase().replace(/^_/, '');
    const kotlinName = name.split('.').pop().replace(/([a-z])([A-Z])/g, '$1$2');
    if (typeof value === 'string' && (value.startsWith('#') || value.startsWith('rgba'))) {
      lines.push(`        val ${kotlinName} = Color(${hexToKotlin(value)})`);
    } else {
      lines.push(`        val ${kotlinName} = "${value}"`);
    }
  }
  lines.push('    }');
  lines.push('');
  lines.push('    object Dark {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('color.dark.')) continue;
    const kotlinName = name.replace('color.dark.', '').replace(/([a-z])([A-Z])/g, '$1$2');
    if (typeof value === 'string' && (value.startsWith('#') || value.startsWith('rgba'))) {
      lines.push(`        val ${kotlinName} = Color(${hexToKotlin(value)})`);
    } else {
      lines.push(`        val ${kotlinName} = "${value}"`);
    }
  }
  lines.push('    }');
  lines.push('');
  lines.push('    object Spacing {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('spacing.')) continue;
    const k = name.replace('spacing.', '');
    const num = parseInt(value, 10);
    lines.push(`        val size${k} = ${num}`);
  }
  lines.push('    }');
  lines.push('');
  lines.push('    object Radius {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('radius.')) continue;
    const k = name.replace('radius.', '');
    const num = parseInt(value, 10);
    const caseName = k === 'sm' ? 'Sm' : k === 'md' ? 'Md' : k === 'lg' ? 'Lg' : 'Card';
    lines.push(`        val ${caseName} = ${num}`);
  }
  lines.push('    }');
  lines.push('');
  lines.push('    object Typography {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('typography.')) continue;
    const k = name.replace('typography.', '');
    const camel = k.charAt(0).toLowerCase() + k.slice(1);
    lines.push(`        val ${camel} = "${value.replace(/"/g, '\\"')}"`);
  }
  lines.push('    }');
  lines.push('}');
  return lines.join('\n');
}

function toSwiftName(segment) {
  return segment.replace(/([a-z])([A-Z])/g, '$1_$2').toLowerCase();
}

function emitSwift(tokens) {
  const lines = [
    '// Flagent Design Tokens — generated from tokens.json. Do not edit by hand.',
    '',
    'import SwiftUI',
    '',
    'public enum FlagentTokens {',
    '    public enum Colors {',
  ];
  const skipColorNested = ['light', 'dark'];
  for (const [name, value] of walk(tokens)) {
    const parts = name.split('.');
    if (name.startsWith('color.') && parts.length === 2 && !skipColorNested.includes(parts[1])) {
      const swiftName = parts[1]; // keep camelCase for semantic colors (primaryDark, codeBackground)
      if (typeof value === 'string' && (value.startsWith('#') || value.startsWith('rgba'))) {
        lines.push(`        public static let ${swiftName} = ${hexToSwift(value)}`);
      } else {
        lines.push(`        public static let ${swiftName} = "${(value + '').replace(/"/g, '\\"')}"`);
      }
    }
  }
  const darkEntries = [];
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('color.dark.')) continue;
    const prop = name.replace('color.dark.', '');
    const swiftName = prop.replace(/([a-z])([A-Z])/g, '$1_$2').toLowerCase();
    darkEntries.push([swiftName, value]);
  }
  lines.push('        public enum Dark {');
  for (const [swiftName, value] of darkEntries) {
    const swiftVal = typeof value === 'string' && (value.startsWith('#') || value.startsWith('rgba')) ? hexToSwift(value) : `"${(value + '').replace(/"/g, '\\"')}"`;
    lines.push(`            public static let ${swiftName} = ${swiftVal}`);
  }
  lines.push('        }');
  lines.push('    }');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('shadow.')) continue;
    const key = name.replace('shadow.', '');
    const swiftName = key === 'default' ? 'shadow' : key === 'hover' ? 'shadowHover' : 'shadow' + key.charAt(0).toUpperCase() + key.slice(1);
    lines.push(`    public static let ${swiftName} = "${(value + '').replace(/"/g, '\\"')}"`);
  }
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('gradient.')) continue;
    const key = name.replace('gradient.', '');
    const swiftName = 'gradient' + key.charAt(0).toUpperCase() + key.slice(1);
    lines.push(`    public static let ${swiftName} = "${(value + '').replace(/"/g, '\\"')}"`);
  }
  lines.push('    public enum Spacing {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('spacing.')) continue;
    const k = name.replace('spacing.', '');
    const num = parseInt(value, 10);
    lines.push(`        public static let _${k}: CGFloat = ${num}`);
  }
  lines.push('    }');
  lines.push('    public enum Radius {');
  for (const [name, value] of walk(tokens)) {
    if (!name.startsWith('radius.')) continue;
    const k = name.replace('radius.', '');
    const num = parseInt(value, 10);
    const caseName = k === 'sm' ? 'sm' : k === 'md' ? 'md' : k === 'lg' ? 'lg' : 'card';
    lines.push(`        public static let ${caseName}: CGFloat = ${num}`);
  }
  lines.push('    }');
  lines.push('}');
  return lines.join('\n');
}

function main() {
  const tokens = loadTokens();
  if (!fs.existsSync(BUILD_DIR)) fs.mkdirSync(BUILD_DIR, { recursive: true });

  const cssDir = path.join(BUILD_DIR, 'css');
  const tsDir = path.join(BUILD_DIR, 'ts');
  const kotlinDir = path.join(BUILD_DIR, 'kotlin', 'com', 'flagent', 'design', 'tokens');
  const swiftDir = path.join(BUILD_DIR, 'swift');
  [cssDir, tsDir, kotlinDir, swiftDir].forEach(d => {
    if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  });

  fs.writeFileSync(path.join(cssDir, 'flagent-tokens.css'), emitCss(tokens), 'utf8');
  fs.writeFileSync(path.join(tsDir, 'FlagentTokens.ts'), emitTypeScript(tokens), 'utf8');
  fs.writeFileSync(path.join(kotlinDir, 'FlagentDesignTokens.kt'), emitKotlin(tokens), 'utf8');
  fs.writeFileSync(path.join(swiftDir, 'FlagentTokens.swift'), emitSwift(tokens), 'utf8');

  console.log('Flagent design tokens generated: css/, ts/, kotlin/, swift/');
}

main();

import React, { useCallback, useContext, useEffect, useState } from 'react';
import { FlagentContext } from './FlagentContext';
import { FlagentTokens } from './FlagentTokens';

/** Display model for a flag in the flags list */
export interface FlagRow {
  key: string;
  id: number;
  enabled: boolean;
  variantKeys: string[];
}

export interface FlagentDebugPanelProps {
  /** Flag keys to display when flagsProvider is not set. If both empty, panel shows "Add flagKeys or flagsProvider to inspect" */
  flagKeys?: string[];
  /** Optional. When provided, loads and shows all flags (e.g. from FlagApi.findFlags() or ExportApi.getExportEvalCacheJSON()) */
  flagsProvider?: () => Promise<FlagRow[]>;
  /** Position: 'bottom-right' | 'bottom-left' */
  position?: 'bottom-right' | 'bottom-left';
}

interface FlagState {
  key: string;
  enabled: boolean;
  loading: boolean;
  variantKey?: string | null;
}

const LAST_EVALS_MAX = 10;
const c = FlagentTokens.color;
const s = FlagentTokens.spacing;
const radiusPx = (key: keyof typeof FlagentTokens.radius) =>
  parseInt(FlagentTokens.radius[key], 10);
const spacingPx = (key: keyof typeof FlagentTokens.spacing) =>
  parseInt(FlagentTokens.spacing[key], 10);

export const FlagentDebugPanel: React.FC<FlagentDebugPanelProps> = ({
  flagKeys = [],
  flagsProvider,
  position = 'bottom-right',
}) => {
  const manager = useContext(FlagentContext);
  const [expanded, setExpanded] = useState(false);
  const [flags, setFlags] = useState<FlagState[]>([]);
  const [allFlags, setAllFlags] = useState<FlagRow[]>([]);
  const [flagsLoading, setFlagsLoading] = useState(false);
  const [overrides, setOverrides] = useState<Record<string, string>>({});
  const [evalFlagKey, setEvalFlagKey] = useState('');
  const [entityId, setEntityId] = useState('');
  const [entityType, setEntityType] = useState('');
  const [enableDebug, setEnableDebug] = useState(false);
  const [evalResult, setEvalResult] = useState<{ variantKey?: string | null; evalDebugLog?: { msg?: string } } | null>(null);
  const [evalError, setEvalError] = useState<string | null>(null);
  const [lastEvals, setLastEvals] = useState<Array<{ flagKey?: string; variantKey?: string | null }>>([]);
  const [cacheMessage, setCacheMessage] = useState<string | null>(null);

  const runEvaluate = useCallback(async () => {
    if (!manager) return;
    const key = evalFlagKey.trim() || undefined;
    const overrideVariant = key ? overrides[key] : undefined;
    setEvalError(null);
    setEvalResult(null);
    try {
      if (overrideVariant !== undefined) {
        const synthetic = { variantKey: overrideVariant };
        setEvalResult(synthetic);
        setLastEvals((prev) => [synthetic, ...prev.slice(0, LAST_EVALS_MAX - 1)]);
        return;
      }
      const r = await manager.evaluate({
        flagKey: key,
        entityID: entityId || undefined,
        entityType: entityType || undefined,
        enableDebug,
      });
      setEvalResult(r);
      setLastEvals((prev) => [{ flagKey: r.flagKey ?? key, variantKey: r.variantKey }, ...prev.slice(0, LAST_EVALS_MAX - 1)]);
    } catch (e: unknown) {
      setEvalError(e instanceof Error ? e.message : 'Evaluation failed');
    }
  }, [manager, evalFlagKey, entityId, entityType, enableDebug, overrides]);

  const clearCache = useCallback(async () => {
    if (!manager) return;
    await manager.clearCache();
    setCacheMessage('Cache cleared');
    setTimeout(() => setCacheMessage(null), 2000);
  }, [manager]);

  const evictExpired = useCallback(async () => {
    if (!manager) return;
    await manager.evictExpired();
    setCacheMessage('Evict expired done');
    setTimeout(() => setCacheMessage(null), 2000);
  }, [manager]);

  useEffect(() => {
    if (!manager || flagKeys.length === 0) {
      if (!flagsProvider) return;
    }
    if (flagsProvider && expanded) {
      let cancelled = false;
      setFlagsLoading(true);
      flagsProvider()
        .then((list) => {
          if (!cancelled) setAllFlags(list);
        })
        .finally(() => {
          if (!cancelled) setFlagsLoading(false);
        });
      return () => {
        cancelled = true;
      };
    }
  }, [manager, flagsProvider, expanded]);

  useEffect(() => {
    if (!manager || (flagKeys.length === 0 && !flagsProvider)) return;
    if (flagKeys.length === 0) return;
    let cancelled = false;
    const load = async () => {
      const results = await Promise.all(
        flagKeys.map(async (key) => {
          try {
            const overrideVariant = overrides[key];
            if (overrideVariant !== undefined) {
              return { key, enabled: overrideVariant !== 'disabled', loading: false, variantKey: overrideVariant };
            }
            const r = await manager!.evaluate({ flagKey: key });
            if (cancelled) return { key, enabled: false, loading: false };
            return {
              key,
              enabled: (r.variantKey ?? 'disabled') !== 'disabled',
              loading: false,
              variantKey: r.variantKey,
            };
          } catch {
            return { key, enabled: false, loading: false };
          }
        })
      );
      if (!cancelled) setFlags(results);
    };
    setFlags(flagKeys.map((k) => ({ key: k, enabled: false, loading: true })));
    load();
    return () => {
      cancelled = true;
    };
  }, [manager, flagKeys.join(','), overrides]);

  if (!manager) return null;

  const pos =
    position === 'bottom-right'
      ? { right: spacingPx('16'), bottom: spacingPx('16') }
      : { left: spacingPx('16'), bottom: spacingPx('16') };

  const hasFlagsProvider = typeof flagsProvider === 'function';
  const showFlagsList = hasFlagsProvider ? allFlags.length > 0 || flagsLoading : flagKeys.length > 0;

  return (
    <div
      style={{
        position: 'fixed',
        ...pos,
        zIndex: 99999,
        fontFamily: FlagentTokens.typography.fontFamilyMono,
        fontSize: parseInt(FlagentTokens.typography.fontSize12, 10),
        background: c.dark.background,
        color: c.dark.textLight,
        borderRadius: radiusPx('lg'),
        boxShadow: `0 4px 12px ${FlagentTokens.shadow.hover}`,
        overflow: 'hidden',
      }}
    >
      <button
        onClick={() => setExpanded(!expanded)}
        style={{
          width: '100%',
          padding: `${s['8']} ${s['12']}`,
          border: 'none',
          background: c.dark.sidebarBg,
          color: c.dark.text,
          cursor: 'pointer',
          textAlign: 'left',
          display: 'flex',
          justifyContent: 'space-between',
        }}
      >
        <span>🚩 Flagent</span>
        <span>{expanded ? '−' : '+'}</span>
      </button>
      {expanded && (
        <div
          style={{
            padding: spacingPx('12'),
            maxHeight: 480,
            overflowY: 'auto',
          }}
        >
          {!showFlagsList && !hasFlagsProvider ? (
            <div style={{ color: c.dark.textLight, marginBottom: 8 }}>
              Pass flagKeys or flagsProvider to inspect
            </div>
          ) : null}

          {hasFlagsProvider && (
            <section style={{ marginBottom: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                <strong style={{ color: c.dark.text }}>Flags</strong>
                <span>
                  <button
                    type="button"
                    onClick={() => {
                      setFlagsLoading(true);
                      flagsProvider!().then(setAllFlags).finally(() => setFlagsLoading(false));
                    }}
                    style={{ marginRight: 8, padding: '2px 8px', cursor: 'pointer' }}
                  >
                    Refresh
                  </button>
                  {Object.keys(overrides).length > 0 && (
                    <button
                      type="button"
                      onClick={() => setOverrides({})}
                      style={{ padding: '2px 8px', cursor: 'pointer' }}
                    >
                      Clear all overrides
                    </button>
                  )}
                </span>
              </div>
              {flagsLoading && allFlags.length === 0 ? (
                <div style={{ color: c.dark.textLight }}>Loading…</div>
              ) : allFlags.length === 0 ? (
                <div style={{ color: c.dark.textLight }}>No flags</div>
              ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <tbody>
                    {allFlags.map((row) => (
                      <tr key={row.key}>
                        <td style={{ padding: `${s['4']} ${s['8']} ${s['4']} 0` }}>{row.key}</td>
                        <td style={{ padding: s['4'] }}>{row.enabled ? 'on' : 'off'}</td>
                        <td style={{ padding: s['4'] }}>{overrides[row.key] ?? '—'}</td>
                        <td style={{ padding: s['4'] }}>
                          <select
                            value={overrides[row.key] ?? ''}
                            onChange={(e) => {
                              const v = e.target.value;
                              if (v) setOverrides((prev) => ({ ...prev, [row.key]: v }));
                              else setOverrides((prev) => {
                                const next = { ...prev };
                                delete next[row.key];
                                return next;
                              });
                            }}
                            style={{ padding: 2, minWidth: 80 }}
                          >
                            <option value="">Override</option>
                            <option value="disabled">disabled</option>
                            {row.variantKeys.map((vk) => (
                              <option key={vk} value={vk}>{vk}</option>
                            ))}
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}

          {!hasFlagsProvider && flagKeys.length > 0 && (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: 12 }}>
              <tbody>
                {flags.map((f) => (
                  <tr key={f.key}>
                    <td style={{ padding: `${s['4']} ${s['8']} ${s['4']} 0` }}>{f.key}</td>
                    <td style={{ padding: s['4'], textAlign: 'right' }}>
                      {f.loading ? (
                        <span style={{ color: c.dark.textLight }}>…</span>
                      ) : (
                        <span style={{ color: f.enabled ? c.success : c.error }}>
                          {f.enabled ? 'on' : 'off'}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <section style={{ marginBottom: 12 }}>
            <strong style={{ color: c.dark.text }}>Evaluate</strong>
            <div style={{ marginTop: 4 }}>
              <input
                type="text"
                placeholder="Flag key"
                value={evalFlagKey}
                onChange={(e) => setEvalFlagKey(e.target.value)}
                style={{ width: '100%', marginBottom: 4, padding: 4, boxSizing: 'border-box' }}
              />
              <div style={{ color: c.dark.textLight, fontSize: 10, marginBottom: 4 }}>Current entity (used for evaluate)</div>
              <input
                type="text"
                placeholder="Entity ID"
                value={entityId}
                onChange={(e) => setEntityId(e.target.value)}
                style={{ width: '100%', marginBottom: 4, padding: 4, boxSizing: 'border-box' }}
              />
              <input
                type="text"
                placeholder="Entity type"
                value={entityType}
                onChange={(e) => setEntityType(e.target.value)}
                style={{ width: '100%', marginBottom: 4, padding: 4, boxSizing: 'border-box' }}
              />
              <label style={{ display: 'block', marginBottom: 4 }}>
                <input
                  type="checkbox"
                  checked={enableDebug}
                  onChange={(e) => setEnableDebug(e.target.checked)}
                />{' '}
                Debug
              </label>
              <button type="button" onClick={runEvaluate} style={{ padding: '4px 8px', cursor: 'pointer' }}>
                Evaluate
              </button>
            </div>
            {evalError && <div style={{ color: c.error, marginTop: 4 }}>{evalError}</div>}
            {evalResult && (
              <div style={{ marginTop: 4, color: c.dark.textLight }}>
                Result: variantKey = {evalResult.variantKey ?? '—'}
                {evalResult.evalDebugLog?.msg && (
                  <div style={{ fontSize: 10, marginTop: 2 }}>Debug: {evalResult.evalDebugLog.msg}</div>
                )}
              </div>
            )}
          </section>

          <section style={{ marginBottom: 12 }}>
            <strong style={{ color: c.dark.text }}>Cache</strong>
            <div style={{ marginTop: 4 }}>
              <button type="button" onClick={clearCache} style={{ marginRight: 8, padding: '4px 8px', cursor: 'pointer' }}>
                Clear cache
              </button>
              <button type="button" onClick={evictExpired} style={{ padding: '4px 8px', cursor: 'pointer' }}>
                Evict expired
              </button>
              {cacheMessage && <span style={{ marginLeft: 8, color: c.dark.textLight }}>{cacheMessage}</span>}
            </div>
          </section>

          {lastEvals.length > 0 && (
            <section>
              <strong style={{ color: c.dark.text }}>Last evaluations</strong>
              <ul style={{ margin: '4px 0 0', paddingLeft: 16, color: c.dark.textLight }}>
                {lastEvals.slice(0, 5).map((r, i) => (
                  <li key={i}>{r.flagKey ?? '?'} → {r.variantKey ?? '—'}</li>
                ))}
              </ul>
            </section>
          )}
        </div>
      )}
    </div>
  );
};

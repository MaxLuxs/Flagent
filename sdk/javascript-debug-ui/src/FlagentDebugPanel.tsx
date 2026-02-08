import React, { useContext, useEffect, useState } from 'react';
import { FlagentContext } from './FlagentContext';

export interface FlagentDebugPanelProps {
  /** Flag keys to display. If empty, panel shows "Add flags to inspect" */
  flagKeys?: string[];
  /** Position: 'bottom-right' | 'bottom-left' */
  position?: 'bottom-right' | 'bottom-left';
}

interface FlagState {
  key: string;
  enabled: boolean;
  loading: boolean;
}

export const FlagentDebugPanel: React.FC<FlagentDebugPanelProps> = ({
  flagKeys = [],
  position = 'bottom-right',
}) => {
  const manager = useContext(FlagentContext);
  const [expanded, setExpanded] = useState(false);
  const [flags, setFlags] = useState<FlagState[]>([]);

  useEffect(() => {
    if (!manager || flagKeys.length === 0) return;
    let cancelled = false;
    const load = async () => {
      const results = await Promise.all(
        flagKeys.map(async (key) => {
          try {
            const r = await manager.evaluate({ flagKey: key });
            if (cancelled) return { key, enabled: false, loading: false };
            return {
              key,
              enabled: (r.variantKey ?? 'disabled') !== 'disabled',
              loading: false,
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
  }, [manager, flagKeys.join(',')]);

  if (!manager) return null;

  const pos =
    position === 'bottom-right'
      ? { right: 16, bottom: 16 }
      : { left: 16, bottom: 16 };

  return (
    <div
      style={{
        position: 'fixed',
        ...pos,
        zIndex: 99999,
        fontFamily: 'monospace',
        fontSize: 12,
        background: '#1e1e1e',
        color: '#d4d4d4',
        borderRadius: 8,
        boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
        overflow: 'hidden',
      }}
    >
      <button
        onClick={() => setExpanded(!expanded)}
        style={{
          width: '100%',
          padding: '8px 12px',
          border: 'none',
          background: '#2d2d2d',
          color: '#fff',
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
        <div style={{ padding: 12, maxHeight: 300, overflowY: 'auto' }}>
          {flagKeys.length === 0 ? (
            <div style={{ color: '#888' }}>Pass flagKeys prop to inspect</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <tbody>
                {flags.map((f) => (
                  <tr key={f.key}>
                    <td style={{ padding: '4px 8px 4px 0' }}>{f.key}</td>
                    <td style={{ padding: 4, textAlign: 'right' }}>
                      {f.loading ? (
                        <span style={{ color: '#888' }}>…</span>
                      ) : (
                        <span style={{ color: f.enabled ? '#4ec9b0' : '#f14c4c' }}>
                          {f.enabled ? 'on' : 'off'}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
};

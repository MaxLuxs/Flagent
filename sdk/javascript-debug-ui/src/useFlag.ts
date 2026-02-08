import { useState, useEffect } from 'react';
import { FlagentManager } from '@flagent/enhanced-client';

export interface UseFlagResult {
  enabled: boolean;
  loading: boolean;
}

export function useFlag(
  manager: FlagentManager,
  key: string,
  entityID?: string,
  entityContext?: Record<string, unknown>
): UseFlagResult {
  const [enabled, setEnabled] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    manager
      .evaluate({
        flagKey: key,
        entityID,
        entityContext,
      })
      .then((r) => setEnabled((r.variantKey ?? 'disabled') !== 'disabled'))
      .catch(() => setEnabled(false))
      .finally(() => setLoading(false));
  }, [manager, key, entityID, JSON.stringify(entityContext ?? {})]);

  return { enabled, loading };
}

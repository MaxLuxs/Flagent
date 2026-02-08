import React, { useContext } from 'react';
import { FlagentContext } from './FlagentContext';
import { useFlag } from './useFlag';

export interface FeatureFlagProps {
  flagKey: string;
  fallback?: React.ReactNode;
  entityID?: string;
  entityContext?: Record<string, unknown>;
  children: React.ReactNode;
}

export const FeatureFlag: React.FC<FeatureFlagProps> = ({
  flagKey,
  fallback = null,
  entityID,
  entityContext,
  children,
}) => {
  const manager = useContext(FlagentContext);
  if (!manager) {
    throw new Error('FeatureFlag must be used within FlagentProvider');
  }
  const { enabled, loading } = useFlag(manager, flagKey, entityID, entityContext);
  if (loading) return <>{fallback ?? null}</>;
  return <>{enabled ? children : fallback}</>;
};

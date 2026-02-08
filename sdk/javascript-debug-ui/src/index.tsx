import React from 'react';
import { FlagentManager } from '@flagent/enhanced-client';

export { FeatureFlag } from './FeatureFlag';
export { FlagentProvider } from './FlagentProvider';
export { useFlag } from './useFlag';
export type { FeatureFlagProps } from './FeatureFlag';
export type { FlagentProviderProps } from './FlagentProvider';
export type { UseFlagResult } from './useFlag';

export interface FlagentDebugUIProps {
  manager: FlagentManager;
}

export const FlagentDebugUI: React.FC<FlagentDebugUIProps> = ({ manager }) => {
  return (
    <div>
      <h1>Flagent Debug UI</h1>
      {/* TODO: Implement flags list, details, overrides, logs */}
    </div>
  );
};
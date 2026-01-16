import React from 'react';
import { FlagentManager } from '@flagent/enhanced-client';

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
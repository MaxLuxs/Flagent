import React from 'react';
import { FlagentManager } from '@flagent/enhanced-client';
import { FlagentContext } from './FlagentContext';

export interface FlagentProviderProps {
  manager: FlagentManager;
  children: React.ReactNode;
}

export const FlagentProvider: React.FC<FlagentProviderProps> = ({
  manager,
  children,
}) => (
  <FlagentContext.Provider value={manager}>{children}</FlagentContext.Provider>
);

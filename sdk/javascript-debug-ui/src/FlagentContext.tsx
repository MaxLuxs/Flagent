import React from 'react';
import { FlagentManager } from '@flagent/enhanced-client';

export const FlagentContext = React.createContext<FlagentManager | null>(null);

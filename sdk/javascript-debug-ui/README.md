# Flagent JavaScript Debug UI

Debug UI library for Flagent Enhanced SDK using React.

## Installation

```bash
npm install @flagent/debug-ui
```

## Usage

```typescript
import { FlagentDebugUI } from '@flagent/debug-ui';
import { FlagentManager } from '@flagent/enhanced-client';

const manager = new FlagentManager(config);
<FlagentDebugUI manager={manager} />
```

## Features

- List all flags
- View flag details with evaluation logs
- Local overrides
- Evaluation logs viewer
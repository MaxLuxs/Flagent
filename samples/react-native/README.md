# Flagent React Native Sample

Minimal example demonstrating Flagent SDK usage in React Native.

## Overview

React Native uses JavaScript runtime, so it uses the same packages as web:
- `@flagent/client` - Base API client
- `@flagent/enhanced-client` - Enhanced SDK with caching

## Setup

### Option 1: Use in existing React Native project

```bash
npm install @flagent/client @flagent/enhanced-client
```

See [sdk/REACT_NATIVE.md](../../sdk/REACT_NATIVE.md) for usage examples.

### Option 2: Run this sample

This sample uses local SDK paths. To run:

1. **Start Flagent backend:**
   ```bash
   cd backend && ./gradlew run
   ```

2. **Install dependencies:**
   ```bash
   cd samples/react-native
   npm install
   ```

3. **Build JavaScript SDKs** (if not built):
   ```bash
   cd ../../sdk/javascript && npm run build
   cd ../javascript-enhanced && npm run build
   ```

4. **Create React Native app** (this folder contains only the App component):
   ```bash
   npx @react-native-community/cli init FlagentSample --directory .
   # Then replace App.tsx with our version and add dependencies
   ```

**Note:** This sample provides `App.tsx` and `package.json` as reference. For a full runnable app, create a new React Native project and add the Flagent dependencies:

```bash
npx @react-native-community/cli init MyApp
cd MyApp
npm install @flagent/client @flagent/enhanced-client
# Copy the evaluate logic from App.tsx into your app
```

## Usage example

```tsx
import { Configuration } from '@flagent/client';
import { FlagentManager } from '@flagent/enhanced-client';

const manager = new FlagentManager(
  new Configuration({ basePath: 'http://localhost:18000/api/v1' }),
  { enableCache: true, cacheTtlMs: 60000 }
);

const result = await manager.evaluate({
  flagKey: 'my_feature_flag',
  entityID: 'user123',
  entityContext: { region: 'US' },
});
```

## License

Apache 2.0 - See parent project license

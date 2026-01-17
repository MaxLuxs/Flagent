# Flagent JavaScript Sample

Sample web application demonstrating the usage of Flagent SDK for feature flagging and A/B testing in JavaScript/TypeScript.

## Features

- **Single Flag Evaluation** - Evaluate individual flags using basic API
- **Batch Evaluation** - Evaluate multiple flags for multiple entities efficiently
- **Entity Context** - Provide additional context for evaluation (region, tier, etc.)
- **Debug Mode** - Enable debug mode for detailed evaluation logs
- **Simple UI** - Clean, responsive HTML interface

## Requirements

- Modern web browser (Chrome, Firefox, Safari, Edge)
- Flagent backend server running (default: `http://localhost:18000`)

## Setup

### 1. Start Flagent Backend

Before using the sample, ensure the Flagent backend server is running:

```bash
cd flagent/backend
./gradlew run
```

The server will start on `http://localhost:18000` by default.

### 2. Open Sample

Simply open `index.html` in your web browser:

```bash
# Using Python's built-in server
python3 -m http.server 8080

# Or using Node.js http-server
npx http-server

# Then open http://localhost:8080/index.html
```

Or open the file directly in your browser (some browsers may block CORS requests).

## Usage

### Single Flag Evaluation

1. Enter Flagent Base URL (default: `http://localhost:18000/api/v1`)
2. Enter flag key
3. Enter entity ID
4. Optionally provide entity type and context as JSON (e.g., `{"region": "US", "tier": "premium"}`)
5. Toggle debug mode if needed
6. Click "Evaluate Flag"

The result will show:
- Assigned variant key
- Flag and variant IDs
- Full evaluation result as JSON

### Batch Evaluation

1. Enter comma-separated flag keys (e.g., `flag1,flag2,flag3`)
2. Provide entities as JSON array:
   ```json
   [
     {
       "entityID": "user1",
       "entityType": "user",
       "entityContext": {"region": "US"}
     },
     {
       "entityID": "user2",
       "entityType": "user"
     }
   ]
   ```
3. Click "Evaluate Batch"

Results are displayed as JSON showing all evaluation results.

## Integration with SDK

This sample uses the Flagent API directly via `fetch`. In a real application, you would use the Flagent JavaScript SDK:

### Basic SDK

```javascript
import { EvaluationApi, Configuration } from '@flagent/client';

const configuration = new Configuration({
    basePath: 'http://localhost:18000/api/v1'
});

const evaluationApi = new EvaluationApi(configuration);

const result = await evaluationApi.postEvaluation({
    flagKey: 'my_feature_flag',
    entityID: 'user123',
    entityContext: { region: 'US' }
});
```

### Enhanced SDK (with caching)

```javascript
import { FlagentManager, Configuration } from '@flagent/enhanced-client';

const configuration = new Configuration({
    basePath: 'http://localhost:18000/api/v1'
});

const manager = new FlagentManager(configuration, {
    enableCache: true,
    cacheTtlMs: 60000 // 1 minute
});

const result = await manager.evaluate({
    flagKey: 'my_feature_flag',
    entityID: 'user123',
    entityContext: { region: 'US' }
});
```

## CORS Configuration

If opening `index.html` directly from the file system, you may encounter CORS issues. To resolve this:

1. Use a local web server (as shown in Setup)
2. Or configure CORS on the Flagent backend to allow your origin

## Troubleshooting

### Connection Issues

- Ensure Flagent backend is running
- Check base URL is correct (`http://localhost:18000/api/v1`)
- Verify CORS is configured on the backend if accessing from different origin

### Invalid JSON Errors

- Ensure entity context and batch entities are valid JSON
- Use double quotes for JSON keys and string values
- Check for trailing commas in JSON

## License

Apache 2.0 - See parent project license

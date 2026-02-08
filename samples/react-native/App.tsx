/**
 * Flagent React Native Sample
 *
 * Minimal example demonstrating Flagent SDK usage in React Native.
 * Uses @flagent/client + @flagent/enhanced-client.
 *
 * Usage:
 *   1. Install dependencies: npm install
 *   2. Start Flagent backend: cd ../../backend && ./gradlew run
 *   3. Run: npx react-native start (then run android/ios in another terminal)
 *
 * Or use in an existing React Native project:
 *   npm install @flagent/client @flagent/enhanced-client
 */

import React, { useEffect, useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
  Button,
} from 'react-native';
import { Configuration } from '@flagent/client';
import { FlagentManager } from '@flagent/enhanced-client';

// For Android emulator use 10.0.2.2, for iOS simulator use localhost
const FLAGENT_BASE_URL =
  process.env.FLAGENT_BASE_URL ?? 'http://localhost:18000/api/v1';

const configuration = new Configuration({
  basePath: FLAGENT_BASE_URL,
});

const manager = new FlagentManager(configuration, {
  cacheTtlMs: 5 * 60 * 1000,
  enableCache: true,
});

function App(): React.JSX.Element {
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const evaluateFlag = async () => {
    setError(null);
    setResult(null);
    try {
      const evalResult = await manager.evaluate({
        flagKey: 'my_feature_flag',
        entityID: 'user123',
        entityContext: { region: 'US', tier: 'premium' },
        enableDebug: true,
      });
      setResult(
        JSON.stringify(
          {
            flagKey: evalResult.flagKey,
            variantKey: evalResult.variantKey,
            flagID: evalResult.flagID,
            variantID: evalResult.variantID,
          },
          null,
          2,
        ),
      );
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <View style={styles.content}>
          <Text style={styles.title}>Flagent React Native Sample</Text>
          <Text style={styles.subtitle}>
            Base URL: {FLAGENT_BASE_URL}
          </Text>
          <Button title="Evaluate Flag" onPress={evaluateFlag} />
          {result && (
            <View style={styles.result}>
              <Text style={styles.resultTitle}>Result:</Text>
              <Text style={styles.resultText}>{result}</Text>
            </View>
          )}
          {error && (
            <View style={styles.error}>
              <Text style={styles.errorText}>Error: {error}</Text>
            </View>
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  content: {
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    marginBottom: 20,
  },
  result: {
    marginTop: 20,
    padding: 12,
    backgroundColor: '#f0f0f0',
    borderRadius: 8,
  },
  resultTitle: {
    fontWeight: '600',
    marginBottom: 8,
  },
  resultText: {
    fontFamily: 'monospace',
    fontSize: 12,
  },
  error: {
    marginTop: 20,
    padding: 12,
    backgroundColor: '#fee',
    borderRadius: 8,
  },
  errorText: {
    color: '#c00',
  },
});

export default App;

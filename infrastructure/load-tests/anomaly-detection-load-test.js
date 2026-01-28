import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const detectionDuration = new Trend('detection_duration');
const anomaliesDetected = new Counter('anomalies_detected');

// Test configuration
export const options = {
  scenarios: {
    // Scenario 1: Continuous metric submission to generate data
    metric_generation: {
      executor: 'constant-vus',
      vus: 20,
      duration: '5m',
      exec: 'generateMetrics',
    },
    // Scenario 2: Periodic anomaly detection
    anomaly_detection: {
      executor: 'constant-vus',
      vus: 5,
      duration: '5m',
      exec: 'detectAnomalies',
      startTime: '30s', // Start after some metrics are collected
    },
    // Scenario 3: Alert management
    alert_management: {
      executor: 'constant-vus',
      vus: 3,
      duration: '5m',
      exec: 'manageAlerts',
      startTime: '1m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'], // More lenient for complex operations
    http_req_failed: ['rate<0.10'],
    detection_duration: ['p(95)<1000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8000';

// Generate metrics with occasional anomalies
function generateAnomalousMetric(flagId) {
  const rand = Math.random();
  
  // 10% chance of anomalous data
  const isAnomaly = rand < 0.1;
  
  const metrics = [];
  
  // Success rate metric
  metrics.push({
    flagId: flagId,
    flagKey: `test_flag_${flagId}`,
    metricType: 'SUCCESS_RATE',
    metricValue: isAnomaly ? (0.5 + Math.random() * 0.3) : (0.95 + Math.random() * 0.05),
    timestamp: Date.now(),
    entityId: `user_${Math.floor(Math.random() * 1000)}`,
  });
  
  // Error rate metric
  metrics.push({
    flagId: flagId,
    flagKey: `test_flag_${flagId}`,
    metricType: 'ERROR_RATE',
    metricValue: isAnomaly ? (0.1 + Math.random() * 0.2) : (Math.random() * 0.02),
    timestamp: Date.now(),
    entityId: `user_${Math.floor(Math.random() * 1000)}`,
  });
  
  return metrics;
}

// Scenario 1: Generate metrics continuously
export function generateMetrics() {
  const flagId = Math.floor(Math.random() * 5) + 1;
  const metrics = generateAnomalousMetric(flagId);
  
  const response = http.post(
    `${BASE_URL}/api/v1/metrics/batch`,
    JSON.stringify(metrics),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'GenerateMetrics' },
    }
  );
  
  check(response, {
    'metrics submitted': (r) => r.status === 200,
  });
  
  sleep(1); // 1 metric batch per second per VU
}

// Scenario 2: Detect anomalies
export function detectAnomalies() {
  const flagId = Math.floor(Math.random() * 5) + 1;
  
  const startTime = Date.now();
  const response = http.post(
    `${BASE_URL}/api/v1/anomaly/detect/${flagId}`,
    null,
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'DetectAnomalies' },
    }
  );
  
  const duration = Date.now() - startTime;
  detectionDuration.add(duration);
  
  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'response has data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.flag_id !== undefined;
      } catch {
        return false;
      }
    },
  });
  
  if (success) {
    try {
      const body = JSON.parse(response.body);
      if (body.has_anomalies) {
        anomaliesDetected.add(body.anomalies_detected);
      }
    } catch (e) {
      // Ignore parsing errors
    }
  }
  
  errorRate.add(!success);
  
  sleep(5); // Detect every 5 seconds
}

// Scenario 3: Manage alerts
export function manageAlerts() {
  const actions = ['getAlerts', 'getUnresolved', 'resolveAlert'];
  const action = actions[Math.floor(Math.random() * actions.length)];
  
  let response;
  
  switch (action) {
    case 'getAlerts':
      const flagId = Math.floor(Math.random() * 5) + 1;
      response = http.get(
        `${BASE_URL}/api/v1/anomaly/alerts/${flagId}?limit=50`,
        {
          tags: { name: 'GetAlerts' },
        }
      );
      
      check(response, {
        'get alerts success': (r) => r.status === 200,
      });
      break;
      
    case 'getUnresolved':
      response = http.get(
        `${BASE_URL}/api/v1/anomaly/alerts/unresolved`,
        {
          tags: { name: 'GetUnresolved' },
        }
      );
      
      check(response, {
        'get unresolved success': (r) => r.status === 200,
      });
      
      // Try to resolve some alerts
      if (response.status === 200) {
        try {
          const alerts = JSON.parse(response.body);
          if (alerts.length > 0) {
            const alertId = alerts[0].id;
            http.post(
              `${BASE_URL}/api/v1/anomaly/alerts/${alertId}/resolve`,
              null,
              {
                tags: { name: 'ResolveAlert' },
              }
            );
          }
        } catch (e) {
          // Ignore
        }
      }
      break;
  }
  
  sleep(3); // Manage alerts every 3 seconds
}

// Setup: Create anomaly detection configs
export function setup() {
  console.log('Setting up anomaly detection configs...');
  
  for (let i = 1; i <= 5; i++) {
    const config = {
      flagId: i,
      enabled: true,
      errorRateThreshold: 0.05,
      successRateThreshold: 0.95,
      latencyThresholdMs: 1000,
      minSampleSize: 50,
      windowSizeMs: 60000, // 1 minute window
      autoKillSwitch: false,
      autoRollback: true,
    };
    
    const response = http.post(
      `${BASE_URL}/api/v1/anomaly/config`,
      JSON.stringify(config),
      {
        headers: { 'Content-Type': 'application/json' },
      }
    );
    
    if (response.status !== 201) {
      console.log(`Failed to create config for flag ${i}: ${response.status}`);
    }
  }
  
  console.log('Setup complete. Generating metrics for 30 seconds before anomaly detection starts...');
}

// Teardown
export function teardown(data) {
  console.log('Load test completed.');
  console.log(`Total anomalies detected: ${anomaliesDetected.value}`);
}

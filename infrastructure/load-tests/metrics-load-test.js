import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const metricCollectionDuration = new Trend('metric_collection_duration');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp up to 10 users
    { duration: '1m', target: 50 },    // Ramp up to 50 users
    { duration: '2m', target: 100 },   // Ramp up to 100 users
    { duration: '3m', target: 100 },   // Stay at 100 users
    { duration: '1m', target: 200 },   // Spike to 200 users
    { duration: '2m', target: 200 },   // Stay at 200 users
    { duration: '1m', target: 0 },     // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% of requests < 500ms, 99% < 1000ms
    http_req_failed: ['rate<0.05'],                  // Error rate < 5%
    errors: ['rate<0.05'],                           // Custom error rate < 5%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8000';

// Generate random metric data
function generateMetric() {
  const metricTypes = ['SUCCESS_RATE', 'ERROR_RATE', 'LATENCY_MS', 'CONVERSION_RATE'];
  const flagIds = [1, 2, 3, 4, 5];
  const metricType = metricTypes[Math.floor(Math.random() * metricTypes.length)];
  
  let metricValue;
  switch (metricType) {
    case 'SUCCESS_RATE':
      metricValue = 0.9 + Math.random() * 0.1; // 90-100%
      break;
    case 'ERROR_RATE':
      metricValue = Math.random() * 0.05; // 0-5%
      break;
    case 'LATENCY_MS':
      metricValue = 50 + Math.random() * 200; // 50-250ms
      break;
    case 'CONVERSION_RATE':
      metricValue = 0.05 + Math.random() * 0.1; // 5-15%
      break;
  }
  
  return {
    flagId: flagIds[Math.floor(Math.random() * flagIds.length)],
    flagKey: `test_flag_${Math.floor(Math.random() * 5) + 1}`,
    variantId: Math.floor(Math.random() * 3) + 1,
    variantKey: `variant_${Math.floor(Math.random() * 3) + 1}`,
    metricType: metricType,
    metricValue: metricValue,
    timestamp: Date.now(),
    entityId: `user_${Math.floor(Math.random() * 10000)}`,
  };
}

// Test: Single metric submission
export function testSingleMetric() {
  const metric = generateMetric();
  
  const response = http.post(
    `${BASE_URL}/api/v1/metrics`,
    JSON.stringify(metric),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'SingleMetric' },
    }
  );
  
  const success = check(response, {
    'status is 201': (r) => r.status === 201,
    'response time < 100ms': (r) => r.timings.duration < 100,
  });
  
  errorRate.add(!success);
  metricCollectionDuration.add(response.timings.duration);
  
  return response;
}

// Test: Batch metric submission
export function testBatchMetrics() {
  const batchSize = 50;
  const metrics = [];
  
  for (let i = 0; i < batchSize; i++) {
    metrics.push(generateMetric());
  }
  
  const response = http.post(
    `${BASE_URL}/api/v1/metrics/batch`,
    JSON.stringify(metrics),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'BatchMetrics' },
    }
  );
  
  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'success rate > 90%': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success_rate > 0.9;
      } catch {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
  
  return response;
}

// Test: Get metrics for flag
export function testGetMetrics() {
  const flagId = Math.floor(Math.random() * 5) + 1;
  const startTime = Date.now() - 3600000; // 1 hour ago
  const endTime = Date.now();
  
  const response = http.get(
    `${BASE_URL}/api/v1/metrics/${flagId}?start_time=${startTime}&end_time=${endTime}`,
    {
      tags: { name: 'GetMetrics' },
    }
  );
  
  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
    'response is array': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body));
      } catch {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
  
  return response;
}

// Test: Get aggregated metrics
export function testGetAggregation() {
  const flagId = Math.floor(Math.random() * 5) + 1;
  const metricType = 'SUCCESS_RATE';
  const windowStart = Date.now() - 3600000; // 1 hour ago
  const windowEnd = Date.now();
  
  const response = http.get(
    `${BASE_URL}/api/v1/metrics/${flagId}/aggregation?metric_type=${metricType}&window_start=${windowStart}&window_end=${windowEnd}`,
    {
      tags: { name: 'GetAggregation' },
    }
  );
  
  const success = check(response, {
    'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'response time < 300ms': (r) => r.timings.duration < 300,
  });
  
  errorRate.add(!success);
  
  return response;
}

// Main test scenario
export default function () {
  // 70% single metrics, 20% batch, 10% reads
  const rand = Math.random();
  
  if (rand < 0.7) {
    testSingleMetric();
  } else if (rand < 0.9) {
    testBatchMetrics();
  } else {
    const readRand = Math.random();
    if (readRand < 0.5) {
      testGetMetrics();
    } else {
      testGetAggregation();
    }
  }
  
  sleep(0.1); // 100ms between requests per user
}

// Setup: Create test flags
export function setup() {
  console.log('Setting up test data...');
  
  // Create test flags (if they don't exist)
  for (let i = 1; i <= 5; i++) {
    const flag = {
      key: `test_flag_${i}`,
      description: `Load test flag ${i}`,
    };
    
    http.post(
      `${BASE_URL}/api/v1/flags`,
      JSON.stringify(flag),
      {
        headers: { 'Content-Type': 'application/json' },
      }
    );
  }
  
  console.log('Setup complete');
}

// Teardown: Clean up test data (optional)
export function teardown(data) {
  console.log('Test completed. Check results above.');
}

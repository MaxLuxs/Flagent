/**
 * Evaluation API load test.
 * Targets POST /api/v1/evaluation - the core flag evaluation endpoint.
 * Goal: ~2000 req/s, mean latency < 1ms, p99 < 10ms, error rate < 1%.
 * Use EVAL_VUS=200 for CI (shared runners); default 2000 for local runs.
 */
import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const evalDuration = new Trend('eval_duration');

const VUS = parseInt(__ENV.EVAL_VUS || '2000', 10);
const DURATION = __ENV.EVAL_DURATION || '30s';

export const options = {
  scenarios: {
    evaluation_constant: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
      exec: 'evaluateFlag',
    },
  },
  thresholds: {
    http_req_duration: ['p(50)<5', 'p(95)<50', 'p(99)<100'],
    http_req_failed: ['rate<0.01'],
    errors: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8000';

function makeEvalBody(i) {
  return JSON.stringify({
    flagID: (i % 5) + 1, // flags 1-5 created in setup
    entityID: `user_${i}`,
    entityType: 'user',
    entityContext: { country: 'US' },
    enableDebug: false,
  });
}

export function evaluateFlag() {
  const i = __VU * 1000000 + __ITER;
  const body = makeEvalBody(i);

  const response = http.post(
    `${BASE_URL}/api/v1/evaluation`,
    body,
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Evaluation' },
    }
  );

  evalDuration.add(response.timings.duration);

  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'has flagID': (r) => {
      try {
        const b = JSON.parse(r.body);
        return b.flagID !== undefined || b.flag_key !== undefined;
      } catch {
        return false;
      }
    },
  });

  errorRate.add(!success);
}

export function setup() {
  console.log('Creating test flags for evaluation load test...');
  for (let i = 1; i <= 5; i++) {
    const res = http.post(
      `${BASE_URL}/api/v1/flags`,
      JSON.stringify({
        key: `eval_flag_${i}`,
        description: `Load test evaluation flag ${i}`,
        enabled: true,
      }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status !== 201 && res.status !== 200) {
      console.log(`Flag ${i} may already exist or creation failed: ${res.status}`);
    }
  }
  console.log('Setup complete');
}

export function teardown() {
  console.log('Evaluation load test completed');
}

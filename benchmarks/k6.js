import http from 'k6/http';
import { check } from 'k6';
export const options = { vus: 100, duration: '60s', thresholds: { http_req_duration: ['p(95)<20', 'p(99)<100'] } };
const base = __ENV.BASE_URL || 'http://localhost:8080';
const code = __ENV.SHORT_CODE || 'benchmark-code';
export default function () {
  const response = http.get(`${base}/${code}`, { redirects: 0 });
  check(response, { 'redirect or expected miss': r => [302, 404, 410, 429].includes(r.status) });
}

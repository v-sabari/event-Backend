# Load & Resilience Tests

This folder contains [k6](https://k6.io/) scripts used to exercise the backend under load. All of them target `GET /api/health` (or a small set of core endpoints for `db-connection-test.js`), so they can be run against any environment without needing auth tokens or seed data.

## Prerequisites

Install k6: https://grafana.com/docs/k6/latest/set-up/install-k6/

By default these scripts point at `http://localhost:8080`. Edit the URL at the top of a script (or refactor to use a `BASE_URL` env var via `__ENV.BASE_URL`) to target a deployed environment instead.

## Scripts

| Script | What it does | Ramp profile |
|---|---|---|
| `load-test.js` | Baseline sanity check under light load | 10 → 50 VUs over ~6 min |
| `health-check-load-test.js` | Sustained high-concurrency hammering of the health endpoint, with strict latency thresholds (p90 < 100ms, p95 < 200ms, p99 < 500ms, <1% failures) | up to 200 VUs over ~9 min |
| `stress-test.js` | Finds the breaking point by ramping to extreme concurrency | up to 300 VUs over ~11 min |
| `db-connection-test.js` | Hits multiple endpoints simultaneously to stress the DB connection pool specifically, not just the web layer | up to 150 VUs over ~9 min |
| `graceful-shutdown-test.js` | Sustains load while you manually kill the backend mid-run, to verify in-flight requests fail gracefully rather than hanging | 50 VUs sustained, ~8 min — **kill the backend during the 5-minute plateau** |

## Running a script

```bash
k6 run load-tests/health-check-load-test.js
```

To run against a deployed environment (after editing the target URL, or once a `BASE_URL` env var is wired in):

```bash
k6 run -e BASE_URL=https://your-backend.onrender.com load-tests/load-test.js
```

## Interpreting results

k6 prints a summary at the end of each run, including:
- `http_req_duration` — response time percentiles (p90/p95/p99)
- `http_req_failed` — failure rate
- Whether each configured `thresholds` value passed or failed

A failing threshold doesn't necessarily mean a bug — it may mean the current infrastructure (e.g. free-tier hosting, connection pool size) can't sustain that load, which is exactly what these scripts are meant to surface.

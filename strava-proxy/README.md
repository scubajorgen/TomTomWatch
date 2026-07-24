# Strava OAuth token proxy (Cloudflare Worker)

A tiny stateless proxy that keeps the Strava `client_secret` off the desktop app.
It exposes two endpoints:

- `POST /token`   – body `{ "code": "<authorization code>" }` → returns Strava's token JSON
- `POST /refresh` – body `{ "refresh_token": "<token>" }` → returns Strava's token JSON

The desktop app keeps doing the browser consent + loopback capture, and uploads
activities directly to Strava with the access token. This proxy never sees activity data.

## Prerequisites

- A Strava API application (https://www.strava.com/settings/api) with **Authorization
  Callback Domain = `localhost`** (leave it as `localhost` — the proxy is NOT the redirect
  target). Note the **Client ID** and **Client Secret**.

## Deploy (Cloudflare dashboard)

1. **Create the Worker**: Cloudflare dashboard → **Workers & Pages** → **Create** →
   **Create Worker**. Name it e.g. `strava-proxy` → **Deploy** (creates a hello-world).
   If prompted, enable your free `*.workers.dev` subdomain.
2. **Add the code**: open the Worker → **Edit code** → replace the contents with
   [`worker.js`](./worker.js) → **Deploy**.
3. **Add secrets**: Worker → **Settings** → **Variables and Secrets** → add two
   **Secret** (encrypted) entries and Deploy:
   - `STRAVA_CLIENT_ID`
   - `STRAVA_CLIENT_SECRET`
   - (optional) `APP_KEY` — any string; if set, the client must send header `X-App-Key`.
4. **Note the URL**: `https://strava-proxy.<your-subdomain>.workers.dev`.

## Deploy (Wrangler CLI, alternative)

```
npm install -g wrangler
wrangler login
wrangler deploy worker.js --name strava-proxy
wrangler secret put STRAVA_CLIENT_ID
wrangler secret put STRAVA_CLIENT_SECRET
```

## Test

Sanity check (should return HTTP 400, missing field):

```
curl -i -X POST https://strava-proxy.<sub>.workers.dev/refresh \
  -H "Content-Type: application/json" -d "{}"
```

Reachability + secret check (bogus token → Strava replies 400 with a JSON error, which
proves the Worker is calling Strava with your credentials):

```
curl -i -X POST https://strava-proxy.<sub>.workers.dev/refresh \
  -H "Content-Type: application/json" -d "{\"refresh_token\":\"bogus\"}"
```

## Notes

- Free plan (100k requests/day) is far more than enough for a small group.
- All users share the one Strava app's rate limit (200 req / 15 min, 2,000 / day).
- Standard tier serves up to ~10 athletes without Strava review.

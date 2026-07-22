/*
 * Cloudflare Worker: Strava OAuth token proxy for TomTomWatch.
 *
 * Purpose: keep the Strava client_secret OFF the distributed desktop app.
 * Only the two operations that require the secret run here:
 *   POST /token    - exchange an authorization code for tokens
 *   POST /refresh  - refresh an access token
 *
 * The desktop app still does the browser consent + loopback code capture
 * itself, and uploads activities directly to Strava with the access token
 * (uploads need only the Bearer token, not the secret). This Worker never
 * sees any activity data and stores nothing.
 *
 * Secrets (set in the Cloudflare dashboard as encrypted Secrets, or via
 * `wrangler secret put`):
 *   STRAVA_CLIENT_ID
 *   STRAVA_CLIENT_SECRET
 *
 * Optional variable to filter casual abuse (NOT a real secret - it ships in
 * the client): APP_KEY. If set, requests must send header "X-App-Key: <value>".
 */
export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ error: "Method not allowed" }, 405);
    }

    // Optional lightweight abuse filter. When APP_KEY is set, callers must send
    // a matching "X-App-Key" header. (Not a real secret - it ships in the client.)
    const appKey = env.APP_KEY?.trim();
    if (appKey && request.headers.get("X-App-Key") !== appKey) {
      return json({ error: "Forbidden" }, 403);
    }

    const path = new URL(request.url).pathname;

    let body;
    try {
      body = await request.json();
    } catch (e) {
      return json({ error: "Invalid JSON body" }, 400);
    }

    let grant;
    if (path === "/token") {
      if (!body.code) return json({ error: "Missing 'code'" }, 400);
      grant = { grant_type: "authorization_code", code: body.code };
    } else if (path === "/refresh") {
      if (!body.refresh_token) return json({ error: "Missing 'refresh_token'" }, 400);
      grant = { grant_type: "refresh_token", refresh_token: body.refresh_token };
    } else {
      return json({ error: "Not found" }, 404);
    }

    // Credentials may be added/updated any time; until then, fail clearly
    // instead of sending empty values to Strava. Handles missing, empty ("")
    // and whitespace-only values.
    if (!env.STRAVA_CLIENT_ID?.trim() || !env.STRAVA_CLIENT_SECRET?.trim()) {
      return json({ error: "Server not configured: STRAVA_CLIENT_ID and STRAVA_CLIENT_SECRET are not set" }, 500);
    }

    const form = new URLSearchParams({
      client_id: env.STRAVA_CLIENT_ID,
      client_secret: env.STRAVA_CLIENT_SECRET,
      ...grant,
    });

    let stravaResp;
    try {
      stravaResp = await fetch("https://www.strava.com/oauth/token", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form.toString(),
      });
    } catch (e) {
      return json({ error: "Could not reach Strava: " + e.message }, 502);
    }

    // Pass Strava's response through verbatim so the client parses it as usual
    const text = await stravaResp.text();
    return new Response(text, {
      status: stravaResp.status,
      headers: { "Content-Type": "application/json" },
    });
  },
};

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

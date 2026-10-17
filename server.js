const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

const PORT = parseInt(process.env.DEFAULT_APP_PORT, 10) || 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

process.on('uncaughtException', (err) => {
  console.error('[Server Guard] Caught unhandled exception:', err);
});
process.on('unhandledRejection', (reason, promise) => {
  console.error('[Server Guard] Caught unhandled rejection at:', promise, 'reason:', reason);
});

// Extract API key from env or .env file
let apiKey = process.env.GEMINI_API_KEY || '';
if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
  try {
    const envContent = fs.readFileSync(path.join(__dirname, '.env'), 'utf-8');
    const match = envContent.match(/GEMINI_API_KEY=([^\r\n]+)/);
    if (match && match[1] && match[1] !== 'MY_GEMINI_API_KEY') {
      apiKey = match[1].trim();
    }
  } catch (e) {
    // ignore
  }
}

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.webp': 'image/webp',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf'
};

const server = http.createServer((req, res) => {
  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = parsedUrl.pathname;

  // Health check endpoint
  if (pathname === '/health' || pathname === '/api/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', app: 'FieldShift', timestamp: new Date().toISOString() }));
    return;
  }

  // Gemini AI Chat API
  if (pathname === '/api/chat' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const payload = JSON.parse(body || '{}');
        const userPrompt = payload.prompt || '';

        if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
          // Fallback expert agronomist response
          const fallbackReply = generateFallbackReply(userPrompt);
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ reply: fallbackReply, source: 'agro-expert-engine' }));
          return;
        }

        const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;
        const requestData = JSON.stringify({
          contents: [{
            parts: [{
              text: `You are the FieldShift Lead AI Agronomist & Climate Resilience Specialist. Provide concise, expert, practical advice (under 120 words) for regenerative agriculture, crop rotations, soil health, and NASA Earth telemetry:\n\nUser Question: ${userPrompt}`
            }]
          }]
        });

        const gReq = https.request(geminiUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(requestData)
          }
        }, (gRes) => {
          let gBody = '';
          gRes.on('data', c => { gBody += c; });
          gRes.on('end', () => {
            try {
              const parsed = JSON.parse(gBody);
              const text = parsed?.candidates?.[0]?.content?.parts?.[0]?.text;
              if (text) {
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ reply: text, source: 'gemini-live' }));
              } else {
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ reply: generateFallbackReply(userPrompt), source: 'agro-expert-engine' }));
              }
            } catch (err) {
              res.writeHead(200, { 'Content-Type': 'application/json' });
              res.end(JSON.stringify({ reply: generateFallbackReply(userPrompt), source: 'agro-expert-engine' }));
            }
          });
        });

        gReq.on('error', () => {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ reply: generateFallbackReply(userPrompt), source: 'agro-expert-engine' }));
        });

        gReq.write(requestData);
        gReq.end();
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON request' }));
      }
    });
    return;
  }

  // Normalize path
  if (pathname === '/' || pathname === '') {
    pathname = '/index.html';
  }

  let filePath = path.join(PUBLIC_DIR, pathname);

  // Security check: ensure path is within PUBLIC_DIR
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('403 Forbidden');
    return;
  }

  fs.stat(filePath, (err, stats) => {
    if (err) {
      // Fallback to index.html for SPA routing
      const indexPath = path.join(PUBLIC_DIR, 'index.html');
      fs.readFile(indexPath, (readErr, content) => {
        if (readErr) {
          res.writeHead(404, { 'Content-Type': 'text/plain' });
          res.end('404 Not Found');
        } else {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          res.end(content);
        }
      });
      return;
    }

    if (stats.isDirectory()) {
      filePath = path.join(filePath, 'index.html');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(filePath, (readErr, content) => {
      if (readErr) {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('500 Internal Server Error');
      } else {
        res.writeHead(200, {
          'Content-Type': contentType,
          'Cache-Control': 'no-cache'
        });
        res.end(content);
      }
    });
  });
});

function generateFallbackReply(q) {
  const lower = q.toLowerCase();
  if (lower.includes('nitrogen') || lower.includes('n')) {
    return "Biological Nitrogen Strategy: Intercropping Crimson Clover and Hairy Vetch fixes 110–135 kg N/ha in the root zone. This saves upwards of $45/acre in synthetic fertilizer while buffering soil microbial biodiversity.";
  } else if (lower.includes('cover') || lower.includes('radish') || lower.includes('rye')) {
    return "Bio-Armor Cover Synergies: Cereal Rye scavenges residual nitrogen and delivers 94% allelopathic weed suppression. Daikon Tillage Radish bio-drills up to 24 inches to break plow pans and aerate heavy clay subsoils.";
  } else if (lower.includes('carbon') || lower.includes('credit')) {
    return "Carbon Sequestration Accretion: Continuous no-till combined with diverse multi-species cover crops builds soil organic matter at 1.8–3.2 tons CO₂e/ha annually, generating strong carbon credit dividends ($32/ton).";
  }
  return "FieldShift Agro-Advisory: Based on NASA MODIS surface temperature and regional soil telemetry, maintaining living roots across all seasons maximizes moisture retention, mitigates drought shock, and strengthens long-term economic yield.";
}

server.listen(PORT, '0.0.0.0', () => {
  console.log(`FieldShift Web Server running at http://0.0.0.0:${PORT}`);
});

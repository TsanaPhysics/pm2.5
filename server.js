#!/usr/bin/env node

/**
 * AeroScan PM2.5 - Standalone HTTP Web Server
 * Zero external npm dependencies required.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const url = require('url');

const PORT = parseInt(process.env.PORT || '8080', 10);
const PUBLIC_DIR = path.join(__dirname, 'public');
const APK_CANDIDATE_PATHS = [
  path.join(__dirname, 'AeroScan-PM2.5-debug.apk'),
  path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
];

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.apk': 'application/vnd.android.package-archive',
  '.csv': 'text/csv; charset=utf-8'
};

function getLocalIpAddresses() {
  const interfaces = os.networkInterfaces();
  const ips = [];
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        ips.push(iface.address);
      }
    }
  }
  return ips;
}

function getApkFilePath() {
  for (const candidate of APK_CANDIDATE_PATHS) {
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }
  return null;
}

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = decodeURIComponent(parsedUrl.pathname);

  // CORS Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Route: /api/status
  if (pathname === '/api/status') {
    const apkFile = getApkFilePath();
    let apkSize = null;
    if (apkFile) {
      const stats = fs.statSync(apkFile);
      apkSize = (stats.size / (1024 * 1024)).toFixed(1) + ' MB';
    }

    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      status: 'online',
      app: 'AeroScan PM2.5',
      version: '1.0.0',
      apkAvailable: !!apkFile,
      apkSize: apkSize,
      apkDownloadUrl: '/download/apk',
      localIps: getLocalIpAddresses(),
      port: PORT,
      timestamp: Date.now()
    }, null, 2));
    return;
  }

  // Route: /download/apk or /AeroScan-PM2.5-debug.apk
  if (pathname === '/download/apk' || pathname === '/AeroScan-PM2.5-debug.apk') {
    const apkFile = getApkFilePath();
    if (!apkFile) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('ไม่พบไฟล์ APK ในระบบ กรุณารัน gradle build ก่อนดาวน์โหลด');
      return;
    }

    const stat = fs.statSync(apkFile);
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Length': stat.size,
      'Content-Disposition': 'attachment; filename="AeroScan-PM2.5-debug.apk"'
    });

    const readStream = fs.createReadStream(apkFile);
    readStream.pipe(res);
    return;
  }

  // Static files in /public
  let relativePath = pathname === '/' ? '/index.html' : pathname;
  let filePath = path.join(PUBLIC_DIR, relativePath);

  // Security check: prevent directory traversal
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Forbidden');
    return;
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // Fallback to index.html for Single Page Applications
      const fallbackIndex = path.join(PUBLIC_DIR, 'index.html');
      if (fs.existsSync(fallbackIndex)) {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        fs.createReadStream(fallbackIndex).pipe(res);
        return;
      }
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('404 Not Found');
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    res.writeHead(200, {
      'Content-Type': contentType,
      'Content-Length': stats.size,
      'Cache-Control': 'no-cache'
    });
    fs.createReadStream(filePath).pipe(res);
  });
});

// Self-test mode
if (process.argv.includes('--test')) {
  server.listen(0, '127.0.0.1', () => {
    const address = server.address();
    console.log(`[TEST SUCCESS] Server started on port ${address.port}`);
    server.close(() => {
      console.log('[TEST SUCCESS] Server stopped cleanly');
      process.exit(0);
    });
  });
} else {
  server.listen(PORT, '0.0.0.0', () => {
    const ips = getLocalIpAddresses();
    console.log('\n=============================================================');
    console.log('  🚀 AeroScan PM2.5 Web Application Server is RUNNING');
    console.log('=============================================================');
    console.log(`  👉 Local URL:      http://localhost:${PORT}`);
    ips.forEach(ip => {
      console.log(`  👉 Mobile/LAN URL: http://${ip}:${PORT}  (เข้าใช้งานผ่านมือถือ/Wi-Fi)`);
    });
    console.log(`  📦 APK Download:   http://localhost:${PORT}/download/apk`);
    console.log('=============================================================\n');
  });
}

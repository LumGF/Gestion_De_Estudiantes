const fs = require('fs');
const path = require('path');
const express = require('express');
const cors = require('cors');
const qrcode = require('qrcode-terminal');
const QRCode = require('qrcode');
const { Client, LocalAuth } = require('whatsapp-web.js');

const PORT = process.env.PORT || 3001;
const API_KEY = process.env.WHATSAPP_BRIDGE_API_KEY || '';
const AUTH_DIR = path.join(__dirname, '.wwebjs_auth');
const CACHE_DIR = path.join(__dirname, '.wwebjs_cache');
const WEB_VERSION =
  process.env.WHATSAPP_WEB_VERSION || '2.3000.1041149705-alpha';
const WEB_VERSION_URL = `https://raw.githubusercontent.com/wppconnect-team/wa-version/main/html/${WEB_VERSION}.html`;

let ready = false;
let lastQr = null;
let client = null;
let initializing = false;

function resolveChromePath() {
  if (process.env.CHROME_PATH && fs.existsSync(process.env.CHROME_PATH)) {
    return process.env.CHROME_PATH;
  }
  const candidates = process.platform === 'win32'
    ? [
        'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
        'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
        `${process.env.LOCALAPPDATA}\\Google\\Chrome\\Application\\chrome.exe`,
        `${process.env.PROGRAMFILES}\\Google\\Chrome\\Application\\chrome.exe`,
      ]
    : process.platform === 'darwin'
      ? ['/Applications/Google Chrome.app/Contents/MacOS/Google Chrome']
      : ['/usr/bin/google-chrome', '/usr/bin/chromium-browser', '/usr/bin/chromium'];

  return candidates.find((p) => p && fs.existsSync(p));
}

const chromePath = resolveChromePath();

function buildPuppeteerOptions() {
  const options = {
    headless: true,
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-dev-shm-usage',
      '--disable-accelerated-2d-canvas',
      '--disable-gpu',
      '--disable-features=IsolateOrigins,site-per-process',
      '--disable-features=MemorySaverMode',
      '--disable-blink-features=AutomationControlled',
      '--no-first-run',
      '--no-default-browser-check',
    ],
  };

  if (chromePath) {
    options.executablePath = chromePath;
  }

  return options;
}

function clearSessionData() {
  for (const dir of [AUTH_DIR, CACHE_DIR]) {
    if (fs.existsSync(dir)) {
      fs.rmSync(dir, { recursive: true, force: true });
      console.log('Sesion eliminada:', dir);
    }
  }
}

function createClient() {
  if (client) {
    return client;
  }

  if (chromePath) {
    console.log('Usando Chrome:', chromePath);
  } else {
    console.warn(
      'Chrome no detectado. Instale Google Chrome o defina CHROME_PATH.\n' +
      'Alternativa: npx puppeteer browsers install chrome'
    );
  }

  client = new Client({
    authStrategy: new LocalAuth({ dataPath: AUTH_DIR }),
    puppeteer: buildPuppeteerOptions(),
    webVersionCache: {
      type: 'remote',
      remotePath: WEB_VERSION_URL,
    },
    takeoverOnConflict: true,
    takeoverTimeoutMs: 0,
  });

  client.on('qr', (qr) => {
    ready = false;
    lastQr = qr;
    console.log('\nEscanea este QR con WhatsApp (Dispositivos vinculados):\n');
    qrcode.generate(qr, { small: true });
    console.log(`\nSi no ves el QR en consola, abre: http://localhost:${PORT}/qr\n`);
  });

  client.on('ready', () => {
    ready = true;
    lastQr = null;
    console.log('WhatsApp bridge listo para enviar mensajes.');
  });

  client.on('auth_failure', (msg) => {
    ready = false;
    lastQr = null;
    console.error('Error de autenticacion WhatsApp:', msg);
  });

  client.on('disconnected', (reason) => {
    ready = false;
    lastQr = null;
    console.warn('WhatsApp desconectado:', reason);
  });

  return client;
}

async function destroyClient() {
  if (!client) return;
  try {
    await client.destroy();
  } catch {
    // ignorar errores al cerrar instancia previa
  }
  client = null;
  ready = false;
  lastQr = null;
}

async function initializeWhatsApp({ clearSession = false, attempt = 1, maxAttempts = 3 } = {}) {
  if (initializing) return;
  initializing = true;

  try {
    if (clearSession) {
      await destroyClient();
      clearSessionData();
    }

    const activeClient = createClient();
    await activeClient.initialize();
  } catch (err) {
    const message = err?.message || String(err);
    const isContextDestroyed = /execution context was destroyed/i.test(message);
    const isRecoverable = isContextDestroyed || /navigation/i.test(message);

    console.error(`Error al iniciar WhatsApp (intento ${attempt}/${maxAttempts}):`, message);

    await destroyClient();

    if (attempt < maxAttempts && isRecoverable) {
      const nextClearSession = clearSession || attempt >= 2;
      const waitMs = attempt * 3000;
      console.warn(
        nextClearSession
          ? `Reintentando en ${waitMs / 1000}s con sesion limpia...`
          : `Reintentando en ${waitMs / 1000}s...`
      );
      await new Promise((resolve) => setTimeout(resolve, waitMs));
      initializing = false;
      return initializeWhatsApp({
        clearSession: nextClearSession,
        attempt: attempt + 1,
        maxAttempts,
      });
    }

    console.error('\nNo se pudo iniciar WhatsApp.');
    console.error('Pruebe:');
    console.error('  1. Cerrar Chrome abierto manualmente');
    console.error('  2. Ejecutar .\\Detener Servicios.bat y volver a iniciar');
    console.error('  3. Borrar whatsapp-bridge\\.wwebjs_auth y whatsapp-bridge\\.wwebjs_cache');
    console.error(`  4. Abrir http://localhost:${PORT}/qr cuando aparezca el codigo\n`);
  } finally {
    initializing = false;
  }
}

function toChatId(telefono) {
  const digits = String(telefono).replace(/\D/g, '');
  if (!digits) {
    throw new Error('Telefono vacio');
  }
  return `${digits}@c.us`;
}

const app = express();
app.use(cors());
app.use(express.json());

function requireApiKey(req, res, next) {
  if (!API_KEY) return next();
  const key = req.headers['x-api-key'];
  if (key !== API_KEY) {
    return res.status(401).json({ ok: false, error: 'API key invalida' });
  }
  next();
}

app.use(requireApiKey);

app.get('/status', (_req, res) => {
  res.json({
    ready,
    authenticated: ready,
    chromeConfigured: Boolean(chromePath),
    qrAvailable: Boolean(lastQr),
    message: ready
      ? 'Cliente WhatsApp conectado'
      : lastQr
        ? `QR disponible en http://localhost:${PORT}/qr`
        : chromePath
          ? 'Esperando QR o conexion. Revise la consola del bridge.'
          : 'Chrome no encontrado. Configure CHROME_PATH o instale Google Chrome.',
  });
});

app.get('/qr', async (_req, res) => {
  if (!lastQr) {
    return res.status(503).send(
      '<html><body style="font-family:sans-serif;padding:2rem">' +
      '<h1>QR no disponible</h1>' +
      '<p>El bridge aun esta iniciando o ya esta conectado.</p>' +
      `<p><a href="/status">Ver estado</a></p></body></html>`
    );
  }

  try {
    const dataUrl = await QRCode.toDataURL(lastQr, { margin: 2, width: 320 });
    res.send(
      '<html><body style="font-family:sans-serif;text-align:center;padding:2rem">' +
      '<h1>Escanee con WhatsApp</h1>' +
      '<p>WhatsApp &gt; Dispositivos vinculados &gt; Vincular dispositivo</p>' +
      `<img src="${dataUrl}" alt="QR WhatsApp" />` +
      '<p>Actualice esta pagina si el codigo expira.</p></body></html>'
    );
  } catch (err) {
    res.status(500).send(`No se pudo generar el QR: ${err.message}`);
  }
});

app.post('/send', async (req, res) => {
  const { to, message } = req.body || {};

  if (!ready) {
    return res.status(503).json({
      ok: false,
      error: 'WhatsApp no esta listo. Escanee el QR en la consola del bridge o en /qr.',
    });
  }

  if (!to || !message) {
    return res.status(400).json({
      ok: false,
      error: 'Campos requeridos: to, message',
    });
  }

  try {
    const chatId = toChatId(to);
    const result = await client.sendMessage(chatId, message);
    res.json({
      ok: true,
      to: chatId,
      messageId: result.id?._serialized ?? null,
    });
  } catch (err) {
    console.error('Error al enviar:', err);
    res.status(500).json({
      ok: false,
      error: err.message || 'No se pudo enviar el mensaje',
    });
  }
});

const server = app.listen(PORT, () => {
  console.log(`WhatsApp bridge en http://localhost:${PORT}`);
  console.log(`QR en navegador: http://localhost:${PORT}/qr`);
  initializeWhatsApp();
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`\nERROR: El puerto ${PORT} ya esta en uso.`);
    console.error('Ejecute desde la raiz del proyecto: .\\Detener Servicios.bat');
    console.error('O mate el proceso: netstat -ano | findstr :' + PORT);
    process.exit(1);
  }
  throw err;
});

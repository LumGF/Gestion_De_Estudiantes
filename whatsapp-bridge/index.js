const fs = require('fs');
const express = require('express');
const cors = require('cors');
const qrcode = require('qrcode-terminal');
const { Client, LocalAuth } = require('whatsapp-web.js');

const PORT = process.env.PORT || 3001;
const API_KEY = process.env.WHATSAPP_BRIDGE_API_KEY || '';
let ready = false;

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
const puppeteerOptions = {
  headless: true,
  args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-dev-shm-usage'],
};

if (chromePath) {
  puppeteerOptions.executablePath = chromePath;
  console.log('Usando Chrome:', chromePath);
} else {
  console.warn(
    'Chrome no detectado. Instale Google Chrome o defina CHROME_PATH.\n' +
    'Alternativa: npx puppeteer browsers install chrome'
  );
}

const client = new Client({
  authStrategy: new LocalAuth({ dataPath: './.wwebjs_auth' }),
  puppeteer: puppeteerOptions,
});

client.on('qr', (qr) => {
  ready = false;
  console.log('\nEscanea este QR con WhatsApp (Dispositivos vinculados):\n');
  qrcode.generate(qr, { small: true });
});

client.on('ready', () => {
  ready = true;
  console.log('WhatsApp bridge listo para enviar mensajes.');
});

client.on('auth_failure', (msg) => {
  ready = false;
  console.error('Error de autenticación WhatsApp:', msg);
});

client.on('disconnected', (reason) => {
  ready = false;
  console.warn('WhatsApp desconectado:', reason);
});

function toChatId(telefono) {
  const digits = String(telefono).replace(/\D/g, '');
  if (!digits) {
    throw new Error('Teléfono vacío');
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
    return res.status(401).json({ ok: false, error: 'API key inválida' });
  }
  next();
}

app.use(requireApiKey);

app.get('/status', (_req, res) => {
  res.json({
    ready,
    authenticated: ready,
    chromeConfigured: Boolean(chromePath),
    message: ready
      ? 'Cliente WhatsApp conectado'
      : chromePath
        ? 'Esperando QR o conexión. Revise la consola del bridge.'
        : 'Chrome no encontrado. Configure CHROME_PATH o instale Google Chrome.',
  });
});

app.post('/send', async (req, res) => {
  const { to, message } = req.body || {};

  if (!ready) {
    return res.status(503).json({
      ok: false,
      error: 'WhatsApp no está listo. Escanee el QR en la consola del bridge.',
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
  client.initialize().catch((err) => {
    console.error('Error al iniciar WhatsApp:', err.message);
  });
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`\nERROR: El puerto ${PORT} ya está en uso.`);
    console.error('Ejecute desde la raíz del proyecto: .\\stop.ps1');
    console.error('O mate el proceso: netstat -ano | findstr :' + PORT);
    process.exit(1);
  }
  throw err;
});

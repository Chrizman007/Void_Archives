require('dotenv').config();

const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');
const config = require('./config');
const app = express();
app.use(express.json());
app.use(cors());
app.use('/public', express.static(path.join(__dirname, 'api', 'v1', 'articulos', 'public')));

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'Void Archives API Gateway',
    timestamp: new Date().toISOString()
  });
});

app.use('/articulos', createProxyMiddleware({
  target: config.services.articulos || 'http://localhost:3001',
  changeOrigin: true,
  onProxyReq: (proxyReq, req) => {
    if (req.body && Object.keys(req.body).length > 0) {
      const bodyData = JSON.stringify(req.body);
      proxyReq.setHeader('Content-Type', 'application/json');
      proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
      proxyReq.write(bodyData);
    }
  }
}));

app.use('/auth', createProxyMiddleware({
  target: (config.services && config.services.usuarios) ? config.services.usuarios : 'http://localhost:3002',
  changeOrigin: true,
  onProxyReq: (proxyReq, req) => {
    if (req.body && Object.keys(req.body).length > 0) {
      const bodyData = JSON.stringify(req.body);
      proxyReq.setHeader('Content-Type', 'application/json');
      proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
      proxyReq.write(bodyData);
    }
  }
}));

app.use('/comentarios', createProxyMiddleware({
  target: config.services.comentarios || 'http://localhost:3004',
  changeOrigin: true,
  onProxyReq: (proxyReq, req) => {
    if (req.body && Object.keys(req.body).length > 0) {
      const bodyData = JSON.stringify(req.body);
      proxyReq.setHeader('Content-Type', 'application/json');
      proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
      proxyReq.write(bodyData);
    }
  }
}));

app.use((req, res) => {
  res.status(404).json({
    error: 'RouteNotFound',
    message: 'La ruta solicitada no existe en el API Gateway de Void Archives.'
  });
});

app.use((err, req, res, next) => {
  console.error('🚨 [Gateway] Error interno de Express:', err);
  res.status(500).json({
    error: 'InternalGatewayError',
    message: 'Ocurrió un error inesperado en la gestión de pasarela de Void Archives.'
  });
});

function startServer() {
  const port = config.port || 3000;

  app.listen(port, () => {
    console.log(`Void Archives API Gateway running on port ${port}`);
    console.log(`Health check: http://localhost:${port}/health`);
    console.log(`Proxying /articulos   -> ${config.services.articulos || 'http://localhost:3001'}`);
    console.log(`Proxying /auth        -> ${(config.services && config.services.usuarios) ? config.services.usuarios : 'http://localhost:3002'}`);
    console.log(`Proxying /comentarios -> ${config.services.comentarios || 'http://localhost:3004'}`);
  });
}

process.on('unhandledRejection', (reason) => {
  console.error('🚨 [Gateway Crítico] Rechazo asíncrono no manejado:', reason);
});

process.on('uncaughtException', (error) => {
  console.error('🚨 [Gateway Crítico] Excepción síncrona no capturada:', error.message);
  console.error(error.stack);
});

module.exports = { app, config };

if (require.main === module) {
  startServer();
}

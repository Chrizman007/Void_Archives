/**
 * Void Archives - Configuration
 * Centralized configuration for the API Gateway
 */
require('dotenv').config();

module.exports = {
  port: process.env.PORT || 3000,
  services: {
    articulos: process.env.ARTICULOS_URL || 'http://localhost:3001',
    usuarios: process.env.USUARIOS_URL || 'http://localhost:3002',
    comentarios: process.env.COMENTARIOS_URL || 'http://localhost:3004'
  }
};

require('dotenv').config();

const config = {
  port: process.env.PORT || process.env.PORT_AUTH || 3002,
  mongoUri: process.env.MONGO_URI,
  jwtSecret: process.env.JWT_SECRET,
  jwtExpiresIn: '2h'
};

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Void Archives - Auth API',
      version: '1.0.0',
      description: 'Sistema de Seguridad, Sellos JWT y Usuarios de Void Archives'
    },
    servers: [{ url: `http://localhost:${config.port}` }]
  },
  apis: ['./src/negocios/UsuarioService.js']
};

module.exports = {
  config,
  swaggerOptions
};

require('dotenv').config();

const config = {
  port: process.env.PORT || 3001,
  grpcPort: process.env.GRPC_PORT || 50051,
  mongoUri: process.env.MONGO_URI,
  jwtSecret: process.env.JWT_SECRET,
  viewCooldownMs: 30 * 1000,
  viewCacheMaxAgeMs: 60 * 60 * 1000
};

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Void Archives - Artículos API',
      version: '1.0.0',
      description: 'Documentación interactiva del microservicio de artículos y lore'
    },
    servers: [{ url: `http://localhost:${config.port}` }],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT'
        }
      }
    }
  },
  apis: ['./src/negocios/ArticuloService.js']
};

module.exports = {
  config,
  swaggerOptions
};

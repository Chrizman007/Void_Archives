require('dotenv').config();

const config = {
  port: process.env.PORT || 3004,
  mongoUri: process.env.MONGO_URI,
  jwtSecret: process.env.JWT_SECRET
};

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Void Archives - Comentarios API',
      version: '1.0.0',
      description: 'Documentación interactiva del microservicio de comentarios'
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
  apis: ['./src/negocios/ComentarioService.js']
};

module.exports = {
  config,
  swaggerOptions
};

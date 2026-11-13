const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const swaggerUi = require('swagger-ui-express');
const swaggerJsDoc = require('swagger-jsdoc');

// ── Importación de Capas Separadas (Arquitectura Modular) ───────────────────
const { config, swaggerOptions } = require('../infraestructura/config/Config');
const { UsuarioDTO } = require('../infraestructura/dto/UsuarioDTO');
const { UsuarioRepository } = require('../dominio/repositorio/UsuarioRepositorio');
const {
  CredencialesInvalidasException,
  UsuarioYaExisteException,
  FaltanDatosAuthException,
  manejarExcepcionAuthHTTP
} = require('./middlewares/ErrorHandler');

const router = express.Router();

// ── Documentación Swagger ───────────────────────────────────────────────────
const swaggerDocs = swaggerJsDoc(swaggerOptions);
router.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocs));

// ── Capa de Lógica de Negocio (Service Layer) ──────────────────────────────
const AuthLogicService = {
  registrarUsuario: async (datos) => {
    if (!datos.nombre || !datos.correo || !datos.password) {
      throw new FaltanDatosAuthException(['nombre', 'correo', 'password']);
    }

    const existe = await UsuarioRepository.buscarPorCorreo(datos.correo);
    if (existe) {
      throw new UsuarioYaExisteException(datos.correo);
    }

    const usuarioGuardado = await UsuarioRepository.crear({
      nombre: datos.nombre,
      correo: datos.correo,
      password: datos.password,
      rol: datos.rol ? datos.rol.toLowerCase() : 'lector'
    });

    return new UsuarioDTO(usuarioGuardado);
  },

  iniciarSesion: async (correo, password) => {
    if (!correo || !password) {
      throw new FaltanDatosAuthException(['correo', 'password']);
    }

    const usuario = await UsuarioRepository.buscarPorCorreo(correo);
    if (!usuario) {
      throw new CredencialesInvalidasException();
    }

    const esPasswordValido = await bcrypt.compare(password, usuario.password);
    if (!esPasswordValido) {
      throw new CredencialesInvalidasException();
    }

    const token = jwt.sign(
      { id: usuario._id, nombre: usuario.nombre, rol: usuario.rol },
      config.jwtSecret,
      { expiresIn: config.jwtExpiresIn }
    );

    return {
      usuario: new UsuarioDTO(usuario),
      token: token
    };
  }
};

// ── Rutas REST (Controladores HTTP) ─────────────────────────────────────────

/**
 * @swagger
 * /auth/registro:
 *   post:
 *     summary: Registra un nuevo usuario en Void Archives
 */
router.post('/registro', async (req, res) => {
  try {
    const usuarioDTO = await AuthLogicService.registrarUsuario(req.body);
    res.status(201).json({
      message: 'Usuario creado exitosamente',
      usuario: usuarioDTO
    });
  } catch (error) {
    manejarExcepcionAuthHTTP(error, res);
  }
});

/**
 * @swagger
 * /auth/login:
 *   post:
 *     summary: Inicia sesión y devuelve un Token JWT en el header Authorization
 */
router.post('/login', async (req, res) => {
  try {
    const authData = await AuthLogicService.iniciarSesion(req.body.correo, req.body.password);

    res.setHeader(
      'Authorization',
      `Bearer ${authData.token}`
    );

    res.status(200).json({
      message: 'Login exitoso',
      nombre: authData.usuario.nombre,
      rol: authData.usuario.rol
    });
  } catch (error) {
    manejarExcepcionAuthHTTP(error, res);
  }
});

module.exports = { router, config, AuthLogicService };

// ── Inicialización del Microservicio ────────────────────────────────────────
async function conectarMongoConReintento() {
  try {
    await mongoose.connect(config.mongoUri, {
      serverSelectionTimeoutMS: 10000
    });
    console.log('¡Conectado exitosamente a MongoDB Atlas (Auth)!');
  } catch (error) {
    console.error('⚠️ [Auth] No se pudo conectar a MongoDB Atlas, reintentando en 5s:', error.message);
    setTimeout(conectarMongoConReintento, 5000);
  }
}

async function startAuthServer() {
  mongoose.connection.on('error', (err) => {
    console.error('❌ [MongoDB Atlas - Auth] Error de conexión en la nube:', err.message);
  });

  mongoose.connection.on('disconnected', () => {
    console.warn('⚠️ [MongoDB Atlas - Auth] Conexión perdida. Intentando reestablecer...');
  });

  mongoose.connection.on('reconnected', () => {
    console.log('✨ [MongoDB Atlas - Auth] Conexión reestablecida con éxito.');
  });

  const app = express();
  app.use(express.json());
  app.use(cors());
  app.use('/auth', router);

  app.listen(config.port, () => {
    console.log(`Void Archives Auth Service running on port ${config.port}`);
    console.log(`Swagger docs available at: http://localhost:${config.port}/auth/api-docs`);
  });

  await conectarMongoConReintento();
}

process.on('unhandledRejection', (reason) => {
  console.error('🚨 [Auth Proceso] Rechazo asíncrono no manejado:', reason);
});

process.on('uncaughtException', (error) => {
  console.error('🚨 [Auth Proceso] Excepción síncrona no capturada:', error.message);
  console.error(error.stack);
});

if (require.main === module) {
  startAuthServer();
}

const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const swaggerUi = require('swagger-ui-express');
const swaggerJsDoc = require('swagger-jsdoc');

// ── Importación de Capas Separadas (Arquitectura Modular) ───────────────────
const { config, swaggerOptions } = require('../infraestructura/config/Config');
const { ComentarioDTO } = require('../infraestructura/dto/ComentarioDTO');
const { ComentarioRepository } = require('../dominio/repositorio/ComentarioRepositorio');
const { verificarPermisos } = require('./middlewares/Guard');
const {
  NotFoundException,
  BadRequestException,
  manejarError
} = require('./middlewares/ErrorHandler');

const router = express.Router();

// ── Documentación Swagger ───────────────────────────────────────────────────
const swaggerDocs = swaggerJsDoc(swaggerOptions);
router.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocs));

// ── Capa de Lógica de Negocio (Service Layer) ──────────────────────────────
const ComentarioLogicService = {
  listarComentarios: async (articuloId, limit, skip) => {
    if (!mongoose.Types.ObjectId.isValid(articuloId)) {
      throw new BadRequestException('ID de articulo inválido');
    }
    const comentarios = await ComentarioRepository.listarPorArticulo(articuloId, limit, skip);
    return comentarios.map(c => new ComentarioDTO(c));
  },

  agregarComentario: async (articuloId, autorId, autorNombre, contenido) => {
    if (!mongoose.Types.ObjectId.isValid(articuloId)) {
      throw new BadRequestException('ID de articulo inválido');
    }
    if (!contenido || contenido.trim().length === 0) {
      throw new BadRequestException('Contenido de comentario vacío');
    }
    if (contenido.length > 512) {
      throw new BadRequestException('Contenido de comentario excede el máximo de 512 caracteres');
    }

    const comentario = await ComentarioRepository.crearConSecuencia(
      articuloId,
      autorId,
      autorNombre,
      contenido
    );
    return new ComentarioDTO(comentario);
  },

  eliminarComentario: async (id) => {
    if (!mongoose.Types.ObjectId.isValid(id)) {
      throw new BadRequestException('ID de comentario inválido');
    }

    const eliminado = await ComentarioRepository.eliminarPorId(id);
    if (!eliminado) {
      throw new NotFoundException(id);
    }
    return true;
  }
};

// ── Rutas REST (Controladores HTTP) ─────────────────────────────────────────

/**
 * @swagger
 * /comentarios/{articuloId}:
 *   get:
 *     summary: Lista comentarios de un artículo
 */
router.get('/:articuloId', async (req, res) => {
  try {
    const { articuloId } = req.params;
    const limit = parseInt(req.query.limit) || 100;
    const skip = parseInt(req.query.skip) || 0;
    const comentarios = await ComentarioLogicService.listarComentarios(articuloId, limit, skip);
    res.json({ total: comentarios.length, comentarios });
  } catch (err) {
    manejarError(res, err);
  }
});

/**
 * @swagger
 * /comentarios/{articuloId}:
 *   post:
 *     summary: Agrega un comentario a un artículo (Requiere token)
 */
router.post('/:articuloId', verificarPermisos(['administrador', 'lector', 'desarrollador']), async (req, res) => {
  try {
    const { articuloId } = req.params;
    const autorId = req.user.id || req.user._id || req.user.sub;
    const autorNombre = req.user.nombre || req.user.name || 'Usuario';
    const { contenido } = req.body;

    const comentarioDTO = await ComentarioLogicService.agregarComentario(
      articuloId,
      autorId,
      autorNombre,
      contenido
    );
    res.status(201).json({ message: 'Comentario creado', comentario: comentarioDTO });
  } catch (err) {
    manejarError(res, err);
  }
});

/**
 * @swagger
 * /comentarios/{id}:
 *   delete:
 *     summary: Elimina un comentario (Solo administradores)
 */
router.delete('/:id', verificarPermisos(['administrador']), async (req, res) => {
  try {
    const { id } = req.params;
    await ComentarioLogicService.eliminarComentario(id);
    res.status(200).json({ message: 'Comentario eliminado por un administrador.' });
  } catch (err) {
    manejarError(res, err);
  }
});

module.exports = { router, config, ComentarioLogicService };

// ── Inicialización del Microservicio ────────────────────────────────────────
async function conectarMongoConReintento() {
  try {
    await mongoose.connect(config.mongoUri, {
      serverSelectionTimeoutMS: 10000
    });
    console.log('Conectado a MongoDB Atlas (Comentarios)');
  } catch (err) {
    console.error('⚠️ [Comentarios] No se pudo conectar a MongoDB Atlas, reintentando en 5s:', err.message);
    setTimeout(conectarMongoConReintento, 5000);
  }
}

async function startServer() {
  mongoose.connection.on('error', (err) => {
    console.error('❌ [MongoDB Atlas - Comentarios] Error crítico de conexión en la nube:', err.message);
  });

  mongoose.connection.on('disconnected', () => {
    console.warn('⚠️ [MongoDB Atlas - Comentarios] Conexión interrumpida con el clúster. Reintentando automáticamente...');
  });

  mongoose.connection.on('reconnected', () => {
    console.log('✨ [MongoDB Atlas - Comentarios] ¡Conexión restablecida con éxito!');
  });

  const app = express();
  app.use(express.json());
  app.use(cors());

  app.use('/comentarios', router);

  app.listen(config.port, () => {
    console.log(`Servicio de Comentarios (REST) corriendo en puerto ${config.port}`);
  });

  await conectarMongoConReintento();
}

process.on('unhandledRejection', (reason) => {
  console.error('🚨 [Comentarios Crítico] Rechazo asíncrono no manejado:', reason);
});

process.on('uncaughtException', (error) => {
  console.error('🚨 [Comentarios Crítico] Excepción síncrona no capturada:', error.message);
  console.error(error.stack);
});

if (require.main === module) {
  startServer();
}

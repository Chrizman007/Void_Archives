const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const path = require('path');
const fs = require('fs');
const swaggerUi = require('swagger-ui-express');
const swaggerJsDoc = require('swagger-jsdoc');

// ── Importación de Capas Separadas (Arquitectura Modular) ───────────────────
const { config, swaggerOptions } = require('../infraestructura/config/Config');
const { ArticuloDTO } = require('../infraestructura/dto/ArticuloDTO');
const { CATEGORIAS_VALIDAS, ESTADOS_VALIDOS } = require('../dominio/schemas/ArticuloSchema');
const { ArticuloRepository } = require('../dominio/repositorio/ArticuloRepositorio');
const { verificarPermisos, deriveUserKey } = require('./middlewares/Guard');
const {
  DocumentoNoEncontradoException,
  FormatoIdInvalidoException,
  CamposRequeridosFaltantesException,
  CategoriaInvalidaException,
  EstadoInvalidoException,
  manejarExcepcionHTTP,
  globalErrorMiddleware
} = require('./middlewares/ErrorHandler');
const { iniciarServidorGrpc } = require('../../grpc/GrpcServer');

const router = express.Router();

// ── Documentación Swagger ───────────────────────────────────────────────────
const swaggerDocs = swaggerJsDoc(swaggerOptions);
router.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocs));

// ── Control de Cooldown de Vistas ───────────────────────────────────────────
const viewCooldownStore = new Map();

async function handleArticleView(req, articleId) {
  const userKey = deriveUserKey(req);
  const now = Date.now();

  if (!viewCooldownStore.has(articleId)) {
    viewCooldownStore.set(articleId, new Map());
  }

  const userMap = viewCooldownStore.get(articleId);
  const lastViewTime = userMap.get(userKey) || 0;

  if (now - lastViewTime > config.viewCooldownMs) {
    userMap.set(userKey, now);
    await ArticuloRepository.incrementarVistas(articleId);
    return true;
  }

  return false;
}

setInterval(() => {
  const now = Date.now();
  for (const [articleId, userMap] of viewCooldownStore.entries()) {
    for (const [userKey, ts] of userMap.entries()) {
      if (now - ts > config.viewCacheMaxAgeMs) userMap.delete(userKey);
    }
    if (userMap.size === 0) viewCooldownStore.delete(articleId);
  }
}, 60 * 1000).unref();

// ── Capa de Lógica de Negocio (Service Layer) ──────────────────────────────
const ArticuloLogicService = {
  obtenerArticulos: async () => {
    const articulos = await ArticuloRepository.obtenerTodos();
    return articulos.map(art => new ArticuloDTO(art));
  },

  obtenerArticuloPorId: async (id) => {
    if (!mongoose.Types.ObjectId.isValid(id)) {
      throw new FormatoIdInvalidoException();
    }
    const articulo = await ArticuloRepository.obtenerPorId(id);
    if (!articulo) {
      throw new DocumentoNoEncontradoException(id);
    }
    return new ArticuloDTO(articulo);
  },

  crearArticulo: async (datos) => {
    const requiredFields = ['titulo', 'categoria', 'descripcion', 'contenido', 'autorId', 'autor'];
    const missingFields = requiredFields.filter(field => !datos[field]);

    if (missingFields.length > 0) {
      throw new CamposRequeridosFaltantesException(missingFields);
    }

    if (datos.categoria) {
      datos.categoria = datos.categoria.charAt(0).toUpperCase() + datos.categoria.slice(1).toLowerCase();
    }

    if (!CATEGORIAS_VALIDAS.includes(datos.categoria)) {
      throw new CategoriaInvalidaException(datos.categoria);
    }

    const articuloGuardado = await ArticuloRepository.crear(datos);
    return new ArticuloDTO(articuloGuardado);
  },

  actualizarArticulo: async (id, datos) => {
    if (!mongoose.Types.ObjectId.isValid(id)) {
      throw new FormatoIdInvalidoException();
    }

    const articuloExistente = await ArticuloRepository.obtenerPorId(id);
    if (!articuloExistente) {
      throw new DocumentoNoEncontradoException(id);
    }

    if (datos.categoria) {
      datos.categoria = datos.categoria.charAt(0).toUpperCase() + datos.categoria.slice(1).toLowerCase();
    }

    if (datos.estado && !ESTADOS_VALIDOS.includes(datos.estado)) {
      throw new EstadoInvalidoException(datos.estado);
    }

    const articuloActualizado = await ArticuloRepository.actualizar(id, datos);
    return new ArticuloDTO(articuloActualizado);
  }
};

// ── Rutas REST (Controladores HTTP) ─────────────────────────────────────────

/**
 * @swagger
 * /articulos:
 *   get:
 *     summary: Obtiene todos los articulos de la Wiki (Público)
 *     responses:
 *       200:
 *         description: Exito. Devuelve el total y el arreglo de articulos.
 */
router.get('/', async (req, res) => {
  try {
    const articulosDTO = await ArticuloLogicService.obtenerArticulos();
    res.json({
      total: articulosDTO.length,
      articulos: articulosDTO
    });
  } catch (error) {
    manejarExcepcionHTTP(error, res);
  }
});

/**
 * @swagger
 * /articulos/{id}:
 *   get:
 *     summary: Obtiene un articulo por su ID (Público)
 */
router.get('/:id', async (req, res) => {
  try {
    const articuloDTO = await ArticuloLogicService.obtenerArticuloPorId(req.params.id);

    try {
      const incremented = await handleArticleView(req, req.params.id);
      if (incremented) {
        articuloDTO.vistas = (articuloDTO.vistas || 0) + 1;
      }
    } catch (viewErr) {
      console.warn('No se pudo procesar el contador de vistas:', viewErr.message);
    }

    res.json(articuloDTO);
  } catch (error) {
    manejarExcepcionHTTP(error, res);
  }
});

/**
 * @swagger
 * /articulos:
 *   post:
 *     summary: Crea un nuevo articulo en la base de datos (Protegido - Requiere Token)
 */
router.post('/', verificarPermisos(['administrador']), async (req, res) => {
  try {
    req.body.autorId = req.user.id || req.user._id || req.user.sub;
    req.body.autor = req.user.nombre || req.user.name || 'Administrador';

    const articuloDTO = await ArticuloLogicService.crearArticulo(req.body);
    res.status(201).json({
      message: 'Articulo creado exitosamente',
      articulo: articuloDTO
    });
  } catch (error) {
    manejarExcepcionHTTP(error, res);
  }
});

/**
 * @swagger
 * /articulos/{id}:
 *   put:
 *     summary: Actualiza un articulo existente (Protegido - Requiere Token)
 */
router.put('/:id', verificarPermisos(['administrador']), async (req, res) => {
  try {
    const articuloDTO = await ArticuloLogicService.actualizarArticulo(req.params.id, req.body);
    res.status(200).json({
      message: 'Articulo actualizado exitosamente',
      articulo: articuloDTO
    });
  } catch (error) {
    manejarExcepcionHTTP(error, res);
  }
});

/**
 * @swagger
 * /articulos/{id}/imagen:
 *   post:
 *     summary: Sube y vincula una imagen desde el cliente móvil a un artículo (Protegido - Requiere Token)
 */
router.post('/:id/imagen', verificarPermisos(['administrador']), async (req, res) => {
  try {
    const { nombreArchivo, imagenBase64 } = req.body;
    if (!nombreArchivo || !imagenBase64) {
      return res.status(400).json({
        error: 'CamposRequeridosFaltantesException',
        message: 'Se requiere nombreArchivo e imagenBase64.'
      });
    }

    const directorioImagenes = path.join(__dirname, '../../public/imagenes');
    if (!fs.existsSync(directorioImagenes)) {
      fs.mkdirSync(directorioImagenes, { recursive: true });
    }

    const nombreSeguro = nombreArchivo.replace(/[^a-zA-Z0-9.\-_]/g, '_');
    const rutaDestino = path.join(directorioImagenes, nombreSeguro);
    const bufferImagen = Buffer.from(imagenBase64, 'base64');

    fs.writeFileSync(rutaDestino, bufferImagen);
    const articuloActualizado = await ArticuloRepository.actualizarImagen(req.params.id, nombreSeguro);

    res.status(200).json({
      message: 'Reliquia multimedia guardada exitosamente',
      articulo: articuloActualizado ? new ArticuloDTO(articuloActualizado) : null
    });
  } catch (error) {
    manejarExcepcionHTTP(error, res);
  }
});

module.exports = { router, config, ArticuloLogicService };

// ── Inicialización del Microservicio (Express + MongoDB + gRPC) ─────────────
async function conectarMongoConReintento() {
  try {
    await mongoose.connect(config.mongoUri, {
      serverSelectionTimeoutMS: 10000
    });
    console.log('¡Conectado exitosamente a MongoDB Atlas (Artículos)!');
  } catch (error) {
    console.error('⚠️ [Artículos] No se pudo conectar a MongoDB Atlas, reintentando en 5s:', error.message);
    setTimeout(conectarMongoConReintento, 5000);
  }
}

async function startServer() {
  mongoose.connection.on('error', (err) => {
    console.error('❌ [MongoDB Atlas] Error crítico de conexión en la nube:', err.message);
  });

  mongoose.connection.on('disconnected', () => {
    console.warn('⚠️ [MongoDB Atlas] Se perdió la conexión con el clúster. Reintentando...');
  });

  mongoose.connection.on('reconnected', () => {
    console.log('✨ [MongoDB Atlas] Conexión reestablecida con éxito.');
  });

  const app = express();
  app.use(express.json({ limit: '25mb' }));
  app.use(cors());

  const directorioImagenes = path.join(__dirname, '../../public/imagenes');
  if (!fs.existsSync(directorioImagenes)) {
    fs.mkdirSync(directorioImagenes, { recursive: true });
  }
  app.use('/articulos/public/imagenes', express.static(directorioImagenes));

  app.use('/articulos', router);
  app.use(globalErrorMiddleware);

  app.listen(config.port, () => {
    console.log(`🌐 Void Archives Article Service (REST) running on port ${config.port}`);
  });

  iniciarServidorGrpc(config.grpcPort);
  await conectarMongoConReintento();
}

process.on('unhandledRejection', (reason) => {
  console.error('🚨 [CRÍTICO] Rechazo no manejado en una Promesa:', reason);
});

process.on('uncaughtException', (error) => {
  console.error('🚨 [CRÍTICO] Excepción no capturada en el hilo principal:', error.message);
  console.error(error.stack);
});

if (require.main === module) {
  startServer();
}

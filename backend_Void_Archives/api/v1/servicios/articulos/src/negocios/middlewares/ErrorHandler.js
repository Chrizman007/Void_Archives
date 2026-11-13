class VoidArchivesException extends Error {
  constructor(mensaje, statusCode) {
    super(mensaje);
    this.name = this.constructor.name;
    this.statusCode = statusCode;
  }
}

class DocumentoNoEncontradoException extends VoidArchivesException {
  constructor(id) {
    super(`No se encontró ningún artículo con el ID: ${id}`, 404);
  }
}

class FormatoIdInvalidoException extends VoidArchivesException {
  constructor() {
    super('El formato del ID no es valido', 400);
  }
}

class CamposRequeridosFaltantesException extends VoidArchivesException {
  constructor(campos) {
    super(`Los siguientes campos son requeridos: ${campos.join(', ')}`, 400);
  }
}

class CategoriaInvalidaException extends VoidArchivesException {
  constructor(categoria) {
    super(`La categoría '${categoria}' no es válida.`, 400);
  }
}

class EstadoInvalidoException extends VoidArchivesException {
  constructor(estado) {
    super(`El estado '${estado}' no es válido.`, 400);
  }
}

class RecursoDuplicadoException extends VoidArchivesException {
  constructor(mensaje) {
    super(mensaje, 409);
  }
}

class ErrorBaseDeDatosException extends VoidArchivesException {
  constructor(detalle) {
    super(`Error en la operación de base de datos: ${detalle}`, 500);
  }
}

function manejarExcepcionHTTP(error, res) {
  if (error instanceof VoidArchivesException) {
    console.error(`🚨 ERROR RECHAZADO POR MONGO: ${error.message}`);
    return res.status(error.statusCode).json({
      error: error.name,
      message: error.message
    });
  }
  console.error('Excepcion no controlada:', error);
  return res.status(500).json({
    error: 'ErrorCriticoDelServidor',
    message: 'Se produjo un error inesperado en la ejecucion.'
  });
}

function globalErrorMiddleware(err, req, res, next) {
  console.error(err);
  res.status(err.statusCode || 500).json({
    error: err.name || 'InternalServerError',
    message: err.message || 'Error interno'
  });
}

module.exports = {
  VoidArchivesException,
  DocumentoNoEncontradoException,
  FormatoIdInvalidoException,
  CamposRequeridosFaltantesException,
  CategoriaInvalidaException,
  EstadoInvalidoException,
  RecursoDuplicadoException,
  ErrorBaseDeDatosException,
  manejarExcepcionHTTP,
  globalErrorMiddleware
};

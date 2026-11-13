class ComentarioException extends Error {
  constructor(message, status) {
    super(message);
    this.name = this.constructor.name;
    this.statusCode = status || 500;
  }
}

class NotFoundException extends ComentarioException {
  constructor(id) {
    super(`No se encontró recurso con id: ${id}`, 404);
  }
}

class BadRequestException extends ComentarioException {
  constructor(msg) {
    super(msg || 'Solicitud inválida', 400);
  }
}

function manejarError(res, err) {
  if (err instanceof ComentarioException) {
    return res.status(err.statusCode || 500).json({
      error: err.name,
      message: err.message
    });
  }
  console.error('Error no controlado comentarios:', err);
  return res.status(500).json({
    error: 'ErrorInterno',
    message: 'Se produjo un error interno'
  });
}

module.exports = {
  ComentarioException,
  NotFoundException,
  BadRequestException,
  manejarError
};

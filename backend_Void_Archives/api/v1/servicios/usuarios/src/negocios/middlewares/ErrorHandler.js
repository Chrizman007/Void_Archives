class VoidArchivesAuthException extends Error {
  constructor(mensaje, statusCode) {
    super(mensaje);
    this.name = this.constructor.name;
    this.statusCode = statusCode;
  }
}

class CredencialesInvalidasException extends VoidArchivesAuthException {
  constructor() {
    super('El correo o la contraseña son incorrectos.', 401);
  }
}

class UsuarioYaExisteException extends VoidArchivesAuthException {
  constructor(correo) {
    super(`Ya existe un usuario registrado con el correo: ${correo}`, 400);
  }
}

class FaltanDatosAuthException extends VoidArchivesAuthException {
  constructor(campos) {
    super(`Faltan campos obligatorios para el registro/login: ${campos.join(', ')}`, 400);
  }
}

function manejarExcepcionAuthHTTP(error, res) {
  if (error instanceof VoidArchivesAuthException) {
    return res.status(error.statusCode).json({
      error: error.name,
      message: error.message
    });
  }

  if (error.name === 'ValidationError') {
    console.error('🚨 [Auth Mongoose] Error de validación detectado:', error.message);
    return res.status(400).json({
      error: 'FormatoDatosInvalido',
      message: 'Los datos enviados no cumplen con las reglas de validación del sistema (ej. rol no permitido).'
    });
  }

  if (error.code === 11000) {
    console.error('🚨 [Auth Mongo] Intento de registro duplicado concurrente:', error.message);
    return res.status(409).json({
      error: 'UsuarioDuplicado',
      message: 'El correo electrónico ya ha sido registrado por otro usuario.'
    });
  }

  console.error('Excepcion no controlada en Auth:', error);
  return res.status(500).json({
    error: 'ErrorCriticoDelServidor',
    message: 'Error interno en autenticación.'
  });
}

module.exports = {
  VoidArchivesAuthException,
  CredencialesInvalidasException,
  UsuarioYaExisteException,
  FaltanDatosAuthException,
  manejarExcepcionAuthHTTP
};

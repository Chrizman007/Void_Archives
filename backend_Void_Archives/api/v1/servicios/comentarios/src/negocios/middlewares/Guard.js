const jwt = require('jsonwebtoken');
const { config } = require('../../infraestructura/config/Config');

const verificarPermisos = (rolesPermitidos) => {
  return (req, res, next) => {
    const authHeader = req.headers['authorization'] || req.headers['Authorization'];
    if (!authHeader) {
      return res.status(403).json({
        error: 'NoTokenProvided',
        message: 'No se proporcionó un token de seguridad.'
      });
    }

    try {
      let token = authHeader;
      if (authHeader.toLowerCase().startsWith('bearer ')) {
        token = authHeader.split(' ')[1];
      }

      const decoded = jwt.verify(token, config.jwtSecret);
      req.user = decoded;

      if (rolesPermitidos && !rolesPermitidos.includes(decoded.rol)) {
        return res.status(403).json({
          error: 'UnauthorizedRole',
          message: `Acceso denegado: Requiere rol [${rolesPermitidos.join(', ')}].`
        });
      }
      next();
    } catch (err) {
      if (err.name === 'TokenExpiredError') {
        return res.status(401).json({
          error: 'TokenExpired',
          message: 'La sesión ha expirado.'
        });
      }
      if (err.name === 'JsonWebTokenError') {
        return res.status(401).json({
          error: 'InvalidToken',
          message: 'El token proporcionado es inválido.'
        });
      }
      return res.status(401).json({
        error: 'AuthError',
        message: 'Fallo la autenticación del usuario.'
      });
    }
  };
};

module.exports = {
  verificarPermisos
};

const jwt = require('jsonwebtoken');
const { config } = require('../../infraestructura/config/Config');

const verificarPermisos = (rolesPermitidos) => {
  return (req, res, next) => {
    const authHeader = req.headers['authorization'] || req.headers['Authorization'];

    if (!authHeader) {
      return res.status(403).json({ message: 'No se proporcionó un token de seguridad.' });
    }

    try {
      let tokenLimpio = authHeader;

      if (authHeader.toLowerCase().startsWith('bearer ')) {
        const partes = authHeader.split(' ');
        tokenLimpio = partes[1];
      }

      if (!tokenLimpio) {
        return res.status(401).json({ message: 'El token proporcionado está vacío o mal formado.' });
      }

      const decoded = jwt.verify(tokenLimpio, config.jwtSecret);
      req.user = decoded;

      if (rolesPermitidos && !rolesPermitidos.includes(decoded.rol)) {
        return res.status(403).json({
          message: `Acceso denegado: Requiere rol [${rolesPermitidos.join(', ')}]. Tu rol es: ${decoded.rol}`
        });
      }

      next();
    } catch (error) {
      if (error.name === 'TokenExpiredError') {
        return res.status(401).json({
          error: 'TokenExpired',
          message: 'La sesión ha expirado.'
        });
      }

      if (error.name === 'JsonWebTokenError') {
        return res.status(401).json({
          error: 'InvalidToken',
          message: 'El token es inválido.'
        });
      }

      return res.status(500).json({
        error: 'AuthError',
        message: 'Error al validar el token.'
      });
    }
  };
};

function deriveUserKey(req) {
  try {
    const authHeader = req.headers['authorization'] || req.headers['Authorization'];
    if (authHeader && authHeader.toLowerCase().startsWith('bearer ')) {
      const token = authHeader.split(' ')[1];
      if (token) {
        const decoded = jwt.verify(token, config.jwtSecret);
        if (decoded && (decoded.id || decoded._id || decoded.sub)) {
          return `user:${decoded.id || decoded._id || decoded.sub}`;
        }
      }
    }
  } catch (e) {
    // Si el token no es válido en ruta pública, usamos la IP como clave de vista
  }
  const ip = req.ip || (req.connection && req.connection.remoteAddress) || 'anonymous';
  return `ip:${ip}`;
}

module.exports = {
  verificarPermisos,
  deriveUserKey
};

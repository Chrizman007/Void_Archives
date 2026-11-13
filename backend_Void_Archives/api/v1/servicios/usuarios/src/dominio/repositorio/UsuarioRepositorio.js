const { Usuario } = require('../schemas/UsuarioSchema');

const UsuarioRepository = {
  buscarPorCorreo: async (correo) => {
    return await Usuario.findOne({ correo });
  },

  crear: async (datosUsuario) => {
    const nuevoUsuario = new Usuario(datosUsuario);
    return await nuevoUsuario.save();
  }
};

module.exports = {
  UsuarioRepository
};

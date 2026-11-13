const { Articulo } = require('../schemas/ArticuloSchema');
const {
  ErrorBaseDeDatosException,
  RecursoDuplicadoException
} = require('../../negocios/middlewares/ErrorHandler');

const ArticuloRepository = {
  obtenerTodos: async () => {
    try {
      return await Articulo.find();
    } catch (error) {
      throw new ErrorBaseDeDatosException('No se pudieron obtener los artículos.');
    }
  },

  obtenerPorId: async (id) => {
    try {
      return await Articulo.findById(id);
    } catch (error) {
      throw new ErrorBaseDeDatosException('No se pudo recuperar el artículo solicitado.');
    }
  },

  crear: async (datosArticulo) => {
    try {
      const nuevoArticulo = new Articulo(datosArticulo);
      return await nuevoArticulo.save();
    } catch (error) {
      if (error.code === 11000) {
        throw new RecursoDuplicadoException('Ya existe un artículo con ese título.');
      }
      throw new ErrorBaseDeDatosException('No se pudo guardar el artículo.');
    }
  },

  actualizar: async (id, datosActualizados) => {
    try {
      return await Articulo.findByIdAndUpdate(id, datosActualizados, {
        new: true,
        runValidators: true
      });
    } catch (error) {
      if (error.code === 11000) {
        throw new RecursoDuplicadoException('Ya existe un artículo con ese título.');
      }
      throw new ErrorBaseDeDatosException('No se pudo actualizar el artículo.');
    }
  },

  incrementarVistas: async (id) => {
    try {
      return await Articulo.findByIdAndUpdate(id, { $inc: { vistas: 1 } }, { new: true });
    } catch (error) {
      throw new ErrorBaseDeDatosException('No se pudieron actualizar las vistas en el artículo solicitado.');
    }
  },

  actualizarImagen: async (id, nombreImagen) => {
    try {
      return await Articulo.findByIdAndUpdate(id, { imagen: nombreImagen }, { new: true });
    } catch (error) {
      throw new ErrorBaseDeDatosException('No se pudo vincular la imagen al artículo.');
    }
  }
};

module.exports = {
  ArticuloRepository
};

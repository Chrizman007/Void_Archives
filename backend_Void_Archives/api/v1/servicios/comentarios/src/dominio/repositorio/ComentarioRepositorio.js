const mongoose = require('mongoose');
const { CommentCounter, Comentario } = require('../schemas/ComentarioSchema');
const { ComentarioException } = require('../../negocios/middlewares/ErrorHandler');

const ComentarioRepository = {
  listarPorArticulo: async (articuloId, limit = 100, skip = 0) => {
    try {
      return await Comentario.find({ articuloId })
        .sort({ orden: 1 })
        .skip(skip)
        .limit(limit);
    } catch (err) {
      throw new ComentarioException('Error al listar comentarios: ' + err.message);
    }
  },

  crearConSecuencia: async (articuloId, autorId, autorNombre, contenido) => {
    const session = await mongoose.startSession();
    session.startTransaction();
    try {
      const counter = await CommentCounter.findOneAndUpdate(
        { articleId: articuloId },
        { $inc: { seq: 1 } },
        { new: true, upsert: true, session }
      );

      const orden = counter.seq;

      const comentario = new Comentario({
        articuloId,
        autorId,
        autorNombre,
        contenido,
        orden
      });
      await comentario.save({ session });

      await session.commitTransaction();
      session.endSession();

      return comentario;
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw new ComentarioException('Error al crear comentario: ' + err.message);
    }
  },

  eliminarPorId: async (id) => {
    try {
      return await Comentario.findByIdAndDelete(id);
    } catch (err) {
      throw new ComentarioException('Error en base de datos al eliminar: ' + err.message);
    }
  }
};

module.exports = {
  ComentarioRepository
};

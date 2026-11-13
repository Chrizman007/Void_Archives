const mongoose = require('mongoose');

const commentCounterSchema = new mongoose.Schema({
  articleId: {
    type: mongoose.Schema.Types.ObjectId,
    required: true,
    unique: true
  },
  seq: {
    type: Number,
    default: 0
  }
});

const comentarioSchema = new mongoose.Schema({
  articuloId: {
    type: mongoose.Schema.Types.ObjectId,
    required: true,
    index: true
  },
  autorId: {
    type: mongoose.Schema.Types.ObjectId,
    required: true
  },
  autorNombre: {
    type: String,
    required: true
  },
  contenido: {
    type: String,
    required: true
  },
  orden: {
    type: Number,
    required: true
  }
}, {
  timestamps: true
});

const CommentCounter = mongoose.model('CommentCounter', commentCounterSchema);
const Comentario = mongoose.model('Comentario', comentarioSchema);

module.exports = {
  CommentCounter,
  Comentario
};

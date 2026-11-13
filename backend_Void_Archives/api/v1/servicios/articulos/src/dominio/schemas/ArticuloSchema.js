const mongoose = require('mongoose');

const CATEGORIAS_VALIDAS = ['Lore', 'Items', 'Enemigos', 'Ubicaciones', 'Personajes'];
const ESTADOS_VALIDOS = ['EnBorrador', 'EnRevision', 'Publicado', 'Archivado'];

const articuloSchema = new mongoose.Schema({
  titulo: {
    type: String,
    required: [true, 'El título del artículo es obligatorio'],
    unique: true,
    trim: true
  },
  descripcion: {
    type: String,
    required: [true, 'La descripción corta es obligatoria'],
    trim: true
  },
  contenido: {
    type: String,
    required: [true, 'El contenido del artículo es obligatorio']
  },
  categoria: {
    type: String,
    enum: CATEGORIAS_VALIDAS,
    required: [true, 'La categoría es obligatoria']
  },
  estado: {
    type: String,
    enum: ESTADOS_VALIDOS,
    default: 'EnBorrador'
  },
  vistas: {
    type: Number,
    default: 0
  },
  imagen: {
    type: String,
    default: ''
  },
  autorId: {
    type: mongoose.Schema.Types.ObjectId,
    required: [true, 'El ID del autor es obligatorio']
  },
  autor: {
    type: String,
    required: true,
    default: 'Desconocido'
  }
}, {
  timestamps: true
});

const Articulo = mongoose.model('Articulo', articuloSchema);

module.exports = {
  Articulo,
  CATEGORIAS_VALIDAS,
  ESTADOS_VALIDOS
};

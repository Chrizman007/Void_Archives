const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const ROLES_VALIDOS = ['lector', 'administrador', 'desarrollador'];

const usuarioSchema = new mongoose.Schema({
  nombre: {
    type: String,
    required: true,
    trim: true
  },
  correo: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  password: {
    type: String,
    required: true
  },
  rol: {
    type: String,
    enum: ROLES_VALIDOS,
    default: 'lector'
  },
  fechaRegistro: {
    type: Date,
    default: Date.now
  }
});

usuarioSchema.pre('save', async function () {
  if (!this.isModified('password')) return;

  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
});

const Usuario = mongoose.model('Usuario', usuarioSchema);

module.exports = {
  Usuario,
  ROLES_VALIDOS
};

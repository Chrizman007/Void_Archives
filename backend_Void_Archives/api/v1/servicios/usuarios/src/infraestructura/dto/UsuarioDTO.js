class UsuarioDTO {
  constructor(modeloMongoose) {
    this.id = modeloMongoose._id;
    this.nombre = modeloMongoose.nombre;
    this.correo = modeloMongoose.correo;
    this.rol = modeloMongoose.rol;
  }
}

module.exports = {
  UsuarioDTO
};

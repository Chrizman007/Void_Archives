class ArticuloDTO {
  constructor(modeloMongoose) {
    this.id = modeloMongoose._id;
    this.titulo = modeloMongoose.titulo;
    this.categoria = modeloMongoose.categoria;
    this.descripcion = modeloMongoose.descripcion;
    this.contenido = modeloMongoose.contenido;
    this.estado = modeloMongoose.estado;
    this.vistas = modeloMongoose.vistas;
    this.imagen = modeloMongoose.imagen || '';
    this.autorId = modeloMongoose.autorId;
    this.autor = modeloMongoose.autor || 'Desconocido';
    this.fechaCreacion = modeloMongoose.createdAt;
    this.fechaActualizacion = modeloMongoose.updatedAt;
  }
}

module.exports = {
  ArticuloDTO
};

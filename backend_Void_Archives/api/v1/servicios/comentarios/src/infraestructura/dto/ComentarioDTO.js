class ComentarioDTO {
  constructor(model) {
    this.id = model._id;
    this.articuloId = model.articuloId;
    this.autorId = model.autorId;
    this.autorNombre = model.autorNombre;
    this.contenido = model.contenido;
    this.orden = model.orden;
    this.fechaCreacion = model.createdAt;
    this.fechaActualizacion = model.updatedAt;
  }
}

module.exports = {
  ComentarioDTO
};

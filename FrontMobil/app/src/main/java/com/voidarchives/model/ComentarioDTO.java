package com.voidarchives.model;

import com.google.gson.annotations.SerializedName;

public class ComentarioDTO {

    @SerializedName(value = "id", alternate = {"_id"})
    private String id;
    private String articuloId;
    private String autorId;
    private String autorNombre;
    private String contenido;
    private int orden;
    private String fechaCreacion;
    private String fechaActualizacion;

    public ComentarioDTO() {}

    public ComentarioDTO(String autorId, String contenido) {
        this.autorId = autorId;
        this.contenido = contenido;
    }

    public String getId() { return id; }
    public String getArticuloId() { return articuloId; }
    public String getAutorId() { return autorId; }
    public String getAutorNombre() { return autorNombre; }
    public String getContenido() { return contenido; }
    public int getOrden() { return orden; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getFechaActualizacion() { return fechaActualizacion; }

    public void setId(String id) { this.id = id; }
    public void setArticuloId(String articuloId) { this.articuloId = articuloId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setOrden(int orden) { this.orden = orden; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaActualizacion(String fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

package com.voidarchives.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ArticuloDTO implements Serializable {

    @SerializedName(value = "id", alternate = {"_id"})
    private String id;
    private String titulo;
    private String categoria;
    private String descripcion;
    private String contenido;
    private String estado;
    private String imagen;
    private int vistas;
    private String fechaCreacion;
    private String autor;

    public ArticuloDTO() {}

    public ArticuloDTO(String titulo, String categoria, String descripcion, String contenido, String estado, String autor) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.contenido = contenido;
        this.estado = estado;
        this.autor = autor;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getContenido() { return contenido; }
    public String getEstado() { return estado; }
    public String getImagen() { return imagen; }
    public int getVistas() { return vistas; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getAutor() { return autor; }

    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setVistas(int vistas) { this.vistas = vistas; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setAutor(String autor) { this.autor = autor; }
}

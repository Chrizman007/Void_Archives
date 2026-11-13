package com.voidarchives.model;

public class ArticuloRequest {
    private String titulo;
    private String categoria;
    private String descripcion;
    private String contenido;
    private String imagen;
    private String estado;

    public ArticuloRequest(String titulo, String categoria, String descripcion, String contenido, String imagen) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.contenido = contenido;
        this.imagen = imagen;
    }

    public ArticuloRequest(String titulo, String categoria, String descripcion, String contenido, String imagen, String estado) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.contenido = contenido;
        this.imagen = imagen;
        this.estado = estado;
    }

    public String getTitulo() { return titulo; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getContenido() { return contenido; }
    public String getImagen() { return imagen; }
    public String getEstado() { return estado; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setEstado(String estado) { this.estado = estado; }
}

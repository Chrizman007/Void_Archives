package com.voidarchives.model;

public class ComentarioRequest {
    private String contenido;

    public ComentarioRequest() {}

    public ComentarioRequest(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}

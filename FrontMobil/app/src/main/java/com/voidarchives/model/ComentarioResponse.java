package com.voidarchives.model;

public class ComentarioResponse {
    private String message;
    private ComentarioDTO comentario;

    public ComentarioResponse() {}

    public String getMessage() { return message; }
    public ComentarioDTO getComentario() { return comentario; }

    public void setMessage(String message) { this.message = message; }
    public void setComentario(ComentarioDTO comentario) { this.comentario = comentario; }
}

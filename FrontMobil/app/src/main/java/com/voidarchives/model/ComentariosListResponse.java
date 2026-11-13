package com.voidarchives.model;

import java.util.List;

public class ComentariosListResponse {
    private int total;
    private List<ComentarioDTO> comentarios;

    public ComentariosListResponse() {}

    public int getTotal() { return total; }
    public List<ComentarioDTO> getComentarios() { return comentarios; }

    public void setTotal(int total) { this.total = total; }
    public void setComentarios(List<ComentarioDTO> comentarios) { this.comentarios = comentarios; }
}

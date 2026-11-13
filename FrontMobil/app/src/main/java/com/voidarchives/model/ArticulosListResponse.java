package com.voidarchives.model;

import java.util.List;

public class ArticulosListResponse {
    private int total;
    private List<ArticuloDTO> articulos;

    public int getTotal() { return total; }
    public List<ArticuloDTO> getArticulos() { return articulos; }
}

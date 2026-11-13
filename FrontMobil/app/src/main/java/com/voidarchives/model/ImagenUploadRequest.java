package com.voidarchives.model;

public class ImagenUploadRequest {
    private String nombreArchivo;
    private String imagenBase64;

    public ImagenUploadRequest(String nombreArchivo, String imagenBase64) {
        this.nombreArchivo = nombreArchivo;
        this.imagenBase64 = imagenBase64;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }
}

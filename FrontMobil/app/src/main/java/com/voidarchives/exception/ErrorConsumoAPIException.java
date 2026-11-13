package com.voidarchives.exception;

public class ErrorConsumoAPIException extends Exception {
    private final int codigoRespuesta;
    private final String errorBackend;

    public ErrorConsumoAPIException(String mensaje, int codigoRespuesta, String errorBackend) {
        super(mensaje);
        this.codigoRespuesta = codigoRespuesta;
        this.errorBackend = errorBackend;
    }

    public int getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public String getErrorBackend() {
        return errorBackend;
    }
}

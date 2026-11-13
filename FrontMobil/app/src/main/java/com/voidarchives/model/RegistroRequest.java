package com.voidarchives.model;

public class RegistroRequest {
    private String nombre;
    private String correo;
    private String password;

    public RegistroRequest(String nombre, String correo, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
    }

    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPassword() { return password; }
}

package com.voidarchives.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;

public class SessionManager {

    private static final String PREF_NAME = "VoidArchivesSession";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_NOMBRE = "nombre_usuario";
    private static final String KEY_ROL = "rol_usuario";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(String token, String nombre, String rol) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_NOMBRE, nombre != null ? nombre : "Vasija Sin Nombre")
                .putString(KEY_ROL, rol != null ? rol.toLowerCase() : "lector")
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getBearerToken() {
        String token = getToken();
        if (token == null || token.isEmpty()) return null;
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "Vasija Sin Nombre");
    }

    public String getRol() {
        return prefs.getString(KEY_ROL, "lector");
    }

    public boolean tieneSesionActiva() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    public String extraerIdUsuarioDesdeToken() {
        String token = getToken();
        if (token == null || token.isEmpty()) return "";
        try {
            String[] partes = token.split("\\.");
            if (partes.length < 2) return "";
            byte[] decodedBytes = Base64.decode(partes[1], Base64.URL_SAFE | Base64.NO_WRAP);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);
            JsonObject json = new Gson().fromJson(payload, JsonObject.class);
            if (json != null) {
                if (json.has("id")) return json.get("id").getAsString();
                if (json.has("_id")) return json.get("_id").getAsString();
                if (json.has("sub")) return json.get("sub").getAsString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}

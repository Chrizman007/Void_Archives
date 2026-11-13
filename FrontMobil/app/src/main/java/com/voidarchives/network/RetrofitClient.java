package com.voidarchives.network;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.voidarchives.exception.ErrorConsumoAPIException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 1. Por cable USB (adb reverse tcp:3000 tcp:3000)
    private static final String HOST_USB = "127.0.0.1";
    // 2. Respaldo automático por Wi-Fi local de tu PC
    private static final String HOST_WIFI = "192.168.1.51";
    // 3. Respaldo automático por Hotspot de Windows
    private static final String HOST_HOTSPOT = "192.168.137.1";

    private static final String IP_EMULADOR = "http://10.0.2.2:3000/";
    private static final String IP_PC_PRINCIPAL = "http://" + HOST_USB + ":3000/";

    public static final String BASE_URL = esEmulador() ? IP_EMULADOR : IP_PC_PRINCIPAL;
    private static volatile String hostActivo = esEmulador() ? "10.0.2.2" : HOST_USB;
    private static Retrofit retrofit = null;

    private static boolean esEmulador() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || Build.PRODUCT.contains("sdk");
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            Interceptor failoverInterceptor = chain -> {
                Request original = chain.request();
                if (esEmulador()) {
                    return chain.proceed(original);
                }

                String[] candidatos = new String[]{HOST_USB, HOST_WIFI, HOST_HOTSPOT};
                IOException ultimoError = null;

                for (String host : candidatos) {
                    HttpUrl nuevaUrl = original.url().newBuilder()
                            .host(host)
                            .port(3000)
                            .build();
                    Request intento = original.newBuilder().url(nuevaUrl).build();
                    try {
                        okhttp3.Response resp = chain.proceed(intento);
                        hostActivo = host;
                        return resp;
                    } catch (ConnectException e) {
                        ultimoError = e;
                    } catch (IOException e) {
                        ultimoError = e;
                    }
                }
                if (ultimoError != null) {
                    throw ultimoError;
                }
                return chain.proceed(original);
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(failoverInterceptor)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Construye la URL completa de la imagen para Glide usando el host activo.
     */
    public static String getImagenUrl(String imagen) {
        if (imagen == null || imagen.trim().isEmpty() || imagen.equalsIgnoreCase("default.png")) {
            return null;
        }
        String limpia = imagen.trim();
        if (limpia.startsWith("http://") || limpia.startsWith("https://")) {
            return limpia;
        }
        if (limpia.startsWith("/")) {
            limpia = limpia.substring(1);
        }
        return "http://" + hostActivo + ":3000/articulos/public/imagenes/" + limpia;
    }

    public static <T> void procesarRespuestaError(Response<T> response) throws ErrorConsumoAPIException {
        int codigo = response.code();
        String errorBackend = "ErrorDesconocido";
        String mensaje = "Ocurrió un fallo al comunicarse con el servidor.";

        if (response.errorBody() != null) {
            try {
                String cuerpoError = response.errorBody().string();
                JsonObject jsonObject = new Gson().fromJson(cuerpoError, JsonObject.class);

                if (jsonObject != null) {
                    if (jsonObject.has("error")) {
                        errorBackend = jsonObject.get("error").getAsString();
                    }
                    if (jsonObject.has("message")) {
                        mensaje = jsonObject.get("message").getAsString();
                    }
                }
            } catch (Exception e) {
                mensaje = "Error al decodificar la respuesta del servidor: " + e.getMessage();
            }
        }

        throw new ErrorConsumoAPIException(mensaje, codigo, errorBackend);
    }
}

package com.voidarchives.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.voidarchives.R;
import com.voidarchives.exception.ErrorConsumoAPIException;
import com.voidarchives.network.RetrofitClient;
import com.voidarchives.util.SessionManager;
import com.voidarchives.util.WindowInsetsHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class DevConsoleActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private String tokenJwt;

    private TextView tvDevTokenJwt;
    private TextView tvDevBaseUrl;
    private EditText etDevEndpoint;
    private Button btnDevConsumir;
    private TextView tvDevJsonOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_console);
        WindowInsetsHelper.aplicarMargenesSeguros(this);

        sessionManager = new SessionManager(this);
        tokenJwt = sessionManager.getToken() != null ? sessionManager.getToken() : "";

        inicializarVistas();
    }

    private void inicializarVistas() {
        Button btnVolverDev = findViewById(R.id.btnVolverDev);
        TextView btnCopiarTokenDev = findViewById(R.id.btnCopiarTokenDev);
        tvDevTokenJwt = findViewById(R.id.tvDevTokenJwt);
        tvDevBaseUrl = findViewById(R.id.tvDevBaseUrl);
        etDevEndpoint = findViewById(R.id.etDevEndpoint);
        btnDevConsumir = findViewById(R.id.btnDevConsumir);
        tvDevJsonOutput = findViewById(R.id.tvDevJsonOutput);

        Button btnQuickArticulos = findViewById(R.id.btnQuickArticulos);
        Button btnQuickError404 = findViewById(R.id.btnQuickError404);
        Button btnQuickNoEncontrado = findViewById(R.id.btnQuickNoEncontrado);
        Button btnQuickLimpiar = findViewById(R.id.btnQuickLimpiar);

        tvDevTokenJwt.setText(tokenJwt.isEmpty() ? "SIN TOKEN ACTIVO" : tokenJwt);
        tvDevBaseUrl.setText(RetrofitClient.getBaseUrlActiva());

        btnVolverDev.setOnClickListener(v -> finish());

        btnCopiarTokenDev.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("JWT Void Archives", tokenJwt);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Token JWT copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });

        btnDevConsumir.setOnClickListener(v -> ejecutarPeticionCruda());

        btnQuickArticulos.setOnClickListener(v -> {
            etDevEndpoint.setText("articulos");
            ejecutarPeticionCruda();
        });

        btnQuickError404.setOnClickListener(v -> {
            etDevEndpoint.setText("articulos/id_corrupto_123");
            ejecutarPeticionCruda();
        });

        btnQuickNoEncontrado.setOnClickListener(v -> {
            etDevEndpoint.setText("articulos/000000000000000000000000");
            ejecutarPeticionCruda();
        });

        btnQuickLimpiar.setOnClickListener(v -> {
            tvDevJsonOutput.setText("// Terminal purgada. Presiona 'CONSUMIR API' para interceptar la respuesta REST de Node.js...");
        });
    }

    private void ejecutarPeticionCruda() {
        String endpoint = etDevEndpoint.getText() != null
                ? etDevEndpoint.getText().toString().trim()
                : "";
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        final String endpointFinal = endpoint;
        final String urlInicial = RetrofitClient.BASE_URL + endpointFinal;
        final long inicioMs = System.currentTimeMillis();

        btnDevConsumir.setEnabled(false);
        tvDevJsonOutput.setText("📡 Conectando con " + RetrofitClient.getBaseUrlActiva() + endpointFinal + "...\n"
                + "Inyectando cabecera Authorization: Bearer <JWT>...");

        Request request = new Request.Builder()
                .url(urlInicial)
                .get()
                .addHeader("Authorization", "Bearer " + tokenJwt)
                .addHeader("Accept", "application/json")
                .build();

        RetrofitClient.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ErrorConsumoAPIException exEspecifica = new ErrorConsumoAPIException(
                        "Fallo crítico de comunicación física con el API Gateway (" + e.getMessage() + ")",
                        500,
                        "GatewayUnreachable"
                );
                runOnUiThread(() -> {
                    btnDevConsumir.setEnabled(true);
                    mostrarErrorEnTerminal(exEspecifica);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                final long latenciaMs = System.currentTimeMillis() - inicioMs;
                final int statusCode = response.code();
                String cuerpoRaw = "";
                try {
                    if (response.body() != null) {
                        cuerpoRaw = response.body().string();
                    }
                } catch (IOException ignored) {
                }

                final String cuerpoFinal = cuerpoRaw;

                runOnUiThread(() -> {
                    btnDevConsumir.setEnabled(true);
                    tvDevBaseUrl.setText(RetrofitClient.getBaseUrlActiva());

                    if (statusCode != 200) {
                        String errorBackend = "HttpError_" + statusCode;
                        String mensajeServidor = "El servidor respondió con código de error";

                        try {
                            JsonObject jsonObject = new Gson().fromJson(cuerpoFinal, JsonObject.class);
                            if (jsonObject != null) {
                                if (jsonObject.has("error")) {
                                    errorBackend = jsonObject.get("error").getAsString();
                                }
                                if (jsonObject.has("message")) {
                                    mensajeServidor = jsonObject.get("message").getAsString();
                                }
                            } else {
                                errorBackend = "NonJsonResponseBody";
                            }
                        } catch (Exception e) {
                            errorBackend = "NonJsonResponseBody";
                        }

                        ErrorConsumoAPIException exEspecifica = new ErrorConsumoAPIException(
                                mensajeServidor,
                                statusCode,
                                errorBackend
                        );
                        mostrarErrorEnTerminal(exEspecifica);
                    } else {
                        String jsonFormateado = formatearJsonBonito(cuerpoFinal);
                        String cabeceraDiagnostico = "// ✅ RESPUESTA 200 OK (" + latenciaMs + " ms)\n"
                                + "// Endpoint: " + RetrofitClient.getBaseUrlActiva() + endpointFinal + "\n\n";
                        tvDevJsonOutput.setText(cabeceraDiagnostico + jsonFormateado);
                    }
                });
            }
        });
    }

    private void mostrarErrorEnTerminal(ErrorConsumoAPIException exEspecifica) {
        String salida = "🚨 RUPTURA DE PROTOCOLO HTTP 🚨"
                + "\n\nMensaje: " + exEspecifica.getMessage()
                + "\nIdentificador (error): " + exEspecifica.getErrorBackend()
                + "\nCódigo Estado HTTP: " + exEspecifica.getCodigoRespuesta();
        tvDevJsonOutput.setText(salida);
    }

    private String formatearJsonBonito(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return "{}";
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(rawJson);
            Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            return gsonPretty.toJson(jsonElement);
        } catch (Exception e) {
            return rawJson.replace("{", "{\n  ")
                    .replace(",", ",\n  ")
                    .replace("}", "\n}");
        }
    }
}

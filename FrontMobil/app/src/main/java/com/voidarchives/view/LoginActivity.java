package com.voidarchives.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.voidarchives.MainActivity;
import com.voidarchives.R;
import com.voidarchives.model.AuthResponse;
import com.voidarchives.model.LoginRequest;
import com.voidarchives.network.AuthClient;
import com.voidarchives.network.RetrofitClient;
import com.voidarchives.util.SessionManager;

import java.net.ConnectException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etPassword;
    private CheckBox chkMostrarPassword;
    private Button btnLogin;
    private TextView tvIrARegistro;
    private ProgressBar progressLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (sessionManager.tieneSesionActiva()) {
            irAMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        chkMostrarPassword = findViewById(R.id.chkMostrarPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);
        progressLogin = findViewById(R.id.progressLogin);

        chkMostrarPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnLogin.setOnClickListener(v -> intentarLogin());

        tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void intentarLogin() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos del sello.", Toast.LENGTH_SHORT).show();
            return;
        }

        setCargando(true);

        AuthClient client = RetrofitClient.getClient().create(AuthClient.class);
        LoginRequest request = new LoginRequest(correo, password);

        client.iniciarSesion(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setCargando(false);

                if (response.isSuccessful() && response.body() != null) {
                    String authHeader = response.headers().get("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        Toast.makeText(
                                LoginActivity.this,
                                "No se recibió un sello de autenticación válido en el header.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String token = authHeader.substring(7);
                    String nombreCazador = response.body().getNombre();
                    String rolCazador = response.body().getRol();

                    sessionManager.guardarSesion(token, nombreCazador, rolCazador);

                    Toast.makeText(
                            LoginActivity.this,
                            "Bienvenido a Hallownest, " + (nombreCazador != null ? nombreCazador : "Vasija"),
                            Toast.LENGTH_SHORT
                    ).show();

                    irAMainActivity();
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(
                                LoginActivity.this,
                                "El sello no coincide. Verifica tu correo y contraseña.",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Toast.makeText(
                                LoginActivity.this,
                                "Fallo al consultar los archivos del reino (" + response.code() + ")",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setCargando(false);
                if (t instanceof ConnectException) {
                    Toast.makeText(
                            LoginActivity.this,
                            "No se pudo conectar con el portal de Hallownest (puerto 3000).",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Toast.makeText(
                            LoginActivity.this,
                            "Error de red: " + t.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }

    private void setCargando(boolean cargando) {
        btnLogin.setEnabled(!cargando);
        btnLogin.setText(cargando ? "DESCIFRANDO SELLO..." : getString(R.string.btn_ingresar));
        progressLogin.setVisibility(cargando ? View.VISIBLE : View.GONE);
    }

    private void irAMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

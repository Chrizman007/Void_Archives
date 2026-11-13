package com.voidarchives.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.voidarchives.R;
import com.voidarchives.exception.ErrorConsumoAPIException;
import com.voidarchives.model.AuthResponse;
import com.voidarchives.model.RegistroRequest;
import com.voidarchives.network.AuthClient;
import com.voidarchives.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombreRegistro;
    private EditText etCorreoRegistro;
    private EditText etPasswordRegistro;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombreRegistro = findViewById(R.id.etNombreRegistro);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etPasswordRegistro = findViewById(R.id.etPasswordRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView tvVolverLogin = findViewById(R.id.tvVolverLogin);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        tvVolverLogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String nombre = etNombreRegistro.getText().toString().trim();
        String correo = etCorreoRegistro.getText().toString().trim();
        String password = etPasswordRegistro.getText().toString();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todas las inscripciones para forjar el sello.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("FORJANDO SELLO...");

        AuthClient client = RetrofitClient.getClient().create(AuthClient.class);
        RegistroRequest request = new RegistroRequest(nombre, correo, password);

        client.registrarUsuario(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnRegistrar.setEnabled(true);
                btnRegistrar.setText("FORJAR SELLO");

                if (response.isSuccessful()) {
                    Toast.makeText(
                            RegistroActivity.this,
                            "¡Sello forjado con éxito! Ahora puedes ingresar a Hallownest.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                } else {
                    try {
                        RetrofitClient.procesarRespuestaError(response);
                    } catch (ErrorConsumoAPIException ex) {
                        Toast.makeText(RegistroActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnRegistrar.setEnabled(true);
                btnRegistrar.setText("FORJAR SELLO");
                Toast.makeText(
                        RegistroActivity.this,
                        "Fallo de conexión con el servidor: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}

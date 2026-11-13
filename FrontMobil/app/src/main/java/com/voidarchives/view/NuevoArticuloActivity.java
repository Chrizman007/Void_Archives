package com.voidarchives.view;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.voidarchives.R;
import com.voidarchives.exception.ErrorConsumoAPIException;
import com.voidarchives.model.ArticuloDTO;
import com.voidarchives.model.ArticuloRequest;
import com.voidarchives.model.ArticuloResponse;
import com.voidarchives.model.ImagenUploadRequest;
import com.voidarchives.network.ArticuloClient;
import com.voidarchives.network.RetrofitClient;
import com.voidarchives.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoArticuloActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICULO_EDITAR = "extra_articulo_editar";

    private static final String[] CATEGORIAS = {"Lore", "Items", "Enemigos", "Ubicaciones", "Personajes"};
    private static final String[] ESTADOS = {"Publicado", "EnBorrador", "EnRevision", "Archivado"};

    private EditText etTituloArticulo;
    private Spinner spCategoriaArticulo;
    private Spinner spEstadoArticulo;
    private EditText etImagenArticulo;
    private Button btnSeleccionarImagen;
    private ImageView ivPreviewArticulo;
    private EditText etDescripcionArticulo;
    private EditText etContenidoArticulo;
    private Button btnGuardarArticulo;
    private ProgressBar progressGuardarArticulo;

    private SessionManager sessionManager;
    private ArticuloDTO articuloEditar = null;

    private Uri uriImagenSeleccionada = null;
    private String nombreArchivoSeleccionado = null;

    private final ActivityResultLauncher<String> selectorImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uriImagenSeleccionada = uri;
                    nombreArchivoSeleccionado = obtenerNombreArchivo(uri);
                    etImagenArticulo.setText(nombreArchivoSeleccionado);

                    ivPreviewArticulo.setVisibility(View.VISIBLE);
                    Glide.with(this)
                            .load(uri)
                            .centerCrop()
                            .into(ivPreviewArticulo);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_articulo);

        sessionManager = new SessionManager(this);
        articuloEditar = (ArticuloDTO) getIntent().getSerializableExtra(EXTRA_ARTICULO_EDITAR);

        inicializarVistas();
        configurarSpinners();

        if (articuloEditar != null) {
            cargarDatosParaEdicion(articuloEditar);
        }
    }

    private void inicializarVistas() {
        Button btnCancelarArticulo = findViewById(R.id.btnCancelarArticulo);
        TextView tvTituloFormulario = findViewById(R.id.tvTituloFormularioArticulo);
        etTituloArticulo = findViewById(R.id.etTituloArticulo);
        spCategoriaArticulo = findViewById(R.id.spCategoriaArticulo);
        spEstadoArticulo = findViewById(R.id.spEstadoArticulo);
        etImagenArticulo = findViewById(R.id.etImagenArticulo);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        ivPreviewArticulo = findViewById(R.id.ivPreviewArticulo);
        etDescripcionArticulo = findViewById(R.id.etDescripcionArticulo);
        etContenidoArticulo = findViewById(R.id.etContenidoArticulo);
        btnGuardarArticulo = findViewById(R.id.btnGuardarArticulo);
        progressGuardarArticulo = findViewById(R.id.progressGuardarArticulo);

        btnCancelarArticulo.setOnClickListener(v -> finish());

        btnSeleccionarImagen.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));

        if (articuloEditar != null) {
            tvTituloFormulario.setText("REESCRIBIR CRÓNICA");
            btnGuardarArticulo.setText("ACTUALIZAR CRÓNICA");
        }

        btnGuardarArticulo.setOnClickListener(v -> enviarArticulo());
    }

    private String obtenerNombreArchivo(Uri uri) {
        String resultado = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        resultado = cursor.getString(index);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (resultado == null) {
            resultado = "reliquia_" + System.currentTimeMillis() + ".jpg";
        }
        return resultado.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }

    private String convertirUriABase64(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap == null) return null;

            // Redimensionar si es muy grande para transmisión rápida desde el celular
            int maxDim = 1024;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxDim || height > maxDim) {
                float ratio = (float) width / (float) height;
                if (ratio > 1) {
                    width = maxDim;
                    height = (int) (width / ratio);
                } else {
                    height = maxDim;
                    width = (int) (height * ratio);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private void configurarSpinners() {
        spCategoriaArticulo.setAdapter(crearAdapterOscuro(CATEGORIAS));
        spEstadoArticulo.setAdapter(crearAdapterOscuro(ESTADOS));
    }

    private ArrayAdapter<String> crearAdapterOscuro(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(Color.parseColor("#E6E8EE"));
                    ((TextView) v).setTextSize(14f);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                v.setBackgroundColor(Color.parseColor("#141824"));
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(Color.parseColor("#E6E8EE"));
                    tv.setPadding(28, 24, 28, 24);
                }
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void cargarDatosParaEdicion(ArticuloDTO art) {
        etTituloArticulo.setText(art.getTitulo());
        etImagenArticulo.setText(art.getImagen());
        etDescripcionArticulo.setText(art.getDescripcion());
        etContenidoArticulo.setText(art.getContenido());

        String urlImagen = RetrofitClient.getImagenUrl(art.getImagen());
        if (urlImagen != null) {
            ivPreviewArticulo.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(urlImagen)
                    .centerCrop()
                    .into(ivPreviewArticulo);
        }

        if (art.getCategoria() != null) {
            for (int i = 0; i < CATEGORIAS.length; i++) {
                if (CATEGORIAS[i].equalsIgnoreCase(art.getCategoria())) {
                    spCategoriaArticulo.setSelection(i);
                    break;
                }
            }
        }

        if (art.getEstado() != null) {
            for (int i = 0; i < ESTADOS.length; i++) {
                if (ESTADOS[i].equalsIgnoreCase(art.getEstado())) {
                    spEstadoArticulo.setSelection(i);
                    break;
                }
            }
        }
    }

    private void enviarArticulo() {
        String titulo = etTituloArticulo.getText().toString().trim();
        String categoria = spCategoriaArticulo.getSelectedItem().toString();
        String estado = spEstadoArticulo.getSelectedItem().toString();
        String imagen = etImagenArticulo.getText().toString().trim();
        String descripcion = etDescripcionArticulo.getText().toString().trim();
        String contenido = etContenidoArticulo.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || contenido.isEmpty()) {
            Toast.makeText(
                    this,
                    "Por favor, completa título, descripción breve y contenido de la crónica.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        setCargando(true);

        ArticuloClient client = RetrofitClient.getClient().create(ArticuloClient.class);
        ArticuloRequest request = new ArticuloRequest(
                titulo,
                categoria,
                descripcion,
                contenido,
                imagen.isEmpty() ? null : imagen,
                estado
        );

        String bearerToken = sessionManager.getBearerToken();

        Callback<ArticuloResponse> callback = new Callback<ArticuloResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArticuloResponse> call, @NonNull Response<ArticuloResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String idArticulo = (response.body().getArticulo() != null && response.body().getArticulo().getId() != null)
                            ? response.body().getArticulo().getId()
                            : (articuloEditar != null ? articuloEditar.getId() : null);

                    // Si el usuario seleccionó una imagen de la galería de su teléfono, la subimos al servidor
                    if (uriImagenSeleccionada != null && idArticulo != null) {
                        subirImagenSeleccionadaAlServidor(client, bearerToken, idArticulo);
                    } else {
                        setCargando(false);
                        mostrarExitoYFinalizar();
                    }
                } else {
                    setCargando(false);
                    try {
                        RetrofitClient.procesarRespuestaError(response);
                    } catch (ErrorConsumoAPIException ex) {
                        Toast.makeText(NuevoArticuloActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArticuloResponse> call, @NonNull Throwable t) {
                setCargando(false);
                Toast.makeText(
                        NuevoArticuloActivity.this,
                        "Fallo de conexión con el archivo central: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        if (articuloEditar != null && articuloEditar.getId() != null) {
            client.actualizarArticulo(bearerToken, articuloEditar.getId(), request).enqueue(callback);
        } else {
            client.crearArticulo(bearerToken, request).enqueue(callback);
        }
    }

    private void subirImagenSeleccionadaAlServidor(ArticuloClient client, String bearerToken, String idArticulo) {
        btnGuardarArticulo.setText("SUBIENDO RELIQUIA...");

        new Thread(() -> {
            String base64 = convertirUriABase64(uriImagenSeleccionada);
            String nombreFinal = nombreArchivoSeleccionado != null
                    ? nombreArchivoSeleccionado
                    : ("reliquia_" + System.currentTimeMillis() + ".jpg");

            runOnUiThread(() -> {
                if (base64 == null) {
                    setCargando(false);
                    Toast.makeText(
                            NuevoArticuloActivity.this,
                            "Crónica grabada, pero no se pudo procesar la imagen del teléfono.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                    return;
                }

                ImagenUploadRequest uploadReq = new ImagenUploadRequest(nombreFinal, base64);
                client.subirImagenArticulo(bearerToken, idArticulo, uploadReq)
                        .enqueue(new Callback<ArticuloResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<ArticuloResponse> call, @NonNull Response<ArticuloResponse> response) {
                                setCargando(false);
                                if (response.isSuccessful()) {
                                    Toast.makeText(
                                            NuevoArticuloActivity.this,
                                            "¡Crónica y reliquia grabadas exitosamente en Void Archives!",
                                            Toast.LENGTH_LONG
                                    ).show();
                                } else {
                                    Toast.makeText(
                                            NuevoArticuloActivity.this,
                                            "Crónica grabada, pero falló la subida de imagen (" + response.code() + ")",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                                finish();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ArticuloResponse> call, @NonNull Throwable t) {
                                setCargando(false);
                                Toast.makeText(
                                        NuevoArticuloActivity.this,
                                        "Crónica grabada, pero falló el envío de la imagen: " + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                                finish();
                            }
                        });
            });
        }).start();
    }

    private void mostrarExitoYFinalizar() {
        String msg = articuloEditar != null
                ? "¡Crónica actualizada en los anales del reino!"
                : "¡Crónica grabada en los anales del reino!";
        Toast.makeText(NuevoArticuloActivity.this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    private void setCargando(boolean cargando) {
        btnGuardarArticulo.setEnabled(!cargando);
        btnGuardarArticulo.setText(cargando
                ? "GRABANDO EN EL VACÍO..."
                : (articuloEditar != null ? "ACTUALIZAR CRÓNICA" : "GRABAR CRÓNICA"));
        progressGuardarArticulo.setVisibility(cargando ? View.VISIBLE : View.GONE);
    }
}

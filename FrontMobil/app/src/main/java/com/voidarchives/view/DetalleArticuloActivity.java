package com.voidarchives.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.voidarchives.R;
import com.voidarchives.adapter.ComentarioAdapter;
import com.voidarchives.model.ArticuloDTO;
import com.voidarchives.model.ComentarioDTO;
import com.voidarchives.model.ComentarioRequest;
import com.voidarchives.model.ComentarioResponse;
import com.voidarchives.model.ComentariosListResponse;
import com.voidarchives.network.ArticuloClient;
import com.voidarchives.network.ComentarioClient;
import com.voidarchives.network.RetrofitClient;
import com.voidarchives.util.SessionManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleArticuloActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICULO = "extra_articulo";

    private ArticuloDTO articulo;
    private SessionManager sessionManager;

    private ImageView ivDetalleImagen;
    private TextView tvDetalleCategoria;
    private TextView tvDetalleEstado;
    private TextView tvDetalleVistas;
    private TextView tvDetalleTitulo;
    private TextView tvDetalleMeta;
    private TextView tvDetalleDescripcion;
    private TextView tvDetalleContenido;
    private EditText etNuevoComentario;
    private Button btnEnviarComentario;
    private Button btnEditarArticulo;

    private ComentarioAdapter comentarioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_articulo);

        sessionManager = new SessionManager(this);
        articulo = (ArticuloDTO) getIntent().getSerializableExtra(EXTRA_ARTICULO);

        if (articulo == null) {
            finish();
            return;
        }

        inicializarVistas();
        renderizarArticulo(articulo);
        registrarVistaYActualizarDetalle();
        cargarComentarios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (articulo != null && articulo.getId() != null) {
            registrarVistaYActualizarDetalle();
        }
    }

    private void inicializarVistas() {
        Button btnVolverDetalle = findViewById(R.id.btnVolverDetalle);
        btnEditarArticulo = findViewById(R.id.btnEditarArticulo);
        ivDetalleImagen = findViewById(R.id.ivDetalleImagen);
        tvDetalleCategoria = findViewById(R.id.tvDetalleCategoria);
        tvDetalleEstado = findViewById(R.id.tvDetalleEstado);
        tvDetalleVistas = findViewById(R.id.tvDetalleVistas);
        tvDetalleTitulo = findViewById(R.id.tvDetalleTitulo);
        tvDetalleMeta = findViewById(R.id.tvDetalleMeta);
        tvDetalleDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvDetalleContenido = findViewById(R.id.tvDetalleContenido);
        etNuevoComentario = findViewById(R.id.etNuevoComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);
        RecyclerView rvComentarios = findViewById(R.id.rvComentarios);

        btnVolverDetalle.setOnClickListener(v -> finish());

        if ("administrador".equalsIgnoreCase(sessionManager.getRol())) {
            btnEditarArticulo.setVisibility(View.VISIBLE);
            btnEditarArticulo.setOnClickListener(v -> {
                Intent intent = new Intent(DetalleArticuloActivity.this, NuevoArticuloActivity.class);
                intent.putExtra(NuevoArticuloActivity.EXTRA_ARTICULO_EDITAR, articulo);
                startActivity(intent);
            });
        }

        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
        comentarioAdapter = new ComentarioAdapter(
                sessionManager.extraerIdUsuarioDesdeToken(),
                sessionManager.getRol(),
                this::eliminarComentario
        );
        rvComentarios.setAdapter(comentarioAdapter);

        btnEnviarComentario.setOnClickListener(v -> enviarComentario());
    }

    private void renderizarArticulo(ArticuloDTO art) {
        tvDetalleTitulo.setText(art.getTitulo() != null ? art.getTitulo().toUpperCase() : "");
        tvDetalleCategoria.setText(art.getCategoria() != null ? art.getCategoria().toUpperCase() : "LORE");
        tvDetalleEstado.setText(art.getEstado() != null ? art.getEstado().toUpperCase() : "PUBLICADO");
        tvDetalleVistas.setText(art.getVistas() + " Ecos");

        String fechaLimpia = art.getFechaCreacion() != null ? art.getFechaCreacion().split("T")[0] : "Era remota";
        String realAutor = (art.getAutor() != null && !art.getAutor().isEmpty()) ? art.getAutor() : "Vasija del Pasado";
        tvDetalleMeta.setText("Inscrito por: " + realAutor + "  |  Fecha: " + fechaLimpia);

        tvDetalleDescripcion.setText(art.getDescripcion() != null ? art.getDescripcion() : "");
        tvDetalleContenido.setText(art.getContenido() != null ? art.getContenido() : "");

        String urlImagen = RetrofitClient.getImagenUrl(art.getImagen());
        if (urlImagen != null) {
            Glide.with(this)
                    .load(urlImagen)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivDetalleImagen);
        } else {
            ivDetalleImagen.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void registrarVistaYActualizarDetalle() {
        if (articulo.getId() == null) return;

        ArticuloClient client = RetrofitClient.getClient().create(ArticuloClient.class);
        client.obtenerArticuloPorId(articulo.getId(), sessionManager.getBearerToken())
                .enqueue(new Callback<ArticuloDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<ArticuloDTO> call, @NonNull Response<ArticuloDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            articulo = response.body();
                            renderizarArticulo(articulo);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ArticuloDTO> call, @NonNull Throwable t) {
                        // Mantenemos los datos locales si hay fallo de red
                    }
                });
    }

    private void cargarComentarios() {
        if (articulo.getId() == null) return;

        ComentarioClient client = RetrofitClient.getClient().create(ComentarioClient.class);
        client.obtenerComentarios(articulo.getId(), 50, 0).enqueue(new Callback<ComentariosListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComentariosListResponse> call, @NonNull Response<ComentariosListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getComentarios() != null) {
                    comentarioAdapter.setComentarios(response.body().getComentarios());
                } else {
                    comentarioAdapter.setComentarios(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComentariosListResponse> call, @NonNull Throwable t) {
                // Silencioso en caso de que el servicio de comentarios esté apagado
            }
        });
    }

    private void enviarComentario() {
        String contenido = etNuevoComentario.getText().toString().trim();
        if (contenido.isEmpty()) {
            Toast.makeText(this, "Escribe un eco antes de enviarlo.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviarComentario.setEnabled(false);

        ComentarioClient client = RetrofitClient.getClient().create(ComentarioClient.class);
        ComentarioRequest request = new ComentarioRequest(contenido);

        client.crearComentario(articulo.getId(), sessionManager.getBearerToken(), request)
                .enqueue(new Callback<ComentarioResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ComentarioResponse> call, @NonNull Response<ComentarioResponse> response) {
                        btnEnviarComentario.setEnabled(true);
                        if (response.isSuccessful()) {
                            etNuevoComentario.setText("");
                            cargarComentarios();
                        } else {
                            Toast.makeText(
                                    DetalleArticuloActivity.this,
                                    "No se pudo grabar el eco (" + response.code() + ")",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ComentarioResponse> call, @NonNull Throwable t) {
                        btnEnviarComentario.setEnabled(true);
                        Toast.makeText(
                                DetalleArticuloActivity.this,
                                "Error de red al enviar comentario",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void eliminarComentario(ComentarioDTO comentario) {
        if (comentario.getId() == null) return;

        ComentarioClient client = RetrofitClient.getClient().create(ComentarioClient.class);
        client.eliminarComentario(comentario.getId(), sessionManager.getBearerToken())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DetalleArticuloActivity.this, "Eco disipado", Toast.LENGTH_SHORT).show();
                            cargarComentarios();
                        } else {
                            Toast.makeText(
                                    DetalleArticuloActivity.this,
                                    "No tienes permiso para disipar este eco",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(DetalleArticuloActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

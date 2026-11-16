package com.voidarchives.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.voidarchives.R;
import com.voidarchives.adapter.AdminArticuloAdapter;
import com.voidarchives.model.ArticuloDTO;
import com.voidarchives.model.ArticuloRequest;
import com.voidarchives.model.ArticuloResponse;
import com.voidarchives.model.ArticulosListResponse;
import com.voidarchives.network.ArticuloClient;
import com.voidarchives.network.RetrofitClient;
import com.voidarchives.util.SessionManager;
import com.voidarchives.util.WindowInsetsHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String ESTADO_ARCHIVADO = "Archivado";
    private static final String ESTADO_REVISION = "EnRevision";
    private static final String ESTADO_BORRADOR = "EnBorrador";
    private static final String ESTADO_PUBLICADO = "Publicado";

    private static final String FILTRO_ACTIVAS = "ACTIVAS";
    private static final String FILTRO_PROPIAS = "PROPIAS";
    private static final String FILTRO_REVISION = "REVISION";
    private static final String FILTRO_ARCHIVADAS = "ARCHIVADAS";

    private SessionManager sessionManager;
    private String nombreMonarca;

    private TextView tvMonarcaAdmin;
    private TextView tvKpiActivas;
    private TextView tvKpiPropias;
    private TextView tvKpiRevision;
    private TextView tvKpiArchivadas;
    private TextView tvFiltroActivoAdmin;

    private LinearLayout cardKpiActivas;
    private LinearLayout cardKpiPropias;
    private LinearLayout cardKpiRevision;
    private LinearLayout cardKpiArchivadas;

    private EditText etBuscadorAdmin;
    private RecyclerView rvAdminArticulos;
    private ProgressBar progressAdmin;
    private TextView tvAdminEmpty;

    private AdminArticuloAdapter adapter;
    private List<ArticuloDTO> listaArticulosCache = new ArrayList<>();
    private String filtroKpiActual = FILTRO_ACTIVAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        WindowInsetsHelper.aplicarMargenesSeguros(this);

        sessionManager = new SessionManager(this);
        nombreMonarca = sessionManager.getNombre();

        inicializarVistas();
        configurarKpis();
        configurarBuscador();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sincronizarConServidor();
    }

    private void inicializarVistas() {
        Button btnVolverAdmin = findViewById(R.id.btnVolverAdmin);
        Button btnSincronizarAdmin = findViewById(R.id.btnSincronizarAdmin);
        ExtendedFloatingActionButton fabAdminNuevoArticulo = findViewById(R.id.fabAdminNuevoArticulo);

        tvMonarcaAdmin = findViewById(R.id.tvMonarcaAdmin);
        tvKpiActivas = findViewById(R.id.tvKpiActivas);
        tvKpiPropias = findViewById(R.id.tvKpiPropias);
        tvKpiRevision = findViewById(R.id.tvKpiRevision);
        tvKpiArchivadas = findViewById(R.id.tvKpiArchivadas);
        tvFiltroActivoAdmin = findViewById(R.id.tvFiltroActivoAdmin);

        cardKpiActivas = findViewById(R.id.cardKpiActivas);
        cardKpiPropias = findViewById(R.id.cardKpiPropias);
        cardKpiRevision = findViewById(R.id.cardKpiRevision);
        cardKpiArchivadas = findViewById(R.id.cardKpiArchivadas);

        etBuscadorAdmin = findViewById(R.id.etBuscadorAdmin);
        rvAdminArticulos = findViewById(R.id.rvAdminArticulos);
        progressAdmin = findViewById(R.id.progressAdmin);
        tvAdminEmpty = findViewById(R.id.tvAdminEmpty);

        tvMonarcaAdmin.setText("Monarca: " + nombreMonarca);

        rvAdminArticulos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminArticuloAdapter(this, new AdminArticuloAdapter.OnAdminAccionListener() {
            @Override
            public void onInspeccionar(ArticuloDTO articulo) {
                Intent intent = new Intent(AdminDashboardActivity.this, DetalleArticuloActivity.class);
                intent.putExtra(DetalleArticuloActivity.EXTRA_ARTICULO, articulo);
                startActivity(intent);
            }

            @Override
            public void onEditar(ArticuloDTO articulo) {
                Intent intent = new Intent(AdminDashboardActivity.this, NuevoArticuloActivity.class);
                intent.putExtra(NuevoArticuloActivity.EXTRA_ARTICULO_EDITAR, articulo);
                startActivity(intent);
            }

            @Override
            public void onCambiarArchivo(ArticuloDTO articulo, boolean yaEstaArchivado) {
                confirmarCambioEstadoArchivo(articulo, yaEstaArchivado);
            }
        });
        rvAdminArticulos.setAdapter(adapter);

        btnVolverAdmin.setOnClickListener(v -> finish());
        btnSincronizarAdmin.setOnClickListener(v -> sincronizarConServidor());

        fabAdminNuevoArticulo.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, NuevoArticuloActivity.class);
            startActivity(intent);
        });
    }

    private void configurarKpis() {
        cardKpiActivas.setOnClickListener(v -> seleccionarKpi(FILTRO_ACTIVAS, cardKpiActivas, "CRÓNICAS ACTIVAS"));
        cardKpiPropias.setOnClickListener(v -> seleccionarKpi(FILTRO_PROPIAS, cardKpiPropias, "FORJADAS POR MÍ (" + nombreMonarca + ")"));
        cardKpiRevision.setOnClickListener(v -> seleccionarKpi(FILTRO_REVISION, cardKpiRevision, "EN REVISIÓN / BORRADOR"));
        cardKpiArchivadas.setOnClickListener(v -> seleccionarKpi(FILTRO_ARCHIVADAS, cardKpiArchivadas, "AL VACÍO (ARCHIVADAS)"));
    }

    private void seleccionarKpi(String filtro, LinearLayout cardActiva, String etiqueta) {
        this.filtroKpiActual = filtro;

        cardKpiActivas.setBackgroundResource(R.drawable.bg_input_piedra);
        cardKpiPropias.setBackgroundResource(R.drawable.bg_input_piedra);
        cardKpiRevision.setBackgroundResource(R.drawable.bg_input_piedra);
        cardKpiArchivadas.setBackgroundResource(R.drawable.bg_input_piedra);

        cardActiva.setBackgroundResource(R.drawable.bg_boton_primario);
        tvFiltroActivoAdmin.setText("Vista actual: " + etiqueta);

        aplicarFiltrosYBusqueda();
    }

    private void configurarBuscador() {
        etBuscadorAdmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltrosYBusqueda();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sincronizarConServidor() {
        progressAdmin.setVisibility(View.VISIBLE);
        tvAdminEmpty.setVisibility(View.GONE);

        ArticuloClient client = RetrofitClient.getClient().create(ArticuloClient.class);
        client.obtenerArticulos().enqueue(new Callback<ArticulosListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArticulosListResponse> call, @NonNull Response<ArticulosListResponse> response) {
                progressAdmin.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getArticulos() != null) {
                    listaArticulosCache = response.body().getArticulos();
                    actualizarContadoresKpi();
                    aplicarFiltrosYBusqueda();
                } else {
                    Toast.makeText(
                            AdminDashboardActivity.this,
                            "Fallo al sincronizar con la Cámara del Rey (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArticulosListResponse> call, @NonNull Throwable t) {
                progressAdmin.setVisibility(View.GONE);
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Fallo de red en Hallownest: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                aplicarFiltrosYBusqueda();
            }
        });
    }

    private void actualizarContadoresKpi() {
        int activas = 0;
        int propias = 0;
        int enRevision = 0;
        int archivadas = 0;

        for (ArticuloDTO art : listaArticulosCache) {
            String estado = art.getEstado() != null ? art.getEstado() : "";
            String autor = art.getAutor() != null ? art.getAutor() : "";

            if (ESTADO_ARCHIVADO.equalsIgnoreCase(estado)) {
                archivadas++;
            } else {
                activas++;
            }

            if (!autor.isEmpty() && autor.equalsIgnoreCase(nombreMonarca)) {
                propias++;
            }

            if (ESTADO_REVISION.equalsIgnoreCase(estado) || ESTADO_BORRADOR.equalsIgnoreCase(estado)) {
                enRevision++;
            }
        }

        tvKpiActivas.setText(String.valueOf(activas));
        tvKpiPropias.setText(String.valueOf(propias));
        tvKpiRevision.setText(String.valueOf(enRevision));
        tvKpiArchivadas.setText(String.valueOf(archivadas));
    }

    private void aplicarFiltrosYBusqueda() {
        String query = etBuscadorAdmin.getText() != null
                ? etBuscadorAdmin.getText().toString().trim().toLowerCase()
                : "";

        List<ArticuloDTO> filtrados = new ArrayList<>();

        for (ArticuloDTO art : listaArticulosCache) {
            String estado = art.getEstado() != null ? art.getEstado() : "";
            String autor = art.getAutor() != null ? art.getAutor() : "";

            boolean cumpleKpi = false;
            switch (filtroKpiActual) {
                case FILTRO_ACTIVAS:
                    cumpleKpi = !ESTADO_ARCHIVADO.equalsIgnoreCase(estado);
                    break;
                case FILTRO_PROPIAS:
                    cumpleKpi = !autor.isEmpty() && autor.equalsIgnoreCase(nombreMonarca);
                    break;
                case FILTRO_REVISION:
                    cumpleKpi = ESTADO_REVISION.equalsIgnoreCase(estado) || ESTADO_BORRADOR.equalsIgnoreCase(estado);
                    break;
                case FILTRO_ARCHIVADAS:
                    cumpleKpi = ESTADO_ARCHIVADO.equalsIgnoreCase(estado);
                    break;
            }

            if (!cumpleKpi) continue;

            if (!query.isEmpty()) {
                String titulo = art.getTitulo() != null ? art.getTitulo().toLowerCase() : "";
                String cat = art.getCategoria() != null ? art.getCategoria().toLowerCase() : "";
                String autorLower = autor.toLowerCase();
                if (!titulo.contains(query) && !cat.contains(query) && !autorLower.contains(query)) {
                    continue;
                }
            }

            filtrados.add(art);
        }

        adapter.setArticulos(filtrados);
        tvAdminEmpty.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmarCambioEstadoArchivo(ArticuloDTO articulo, boolean yaEstaArchivado) {
        String tituloDialogo = yaEstaArchivado
                ? "Restaurar Crónica del Vacío"
                : "Confirmar Sello al Vacío";
        String mensajeDialogo = yaEstaArchivado
                ? "¿Deseas restaurar \"" + articulo.getTitulo() + "\" y publicarla nuevamente en el Diario de Hallownest?"
                : "¿Deseas enviar \"" + articulo.getTitulo() + "\" al Vacío (archivar)?\nDejará de ser visible en el Diario general de Hallownest.";
        String estadoDestino = yaEstaArchivado ? ESTADO_PUBLICADO : ESTADO_ARCHIVADO;

        new AlertDialog.Builder(this)
                .setTitle(tituloDialogo)
                .setMessage(mensajeDialogo)
                .setPositiveButton(yaEstaArchivado ? "Restaurar" : "Enviar al Vacío", (dialog, which) -> {
                    ejecutarCambioEstadoREST(articulo, estadoDestino);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarCambioEstadoREST(ArticuloDTO articulo, String nuevoEstado) {
        if (articulo.getId() == null) return;

        progressAdmin.setVisibility(View.VISIBLE);

        ArticuloClient client = RetrofitClient.getClient().create(ArticuloClient.class);
        ArticuloRequest request = new ArticuloRequest(
                articulo.getTitulo(),
                articulo.getCategoria(),
                articulo.getDescripcion(),
                articulo.getContenido(),
                articulo.getImagen(),
                nuevoEstado
        );

        client.actualizarArticulo(sessionManager.getBearerToken(), articulo.getId(), request)
                .enqueue(new Callback<ArticuloResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ArticuloResponse> call, @NonNull Response<ArticuloResponse> response) {
                        progressAdmin.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            String msg = ESTADO_ARCHIVADO.equalsIgnoreCase(nuevoEstado)
                                    ? "Crónica enviada al Vacío con éxito."
                                    : "Crónica restaurada al Diario de Hallownest.";
                            Toast.makeText(AdminDashboardActivity.this, msg, Toast.LENGTH_SHORT).show();
                            sincronizarConServidor();
                        } else {
                            Toast.makeText(
                                    AdminDashboardActivity.this,
                                    "Fallo al comunicar con el santuario (" + response.code() + ")",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ArticuloResponse> call, @NonNull Throwable t) {
                        progressAdmin.setVisibility(View.GONE);
                        Toast.makeText(
                                AdminDashboardActivity.this,
                                "Fallo de conexión en Hallownest: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}

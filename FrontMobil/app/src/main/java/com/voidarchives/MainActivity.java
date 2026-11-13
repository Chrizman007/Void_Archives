package com.voidarchives;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.voidarchives.adapter.ArticuloAdapter;
import com.voidarchives.model.ArticuloDTO;
import com.voidarchives.model.ArticulosListResponse;
import com.voidarchives.network.ArticuloClient;
import com.voidarchives.network.RetrofitClient;
import com.voidarchives.util.SessionManager;
import com.voidarchives.view.DetalleArticuloActivity;
import com.voidarchives.view.LoginActivity;
import com.voidarchives.view.NuevoArticuloActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvUsuarioHeader;
    private EditText etBuscador;
    private RecyclerView rvArticulos;
    private ProgressBar progressMain;
    private TextView tvEmptyState;
    private ExtendedFloatingActionButton fabNuevoArticulo;

    private Button btnFiltroTodos;
    private Button btnFiltroDestacados;
    private Button btnFiltroLore;
    private Button btnFiltroItems;
    private Button btnFiltroEnemigos;
    private Button btnFiltroUbicaciones;
    private Button btnFiltroPersonajes;
    private List<Button> botonesFiltro = new ArrayList<>();

    private ArticuloAdapter adapter;
    private List<ArticuloDTO> listaArticulosCache = new ArrayList<>();
    private String categoriaSeleccionada = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (!sessionManager.tieneSesionActiva()) {
            irALogin();
            return;
        }

        setContentView(R.layout.activity_main);

        inicializarVistas();
        configurarFiltros();
        configurarBuscador();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.tieneSesionActiva()) {
            cargarArticulosDesdeApi();
        }
    }

    private void inicializarVistas() {
        tvUsuarioHeader = findViewById(R.id.tvUsuarioHeader);
        etBuscador = findViewById(R.id.etBuscador);
        rvArticulos = findViewById(R.id.rvArticulos);
        progressMain = findViewById(R.id.progressMain);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabNuevoArticulo = findViewById(R.id.fabNuevoArticulo);

        Button btnRefrescar = findViewById(R.id.btnRefrescar);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        String textoUsuario = "Vasija: " + sessionManager.getNombre()
                + " [" + sessionManager.getRol().toUpperCase() + "]";
        tvUsuarioHeader.setText(textoUsuario);

        rvArticulos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticuloAdapter(this, this::abrirDetalleArticulo);
        rvArticulos.setAdapter(adapter);

        btnRefrescar.setOnClickListener(v -> cargarArticulosDesdeApi());

        btnCerrarSesion.setOnClickListener(v -> {
            sessionManager.cerrarSesion();
            irALogin();
        });

        fabNuevoArticulo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NuevoArticuloActivity.class);
            startActivity(intent);
        });
    }

    private void configurarFiltros() {
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnFiltroDestacados = findViewById(R.id.btnFiltroDestacados);
        btnFiltroLore = findViewById(R.id.btnFiltroLore);
        btnFiltroItems = findViewById(R.id.btnFiltroItems);
        btnFiltroEnemigos = findViewById(R.id.btnFiltroEnemigos);
        btnFiltroUbicaciones = findViewById(R.id.btnFiltroUbicaciones);
        btnFiltroPersonajes = findViewById(R.id.btnFiltroPersonajes);

        botonesFiltro.clear();
        Collections.addAll(botonesFiltro,
                btnFiltroTodos, btnFiltroDestacados, btnFiltroLore,
                btnFiltroItems, btnFiltroEnemigos, btnFiltroUbicaciones, btnFiltroPersonajes);

        btnFiltroTodos.setOnClickListener(v -> seleccionarFiltro("TODOS", btnFiltroTodos));
        btnFiltroDestacados.setOnClickListener(v -> seleccionarFiltro("DESTACADOS", btnFiltroDestacados));
        btnFiltroLore.setOnClickListener(v -> seleccionarFiltro("Lore", btnFiltroLore));
        btnFiltroItems.setOnClickListener(v -> seleccionarFiltro("Items", btnFiltroItems));
        btnFiltroEnemigos.setOnClickListener(v -> seleccionarFiltro("Enemigos", btnFiltroEnemigos));
        btnFiltroUbicaciones.setOnClickListener(v -> seleccionarFiltro("Ubicaciones", btnFiltroUbicaciones));
        btnFiltroPersonajes.setOnClickListener(v -> seleccionarFiltro("Personajes", btnFiltroPersonajes));
    }

    private void seleccionarFiltro(String filtro, Button botonActivo) {
        this.categoriaSeleccionada = filtro;
        for (Button b : botonesFiltro) {
            b.setBackgroundResource(R.drawable.bg_input_piedra);
            b.setTextColor(ContextCompat.getColor(this, R.color.texto_gris));
        }
        botonActivo.setBackgroundResource(R.drawable.bg_boton_primario);
        botonActivo.setTextColor(ContextCompat.getColor(this, R.color.texto_hueso));

        aplicarFiltrosActuales();
    }

    private void configurarBuscador() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltrosActuales();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarArticulosDesdeApi() {
        progressMain.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        ArticuloClient client = RetrofitClient.getClient().create(ArticuloClient.class);
        client.obtenerArticulos().enqueue(new Callback<ArticulosListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ArticulosListResponse> call, @NonNull Response<ArticulosListResponse> response) {
                progressMain.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getArticulos() != null) {
                    listaArticulosCache = response.body().getArticulos();
                    aplicarFiltrosActuales();
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Error al obtener crónicas (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                    actualizarListaUI(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArticulosListResponse> call, @NonNull Throwable t) {
                progressMain.setVisibility(View.GONE);
                Toast.makeText(
                        MainActivity.this,
                        "Fallo de conexión con el archivo central: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                actualizarListaUI(listaArticulosCache);
            }
        });
    }

    private void aplicarFiltrosActuales() {
        String termino = etBuscador.getText() != null ? etBuscador.getText().toString().trim().toLowerCase() : "";
        List<ArticuloDTO> resultado = new ArrayList<>();

        for (ArticuloDTO art : listaArticulosCache) {
            // Mostrar únicamente artículos con estado "Publicado" en la vista principal
            if (!"Publicado".equalsIgnoreCase(art.getEstado())) {
                continue;
            }

            // Filtro por categoría
            if (!"TODOS".equals(categoriaSeleccionada) && !"DESTACADOS".equals(categoriaSeleccionada)) {
                if (art.getCategoria() == null || !art.getCategoria().equalsIgnoreCase(categoriaSeleccionada)) {
                    continue;
                }
            }

            // Filtro por búsqueda de texto
            if (!termino.isEmpty()) {
                String titulo = art.getTitulo() != null ? art.getTitulo().toLowerCase() : "";
                String desc = art.getDescripcion() != null ? art.getDescripcion().toLowerCase() : "";
                String cat = art.getCategoria() != null ? art.getCategoria().toLowerCase() : "";
                if (!titulo.contains(termino) && !desc.contains(termino) && !cat.contains(termino)) {
                    continue;
                }
            }

            resultado.add(art);
        }

        if ("DESTACADOS".equals(categoriaSeleccionada)) {
            Collections.sort(resultado, (a, b) -> Integer.compare(b.getVistas(), a.getVistas()));
        }

        actualizarListaUI(resultado);
    }

    private void actualizarListaUI(List<ArticuloDTO> lista) {
        adapter.setArticulos(lista);
        if (lista.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void abrirDetalleArticulo(ArticuloDTO articulo) {
        Intent intent = new Intent(MainActivity.this, DetalleArticuloActivity.class);
        intent.putExtra(DetalleArticuloActivity.EXTRA_ARTICULO, articulo);
        startActivity(intent);
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

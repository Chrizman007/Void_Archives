package com.voidarchives.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.voidarchives.R;
import com.voidarchives.model.ArticuloDTO;

import java.util.ArrayList;
import java.util.List;

public class AdminArticuloAdapter extends RecyclerView.Adapter<AdminArticuloAdapter.AdminViewHolder> {

    public interface OnAdminAccionListener {
        void onInspeccionar(ArticuloDTO articulo);
        void onEditar(ArticuloDTO articulo);
        void onCambiarArchivo(ArticuloDTO articulo, boolean yaEstaArchivado);
    }

    private final Context context;
    private List<ArticuloDTO> articulos = new ArrayList<>();
    private final OnAdminAccionListener listener;

    public AdminArticuloAdapter(Context context, OnAdminAccionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setArticulos(List<ArticuloDTO> nuevosArticulos) {
        this.articulos = nuevosArticulos != null ? nuevosArticulos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_articulo, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        ArticuloDTO art = articulos.get(position);

        String idCompleto = art.getId() != null ? art.getId() : "N/A";
        String idCorto = idCompleto.length() > 6
                ? "#" + idCompleto.substring(idCompleto.length() - 6).toUpperCase()
                : "#" + idCompleto.toUpperCase();
        holder.tvAdminIdCorto.setText(idCorto);

        holder.tvAdminTitulo.setText(art.getTitulo() != null ? art.getTitulo() : "Crónica sin título");
        holder.tvAdminCategoria.setText(art.getCategoria() != null ? art.getCategoria().toUpperCase() : "LORE");

        String autor = (art.getAutor() != null && !art.getAutor().trim().isEmpty())
                ? art.getAutor()
                : "Vasija Desconocida";
        holder.tvAdminAutor.setText("Autor / Vasija: " + autor);
        holder.tvAdminVistas.setText(art.getVistas() + " Ecos");

        String estado = art.getEstado() != null ? art.getEstado() : "EnBorrador";
        holder.tvAdminEstado.setText(estado.toUpperCase());

        // Color del estado según las reglas de la Cámara del Rey (AdminDashboardFrame)
        int colorEstado;
        if ("Publicado".equalsIgnoreCase(estado)) {
            colorEstado = ContextCompat.getColor(context, R.color.acento_alma);
        } else if ("EnRevision".equalsIgnoreCase(estado)) {
            colorEstado = ContextCompat.getColor(context, R.color.acento_infeccion);
        } else if ("Archivado".equalsIgnoreCase(estado)) {
            colorEstado = ContextCompat.getColor(context, R.color.acento_vacio);
        } else {
            colorEstado = ContextCompat.getColor(context, R.color.acento_oro);
        }
        holder.tvAdminEstado.setTextColor(colorEstado);

        boolean yaArchivado = "Archivado".equalsIgnoreCase(estado);
        if (yaArchivado) {
            holder.btnAdminArchivar.setText("↺ Restaurar");
            holder.btnAdminArchivar.setTextColor(ContextCompat.getColor(context, R.color.acento_verde));
        } else {
            holder.btnAdminArchivar.setText("⊗ Al Vacío");
            holder.btnAdminArchivar.setTextColor(ContextCompat.getColor(context, R.color.acento_rojo));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onInspeccionar(art);
        });

        holder.btnAdminInspeccionar.setOnClickListener(v -> {
            if (listener != null) listener.onInspeccionar(art);
        });

        holder.btnAdminEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditar(art);
        });

        holder.btnAdminArchivar.setOnClickListener(v -> {
            if (listener != null) listener.onCambiarArchivo(art, yaArchivado);
        });
    }

    @Override
    public int getItemCount() {
        return articulos.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminIdCorto;
        TextView tvAdminCategoria;
        TextView tvAdminEstado;
        TextView tvAdminTitulo;
        TextView tvAdminAutor;
        TextView tvAdminVistas;
        Button btnAdminInspeccionar;
        Button btnAdminEditar;
        Button btnAdminArchivar;

        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminIdCorto = itemView.findViewById(R.id.tvAdminIdCorto);
            tvAdminCategoria = itemView.findViewById(R.id.tvAdminCategoria);
            tvAdminEstado = itemView.findViewById(R.id.tvAdminEstado);
            tvAdminTitulo = itemView.findViewById(R.id.tvAdminTitulo);
            tvAdminAutor = itemView.findViewById(R.id.tvAdminAutor);
            tvAdminVistas = itemView.findViewById(R.id.tvAdminVistas);
            btnAdminInspeccionar = itemView.findViewById(R.id.btnAdminInspeccionar);
            btnAdminEditar = itemView.findViewById(R.id.btnAdminEditar);
            btnAdminArchivar = itemView.findViewById(R.id.btnAdminArchivar);
        }
    }
}

package com.voidarchives.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.voidarchives.R;
import com.voidarchives.model.ComentarioDTO;

import java.util.ArrayList;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    private List<ComentarioDTO> comentarios = new ArrayList<>();
    private final String usuarioIdActual;
    private final String rolUsuarioActual;
    private final OnEliminarComentarioListener eliminarListener;

    public interface OnEliminarComentarioListener {
        void onEliminar(ComentarioDTO comentario);
    }

    public ComentarioAdapter(String usuarioIdActual, String rolUsuarioActual, OnEliminarComentarioListener eliminarListener) {
        this.usuarioIdActual = usuarioIdActual != null ? usuarioIdActual : "";
        this.rolUsuarioActual = rolUsuarioActual != null ? rolUsuarioActual.toLowerCase() : "lector";
        this.eliminarListener = eliminarListener;
    }

    public void setComentarios(List<ComentarioDTO> nuevosComentarios) {
        this.comentarios = nuevosComentarios != null ? nuevosComentarios : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        holder.bind(comentarios.get(position));
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    class ComentarioViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAutorComentario;
        private final TextView tvFechaComentario;
        private final TextView tvContenidoComentario;
        private final TextView btnEliminarComentario;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAutorComentario = itemView.findViewById(R.id.tvAutorComentario);
            tvFechaComentario = itemView.findViewById(R.id.tvFechaComentario);
            tvContenidoComentario = itemView.findViewById(R.id.tvContenidoComentario);
            btnEliminarComentario = itemView.findViewById(R.id.btnEliminarComentario);
        }

        public void bind(ComentarioDTO comentario) {
            String autor = comentario.getAutorNombre() != null && !comentario.getAutorNombre().isEmpty()
                    ? comentario.getAutorNombre()
                    : "Vasija";
            tvAutorComentario.setText("✧ " + autor);

            String fecha = comentario.getFechaCreacion() != null
                    ? comentario.getFechaCreacion().split("T")[0]
                    : "";
            tvFechaComentario.setText(fecha);

            tvContenidoComentario.setText(comentario.getContenido() != null ? comentario.getContenido() : "");

            boolean esAutor = !usuarioIdActual.isEmpty() && usuarioIdActual.equals(comentario.getAutorId());
            boolean esAdmin = "administrador".equalsIgnoreCase(rolUsuarioActual);

            if (esAutor || esAdmin) {
                btnEliminarComentario.setVisibility(View.VISIBLE);
                btnEliminarComentario.setOnClickListener(v -> {
                    if (eliminarListener != null) {
                        eliminarListener.onEliminar(comentario);
                    }
                });
            } else {
                btnEliminarComentario.setVisibility(View.GONE);
            }
        }
    }
}

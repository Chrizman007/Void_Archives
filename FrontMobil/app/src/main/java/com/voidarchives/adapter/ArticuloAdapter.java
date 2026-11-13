package com.voidarchives.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.voidarchives.R;
import com.voidarchives.model.ArticuloDTO;
import com.voidarchives.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class ArticuloAdapter extends RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder> {

    private final Context context;
    private List<ArticuloDTO> articulos = new ArrayList<>();
    private final OnArticuloClickListener listener;

    public interface OnArticuloClickListener {
        void onArticuloClick(ArticuloDTO articulo);
    }

    public ArticuloAdapter(Context context, OnArticuloClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setArticulos(List<ArticuloDTO> nuevosArticulos) {
        this.articulos = nuevosArticulos != null ? nuevosArticulos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticuloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_articulo, parent, false);
        return new ArticuloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticuloViewHolder holder, int position) {
        ArticuloDTO articulo = articulos.get(position);
        holder.bind(articulo);
    }

    @Override
    public int getItemCount() {
        return articulos.size();
    }

    class ArticuloViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivReliquia;
        private final TextView tvVistasBadge;
        private final TextView tvCategoriaCard;
        private final TextView tvEstadoCard;
        private final TextView tvTituloCard;
        private final TextView tvDescripcionCard;
        private final TextView tvAutorCard;

        public ArticuloViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReliquia = itemView.findViewById(R.id.ivReliquia);
            tvVistasBadge = itemView.findViewById(R.id.tvVistasBadge);
            tvCategoriaCard = itemView.findViewById(R.id.tvCategoriaCard);
            tvEstadoCard = itemView.findViewById(R.id.tvEstadoCard);
            tvTituloCard = itemView.findViewById(R.id.tvTituloCard);
            tvDescripcionCard = itemView.findViewById(R.id.tvDescripcionCard);
            tvAutorCard = itemView.findViewById(R.id.tvAutorCard);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onArticuloClick(articulos.get(pos));
                }
            });
        }

        public void bind(ArticuloDTO articulo) {
            tvTituloCard.setText(articulo.getTitulo() != null ? articulo.getTitulo() : "Sin título");
            tvCategoriaCard.setText(articulo.getCategoria() != null ? articulo.getCategoria().toUpperCase() : "LORE");
            tvEstadoCard.setText(articulo.getEstado() != null ? articulo.getEstado().toUpperCase() : "PUBLICADO");
            tvDescripcionCard.setText(articulo.getDescripcion() != null ? articulo.getDescripcion() : "");
            tvVistasBadge.setText(articulo.getVistas() + " Ecos");

            String autor = (articulo.getAutor() != null && !articulo.getAutor().isEmpty())
                    ? articulo.getAutor()
                    : "Vasija del Pasado";
            tvAutorCard.setText("Inscrito por: " + autor);

            String urlImagen = RetrofitClient.getImagenUrl(articulo.getImagen());
            if (urlImagen != null) {
                Glide.with(context)
                        .load(urlImagen)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivReliquia);
            } else {
                ivReliquia.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}

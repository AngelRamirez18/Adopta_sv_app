package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.Util.EstadoUtils;
import com.example.adoptasv.Util.FechaUtils;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Solicitud solicitud);
    }

    private List<Solicitud> solicitudes;
    private final OnItemClickListener listener;

    public SolicitudAdapter(List<Solicitud> solicitudes, OnItemClickListener listener) {
        this.solicitudes = solicitudes;
        this.listener = listener;
    }

    public void updateData(List<Solicitud> nuevas) {
        this.solicitudes = nuevas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Solicitud s = solicitudes.get(position);
        Mascota m = s.mascota;

        holder.tvNombre.setText(m != null && m.nombre != null ? m.nombre : "Mascota");
        holder.tvSubtitulo.setText(buildSubtitulo(m));
        holder.tvFecha.setText(FechaUtils.formatoCorto(s.createdAt));
        EstadoUtils.aplicarBadge(holder.tvEstado, s.estado);

        String fotoUrl = m != null ? m.fotoUrl : null;
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(fotoUrl)
                    .placeholder(R.drawable.placeholder_mascota)
                    .error(R.drawable.placeholder_mascota)
                    .centerCrop()
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.placeholder_mascota);
        }

        View.OnClickListener click = v -> {
            if (listener != null) listener.onItemClick(s);
        };
        holder.itemView.setOnClickListener(click);
        holder.btnVerDetalles.setOnClickListener(click);
    }

    /** Construye "Cachorro • Hembra" a partir de la edad y el sexo. */
    private String buildSubtitulo(Mascota m) {
        if (m == null) return "";
        String categoria = categoriaEdad(m.edadMeses);
        String sexo = m.sexo != null && !m.sexo.isEmpty() ? capitalize(m.sexo) : null;
        if (categoria != null && sexo != null) return categoria + " • " + sexo;
        if (categoria != null) return categoria;
        if (sexo != null) return sexo;
        return "";
    }

    private String categoriaEdad(Integer meses) {
        if (meses == null) return null;
        if (meses < 12) return "Cachorro";
        if (meses < 36) return "Joven";
        return "Adulto";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public int getItemCount() {
        return solicitudes != null ? solicitudes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNombre, tvSubtitulo, tvFecha, tvEstado;
        MaterialButton btnVerDetalles;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto         = itemView.findViewById(R.id.ivFotoMascota);
            tvNombre       = itemView.findViewById(R.id.tvNombreMascota);
            tvSubtitulo    = itemView.findViewById(R.id.tvSubtitulo);
            tvFecha        = itemView.findViewById(R.id.tvFecha);
            tvEstado       = itemView.findViewById(R.id.tvEstado);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
        }
    }
}

package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Seguimiento;
import com.example.adoptasv.Util.EstadoUtils;
import com.example.adoptasv.Util.FechaUtils;

import java.util.List;

public class SeguimientoAdapter extends RecyclerView.Adapter<SeguimientoAdapter.ViewHolder> {

    private List<Seguimiento> seguimientos;

    public SeguimientoAdapter(List<Seguimiento> seguimientos) {
        this.seguimientos = seguimientos;
    }

    public void updateData(List<Seguimiento> nuevas) {
        this.seguimientos = nuevas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seguimiento, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Seguimiento s = seguimientos.get(position);

        EstadoUtils.aplicarBadge(holder.tvEstado, s.estadoSeguimiento);
        holder.tvFecha.setText(FechaUtils.formatoCorto(s.createdAt));

        holder.tvComentario.setText(
                s.comentario != null && !s.comentario.isEmpty() ? s.comentario : "Sin comentario");

        if (s.estadoMascota != null && !s.estadoMascota.isEmpty()) {
            holder.tvEstadoMascota.setVisibility(View.VISIBLE);
            holder.tvEstadoMascota.setText("Estado: " + s.estadoMascota);
        } else {
            holder.tvEstadoMascota.setVisibility(View.GONE);
        }

        if (s.fotoUrl != null && !s.fotoUrl.isEmpty()) {
            holder.cvFoto.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(s.fotoUrl)
                    .centerCrop()
                    .into(holder.ivFoto);
        } else {
            holder.cvFoto.setVisibility(View.GONE);
        }

        if (s.observacionRefugio != null && !s.observacionRefugio.isEmpty()) {
            holder.llObservacion.setVisibility(View.VISIBLE);
            holder.tvObservacion.setText(s.observacionRefugio);
        } else {
            holder.llObservacion.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return seguimientos != null ? seguimientos.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEstado, tvFecha, tvComentario, tvEstadoMascota, tvObservacion;
        CardView cvFoto;
        ImageView ivFoto;
        LinearLayout llObservacion;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEstado        = itemView.findViewById(R.id.tvEstadoSeguimiento);
            tvFecha         = itemView.findViewById(R.id.tvFecha);
            tvComentario    = itemView.findViewById(R.id.tvComentario);
            tvEstadoMascota = itemView.findViewById(R.id.tvEstadoMascota);
            cvFoto          = itemView.findViewById(R.id.cvFoto);
            ivFoto          = itemView.findViewById(R.id.ivFoto);
            llObservacion   = itemView.findViewById(R.id.llObservacion);
            tvObservacion   = itemView.findViewById(R.id.tvObservacion);
        }
    }
}

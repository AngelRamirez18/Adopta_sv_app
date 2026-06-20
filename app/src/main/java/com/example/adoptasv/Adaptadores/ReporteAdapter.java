package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Reporte;
import com.example.adoptasv.Util.EstadoUtils;
import com.example.adoptasv.Util.FechaUtils;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onCambiarEstado(Reporte reporte);
    }

    private List<Reporte> reportes;
    private final OnItemClickListener listener;

    public ReporteAdapter(List<Reporte> reportes, OnItemClickListener listener) {
        this.reportes = reportes;
        this.listener = listener;
    }

    public void updateData(List<Reporte> nuevos) {
        this.reportes = nuevos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reporte, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reporte r = reportes.get(position);

        holder.tvReportador.setText(r.reportador != null && r.reportador.name != null
                ? r.reportador.name : "Reporte SOS");
        holder.tvDescripcion.setText(r.descripcion != null ? r.descripcion : "—");
        holder.tvFecha.setText(FechaUtils.formatoCorto(r.createdAt));
        EstadoUtils.aplicarBadge(holder.tvEstado, r.estadoReporte);

        if (r.fotoUrl != null && !r.fotoUrl.isEmpty()) {
            holder.cvFoto.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(r.fotoUrl).centerCrop().into(holder.ivFoto);
        } else {
            holder.cvFoto.setVisibility(View.GONE);
        }

        View.OnClickListener cambiar = v -> {
            if (listener != null) listener.onCambiarEstado(r);
        };
        holder.btnEstado.setOnClickListener(cambiar);
        holder.itemView.setOnClickListener(cambiar);
    }

    @Override
    public int getItemCount() {
        return reportes != null ? reportes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportador, tvDescripcion, tvFecha, tvEstado;
        CardView cvFoto;
        ImageView ivFoto;
        MaterialButton btnEstado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportador  = itemView.findViewById(R.id.tvReportador);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFecha       = itemView.findViewById(R.id.tvFecha);
            tvEstado      = itemView.findViewById(R.id.tvEstado);
            cvFoto        = itemView.findViewById(R.id.cvFoto);
            ivFoto        = itemView.findViewById(R.id.ivFoto);
            btnEstado     = itemView.findViewById(R.id.btnEstado);
        }
    }
}

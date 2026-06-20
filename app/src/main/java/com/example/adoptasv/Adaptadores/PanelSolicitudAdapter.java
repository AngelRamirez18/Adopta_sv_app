package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.Conexion.Modelos.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter de solicitudes para el panel del refugio, con acciones
 * Aprobar / Rechazar. Las acciones solo se muestran si la solicitud
 * está pendiente o en revisión.
 */
public class PanelSolicitudAdapter extends RecyclerView.Adapter<PanelSolicitudAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onAprobar(Solicitud solicitud);
        void onRechazar(Solicitud solicitud);
    }

    private List<Solicitud> solicitudes;
    private final OnItemClickListener listener;

    public PanelSolicitudAdapter(List<Solicitud> solicitudes, OnItemClickListener listener) {
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
                .inflate(R.layout.item_panel_solicitud, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Solicitud s = solicitudes.get(position);
        User adoptante = s.adoptante;
        Mascota mascota = s.mascota;

        holder.tvAdoptante.setText(adoptante != null && adoptante.name != null
                ? adoptante.name : "Adoptante");
        holder.tvSubAdoptante.setText(adoptante != null && adoptante.direccion != null
                && !adoptante.direccion.isEmpty() ? adoptante.direccion : "Solicitante de adopción");
        holder.tvMatch.setText(s.puntajeEvaluacion + "% Match");

        holder.tvMascota.setText(mascota != null && mascota.nombre != null ? mascota.nombre : "Mascota");
        holder.tvQuote.setText(buildQuote(mascota));

        // Avatar adoptante
        String avatarUrl = adoptante != null ? adoptante.fotoPerfil : null;
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(avatarUrl)
                    .placeholder(R.drawable.ic_person).error(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }

        // Foto mascota
        String fotoUrl = mascota != null ? mascota.fotoUrl : null;
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(fotoUrl)
                    .placeholder(R.drawable.mascota).error(R.drawable.mascota)
                    .into(holder.ivMascota);
        } else {
            holder.ivMascota.setImageResource(R.drawable.mascota);
        }

        // Solo se puede actuar sobre pendiente / en_revision
        String estado = s.estado == null ? "" : s.estado.toLowerCase();
        boolean accionable = estado.equals("pendiente") || estado.equals("en_revision");
        holder.llAcciones.setVisibility(accionable ? View.VISIBLE : View.GONE);

        holder.btnAprobar.setOnClickListener(v -> {
            if (listener != null) listener.onAprobar(s);
        });
        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(s);
        });
    }

    private String buildQuote(Mascota m) {
        if (m == null) return "";
        if (m.descripcion != null && !m.descripcion.isEmpty()) return "\"" + m.descripcion + "\"";
        if (m.personalidad != null && !m.personalidad.isEmpty()) return m.personalidad.replace(",", " • ");
        return "Busca un hogar lleno de amor";
    }

    @Override
    public int getItemCount() {
        return solicitudes != null ? solicitudes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar, ivMascota;
        TextView tvAdoptante, tvSubAdoptante, tvMatch, tvMascota, tvQuote;
        LinearLayout llAcciones;
        MaterialButton btnAprobar, btnRechazar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar       = itemView.findViewById(R.id.ivAvatar);
            ivMascota      = itemView.findViewById(R.id.ivMascota);
            tvAdoptante    = itemView.findViewById(R.id.tvAdoptante);
            tvSubAdoptante = itemView.findViewById(R.id.tvSubAdoptante);
            tvMatch        = itemView.findViewById(R.id.tvMatch);
            tvMascota      = itemView.findViewById(R.id.tvMascota);
            tvQuote        = itemView.findViewById(R.id.tvQuote);
            llAcciones     = itemView.findViewById(R.id.llAcciones);
            btnAprobar     = itemView.findViewById(R.id.btnAprobar);
            btnRechazar    = itemView.findViewById(R.id.btnRechazar);
        }
    }
}

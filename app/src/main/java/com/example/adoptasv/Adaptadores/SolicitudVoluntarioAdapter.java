package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Conexion.Modelos.SolicitudVoluntario;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter de solicitudes de voluntariado para el panel del refugio (admin), con
 * acciones Aprobar / Rechazar. Las acciones solo se muestran si la solicitud está
 * pendiente. Comparte el estilo visual con {@link PanelSolicitudAdapter}.
 */
public class SolicitudVoluntarioAdapter
        extends RecyclerView.Adapter<SolicitudVoluntarioAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onAprobar(SolicitudVoluntario solicitud);
        void onRechazar(SolicitudVoluntario solicitud);
    }

    private List<SolicitudVoluntario> solicitudes;
    private final OnItemClickListener listener;

    public SolicitudVoluntarioAdapter(List<SolicitudVoluntario> solicitudes, OnItemClickListener listener) {
        this.solicitudes = solicitudes;
        this.listener = listener;
    }

    public void updateData(List<SolicitudVoluntario> nuevas) {
        this.solicitudes = nuevas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud_voluntario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudVoluntario s = solicitudes.get(position);
        User u = s.user;

        holder.tvNombre.setText(u != null && u.name != null ? u.name : "Solicitante");
        holder.tvEmail.setText(u != null && u.email != null && !u.email.isEmpty()
                ? u.email : "Quiere ser voluntario");

        holder.tvRefugio.setText(s.refugio != null && s.refugio.nombre != null
                ? s.refugio.nombre : "Refugio");
        holder.tvMensaje.setText(s.mensaje != null && !s.mensaje.isEmpty()
                ? s.mensaje : "Solicitud de voluntariado");

        // Badge de estado
        holder.tvEstado.setText(estadoLabel(s.estado));

        // Avatar
        String avatarUrl = u != null ? u.fotoPerfil : null;
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(avatarUrl)
                    .placeholder(R.drawable.ic_person).error(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }

        // Solo se puede actuar sobre pendientes
        String estado = s.estado == null ? "" : s.estado.toLowerCase();
        boolean accionable = estado.equals("pendiente");
        holder.llAcciones.setVisibility(accionable ? View.VISIBLE : View.GONE);

        holder.btnAprobar.setOnClickListener(v -> {
            if (listener != null) listener.onAprobar(s);
        });
        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(s);
        });
    }

    private String estadoLabel(String estado) {
        if (estado == null) return "—";
        switch (estado.toLowerCase()) {
            case "pendiente": return "Pendiente";
            case "aprobada":  return "Aprobada";
            case "rechazada": return "Rechazada";
            default:          return estado;
        }
    }

    @Override
    public int getItemCount() {
        return solicitudes != null ? solicitudes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvNombre, tvEmail, tvEstado, tvRefugio, tvMensaje;
        LinearLayout llAcciones;
        MaterialButton btnAprobar, btnRechazar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar    = itemView.findViewById(R.id.ivAvatar);
            tvNombre    = itemView.findViewById(R.id.tvNombre);
            tvEmail     = itemView.findViewById(R.id.tvEmail);
            tvEstado    = itemView.findViewById(R.id.tvEstado);
            tvRefugio   = itemView.findViewById(R.id.tvRefugio);
            tvMensaje   = itemView.findViewById(R.id.tvMensaje);
            llAcciones  = itemView.findViewById(R.id.llAcciones);
            btnAprobar  = itemView.findViewById(R.id.btnAprobar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}

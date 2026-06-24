package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.UsuarioAdmin;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onCambiarRol(UsuarioAdmin usuario);
    }

    private List<UsuarioAdmin> usuarios;
    private final OnItemClickListener listener;

    public UsuarioAdapter(List<UsuarioAdmin> usuarios, OnItemClickListener listener) {
        this.usuarios = usuarios;
        this.listener = listener;
    }

    public void updateData(List<UsuarioAdmin> nuevos) {
        this.usuarios = nuevos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioAdmin u = usuarios.get(position);
        holder.tvNombre.setText(u.name != null ? u.name : "Usuario");
        holder.tvEmail.setText(u.email != null ? u.email : "");

        // Avatar (foto de perfil o ícono por defecto)
        if (u.fotoPerfil != null && !u.fotoPerfil.isEmpty()) {
            holder.ivAvatar.setPadding(0, 0, 0, 0);
            Glide.with(holder.ivAvatar.getContext())
                    .load(u.fotoPerfil)
                    .placeholder(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }

        // Punto de estado: verde activo / rojo inactivo, y atenúa la tarjeta si está inactivo
        boolean activo = u.isActivo();
        holder.vStatus.setBackgroundTintList(ContextCompat.getColorStateList(
                holder.itemView.getContext(),
                activo ? R.color.status_active : R.color.status_inactive));
        holder.cardRoot.setAlpha(activo ? 1f : 0.6f);

        // Badge de rol con color por tipo
        aplicarBadgeRol(holder, u.rolPrincipal());

        View.OnClickListener abrir = v -> {
            if (listener != null) listener.onCambiarRol(u);
        };
        holder.itemView.setOnClickListener(abrir);
        holder.btnMore.setOnClickListener(abrir);
    }

    private void aplicarBadgeRol(ViewHolder holder, String rol) {
        int bg, text;
        String etiqueta;
        switch (rol == null ? "" : rol.toLowerCase()) {
            case "admin":
                bg = R.color.md_primary_fixed;
                text = R.color.md_on_primary_fixed_variant;
                etiqueta = "Administrador";
                break;
            case "voluntario":
                bg = R.color.md_secondary_fixed;
                text = R.color.md_on_secondary_fixed_variant;
                etiqueta = "Refugio";
                break;
            case "adoptante":
                bg = R.color.md_tertiary_fixed;
                text = R.color.md_on_tertiary_fixed_variant;
                etiqueta = "Adoptante";
                break;
            default:
                bg = R.color.md_tertiary_fixed;
                text = R.color.md_on_tertiary_fixed_variant;
                etiqueta = "Sin rol";
                break;
        }
        holder.tvRol.setText(etiqueta);
        holder.tvRol.setBackgroundTintList(
                ContextCompat.getColorStateList(holder.itemView.getContext(), bg));
        holder.tvRol.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), text));
    }

    @Override
    public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardRoot;
        ImageView ivAvatar;
        TextView tvNombre, tvEmail, tvRol;
        View vStatus;
        ImageButton btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail  = itemView.findViewById(R.id.tvEmail);
            tvRol    = itemView.findViewById(R.id.tvRol);
            vStatus  = itemView.findViewById(R.id.vStatus);
            btnMore  = itemView.findViewById(R.id.btnMore);
        }
    }
}

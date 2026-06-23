package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Util.EstadoUtils;

import java.util.List;

/**
 * Adapter de mascotas para el panel del refugio. Tap en la card o el lápiz
 * abre la edición; tap en el badge de estado permite cambiarlo.
 */
public class AdminMascotaAdapter extends RecyclerView.Adapter<AdminMascotaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEditar(Mascota mascota);
        void onCambiarEstado(Mascota mascota);
    }

    private List<Mascota> mascotas;
    private final OnItemClickListener listener;

    public AdminMascotaAdapter(List<Mascota> mascotas, OnItemClickListener listener) {
        this.mascotas = mascotas;
        this.listener = listener;
    }

    public void updateData(List<Mascota> nuevas) {
        this.mascotas = nuevas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_mascota, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mascota m = mascotas.get(position);
        holder.tvNombre.setText(m.nombre);
        holder.tvRaza.setText(buildRaza(m));
        EstadoUtils.aplicarBadge(holder.tvEstado, m.estadoAdopcion);

        // Foto con placeholder
        Glide.with(holder.itemView.getContext())
                .load(m.fotoUrl != null && !m.fotoUrl.isEmpty() ? m.fotoUrl : null)
                .placeholder(R.drawable.mascota)
                .error(R.drawable.mascota)
                .centerCrop()
                .into(holder.ivFoto);

        View.OnClickListener editar = v -> {
            if (listener != null) listener.onEditar(m);
        };
        holder.itemView.setOnClickListener(editar);
        holder.btnEditar.setOnClickListener(editar);
        holder.tvEstado.setOnClickListener(v -> {
            if (listener != null) listener.onCambiarEstado(m);
        });
    }

    @Override
    public int getItemCount() {
        return mascotas != null ? mascotas.size() : 0;
    }

    private String buildRaza(Mascota m) {
        String raza = (m.raza != null && !m.raza.isEmpty()) ? m.raza
                : (m.especie != null ? capitalize(m.especie) : null);
        String edad = formatEdad(m.edadMeses);
        if (raza != null && edad != null) return raza + " • " + edad;
        if (raza != null) return raza;
        return edad != null ? edad : "";
    }

    private String formatEdad(Integer meses) {
        if (meses == null) return null;
        if (meses < 12) return meses + (meses == 1 ? " mes" : " meses");
        int anios = meses / 12;
        return anios + (anios == 1 ? " año" : " años");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNombre, tvRaza, tvEstado;
        ImageButton btnEditar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto    = itemView.findViewById(R.id.ivFoto);
            tvNombre  = itemView.findViewById(R.id.tvNombre);
            tvRaza    = itemView.findViewById(R.id.tvRaza);
            tvEstado  = itemView.findViewById(R.id.tvEstado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }
    }
}

package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter de mascotas en formato lista (LinearLayoutManager) con más detalle
 * por card. Usado en AdoptarFragment.
 */
public class MascotaListaAdapter extends RecyclerView.Adapter<MascotaListaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Mascota mascota);
    }

    private List<Mascota> mascotas;
    private final OnItemClickListener listener;

    public MascotaListaAdapter(List<Mascota> mascotas, OnItemClickListener listener) {
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
                .inflate(R.layout.item_mascota_lista, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mascota m = mascotas.get(position);

        holder.tvNombre.setText(m.nombre);
        holder.tvRaza.setText(buildRaza(m));
        holder.tvMeta.setText(buildMeta(m));

        if (m.fotoUrl != null && !m.fotoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(m.fotoUrl)
                    .placeholder(R.drawable.mascota)
                    .error(R.drawable.mascota)
                    .centerCrop()
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.mascota);
        }

        View.OnClickListener click = v -> {
            if (listener != null) listener.onItemClick(m);
        };
        holder.itemView.setOnClickListener(click);
        holder.btnVerMas.setOnClickListener(click);
    }

    @Override
    public int getItemCount() {
        return mascotas != null ? mascotas.size() : 0;
    }

    /** "Raza • edad" para la línea principal. */
    private String buildRaza(Mascota m) {
        String raza = (m.raza != null && !m.raza.isEmpty()) ? m.raza : capitalizeOrNull(m.especie);
        String edad = formatEdad(m.edadMeses);
        if (raza != null) return raza + " • " + edad;
        return edad;
    }

    /** "Macho • Grande" para la línea secundaria. */
    private String buildMeta(Mascota m) {
        String sexo = capitalizeOrNull(m.sexo);
        String tamano = capitalizeOrNull(m.tamano);
        if (sexo != null && tamano != null) return sexo + " • " + tamano;
        if (sexo != null) return sexo;
        if (tamano != null) return tamano;
        return "Sin datos";
    }

    private String capitalizeOrNull(String s) {
        return (s != null && !s.isEmpty()) ? capitalize(s) : null;
    }

    private String formatEdad(Integer meses) {
        if (meses == null) return "Edad N/D";
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
        TextView tvNombre, tvRaza, tvMeta;
        MaterialButton btnVerMas;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto    = itemView.findViewById(R.id.ivFoto);
            tvNombre  = itemView.findViewById(R.id.tvNombre);
            tvRaza    = itemView.findViewById(R.id.tvRaza);
            tvMeta    = itemView.findViewById(R.id.tvMeta);
            btnVerMas = itemView.findViewById(R.id.btnVerMas);
        }
    }
}

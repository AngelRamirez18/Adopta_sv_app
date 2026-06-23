package com.example.adoptasv.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.Modelos.Mascota;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.ViewHolder> {

    public interface OnMascotaClickListener {
        void onMascotaClick(Mascota mascota);
    }

    private List<Mascota> mascotas;
    private OnMascotaClickListener listener;

    public MascotaAdapter(List<Mascota> mascotas, OnMascotaClickListener listener) {
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mascota, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mascota m = mascotas.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvNombre.setText(m.nombre);
        holder.tvEdad.setText(formatEdad(m.edadMeses));
        holder.tvEspecie.setText(m.especie != null ? capitalize(m.especie) : "");
        holder.tvSexo.setText(m.sexo != null ? capitalize(m.sexo) : "");

        // Foto con Glide y Placeholder
        Glide.with(ctx)
                .load(m.fotoUrl != null && !m.fotoUrl.isEmpty() ? m.fotoUrl : null)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.mascota)
                .error(R.drawable.mascota)
                .centerCrop()
                .into(holder.ivFoto);

        // Badge vacunas
        holder.tvVacunas.setVisibility(m.vacunas ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMascotaClick(m);
        });
    }

    private String formatEdad(Integer meses) {
        if (meses == null) return "Edad desconocida";
        if (meses < 12) return meses + " mes" + (meses == 1 ? "" : "es");
        int años = meses / 12;
        return años + " año" + (años == 1 ? "" : "s");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public int getItemCount() { return mascotas != null ? mascotas.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNombre, tvEdad, tvEspecie, tvSexo, tvVacunas;
        ImageButton btnFavorito;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto      = itemView.findViewById(R.id.ivFotoMascota);
            tvNombre    = itemView.findViewById(R.id.tvNombre);
            tvEdad      = itemView.findViewById(R.id.tvEdad);
            tvEspecie   = itemView.findViewById(R.id.tvEspecie);
            tvSexo      = itemView.findViewById(R.id.tvSexo);
            tvVacunas   = itemView.findViewById(R.id.tvVacunas);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
        }
    }
}
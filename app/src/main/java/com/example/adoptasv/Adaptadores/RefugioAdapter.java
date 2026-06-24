package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Lista de refugios para el área administrativa. El click abre el editor;
 * el botón de opciones expone editar y eliminar.
 */
public class RefugioAdapter extends RecyclerView.Adapter<RefugioAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEditar(Refugio refugio);
        void onOpciones(Refugio refugio, View anchor);
    }

    private List<Refugio> refugios;
    private final OnItemClickListener listener;

    public RefugioAdapter(List<Refugio> refugios, OnItemClickListener listener) {
        this.refugios = refugios;
        this.listener = listener;
    }

    public void updateData(List<Refugio> nuevas) {
        this.refugios = nuevas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_refugio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Refugio r = refugios.get(position);

        holder.tvNombre.setText(r.nombre != null ? r.nombre : "Refugio");
        holder.tvDireccion.setText(r.direccion != null && !r.direccion.isEmpty()
                ? r.direccion : "Sin dirección");
        holder.tvTelefono.setText(r.telefono != null && !r.telefono.isEmpty()
                ? r.telefono : "Sin teléfono");

        if (r.activo) {
            holder.tvEstado.setText("Activo");
            holder.tvEstado.setBackgroundResource(R.drawable.bg_pill_role);
            holder.tvEstado.getBackground().setTint(0xFFE8F5E9);
            holder.tvEstado.setTextColor(0xFF2E7D32);
        } else {
            holder.tvEstado.setText("Inactivo");
            holder.tvEstado.setBackgroundResource(R.drawable.bg_pill_role);
            holder.tvEstado.getBackground().setTint(0xFFFFEBEE);
            holder.tvEstado.setTextColor(0xFFC62828);
        }

        if (r.logoUrl != null && !r.logoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(r.logoUrl)
                    .placeholder(R.drawable.ic_home).error(R.drawable.ic_home)
                    .into(holder.ivLogo);
        } else {
            holder.ivLogo.setImageResource(R.drawable.ic_home);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEditar(r);
        });
        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onOpciones(r, v);
        });
    }

    @Override
    public int getItemCount() {
        return refugios != null ? refugios.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivLogo;
        TextView tvNombre, tvDireccion, tvTelefono, tvEstado;
        ImageButton btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo      = itemView.findViewById(R.id.ivLogo);
            tvNombre    = itemView.findViewById(R.id.tvNombre);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvTelefono  = itemView.findViewById(R.id.tvTelefono);
            tvEstado    = itemView.findViewById(R.id.tvEstado);
            btnMore     = itemView.findViewById(R.id.btnMore);
        }
    }
}

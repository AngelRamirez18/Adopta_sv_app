package com.example.adoptasv.Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.tvRol.setText(u.rolPrincipal());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCambiarRol(u);
        });
    }

    @Override
    public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail, tvRol;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail  = itemView.findViewById(R.id.tvEmail);
            tvRol    = itemView.findViewById(R.id.tvRol);
        }
    }
}

package com.example.adoptasv.Conexion.Modelos;

import java.util.Collections;
import java.util.List;

public class DeleteFotoExtraRequest {
    public List<String> fotos;

    public DeleteFotoExtraRequest(String fotoUrl) {
        this.fotos = Collections.singletonList(fotoUrl);
    }

    public DeleteFotoExtraRequest(List<String> fotos) {
        this.fotos = fotos;
    }
}

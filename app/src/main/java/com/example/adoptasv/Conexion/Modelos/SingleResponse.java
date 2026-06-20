package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

/**
 * Envoltura para recursos individuales. Los API Resource de Laravel envuelven
 * la respuesta en una clave "data" (igual que las colecciones paginadas), por
 * lo que un GET de un solo objeto llega como { "data": { ... } }.
 */
public class SingleResponse<T> {

    @SerializedName("data")
    public T data;
}

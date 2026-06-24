package com.example.adoptasv.Conexion.Modelos;


import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaginatedResponse<T> {

    @SerializedName("data")
    public List<T> data;

    // Formato "JsonResource collection": { data, meta:{...}, links:{...} }
    @SerializedName("meta")
    public Meta meta;

    // Formato "paginador crudo de Laravel": los campos van planos en la raíz.
    @SerializedName("current_page") public Integer currentPage;
    @SerializedName("last_page")    public Integer lastPage;
    @SerializedName("total")        public Integer total;
    @SerializedName("per_page")     public Integer perPage;

    // "links" llega como objeto en JsonResource y como array en el paginador
    // crudo; lo dejamos como JsonElement para que Gson no reviente con ninguno.
    @SerializedName("links")
    public JsonElement links;

    public static class Meta {
        @SerializedName("current_page") public int currentPage;
        @SerializedName("last_page")    public int lastPage;
        @SerializedName("total")        public int total;
        @SerializedName("per_page")     public int perPage;
    }
}
package com.example.adoptasv.Conexion.Modelos;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaginatedResponse<T> {

    @SerializedName("data")
    public List<T> data;

    @SerializedName("meta")
    public Meta meta;

    @SerializedName("links")
    public Links links;

    public static class Meta {
        @SerializedName("current_page") public int currentPage;
        @SerializedName("last_page")    public int lastPage;
        @SerializedName("total")        public int total;
        @SerializedName("per_page")     public int perPage;
    }

    public static class Links {
        @SerializedName("first") public String first;
        @SerializedName("last")  public String last;
        @SerializedName("next")  public String next;
        @SerializedName("prev")  public String prev;
    }
}
package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class AddVoluntarioRequest {
    @SerializedName("user_id") public int userId;

    public AddVoluntarioRequest(int userId) {
        this.userId = userId;
    }
}

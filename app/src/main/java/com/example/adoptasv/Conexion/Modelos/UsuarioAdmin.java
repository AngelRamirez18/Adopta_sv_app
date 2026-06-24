package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * Usuario tal como lo devuelve el panel de administración (GET /api/users).
 * A diferencia de {@link User}, aquí los roles pueden venir como objetos
 * {id,name} (Spatie) o como simples strings, según la versión de la API; por
 * eso {@link Rol} se deserializa de forma tolerante a ambos formatos.
 */
public class UsuarioAdmin {
    public int id;
    public String name;
    public String email;
    public String telefono;
    @SerializedName("estado_cuenta") public String estadoCuenta;
    @SerializedName("foto_perfil_url") public String fotoPerfil;
    public List<Rol> roles;

    /**
     * Un rol. La API lo envía indistintamente como objeto {@code {"id":1,"name":"admin"}}
     * o como string suelto {@code "admin"}; el adapter normaliza ambos casos para
     * que Gson no lance {@code IllegalStateException: Expected BEGIN_OBJECT but was STRING}.
     */
    @JsonAdapter(Rol.Adapter.class)
    public static class Rol {
        public int id;
        public String name;

        static class Adapter extends TypeAdapter<Rol> {
            @Override
            public Rol read(JsonReader in) throws IOException {
                JsonToken token = in.peek();
                if (token == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                Rol r = new Rol();
                if (token == JsonToken.STRING) {
                    r.name = in.nextString();
                    return r;
                }
                // Formato objeto {id, name, ...}
                in.beginObject();
                while (in.hasNext()) {
                    String key = in.nextName();
                    if ("id".equals(key) && in.peek() == JsonToken.NUMBER) {
                        r.id = in.nextInt();
                    } else if ("name".equals(key) && in.peek() == JsonToken.STRING) {
                        r.name = in.nextString();
                    } else {
                        in.skipValue();
                    }
                }
                in.endObject();
                return r;
            }

            @Override
            public void write(JsonWriter out, Rol value) throws IOException {
                if (value == null) out.nullValue();
                else out.value(value.name);
            }
        }
    }

    /** Devuelve el nombre del primer rol (en minúscula, ej. "admin"), o "—" si no tiene. */
    public String rolPrincipal() {
        if (roles != null && !roles.isEmpty() && roles.get(0).name != null) {
            return roles.get(0).name;
        }
        return "—";
    }

    /** True si la cuenta está activa (o si no se reportó estado). */
    public boolean isActivo() {
        return estadoCuenta == null || "activo".equalsIgnoreCase(estadoCuenta);
    }
}

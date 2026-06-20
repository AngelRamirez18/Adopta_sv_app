package com.example.adoptasv.Conexion;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Lee booleanos de forma tolerante. La API puede devolver el valor como
 * booleano real (true/false), como número (1/0) o como string ("1"/"true"),
 * según cómo Eloquent castee la columna tinyint. Gson por defecto solo acepta
 * true/false y lanza una excepción ante un número, lo que tumbaría todo el
 * parseo del objeto. Este adapter normaliza todos esos casos.
 */
public class LenientBooleanAdapter extends TypeAdapter<Boolean> {

    @Override
    public Boolean read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        switch (token) {
            case BOOLEAN:
                return in.nextBoolean();
            case NUMBER:
                return in.nextInt() != 0;
            case STRING:
                String s = in.nextString().trim();
                return "1".equals(s) || "true".equalsIgnoreCase(s);
            case NULL:
                in.nextNull();
                return false;
            default:
                in.skipValue();
                return false;
        }
    }

    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        out.value(value != null && value);
    }
}

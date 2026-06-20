# ReporteController

Gestiona los reportes SOS de animales en situación de calle. Cualquier usuario
puede crear y ver reportes; solo voluntarios y admins pueden cambiar su estado.

---

## Endpoints

### `GET /api/reportes`

Lista todos los reportes paginados de 20 en 20, con filtro opcional por estado.
Incluye el usuario que reportó (`reportador`).

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Query params**

| Param | Valores |
|---|---|
| `estado` | `nuevo`, `en_atencion`, `atendido`, `cerrado` |

**Respuesta `200`** — colección paginada de `ReporteResource`.

```json
{
  "data": [
    {
      "id": 1,
      "descripcion": "Perro callejero con herida en la pata...",
      "foto_url": null,
      "latitud": "13.6929000",
      "longitud": "-89.2182000",
      "direccion_referencia": "Frente al Parque Infantil...",
      "estado_reporte": "en_atencion",
      "reportador": { "id": 4, "name": "Ana García" },
      "created_at": "..."
    }
  ]
}
```

---

### `POST /api/reportes`

Crea un nuevo reporte SOS. El `user_id` se asigna automáticamente del usuario
autenticado. Las coordenadas son opcionales pero recomendadas para aparecer en
el mapa.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Body**
```json
{
  "descripcion": "Gata con gatitos recién nacidos bajo un vehículo.",
  "latitud": 13.7018,
  "longitud": -89.2240,
  "direccion_referencia": "Col. San Benito, Calle La Mascota, frente al parque"
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `descripcion` | string | Sí |
| `latitud` | decimal (-90 a 90)\|null | No |
| `longitud` | decimal (-180 a 180)\|null | No |
| `direccion_referencia` | string\|null | No |

**Respuesta `201`** — `ReporteResource` creado.

---

### `GET /api/reportes/{reporte}`

Devuelve el detalle de un reporte con el usuario que lo creó.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`** — `ReporteResource`.

---

### `PATCH /api/reportes/{reporte}/estado`

Actualiza el estado del reporte. Solo voluntarios y admins.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Body**
```json
{ "estado": "en_atencion" }
```

| Estado | Significado |
|---|---|
| `en_atencion` | Un voluntario tomó el caso |
| `atendido` | El animal fue atendido |
| `cerrado` | Caso finalizado (ya no aparece en el mapa) |

**Respuesta `200`** — el reporte actualizado.

---

## Estados del reporte

```
nuevo → en_atencion → atendido → cerrado
```

Los reportes con estado `cerrado` no aparecen en `GET /api/mapa/reportes`.
Todos los demás estados son visibles en el mapa para que voluntarios cercanos
puedan responder.

---

## Notas

- Para subir una foto al reporte usar `POST /api/reportes/{reporte}/foto`
  (`FotoController@reporteFoto`).
- Para ver reportes filtrados por distancia geográfica usar `GET /api/mapa/reportes`.
- Cualquier usuario puede crear un reporte, incluso si no es adoptante formal —
  la idea es que ciudadanos en general puedan reportar animales en la calle.

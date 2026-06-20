# MapaController

Endpoints geográficos que devuelven refugios y reportes filtrados por distancia
usando la fórmula de Haversine. Si no se pasan coordenadas, devuelve todos los
registros sin filtro de distancia.

---

## Endpoints

### `GET /api/mapa/refugios`

Devuelve refugios activos con coordenadas. Si se pasan `lat`/`lng`, calcula la
distancia desde ese punto y filtra por radio, ordenados del más cercano al más lejano.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Query params**

| Param | Tipo | Default | Descripción |
|---|---|---|---|
| `lat` | decimal -90 a 90 | — | Latitud del usuario |
| `lng` | decimal -180 a 180 | — | Longitud del usuario |
| `radio` | decimal 1-100 | 20 | Radio de búsqueda en km |

**Respuesta sin coordenadas `200`** — todos los refugios activos con coordenadas.

**Respuesta con coordenadas `200`**
```json
[
  {
    "id": 1,
    "nombre": "Refugio Amigos Peludos",
    "latitud": "13.7034000",
    "longitud": "-89.2389000",
    "distancia_km": 0.40,
    ...
  },
  {
    "id": 2,
    "nombre": "Hogar Felino Santa Ana",
    "latitud": "13.9944000",
    "longitud": "-89.5592000",
    "distancia_km": 47.53,
    ...
  }
]
```

El campo `distancia_km` solo aparece cuando se envían coordenadas.

---

### `GET /api/mapa/reportes`

Devuelve reportes SOS activos (estado distinto de `cerrado`) con coordenadas.
Mismo comportamiento de filtro por radio que el endpoint de refugios.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Query params** — idénticos a `/mapa/refugios`.

**Respuesta con coordenadas `200`**
```json
[
  {
    "id": 2,
    "descripcion": "Gata con gatitos recién nacidos...",
    "estado_reporte": "nuevo",
    "latitud": "13.7018000",
    "longitud": "-89.2240000",
    "distancia_km": 1.23
  }
]
```

---

## Implementación del filtro geográfico

El cálculo de distancia usa la fórmula de Haversine en SQL, implementada como
subquery para garantizar compatibilidad con SQLite (dev), MySQL y PostgreSQL (prod):

```
distancia_km = ROUND(CAST(
    6371 * ACOS(
        COS(RADIANS(lat_usuario)) * COS(RADIANS(latitud)) *
        COS(RADIANS(longitud) - RADIANS(lng_usuario)) +
        SIN(RADIANS(lat_usuario)) * SIN(RADIANS(latitud))
    ) AS NUMERIC
), 2)
```

El alias `distancia_km` se filtra en el outer `WHERE` de la subquery —
**no** en `HAVING` — porque PostgreSQL no permite referenciar alias de SELECT
en la cláusula HAVING sin subquery.

---

## Notas

- Solo aparecen refugios con `activo = true` y coordenadas (`latitud` y `longitud` no nulos).
- Solo aparecen reportes que **no** están en estado `cerrado`.
- El radio máximo es 100 km para evitar queries excesivamente costosos.
- Si el usuario no envía coordenadas, todos los registros se devuelven sin
  campo `distancia_km` y sin ordenamiento por distancia.
- La respuesta **no** usa `MascotaResource` ni `ReporteResource` — devuelve
  los datos raw del modelo para incluir el campo calculado `distancia_km`.

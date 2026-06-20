# MascotaController

CRUD de mascotas. El listado público muestra solo mascotas disponibles con filtros.
Crear, editar y eliminar requieren ser el voluntario dueño del refugio o admin.

---

## Endpoints

### `GET /api/mascotas`

Lista mascotas disponibles (`estado_adopcion = disponible`) con filtros opcionales,
paginadas de 20 en 20. Incluye el refugio de cada mascota.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Query params**

| Param | Tipo | Ejemplo |
|---|---|---|
| `especie` | string | `perro`, `gato` |
| `sexo` | string | `macho`, `hembra` |
| `tamano` | string | `pequeno`, `mediano`, `grande` |
| `refugio_id` | integer | `1` |

**Respuesta `200`** — colección paginada de `MascotaResource`.

```json
{
  "data": [
    {
      "id": 1,
      "nombre": "Max",
      "especie": "perro",
      "raza": "Labrador Mestizo",
      "edad_meses": 18,
      "sexo": "macho",
      "tamano": "grande",
      "descripcion": "...",
      "personalidad": "jugueton,cariñoso,activo",
      "estado_salud": "Excelente",
      "vacunas": true,
      "esterilizado": false,
      "foto_url": null,
      "fotos_extra": null,
      "estado_adopcion": "disponible",
      "refugio": { "id": 1, "nombre": "Refugio Amigos Peludos", ... }
    }
  ],
  "current_page": 1,
  "total": 7
}
```

---

### `POST /api/mascotas`

Crea una mascota en el refugio del usuario autenticado. El `refugio_id` se asigna
automáticamente del refugio del voluntario; no se envía en el body.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Body**
```json
{
  "nombre": "Rocky",
  "especie": "perro",
  "raza": "Criollo",
  "edad_meses": 36,
  "sexo": "macho",
  "tamano": "grande",
  "descripcion": "Adulto tranquilo y leal.",
  "personalidad": "tranquilo,leal,protector",
  "estado_salud": "Buena",
  "vacunas": true,
  "esterilizado": true
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `nombre` | string (max 100) | Sí |
| `especie` | `perro`\|`gato` | Sí |
| `sexo` | `macho`\|`hembra` | Sí |
| `raza` | string\|null | No |
| `edad_meses` | integer 0-240\|null | No |
| `tamano` | `pequeno`\|`mediano`\|`grande`\|null | No |
| `descripcion` | string\|null | No |
| `personalidad` | string (max 255)\|null | No |
| `estado_salud` | string (max 255)\|null | No |
| `vacunas` | boolean | No (default false) |
| `esterilizado` | boolean | No (default false) |

**Respuesta `201`** — `MascotaResource`.

**Errores**
- `403` — el usuario no tiene refugio registrado

---

### `GET /api/mascotas/{mascota}`

Devuelve el detalle de una mascota con su refugio. Muestra mascotas en cualquier
estado (no solo disponibles), útil para ver el detalle de una adopción aprobada.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`** — `MascotaResource` con `refugio` cargado.

---

### `PUT /api/mascotas/{mascota}`

Actualiza los datos de una mascota. Solo puede hacerlo el voluntario dueño del
refugio al que pertenece la mascota, o un admin.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`  
**Body:** mismos campos que `POST`.

**Errores**
- `403` — el refugio del usuario no coincide con el de la mascota

---

### `DELETE /api/mascotas/{mascota}`

Elimina la mascota permanentemente de la base de datos.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Respuesta `200`**
```json
{ "message": "Mascota eliminada." }
```

**Errores**
- `403` — el refugio del usuario no coincide con el de la mascota

---

## Notas

- El estado `estado_adopcion` no se cambia desde este controller. El flujo es:
  - `disponible` → `en_proceso` al crear una solicitud (`SolicitudController@store`)
  - `en_proceso` → `adoptada` al aprobar la solicitud (`Solicitud::aprobar()`)
  - `adoptada` → `disponible` al rechazar la solicitud (`Solicitud::rechazar()`)
  - El voluntario puede forzar el cambio manual desde `PATCH /api/panel/mascotas/{mascota}/estado`
- Para subir fotos usar `POST /api/mascotas/{mascota}/foto` y `/fotos-extra` (`FotoController`).

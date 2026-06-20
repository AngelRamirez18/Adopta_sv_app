# RefugioController

CRUD de refugios de animales. Cualquier usuario autenticado puede listar y ver
refugios; crear, editar y desactivar requieren rol `voluntario` o `admin`.

---

## Endpoints

### `GET /api/refugios`

Lista todos los refugios activos. No incluye los desactivados (`activo = false`).

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`**
```json
[
  {
    "id": 1,
    "user_id": 2,
    "nombre": "Refugio Amigos Peludos",
    "direccion": "Col. Escalón, 5a Calle Pte. #225, San Salvador",
    "telefono": "2222-3001",
    "correo": "amigospeludos@adoptasv.dev",
    "descripcion": "...",
    "logo_url": null,
    "latitud": "13.7034000",
    "longitud": "-89.2389000",
    "activo": true,
    "created_at": "...",
    "updated_at": "..."
  }
]
```

---

### `POST /api/refugios`

Registra un nuevo refugio para el usuario autenticado. Un usuario solo puede
tener un refugio. Al crear el refugio, el usuario recibe automáticamente el
rol `voluntario` si no lo tenía.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Body**
```json
{
  "nombre": "Mi Refugio",
  "direccion": "Calle Principal #10, San Salvador",
  "telefono": "2222-0000",
  "correo": "refugio@ejemplo.com",
  "descripcion": "Descripción del refugio",
  "latitud": -13.6929,
  "longitud": -89.2182
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `nombre` | string (max 150) | Sí |
| `direccion` | string | Sí |
| `telefono` | string\|null | No |
| `correo` | string email\|null | No |
| `descripcion` | string\|null | No |
| `latitud` | decimal (-90 a 90)\|null | No |
| `longitud` | decimal (-180 a 180)\|null | No |

**Respuesta `201`** — el refugio creado.

**Errores**
- `422` — el usuario ya tiene un refugio registrado

---

### `GET /api/refugios/{refugio}`

Devuelve el detalle de un refugio incluyendo todas sus mascotas.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`** — refugio con relación `mascotas` cargada.

**Errores**
- `404` — refugio no encontrado

---

### `PUT /api/refugios/{refugio}`

Actualiza los datos del refugio. Solo puede hacerlo el dueño (`user_id`) o un admin.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`  
**Body:** mismos campos que `POST`.

**Errores**
- `403` — el usuario no es dueño del refugio ni admin
- `404` — refugio no encontrado

---

### `DELETE /api/refugios/{refugio}`

Desactiva el refugio (soft delete lógico — pone `activo = false`).
No elimina el registro ni sus mascotas.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Respuesta `200`**
```json
{ "message": "Refugio desactivado." }
```

**Errores**
- `403` — el usuario no es dueño del refugio ni admin

---

## Notas

- Para buscar refugios por distancia geográfica usar `GET /api/mapa/refugios`
  (`MapaController`), que aplica la fórmula de Haversine.
- Para subir el logo del refugio usar `POST /api/refugios/{refugio}/logo`
  (`FotoController@refugioLogo`).
- Un refugio desactivado no aparece en `GET /api/refugios` ni en el mapa,
  pero sus mascotas y solicitudes siguen en la base de datos.

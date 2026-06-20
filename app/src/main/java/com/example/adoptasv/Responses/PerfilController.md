# PerfilController

Gestiona el perfil del usuario autenticado. Permite ver y actualizar sus datos
personales. No gestiona la foto de perfil (eso es `FotoController`).

---

## Endpoints

### `GET /api/perfil`

Devuelve el perfil completo del usuario autenticado, incluyendo su refugio
si tiene uno registrado.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`**
```json
{
  "id": 2,
  "firebase_uid": "uid_vol_001",
  "name": "María Rodríguez",
  "email": "voluntario1@adoptasv.dev",
  "telefono": "7777-1001",
  "direccion": "Colonia Escalón, San Salvador",
  "foto_perfil_url": "https://storage.googleapis.com/...",
  "estado_cuenta": "activo",
  "roles": ["voluntario"],
  "refugio": {
    "id": 1,
    "nombre": "Refugio Amigos Peludos",
    ...
  }
}
```

`refugio` es `null` si el usuario no tiene refugio registrado.

---

### `PUT /api/perfil`

Actualiza los datos personales del usuario autenticado. Todos los campos son
opcionales (`sometimes`), por lo que el cliente puede enviar solo los que cambiaron.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Body**
```json
{
  "name": "María Rodríguez Flores",
  "telefono": "7777-9999",
  "direccion": "Col. San Benito, San Salvador",
  "foto_perfil_url": "https://..."
}
```

| Campo | Tipo | Restricciones |
|---|---|---|
| `name` | string | max 150 |
| `telefono` | string\|null | max 20 |
| `direccion` | string\|null | max 255 |
| `foto_perfil_url` | string\|null | URL válida |

**Respuesta `200`** — devuelve el usuario completo actualizado.

---

## Notas

- Para actualizar la foto de perfil con un archivo, usar `POST /api/perfil/foto`
  (`FotoController@perfilFoto`). Este endpoint solo acepta una URL ya subida.
- `firebase_uid`, `email` y `estado_cuenta` no son editables desde este endpoint.

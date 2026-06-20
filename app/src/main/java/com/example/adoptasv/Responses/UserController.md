# UserController

Panel de administración de usuarios. Todos los endpoints requieren rol `admin`.
Permite listar usuarios, ver su detalle y cambiar sus roles.

---

## Endpoints

### `GET /api/users`

Lista todos los usuarios paginados de 20 en 20, con sus roles incluidos.

**Auth requerida:** `firebase.auth` + `role:admin`

**Respuesta `200`**
```json
{
  "data": [
    {
      "id": 1,
      "name": "Admin AdoptaSV",
      "email": "admin@adoptasv.dev",
      "roles": [{ "id": 1, "name": "admin" }],
      ...
    }
  ],
  "current_page": 1,
  "total": 6,
  ...
}
```

---

### `GET /api/users/{id}`

Devuelve el detalle completo de un usuario, incluyendo todos sus permisos efectivos.

**Auth requerida:** `firebase.auth` + `role:admin`

**Respuesta `200`**
```json
{
  "id": 4,
  "firebase_uid": "seed_uid_adop_001",
  "name": "Ana García",
  "email": "adoptante1@adoptasv.dev",
  "telefono": "6666-2001",
  "estado_cuenta": "activo",
  "roles": ["adoptante"],
  "permissions": ["animals.view", "adoptions.view", "adoptions.create"],
  "creado_en": "2026-06-17 01:04:55"
}
```

**Errores**
- `404` — usuario no encontrado

---

### `PATCH /api/users/{id}/role`

Asigna un rol al usuario usando `syncRoles`, lo que reemplaza cualquier rol
anterior. Un usuario solo puede tener un rol activo a la vez.

**Auth requerida:** `firebase.auth` + `role:admin`

**Body**
```json
{
  "role": "voluntario"
}
```

| Campo | Tipo | Valores válidos |
|---|---|---|
| `role` | string | `adoptante`, `voluntario`, `admin` |

**Respuesta `200`**
```json
{
  "message": "Rol asignado correctamente.",
  "roles": ["voluntario"]
}
```

**Errores**
- `422` — el rol no existe en la base de datos
- `404` — usuario no encontrado

---

## Notas

- Este controlador no tiene endpoint para crear ni eliminar usuarios — los usuarios
  se crean automáticamente en el primer login via `FirebaseAuthenticate`.
- Para desactivar un usuario se debe cambiar `estado_cuenta` directamente en la
  base de datos o añadir un endpoint futuro a `PerfilController`.

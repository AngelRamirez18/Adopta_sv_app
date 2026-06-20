# AuthController

Alias de perfil para el cliente Android. Devuelve los datos de identidad y permisos
del usuario autenticado en el formato que espera el cliente.

---

## Endpoints

### `GET /api/auth/me`

Devuelve la identidad completa del usuario actualmente autenticado, incluyendo sus
roles y permisos de Spatie. Útil para que el cliente decida qué pantallas mostrar.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`**
```json
{
  "id": 4,
  "firebase_uid": "abc123uid",
  "name": "Ana García",
  "email": "ana@example.com",
  "roles": ["adoptante"],
  "permissions": ["animals.view", "adoptions.view", "adoptions.create"]
}
```

---

## Notas

- Es funcionalmente equivalente a `GET /api/perfil` pero devuelve menos campos
  (no incluye teléfono, dirección, refugio, etc.).
- El cliente Android lo usa en el arranque para sincronizar el estado de sesión
  sin hacer dos llamadas.
- `firebase_uid` se incluye para que el cliente pueda verificar que está hablando
  del mismo usuario que tiene en Firebase localmente.

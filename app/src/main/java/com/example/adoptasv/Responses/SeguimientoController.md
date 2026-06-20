# SeguimientoController

Gestiona los seguimientos post-adopción. Solo los adoptantes con una adopción
**aprobada** pueden crear seguimientos. Los voluntarios/admins agregan observaciones.

---

## Endpoints

### `GET /api/seguimientos/mis-seguimientos`

Devuelve todos los seguimientos enviados por el adoptante autenticado, con
mascota y solicitud cargadas.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`** — colección de `SeguimientoResource`.

---

### `POST /api/seguimientos`

Registra un nuevo seguimiento para una adopción aprobada. Validaciones:
- El adoptante debe ser el dueño de la solicitud.
- La solicitud debe tener estado `aprobada`.

El `mascota_id` se obtiene automáticamente de la solicitud, no se envía en el body.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Body**
```json
{
  "solicitud_id": 1,
  "comentario": "Rocky se adaptó muy bien. Ya reconoce su nombre y come sin problemas.",
  "estado_mascota": "Excelente"
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `solicitud_id` | integer (existe en solicitudes) | Sí |
| `comentario` | string\|null | No |
| `estado_mascota` | string\|null | No |

**Respuesta `201`** — `SeguimientoResource` con mascota.

**Errores**
- `403` — el adoptante no es el dueño de la solicitud
- `422` — la solicitud no está aprobada

---

### `GET /api/seguimientos/{seguimiento}`

Devuelve el detalle de un seguimiento con mascota, solicitud y adoptante.
Solo pueden verlo:
- El adoptante dueño del seguimiento
- El refugio de la solicitud asociada
- Un admin

**Auth requerida:** `firebase.auth`

**Respuesta `200`** — `SeguimientoResource` completo.

**Errores**
- `403` — el usuario no tiene acceso a este seguimiento

---

### `PATCH /api/seguimientos/{seguimiento}/observacion`

El refugio o admin agrega una observación sobre el estado del animal y opcionalmente
cambia el estado del seguimiento.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Body**
```json
{
  "observacion_refugio": "Todo en orden. El adoptante demuestra gran compromiso.",
  "estado_seguimiento": "revisado"
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `observacion_refugio` | string | Sí |
| `estado_seguimiento` | `revisado`\|`observado`\|null | No (default: `revisado`) |

**Respuesta `200`** — `SeguimientoResource` actualizado.

**Errores**
- `403` — el refugio del usuario no es el dueño de la solicitud

---

## Estados del seguimiento

| Estado | Descripción |
|---|---|
| `pendiente` | Enviado por el adoptante, esperando revisión del refugio |
| `revisado` | El refugio lo revisó y todo está en orden |
| `observado` | El refugio tiene comentarios o preocupaciones sobre el animal |

---

## Flujo típico

```
Adopción aprobada
      │
      │  Adoptante envía seguimiento (POST /api/seguimientos)
      ▼
  estado: pendiente
      │
      │  Voluntario revisa y agrega observación
      │  (PATCH /api/seguimientos/{id}/observacion)
      ▼
  estado: revisado | observado
```

El adoptante puede ver también todos sus seguimientos via
`GET /api/solicitudes/{solicitud}/seguimientos` (`SolicitudController`).

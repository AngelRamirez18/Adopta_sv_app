# SolicitudController

Gestiona el ciclo de vida de las solicitudes de adopción. Los adoptantes crean y
consultan sus solicitudes; los voluntarios/admin las revisan y cambian de estado.

Al crear una solicitud, el `ScoringService` calcula automáticamente un puntaje
0-100 basado en las respuestas del formulario.

---

## Endpoints

### `GET /api/solicitudes/mis-solicitudes`

Devuelve todas las solicitudes del adoptante autenticado con mascota y refugio.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Respuesta `200`** — colección de `SolicitudResource`.

---

### `POST /api/solicitudes`

Crea una solicitud de adopción para una mascota disponible. Validaciones:
- La mascota debe estar en estado `disponible`.
- El adoptante no puede tener ya una solicitud `pendiente` o `en_revision` para la misma mascota.

Al crear la solicitud:
1. La mascota pasa a `en_proceso`.
2. El `ScoringService` calcula y guarda el `puntaje_evaluacion` automáticamente.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Body**
```json
{
  "mascota_id": 1,
  "respuestas_formulario": {
    "tipo_vivienda": "casa",
    "tiene_patio": true,
    "otros_animales": false,
    "experiencia": "Tuve perros durante 10 años en mi infancia.",
    "horas_en_casa": 8,
    "compromiso": "Me comprometo a darle el mejor hogar."
  }
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `mascota_id` | integer (existe en mascotas) | Sí |
| `respuestas_formulario.tipo_vivienda` | string | Sí |
| `respuestas_formulario.tiene_patio` | boolean | Sí |
| `respuestas_formulario.otros_animales` | boolean | Sí |
| `respuestas_formulario.experiencia` | string | Sí |
| `respuestas_formulario.horas_en_casa` | integer 0-24 | Sí |
| `respuestas_formulario.compromiso` | string | Sí |

**Respuesta `201`** — `SolicitudResource` con mascota cargada y `puntaje_evaluacion` calculado.

**Errores**
- `422` — la mascota no está disponible
- `422` — ya tienes una solicitud activa para esta mascota

---

### `GET /api/solicitudes/{solicitud}`

Devuelve el detalle de una solicitud. Solo pueden verla:
- El adoptante que la envió
- El refugio al que pertenece la mascota
- Un admin

**Auth requerida:** `firebase.auth`

**Respuesta `200`** — `SolicitudResource` con mascota, adoptante y refugio.

**Errores**
- `403` — el usuario no tiene acceso a esta solicitud

---

### `GET /api/solicitudes/{solicitud}/seguimientos`

Lista los seguimientos post-adopción de una solicitud aprobada. Mismo control
de acceso que `show`.

**Auth requerida:** `firebase.auth`

**Respuesta `200`** — colección de `SeguimientoResource`.

---

### `GET /api/solicitudes`

Lista todas las solicitudes del refugio del voluntario autenticado, con filtro
opcional por estado. Un admin ve todas las solicitudes del sistema.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Query params**

| Param | Valores |
|---|---|
| `estado` | `pendiente`, `en_revision`, `aprobada`, `rechazada` |

**Respuesta `200`** — colección paginada de `SolicitudResource`.

---

### `PATCH /api/solicitudes/{solicitud}/estado`

Cambia el estado de una solicitud. Solo el refugio dueño o un admin.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Body**
```json
{
  "estado": "aprobada",
  "comentario": "Perfil excelente. Adoptante aprobado."
}
```

| `estado` | Efecto |
|---|---|
| `en_revision` | Solicitud marcada en revisión |
| `aprobada` | Llama a `Solicitud::aprobar()` → mascota pasa a `adoptada` |
| `rechazada` | Llama a `Solicitud::rechazar()` → mascota vuelve a `disponible` |

**Respuesta `200`** — `SolicitudResource` actualizada.

**Errores**
- `403` — el refugio del usuario no es el dueño de la solicitud

---

## Scoring automático

El `ScoringService` asigna puntos 0-100 al crear la solicitud:

| Criterio | Puntos |
|---|---|
| `tipo_vivienda = casa` | +25 |
| `tipo_vivienda = apartamento` | +15 |
| `tiene_patio = true` | +20 |
| Sin otros animales o con experiencia | +15 |
| Experiencia previa (texto > 20 chars) | +20 |
| `horas_en_casa >= 8` | +20 |
| `horas_en_casa >= 6` | +15 |
| `horas_en_casa >= 4` | +8 |

El puntaje se guarda en `solicitudes.puntaje_evaluacion` y es visible en el panel
del refugio para ayudar a priorizar candidatos.

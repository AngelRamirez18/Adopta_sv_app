# PanelRefugioController

Dashboard privado del refugio. Todos los endpoints requieren rol `voluntario`
o `admin` y operan sobre el refugio asociado al usuario autenticado.

Un admin ve datos globales en algunos endpoints; un voluntario solo ve los datos
de su refugio.

---

## Endpoints

### `GET /api/panel/resumen`

Resumen estadístico rápido para el dashboard: conteo de mascotas por estado,
solicitudes por estado, y seguimientos pendientes.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Respuesta `200`**
```json
{
  "mascotas": {
    "disponibles": 5,
    "en_proceso": 2,
    "adoptadas": 1
  },
  "solicitudes": {
    "pendientes": 3,
    "en_revision": 1,
    "aprobadas": 4
  },
  "seguimientos_pendientes": 2
}
```

**Errores**
- `403` — el usuario no tiene un refugio registrado

---

### `GET /api/panel/mascotas`

Lista todas las mascotas del refugio (en todos los estados), filtrable por estado.
A diferencia de `GET /api/mascotas` público, incluye mascotas en proceso y adoptadas.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Query params**

| Param | Valores |
|---|---|
| `estado` | `disponible`, `en_proceso`, `adoptada` |

**Respuesta `200`** — colección paginada de `MascotaResource`.

---

### `PATCH /api/panel/mascotas/{mascota}/estado`

Cambia el estado de adopción de una mascota manualmente. Útil para corregir
estados o marcar como disponible una mascota cuya solicitud fue cancelada fuera
del flujo normal.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Body**
```json
{ "estado": "disponible" }
```

| Valor | Significado |
|---|---|
| `disponible` | La mascota puede recibir solicitudes |
| `en_proceso` | Tiene una solicitud activa |
| `adoptada` | Ya fue adoptada |

**Respuesta `200`** — `MascotaResource` actualizada con refugio.

**Errores**
- `403` — la mascota no pertenece al refugio del usuario

---

### `GET /api/panel/mascotas/{mascota}/solicitudes`

Lista todas las solicitudes recibidas para una mascota específica del refugio,
con los datos del adoptante incluidos. Útil para comparar candidatos.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Respuesta `200`** — colección de `SolicitudResource` con adoptante.

**Errores**
- `403` — la mascota no pertenece al refugio del usuario

---

### `GET /api/panel/solicitudes`

Lista todas las solicitudes del refugio con filtro opcional por estado.
Incluye mascota y adoptante. Paginado de 20 en 20.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Query params**

| Param | Valores |
|---|---|
| `estado` | `pendiente`, `en_revision`, `aprobada`, `rechazada` |

**Respuesta `200`** — colección paginada de `SolicitudResource`.

---

### `GET /api/panel/seguimientos`

Lista los seguimientos post-adopción de las solicitudes del refugio.
Por defecto muestra solo los `pendientes` de revisión; se puede filtrar por estado.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`

**Query params**

| Param | Valores | Default |
|---|---|---|
| `estado` | `pendiente`, `revisado`, `observado` | `pendiente` |

**Respuesta `200`** — colección paginada de `SeguimientoResource` con mascota,
adoptante y solicitud.

---

## Notas

- Para aprobar/rechazar una solicitud usar `PATCH /api/solicitudes/{solicitud}/estado`
  (`SolicitudController`), no este panel.
- Para agregar observaciones a un seguimiento usar
  `PATCH /api/seguimientos/{seguimiento}/observacion` (`SeguimientoController`).
- Este panel solo es de lectura y gestión de estado — no crea ni elimina recursos.

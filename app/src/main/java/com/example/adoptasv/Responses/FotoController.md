# FotoController

Sube imágenes a Firebase Storage y actualiza la URL correspondiente en la base
de datos. Todos los endpoints reciben un archivo `multipart/form-data`.

El servicio subyacente es `FirebaseStorageService`, que genera una URL pública
firmada y elimina la imagen anterior si existía.

---

## Endpoints

### `POST /api/perfil/foto`

Sube la foto de perfil del usuario autenticado.

**Auth requerida:** `firebase.auth`  
**Roles:** cualquiera

**Body** `multipart/form-data`

| Campo | Tipo | Restricciones |
|---|---|---|
| `foto` | image | max 2 MB, jpg/jpeg/png/webp |

**Respuesta `200`**
```json
{ "foto_perfil_url": "https://storage.googleapis.com/..." }
```

**Comportamiento:** elimina la foto anterior si existía antes de subir la nueva.  
**Path en Storage:** `perfiles/{user_id}`

---

### `POST /api/mascotas/{mascota}/foto`

Sube la foto principal de una mascota.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`  
**Control de acceso:** el refugio del usuario debe coincidir con el de la mascota.

**Body** `multipart/form-data`

| Campo | Tipo | Restricciones |
|---|---|---|
| `foto` | image | max 5 MB, jpg/jpeg/png/webp |

**Respuesta `200`**
```json
{ "foto_url": "https://storage.googleapis.com/..." }
```

**Comportamiento:** reemplaza `mascotas.foto_url`. Elimina la imagen anterior.  
**Path en Storage:** `mascotas/{mascota_id}`

---

### `POST /api/mascotas/{mascota}/fotos-extra`

Agrega una foto adicional a la galería de una mascota (campo `fotos_extra` JSON).
No elimina las existentes — acumula.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`  
**Control de acceso:** mismo que `/foto`.

**Body** `multipart/form-data`

| Campo | Tipo | Restricciones |
|---|---|---|
| `foto` | image | max 5 MB, jpg/jpeg/png/webp |

**Respuesta `200`**
```json
{
  "foto_url": "https://storage.googleapis.com/...",
  "fotos_extra": [
    "https://storage.googleapis.com/.../1",
    "https://storage.googleapis.com/.../2"
  ]
}
```

**Path en Storage:** `mascotas/{mascota_id}/extra`

---

### `POST /api/reportes/{reporte}/foto`

Sube la foto de evidencia de un reporte SOS.

**Auth requerida:** `firebase.auth`  
**Control de acceso:** solo el usuario que creó el reporte o un admin.

**Body** `multipart/form-data`

| Campo | Tipo | Restricciones |
|---|---|---|
| `foto` | image | max 5 MB, jpg/jpeg/png/webp |

**Respuesta `200`**
```json
{ "foto_url": "https://storage.googleapis.com/..." }
```

**Path en Storage:** `reportes/{reporte_id}`

---

### `POST /api/refugios/{refugio}/logo`

Sube el logo de un refugio.

**Auth requerida:** `firebase.auth` + `role:admin|voluntario`  
**Control de acceso:** solo el dueño del refugio o un admin.

**Body** `multipart/form-data`

| Campo | Tipo | Restricciones |
|---|---|---|
| `logo` | image | max 2 MB, jpg/jpeg/png/webp |

**Respuesta `200`**
```json
{ "logo_url": "https://storage.googleapis.com/..." }
```

**Path en Storage:** `refugios/{refugio_id}`

---

### `POST /api/seguimientos/{seguimiento}/foto`

Sube una foto de evidencia de un seguimiento post-adopción.

**Auth requerida:** `firebase.auth`  
**Control de acceso:** solo el adoptante dueño del seguimiento.

**Body** `multipart/form-data`

| Campo | Tipo | Restricciones |
|---|---|---|
| `foto` | image | max 5 MB, jpg/jpeg/png/webp |

**Respuesta `200`**
```json
{ "foto_url": "https://storage.googleapis.com/..." }
```

**Path en Storage:** `seguimientos/{seguimiento_id}`

---

## Errores comunes

| Código | Causa |
|---|---|
| `403` | El usuario no tiene permiso sobre el recurso |
| `422` | El archivo no es una imagen válida o excede el tamaño permitido |

---

## Notas

- Las URLs generadas son públicas y permanentes (no expiran). Firebase Storage
  las sirve directamente al cliente, sin pasar por la API.
- El campo de foto en la base de datos almacena solo la URL, no el archivo.
- Si Firebase Storage no está configurado (`FIREBASE_STORAGE_DEFAULT_BUCKET` vacío),
  los uploads fallarán con un error 500 en producción.

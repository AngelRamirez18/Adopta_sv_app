# AdoptaSV

App Android nativa (Java) de adopción de mascotas en El Salvador. Backend REST en
Laravel consumido con Retrofit + OkHttp; autenticación con Firebase (email/password
y Google Sign-In). Todo recurso de la API viene envuelto en `{ "data": ... }`.

- **Build:** Gradle con version catalog (`gradle/libs.versions.toml`), AGP 8.13.2, compileSdk 36, minSdk 25, Java 11.
- **Paquete:** `com.example.adoptasv`
- **Backend:** `https://api-adoptasv-production.up.railway.app/api/`

---

## Flujo de entrada y enrutamiento por rol

```
SplashActivity → MainActivity (login Firebase)
                      │
                      │ getMe() → roles[]
                      ▼
        ┌─────────────┴───────────────┐
   admin │        voluntario │   adoptante (roles nulo/vacío)
        ▼                    ▼                    ▼
  AdminActivity      VoluntarioActivity      HomeActivity
```

El rol se resuelve en `MainActivity.navegarSegunRol`. **Un `roles` nulo o vacío se
trata como cliente adoptante.** Cada activity tiene su propio `BottomNavigationView`.

---

## 🟤 Módulo CLIENTE / ADOPTANTE — `HomeActivity`

El usuario final que busca adoptar. Bottom nav de 4 tabs:

| Tab | Pantalla | Qué hace |
|-----|----------|----------|
| **Inicio** | `HomeFragment` | Catálogo de mascotas disponibles (grid + chips de filtro). `GET /mascotas` |
| **Favoritos** | `AdoptarFragment` | Catálogo en lista con filtros y búsqueda local |
| **Mi actividad** | `HubFragment` | Menú-hub hacia las pantallas sin tab propio |
| **Perfil** | `PerfilFragment` | Vista de adoptante (ver sección compartida) |

Desde el **Hub** se accede a las pantallas secundarias:

- **`MisSolicitudesFragment`** — sus solicitudes de adopción (`GET /solicitudes/mis-solicitudes`)
- **`MapaFragment`** — mapa de refugios y reportes cercanos (Google Maps)
- **`SeguimientoFragment`** — seguimientos post-adopción
- **`DetallesMascotaFragment`** — detalle de una mascota + crear solicitud de adopción
- **`PanelFragment`** — solo si el usuario además es voluntario/admin

**Acciones clave:** explorar/filtrar mascotas, ver detalle, **enviar solicitud de
adopción**, dar seguimiento, crear reportes SOS y ver refugios en el mapa.

---

## 🟠 Módulo VOLUNTARIO — `VoluntarioActivity`

Gestor de **su propio refugio**. Módulo dedicado, separado del admin. Bottom nav:
**Inicio · Mascotas · Solicitudes · Reportes · Perfil**.

| Tab | Pantalla | Qué hace |
|-----|----------|----------|
| **Inicio** | `VoluntarioDashboardFragment` | Stats del refugio (disponibles / solicitudes / seguimientos) + accesos rápidos. Subtítulo con el nombre del refugio |
| **Mascotas** | `AdminMascotasFragment` | Lista del refugio con chips por estado; FAB nueva; editar; cambiar estado. `GET /panel/mascotas?refugio_id=` |
| **Solicitudes** | `PanelFragment` | Solicitudes de su refugio; aprobar/rechazar con comentario; detalle del adoptante. `GET /solicitudes` |
| **Reportes** | `AdminReportesFragment` | Reportes SOS; cambiar estado |
| **Perfil** | `PerfilFragment` | Sección "Gestión del refugio" |

**Crear/editar mascota** (`CrearMascotaFragment`): formulario + subida de fotos
multipart. El módulo **no** incluye Usuarios ni CRUD de refugios. El backend acota
automáticamente todos los datos al refugio del voluntario.

> Reutiliza los fragments de gestión y conserva el id de contenedor `adminContainer`
> y un `setTab(menuId)` propio para no romper su navegación interna.

---

## 🔴 Módulo ADMIN — `AdminActivity`

Administración general del sistema. Mismo bottom nav que el voluntario, pero con
alcance global y opciones extra:

| Tab | Pantalla | Diferencia vs. voluntario |
|-----|----------|---------------------------|
| **Inicio** | `AdminDashboardFragment` | Igual + card **Usuarios** + card **Refugios** |
| **Mascotas** | `AdminMascotasFragment` | Compartido |
| **Solicitudes** | `PanelFragment` | Ve **todas** las solicitudes del sistema |
| **Reportes** | `AdminReportesFragment` | Todos los reportes |
| **Perfil** | `PerfilFragment` | Sección "Administración" |

**Exclusivo del admin** (desde el dashboard / perfil):

- **`AdminUsuariosFragment`** — lista de usuarios y cambio de rol (`GET /users`, `PATCH /users/{id}/role`)
- **`AdminRefugiosFragment` / `CrearRefugioFragment`** — CRUD del refugio propio

---

## ⚪ Compartido entre los tres módulos

- **`PerfilFragment`** — única vista realmente común; **se adapta por rol**
  (`aplicarVistaPorRol`): Administración / Gestión del refugio / secciones de
  adoptante. Incluye badge de rol; la sección de Configuración (Cerrar sesión) es
  siempre visible.
- **Capa de red** — `ApiClient` (singleton Retrofit) + `FirebaseTokenInterceptor`
  que inyecta el Bearer token de Firebase. `ApiService` define todos los endpoints.
- **Modelos** — `Mascota`, `User`, `UsuarioAdmin`, `Refugio`, `Reporte`,
  `Seguimiento`, `Solicitud`, y los wrappers `SingleResponse<T>` /
  `PaginatedResponse<T>`.
- **Diseño** — paleta Material 3 marrón/naranja (`md_*`, `adopta_*`) consistente en
  todos los módulos.

---

## Resumen en una frase

El **cliente** busca y solicita adopciones; el **voluntario** gestiona las mascotas,
solicitudes y reportes de su refugio; el **admin** hace todo lo anterior a nivel
global, más la administración de usuarios y refugios.

---

## Estructura del proyecto

```
app/src/main/java/com/example/adoptasv/
├── SplashActivity · MainActivity · HomeActivity · AdminActivity · VoluntarioActivity
├── Fragments/      → pantallas (Home, Adoptar, Hub, Perfil, dashboards, paneles, etc.)
├── Adaptadores/    → adapters de RecyclerView
├── Conexion/       → ApiClient, ApiService, interceptores
│   └── Modelos/    → modelos de datos + wrappers de respuesta
└── Util/           → utilidades (fechas, estados, multipart)
```

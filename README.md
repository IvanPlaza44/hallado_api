# hallado-api

Backend del TPO **Hallado** (objetos perdidos / encontrados en el barrio). Reutiliza la
infraestructura genérica del backend de Ronda (JWT, OTP por mail, carga de imágenes a
Cloudinary, Swagger) en un repositorio nuevo, y agrega sobre eso todo el dominio propio
de Hallado: publicaciones de "perdí algo" / "encontré algo", reclamos, seguimientos,
valoraciones/reputación y búsquedas guardadas (alertas).

## Stack

- Java 17 + Spring Boot 3.3.4 (Web, Data JPA, Security, Validation, Mail)
- MySQL (prod/dev) — Hibernate con `ddl-auto=update`, así que las tablas se crean solas
- JWT (jjwt 0.12.5) para la sesión, sin estado en el servidor
- Cloudinary para las fotos (de publicaciones y de perfil)
- Gmail SMTP para los mails de OTP (registro/login y recupero de contraseña)
- springdoc-openapi (Swagger UI) para explorar y probar la API

## Puesta en marcha

### 1. Variables de entorno

| Variable | Para qué | Default si no se define |
|---|---|---|
| `DB_URL` | JDBC de MySQL | `jdbc:mysql://localhost:3306/hallado?...&createDatabaseIfNotExist=true` |
| `DB_USER` / `DB_PASSWORD` | credenciales de MySQL | `hallado` / `hallado` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | cuenta Gmail que envía los OTP (usar una **contraseña de aplicación**, no la contraseña normal de la cuenta) | — (obligatorias) |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | credenciales de Cloudinary para subir fotos | — (obligatorias) |
| `PORT` | puerto HTTP | `8080` |

El JWT secret y los tiempos de expiración de JWT/OTP están en
`src/main/resources/application.properties` (`app.jwt.secret`, `app.jwt.expiration-ms`,
`app.otp.expiration-minutes`, `app.otp.length`) — para la entrega del TPO alcanza con
dejarlos como están; si se sube a un entorno real, cambiar `app.jwt.secret`.

### 2. Levantar la base

Lo más simple es tener un MySQL corriendo localmente con una base `hallado` (o dejar que
`createDatabaseIfNotExist=true` la cree sola) y un usuario con los permisos de `DB_USER`.
También se puede levantar con Docker:

```bash
docker run -d --name hallado-mysql -e MYSQL_DATABASE=hallado \
  -e MYSQL_USER=hallado -e MYSQL_PASSWORD=hallado -e MYSQL_ROOT_PASSWORD=root \
  -p 3306:3306 mysql:8
```

### 3. Correr la aplicación

```bash
export MAIL_USERNAME=...
export MAIL_PASSWORD=...
export CLOUDINARY_CLOUD_NAME=...
export CLOUDINARY_API_KEY=...
export CLOUDINARY_API_SECRET=...

mvn spring-boot:run
```

O con el `Dockerfile` incluido (multi-stage, build con Maven y runtime en JRE):

```bash
docker build -t hallado-api .
docker run -p 8080:8080 --env-file .env hallado-api
```

### 4. Swagger

Con la app corriendo, la documentación interactiva de todos los endpoints está en:

```
http://localhost:8080/swagger-ui.html
```

Ahí se puede autenticar con el botón "Authorize" pegando `Bearer <token>` y probar cada
endpoint directamente.

## Cómo se autentica

Hay dos caminos para obtener el JWT, igual que en Ronda:

1. **Usuario + contraseña**: `POST /api/auth/registro` → `POST /api/auth/login`.
2. **Sólo con el mail (OTP)**: `POST /api/auth/otp/solicitar` → el usuario recibe un
   código de 6 dígitos por mail → `POST /api/auth/otp/verificar` con ese código, que
   devuelve el JWT y crea el usuario si todavía no existía.

En ambos casos, el token se manda en cada request protegido como:

```
Authorization: Bearer <token>
```

## Endpoints

Todas las rutas empiezan con `/api`. "Auth" = requiere el header `Authorization: Bearer
<token>` de un usuario logueado. "Público" = no requiere login (si se manda el token
igual se usa para personalizar la respuesta, pero no es obligatorio).

### Autenticación — `/api/auth`

| Método y ruta | Auth | Body | Qué hace |
|---|---|---|---|
| `POST /otp/solicitar` | Público | `{ "email" }` | Genera y manda por mail un código OTP de login/registro |
| `POST /otp/reenviar` | Público | `{ "email" }` | Reenvía el OTP de login/registro |
| `POST /otp/verificar` | Público | `{ "email", "codigo" }` | Valida el código; crea el usuario si no existía y devuelve `{ token, usuarioId, email, username }` |
| `POST /login` | Público | `{ "usernameOrEmail", "password" }` | Login clásico, devuelve el JWT |
| `POST /registro` | Público | `{ "usernameOrEmail" (=email), "password" }` | Crea un usuario con contraseña (username = email por ahora, se puede cambiar después desde el perfil) |
| `POST /password/recuperar` | Público | `{ "email" }` | Si el mail existe, manda un OTP de recuperación (no avisa si el mail no existe, para no filtrar qué cuentas están registradas) |
| `POST /password/recuperar/reenviar` | Público | `{ "email" }` | Reenvía el OTP de recuperación |
| `POST /password/restablecer` | Público | `{ "email", "codigo", "passwordNueva" }` | Valida el OTP de recuperación y define la nueva contraseña |

> El código de recuperación de contraseña y el código de login/registro son
> independientes (campo `proposito` en `CodigoOtp`): uno no sirve para la otra operación.

### Perfil / usuario — `/api/usuarios`

| Método y ruta | Auth | Body / params | Qué hace |
|---|---|---|---|
| `GET /me` | Auth | — | Perfil propio completo: datos, reputación y lista de insignias |
| `PUT /me` | Auth | `{ "nombre"?, "telefono"?, "telefonoVisible"?, "barrio"?, "username"?, "password"? }` (todos opcionales, sólo se actualiza lo que se manda) | Edita el perfil propio |
| `POST /me/foto` | Auth | `multipart/form-data`, campo `archivo` | Sube/cambia la foto de perfil (Cloudinary) |
| `GET /{id}` | Público | — | Perfil público de otro usuario: sin teléfono ni mail, con reputación e insignias |

### Categorías — `/api/categorias`

| Método y ruta | Auth | Qué hace |
|---|---|---|
| `GET /` | Público | Devuelve la lista fija de categorías (`DOCUMENTOS`, `LLAVES`, `ELECTRONICA`, `MASCOTAS`, `ROPA`, `OTROS`) para armar el selector en el front |

### Publicaciones — `/api/publicaciones`

Una publicación es "perdí algo" (`PERDIDO`) o "encontré algo" (`ENCONTRADO`).

| Método y ruta | Auth | Body / params | Qué hace |
|---|---|---|---|
| `GET /` | Público | query params: `tipo`, `categoria`, `palabrasClave`, `barrio`, `rango` (`HOY`/`ULTIMOS_3_DIAS`/`ULTIMA_SEMANA`/`RANGO`), `fechaDesde`, `fechaHasta`, `orden` (`RECIENTES`/`CERCANOS`), `pagina`, `tamanio` | Búsqueda guiada paginada. **Importante**: `tipo` es lo que el usuario busca ("perdí algo" o "encontré algo"), y el backend automáticamente muestra las publicaciones del tipo **opuesto** (si buscás porque perdiste algo, te muestra lo que la gente encontró, y viceversa) |
| `GET /{id}` | Público (mejora con login) | — | Detalle completo. Si el que mira está logueado, además devuelve si la publicación es propia, si la sigue y el estado de su propio reclamo (si hizo uno). La `direccionExacta` sólo se devuelve al publicador o a quien tuvo un reclamo **aceptado** |
| `POST /` | Auth | `{ "tipo", "categoria"?, "titulo"?, "descripcion"?, "palabrasClave"?, "fechaSuceso"?, "franjaHoraria"?, "barrio"?, "latitud"?, "longitud"?, "direccionExacta"? }` (sólo `tipo` es obligatorio) | Crea la publicación. Pensada para una carga guiada: se puede crear con lo mínimo y completar los demás campos con `PUT /{id}` en pasos siguientes |
| `PUT /{id}` | Auth (dueño) | mismo body que el `POST`, todos los campos opcionales | Edita/completa campos de la publicación (sólo pisa los campos que vienen no-nulos) |
| `POST /{id}/fotos` | Auth (dueño) | `multipart/form-data`, campo `archivo` | Agrega una foto más a la publicación (se pueden mandar varias, una por request) |
| `DELETE /{id}` | Auth (dueño) | — | Borra la publicación |
| `GET /mias` | Auth | — | Lista las publicaciones propias (abiertas, en reclamo y resueltas) |
| `POST /{id}/reclamos` | Auth | `{ "mensaje" }` | Reclama una publicación ajena ("es mío" / "lo encontré", según el `tipo` de la publicación). No se puede reclamar la propia, ni si ya está resuelta, ni dos veces en paralelo |
| `GET /{id}/reclamos` | Auth (dueño) | — | Lista los reclamos recibidos sobre una publicación propia |
| `PATCH /{id}/reclamos/{reclamoId}/aceptar` | Auth (dueño) | — | Acepta un reclamo: lo marca `ACEPTADO`, rechaza automáticamente los demás reclamos pendientes de esa publicación, y marca la publicación como `RESUELTA` (a partir de ahí el reclamante aceptado puede ver la `direccionExacta`) |
| `PATCH /{id}/reclamos/{reclamoId}/rechazar` | Auth (dueño) | — | Rechaza un reclamo puntual; si no quedan más reclamos pendientes, la publicación vuelve a `ABIERTA` |
| `POST /{id}/seguir` | Auth | — | Sigue una publicación (para enterarse de novedades) |
| `DELETE /{id}/seguir` | Auth | — | Deja de seguirla |

Estados de una publicación: `ABIERTA` → `EN_RECLAMO` (tiene al menos un reclamo
pendiente) → `RESUELTA` (un reclamo fue aceptado).

### Valoraciones — `/api/valoraciones`

| Método y ruta | Auth | Body | Qué hace |
|---|---|---|---|
| `POST /` | Auth | `{ "positiva": true/false, "comentario"?, "publicacionId" }` | Califica a la otra parte de una devolución ya resuelta (publicador y reclamante aceptado se pueden calificar entre sí, una sola vez cada uno por publicación) |

La reputación (positivas/negativas) y las insignias (hoy: "Vecino solidario" a partir de
5 devoluciones concretadas) **no se guardan**, se calculan al vuelo a partir de las
publicaciones resueltas y los reclamos aceptados — así nunca quedan desactualizadas.

### Búsquedas guardadas (alertas) — `/api/busquedas-guardadas`

| Método y ruta | Auth | Body | Qué hace |
|---|---|---|---|
| `POST /` | Auth | `{ "nombre", "tipo"?, "categoria"?, "palabrasClave"?, "barrio"?, "rangoFecha"?, "fechaDesde"?, "fechaHasta"? }` | Guarda los filtros de una búsqueda para no repetirlos |
| `GET /` | Auth | — | Lista las búsquedas guardadas propias; cada una trae `hayNovedades: true/false` según si apareció algo nuevo desde la última vez que se revisó |
| `PATCH /{id}/revisar` | Auth | — | Marca la búsqueda como revisada ahora (resetea el contador de novedades) |
| `DELETE /{id}` | Auth | — | Borra la búsqueda guardada |

## Decisiones de diseño que vale la pena explicar

- **Por qué un repo nuevo y no el mismo backend de Ronda**: son dos entregas académicas
  distintas con dominios distintos (productos en venta vs. objetos perdidos/encontrados).
  Se reutilizó la infraestructura genérica (seguridad, JWT, OTP, mail, Cloudinary,
  manejo de errores, Swagger) copiándola a un proyecto propio en vez de agregar el
  dominio de Hallado adentro del repo de Ronda, para no mezclar ambos TPOs en un mismo
  código ni arrastrar tablas/entidades que no aplican.
- **Búsqueda "tipo opuesto"**: pedido del enunciado — si alguien perdió algo, lo que le
  sirve ver son las publicaciones de objetos *encontrados*, y viceversa. Esa inversión se
  hace en `PublicacionService.buscar(...)` y en `BusquedaGuardadaService` para que las
  alertas guardadas respeten la misma lógica.
- **`direccionExacta` oculta por default**: la ubicación exacta de la entrega sólo se
  revela cuando ya hay compromiso de las dos partes (reclamo aceptado), para no exponer
  domicilios a cualquiera que mire una publicación pública.
- **Reputación e insignias no persistidas**: se calculan en `ReputacionService` a partir
  de los datos reales (publicaciones resueltas, reclamos aceptados, valoraciones) en el
  momento de la consulta, en vez de guardar un contador que podría desincronizarse.
- **OTP con `proposito`**: el mismo mecanismo de códigos por mail se reutiliza para login
  y para recuperar contraseña, pero con un campo (`REGISTRO` / `RECUPERACION`) que impide
  que el código de una operación sirva para la otra.

## Pendiente / posibles mejoras futuras

- Ordenar por cercanía real (`orden=CERCANOS`) requeriría una consulta geoespacial
  (por ejemplo con la fórmula de Haversine en la query); hoy ese orden cae de nuevo a
  "más recientes" como aproximación simple.
- No hay un endpoint de notificaciones push para las búsquedas guardadas: hoy
  `hayNovedades` se calcula on-demand cuando el usuario lista sus alertas.

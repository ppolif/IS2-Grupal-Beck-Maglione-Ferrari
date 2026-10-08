# Tinder de Mascota - API REST (Ejercicio 2.f)

Este proyecto es una evolución y transformación del proyecto Tinder de Mascota, adaptado a una **arquitectura basada en API REST**, implementando **MapStruct** para el mapeo objeto-DTO y **RestTemplate** para el consumo HTTP de los servicios REST.

---

## 🛠️ Tecnologías y Librerías Utilizadas

- **Java 21** & **Spring Boot**
- **Spring Data JPA** & **Hibernate** (con Envers para auditoría)
- **MapStruct 1.5.5.Final**: Mapeo declarativo y tipado en tiempo de compilación entre entidades y DTOs.
- **RestTemplate**: Cliente HTTP de Spring para consumir la API REST.
- **MySQL**: Base de datos dedicada e independiente (`tinder_rest`).
- **Spring Validation**: Validación de datos de entrada (`@Valid`, `@NotBlank`, etc.).
- **Spring Security**: Configurado para permitir acceso directo a endpoints REST (`/api/**`) y recursos públicos.

---

## 🗄️ Base de Datos Independiente

Para evitar cualquier conflicto con la versión anterior de Tinder, se configuró una base de datos nueva:
- **Base de datos**: `tinder_rest`
- **Configuración en `application.properties`**:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/tinder_rest?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
  spring.datasource.username=root
  spring.datasource.password=1234
  ```
- **Inicialización de datos**: La clase `DataInitializer` se ejecuta al iniciar la aplicación y puebla automáticamente zonas ("Zona Centro", "Zona Norte", etc.), usuarios y mascotas de demostración en `tinder_rest`.

---

## 📦 DTOs y Mappers (MapStruct)

Ubicados en el paquete `com.example.tinder.dto` y `com.example.tinder.mappers`:

- **Zonas**:
  - `ZonaDto`, `ZonaRequestDto`
  - Mapper: `ZonaMapper`
- **Usuarios**:
  - `UsuarioDto`, `UsuarioRegistroDto`, `UsuarioUpdateDto`
  - Mapper: `UsuarioMapper` (mapea relaciones, rol a String y fotoId)
- **Mascotas**:
  - `MascotaDto`, `MascotaRequestDto`
  - Mapper: `MascotaMapper` (mapea usuarioId, usuarioNombre, fotoId)
- **Votos**:
  - `VotoDto`, `VotoRequestDto`, `VotoRespuestaDto`
  - Mapper: `VotoMapper` (mapea IDs y nombres de mascotas)
- **Fotos**:
  - `FotoDto`
  - Mapper: `FotoMapper`

---

## 🌐 Endpoints de la API REST

### 1. Zonas (`/api/zonas`)
- `GET /api/zonas` - Listar todas las zonas.
- `GET /api/zonas/{id}` - Obtener zona por ID.
- `POST /api/zonas` - Crear una nueva zona (recibe `ZonaRequestDto`).

### 2. Usuarios (`/api/usuarios`)
- `GET /api/usuarios` - Listar todos los usuarios.
- `GET /api/usuarios/{id}` - Obtener usuario por ID.
- `POST /api/usuarios` - Registrar usuario en formato JSON (`UsuarioRegistroDto`).
- `POST /api/usuarios/con-foto` - Registrar usuario con archivo de foto (`multipart/form-data`).
- `PUT /api/usuarios/{id}` - Modificar datos de usuario (`UsuarioUpdateDto`).
- `POST /api/usuarios/{id}/foto` - Actualizar foto de perfil (`multipart/form-data`).
- `PATCH /api/usuarios/{id}/deshabilitar` - Dar de baja usuario.
- `PATCH /api/usuarios/{id}/habilitar` - Reactivar usuario.

### 3. Mascotas (`/api/mascotas`)
- `GET /api/mascotas` - Listar todas las mascotas activas.
- `GET /api/mascotas/{id}` - Obtener mascota por ID.
- `GET /api/mascotas/usuario/{usuarioId}` - Listar mascotas pertenecientes a un usuario.
- `POST /api/mascotas` - Crear mascota en formato JSON (`MascotaRequestDto`).
- `POST /api/mascotas/con-foto` - Crear mascota con archivo de foto (`multipart/form-data`).
- `PUT /api/mascotas/{id}` - Modificar mascota (`MascotaRequestDto`).
- `POST /api/mascotas/{id}/foto` - Actualizar foto de mascota.
- `DELETE /api/mascotas/{id}?idUsuario=...` - Dar de baja mascota.

### 4. Votos (`/api/votos`)
- `GET /api/votos` - Listar todos los votos registrados.
- `GET /api/votos/recibidos/{idMascota}` - Votos recibidos por una mascota.
- `GET /api/votos/propios/{idMascota}` - Votos emitidos por una mascota.
- `POST /api/votos` - Emitir un voto (`VotoRequestDto`: idUsuario, idMascota1, idMascota2).
- `POST /api/votos/responder` - Responder / corresponder un voto (`VotoRespuestaDto`: idUsuario, idVoto).

### 5. Fotos (`/api/fotos`)
- `GET /api/fotos/{id}` - Obtener bytes de imagen por ID con Content-Type correspondiente.
- `GET /api/fotos/usuario/{id}` - Obtener foto de un usuario.
- `GET /api/fotos/mascota/{id}` - Obtener foto de una mascota.
- `POST /api/fotos` - Subir una foto directamente.

---

## 📡 Cliente RestTemplate

Se implementó el cliente `TinderRestClient` (`com.example.tinder.cliente.TinderRestClient`) y su configuración `RestTemplateConfig`:
- Utiliza `RestTemplate` para comunicarse con los endpoints de la API.
- Deserializa automáticamente los JSON devueltos en las clases DTOs correspondientes mapeadas por MapStruct.
- **Controlador de consumo demostrativo**: `ClienteConsumoRestController` (`/api/consumo/...`), que permite interactuar con el cliente RestTemplate directamente por HTTP (e.g. `GET /api/consumo/mascotas`, `GET /api/consumo/usuarios`, `POST /api/consumo/votar`).

---

## 🧪 Pruebas Unitarias e Integración

Se agregaron tests automatizados:
1. `MapStructMappersTest`: Verifica el mapeo bidireccional correcto de todos los mappers entre Entidades y DTOs.
2. `TinderRestApiTest`: Levanta el contexto completo en puerto dinámico y ejecuta un ciclo de vida completo mediante `RestTemplate` (creación de zonas, registro de usuarios, creación de mascotas, emisión y correspondencia de votos).

Para ejecutar las pruebas:
```bash
./mvnw test
```


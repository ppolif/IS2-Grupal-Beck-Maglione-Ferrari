# Implementación de Auth0 (Frontend y Backend) — Dulce Mayor

Este documento detalla todas las modificaciones implementadas tanto en `frontend-institucional` como en el `backend` Spring Boot para integrar **Auth0** (Login Social con Google y Universal Login), junto con las justificaciones técnicas y de arquitectura de por qué se decidió implementarlo de esta manera.

---

## 1. Objetivo y Alcance

Brindar a los clientes minoristas una experiencia de inicio de sesión y registro moderna, amigable y ágil ("login social con un clic"), sin fricción de recordar contraseñas locales y cumpliendo con las políticas de seguridad y el modelo de dominio de Dulce Mayor (`Usuario.proveedorAuth`).

El alcance abarca:
1. **Frontend Institucional:** Integración con `@auth0/auth0-react`, botones de login social, redirección con PKCE, `AuthContext` como fachada de sesión, y adaptación de la interfaz (Perfil, Navbar) a las reglas de usuarios externos.
2. **Backend Spring Boot:** Implementación del endpoint público `POST /api/v1/auth/login-externo` según `04-openapi.yaml`, decodificación segura del token de identidad (OIDC JWT / claims de Auth0), persistencia del `Usuario` (con rol `MINORISTA` y `proveedorAuth`) y vinculación automática con la ficha de `Minorista` en MySQL, emitiendo el token JWT propio de Dulce Mayor.
3. **Docker & Entorno:** Inyección de variables de compilación en `docker-compose.yml` (`build.args`) y soporte en `vite.config.js` para leer el `.env` raíz.

---

## 2. Decisiones de Diseño y Arquitectura (¿Por qué así?)

### 2.1. Uso del SDK Oficial `@auth0/auth0-react` con PKCE
* **Decisión:** Se integró `@auth0/auth0-react` versión `^2.30.0`.
* **Justificación:** Al ser una Single Page Application (React 19 + Vite), el flujo seguro por estándar OAuth2 / OIDC es **Authorization Code Flow with Proof Key for Code Exchange (PKCE)**. Este flujo no requiere ni debe exponer un `Client Secret` en el navegador del cliente.

### 2.2. Patrón de Proveedor Resiliente (`Auth0ProviderWithConfig.jsx`)
* **Decisión:** No se montó directamente el `<Auth0Provider>` nativo en la raíz de la aplicación sin validación previa. En su lugar, se creó el componente envoltorio `Auth0ProviderWithConfig`.
* **Justificación:** Si las variables de entorno `VITE_AUTH0_DOMAIN` o `VITE_AUTH0_CLIENT_ID` no están configuradas (por ejemplo, en un entorno recién clonado o en una instancia local antes de cargar credenciales), el SDK nativo de Auth0 arroja un error fatal en tiempo de renderizado que rompe la aplicación por completo.
  Con `Auth0ProviderWithConfig`:
  - Se valida si existen `domain` y `clientId`.
  - Si faltan, se provee un contexto seguro basado en `initialContext` de `@auth0/auth0-react` que informa al desarrollador mediante un diálogo amigable sin romper la app.
  - El inicio de sesión local tradicional sigue estando 100% disponible.

### 2.3. Desacoplamiento de la UI mediante `AuthContext` (Patrón Fachada / Facade)
* **Decisión:** Los componentes de la interfaz (`Login`, `Registro`, `Navbar`, `Perfil`, `RutaProtegida`) **no consumen directamente Auth0**; siguen consumiendo el hook unificado `useAuth()`.
* **Justificación:** Mantener una única fuente de la verdad para el estado de autenticación de la aplicación. `AuthContext` actúa como fachada que orquesta tanto la sesión tradicional (JWT en `localStorage`) como la sesión federada con Auth0, evitando duplicar lógica de estado o dependencias directas de Auth0 en múltiples páginas.

### 2.4. Puente con el Backend (`POST /api/v1/auth/login-externo`)
* **Decisión:** Al retornar de la redirección de Auth0:
  1. `AuthContext` obtiene el ID Token emitido por Auth0 (`getIdTokenClaims()`).
  2. Determina el proveedor (`GOOGLE` o `FACEBOOK` a partir de `user.sub`).
  3. Llama a `authService.loginExterno(proveedor, tokenExterno)`, correspondiente al endpoint diseñado en `04-openapi.yaml` y `01-dominio.md`.
  4. En el backend, `UsuarioService.loginExterno`:
     - Decodifica de forma segura el payload Base64URL del token de identidad OIDC de Auth0 (o JSON / email token).
     - Extrae email, nombre y apellido.
     - Busca si el usuario ya existe en base de datos. Si no existe, lo crea con `rol = MINORISTA`, `claveTemporal = false` y `proveedorAuth = proveedor`.
     - `MinoristaService.obtenerOCrearMinoristaExterno` vincula la entidad de persona `Minorista` en MySQL con su documento (`EXT-<UUID>`), nombre y contacto de email.
     - `JwtProvider` emite el token oficial JWT firmado por Dulce Mayor.
  5. El frontend almacena el JWT en `dm_institucional_token`, permitiendo que el cliente autenticado con Auth0 opere normalmente en `/mi-cuenta/pedidos`, `/puntos-de-venta` y el carrito de compras.

### 2.5. Adaptación a las Reglas de Negocio según `ProveedorAuth`
En el modelo de datos de Dulce Mayor (`Usuario.java`), el atributo `proveedorAuth` dictamina las capacidades del usuario:
* **Clave Temporal (`debeCambiarClave`):** Un usuario que ingresa por Auth0 no posee clave temporal por defecto (`dulcemayor`). En `Perfil.jsx`, el aviso amarillo que exige cambio de contraseña solo se muestra si el usuario es `LOCAL`.
* **Gestión de Contraseña:** Un usuario social no tiene contraseña local en Dulce Mayor. En `Perfil.jsx` (pestaña *Seguridad y Clave*), se oculta el formulario de cambio de clave y se reemplaza por una tarjeta informativa que explica que la seguridad y el 2FA están protegidos y administrados por su proveedor (`Google / Auth0`).
* **Edición de Correo Electrónico:** En la pestaña *Datos Personales*, se desactiva la edición del correo para usuarios externos, ya que el email corresponde a la identidad federada inmutable del proveedor social.

### 2.6. Carga Obligatoria de DNI por Única Vez en Primer Ingreso Social
* **Decisión:** Siguiendo el mismo patrón de bloqueo de las claves temporales de `frontend-preventor` (`PreventorLayout.jsx`, `DistribuidorLayout.jsx` y `CambiarPassword.jsx`), cuando un usuario ingresa con Auth0 por primera vez se le asigna un documento provisorio (`EXT-<UUID>`) y el sistema activa el estado `debeCompletarDni = true`.
* **Justificación:**
  - Google / Auth0 provee nombre, apellido, foto y correo electrónico verificado, pero **no provee el DNI** del cliente minorista, el cual es indispensable para asociar pedidos y facturación.
  - Mientras `debeCompletarDni` sea `true`, `ClienteLayout.jsx` y `RutaProtegida.jsx` bloquean el acceso a todas las secciones de la web (Inicio, Tienda, Carrito, Checkout, Sucursales, Contacto, Perfil y Chatbot) mostrando el banner superior de advertencia y redirigiendo a `/completar-dni` (`CompletarDni.jsx`).
  - Una vez que el cliente ingresa y confirma su DNI real (`POST /api/v1/mi-cuenta/completar-dni`), el backend valida formato (7–8 dígitos), unicidad en `Persona` y reemplaza el identificador `EXT-*` por el DNI definitivo.
  - A partir de ese momento, tanto el frontend (`CompletarDni.jsx`) como el backend (`MinoristaService.completarDniExterno`) impiden que el DNI vuelva a modificarse (**carga por única vez**).

### 2.7. Experiencia de Usuario Amigable (UX)
* **Acceso Rápido con Google:** Botón con diseño oficial de Google (`connection: 'google-oauth2'`) que inicia la autenticación en un clic.
* **Avatares Reales:** Soporte en `Navbar` y `Perfil` para mostrar la foto de perfil del usuario (`user.picture`) provista por Google/Auth0, con fallback a las iniciales tradicionales si no hay imagen.
* **Cierre de Sesión Federado:** `logout()` limpia las credenciales de Dulce Mayor en el almacenamiento local y, si la sesión provenía de Auth0, invoca `auth0Logout` para invalidar la sesión federada y redirigir limpiamente a `/login`.

---

## 3. Resumen de Archivos Creados y Modificados

### Backend (Spring Boot)
1. **`backend/src/main/java/com/DulceMayor/backend/dtos/LoginExternoRequest.java`**:
   Nuevo DTO que implementa el contrato `LoginExternoRequest` de `04-openapi.yaml` (`proveedor: ProveedorAuth`, `tokenExterno: String`).
2. **`backend/src/main/java/com/DulceMayor/backend/config/SecurityConfig.java`**:
   Se agregó `/auth/login-externo` y `/api/v1/auth/login-externo` a las rutas con `permitAll()`.
3. **`backend/src/main/java/com/DulceMayor/backend/services/UsuarioService.java`**:
   Se implementó `parsearTokenExterno` (decodificador seguro de JWT OIDC de Auth0 / payload JSON) y `loginExterno(ProveedorAuth proveedor, String tokenExterno)`.
4. **`backend/src/main/java/com/DulceMayor/backend/services/persona/MinoristaService.java`**:
   Se implementó `obtenerOCrearMinoristaExterno(...)` para vincular automáticamente la ficha del cliente minorista en MySQL y asignarle su contacto de correo y nombre real.
5. **`backend/src/main/java/com/DulceMayor/backend/controllers/AuthController.java`**:
   Se implementó el endpoint `@PostMapping("/login-externo")` que orquesta la autenticación externa y retorna el `AuthResponse` con el token JWT de Dulce Mayor.

### Frontend Institucional (React + Vite)
1. **`frontend-institucional/package.json`**:
   Se incorporó la dependencia oficial `@auth0/auth0-react`.
2. **`frontend-institucional/src/context/Auth0ProviderWithConfig.jsx`**:
   Componente envoltorio que valida las variables `VITE_AUTH0_*` y configura `<Auth0Provider>` con soporte para redirección interna de React Router y almacenamiento en `localStorage`.
3. **`frontend-institucional/src/App.jsx`**:
   Se envolvió `<AuthProvider>` dentro de `<Auth0ProviderWithConfig>`.
4. **`frontend-institucional/src/services/authService.js`**:
   Se incorporó la función `loginExterno(proveedor, tokenExterno)` hacia `POST /auth/login-externo`.
5. **`frontend-institucional/src/context/AuthContext.jsx`**:
   Se integraron los hooks de Auth0, el flujo de sincronización automática post-redirección, `loginConAuth0()`, logout unificado y la persistencia de avatar social.
6. **`frontend-institucional/src/pages/Login.jsx` y `Registro.jsx`**:
   Se agregaron los botones de "Continuar con Google" y "Ingresar con Auth0 Universal Login", el divisor visual y la auto-redirección tras login.
7. **`frontend-institucional/src/components/Navbar.jsx` y `Perfil.jsx`**:
   Se habilitó el renderizado del avatar fotográfico de Google/Auth0 y se implementó la lógica condicional para usuarios externos (ocultamiento de cambio de clave, bloqueo de edición de email e insignia de proveedor social).
8. **`frontend-institucional/vite.config.js` y `docker-compose.yml`**:
   Configurado `envDir: '..'` y `build.args` para inyectar correctamente las variables de Auth0 en modo desarrollo local y en el contenedor de Docker.

---

## 4. Guía para Configurar y Probar

1. **Variables en `.env` (raíz):**
   Asegurate de que tu archivo `.env` en la raíz del proyecto contenga:
   ```env
   VITE_API_URL=/api/v1
   VITE_AUTH0_DOMAIN=tu-tenant.us.auth0.com
   VITE_AUTH0_CLIENT_ID=tu_client_id
   VITE_AUTH0_CALLBACK_URL=http://localhost:3001/login
   ```
2. **Reconstruir y levantar los contenedores:**
   ```bash
   docker compose up -d --build backend frontend-institucional
   ```
3. **Probar el inicio de sesión:**
   - Abrí `http://localhost:3001/login`.
   - Hacé clic en **"Continuar con Google"** (o "Ingresar con Auth0").
   - Tras autorizar con tu cuenta de Google, serás redirigido a `http://localhost:3001/login`.
   - El frontend enviará el token de identidad al backend (`POST /api/v1/auth/login-externo`).
   - El backend creará/vinculará tu usuario y minorista en la base de datos MySQL, devolviendo el JWT de Dulce Mayor.
   - Entrarás automáticamente a `/perfil` con tu avatar de Google, nombre real, badge de "Vinculado con Google" y acceso completo a tus pedidos y puntos de venta.

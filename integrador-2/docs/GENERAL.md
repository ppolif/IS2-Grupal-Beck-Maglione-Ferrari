# GENERAL.md — Contexto maestro del proyecto "Dulce Mayor"

> **Instrucción para el asistente (Argon):** leé este archivo completo antes de escribir cualquier línea de código. Es la fuente de verdad junto con los demás archivos de `/docs`. Si algo es ambiguo, está marcado como `[A CONFIRMAR]` o falta información, **preguntá antes de inventar**. No agregues funcionalidades, endpoints, dependencias ni capas que no estén pedidas acá.

---

## 1. Qué estamos construyendo

Software de gestión integral y ecosistema web para **Dulce Mayor**, una empresa mayorista de golosinas de Mendoza (Argentina).

Circuito operativo que el sistema debe cubrir y **trazar de punta a punta**:

1. El **preventor** visita un punto de venta (kiosco, almacén, etc.) y carga el pedido desde su celular.
2. El pedido se sincroniza con el sistema central, administrado desde el almacén.
3. Se controla y actualiza el **stock**. Si hay stock suficiente, el pedido se **procesa y factura automáticamente**.
4. Se registra el **pago** (efectivo, transferencia o billetera virtual).
5. El **distribuidor** entrega siguiendo su ruta diaria; al marcar "Entregado", el sistema propone el siguiente punto en un mapa y envía un correo al próximo cliente avisando que está por llegar.

Además hay un sitio web institucional con catálogo público para que los minoristas vean el surtido.

## 2. Restricciones obligatorias

- Arquitectura **cliente-servidor, al menos 3 capas, API REST**.
- **El servidor y los clientes deben estar en lenguajes de programación distintos.** Por eso, si el backend es Java, **no usar Thymeleaf**: los clientes son React o Angular.
- Se deben implementar **patrones de diseño** (ver sección 8).
- Versionado con **Git**, integración continua con **Jenkins**, despliegue con **Docker**.
- El equipo debe poder explicar qué patrones usó, dónde y por qué, y de dónde salieron las plantillas web. **Escribí código claro y comentá las decisiones de patrones.**

## 3. Las 4 aplicaciones

| App | Usuarios | Descripción |
|---|---|---|
| **Backend (API REST)** | todas las apps | Ejecuta todas las reglas de negocio. Recibe las solicitudes (endpoints) de los clientes y responde. |
| **Dashboard administrativo** | Jefe, Administrativo/Almacenero | Interfaz tipo dashboard con componentes modulares: productos, stock, pedidos, facturación, pagos, promociones, usuarios, reportes, estadísticas. |
| **Sitio institucional + portal del cliente** | Público y Clientes (minoristas) | Catálogo público, info de la empresa, sucursales, cotización del dólar, chatbot con IA. Con login: historial de pedidos, estado de envíos, facturas, QR de pago. |
| **Portal móvil (responsive)** | Preventor, Distribuidor | Optimizado para celular: catálogo con stock real, alta de clientes, carga de pedidos con código de promoción, ruta diaria en mapa. El distribuidor usa este portal. |

Las 3 apps frontend consumen **únicamente** la API REST del backend. Ninguna accede a la base de datos.

## 4. Roles y permisos

| Rol | Puede |
|---|---|
| **Preventor** (preventista) | Ver catálogo con stock en tiempo real. Registrar nuevos clientes minoristas y puntos de venta. Cargar pedidos aplicando códigos de promoción. Consultar el estado de los pedidos que gestionó. Ver su ruta diaria y el siguiente punto sugerido en un mapa. |
| **Distribuidor** | Ver su ruta de entregas del día en un mapa. Marcar pedidos como "Entregado" y recibir la sugerencia del siguiente punto. |
| **Administrativo / Almacenero** | Alta y modificación de productos. Verificar stock. Procesar y facturar pedidos. Registrar pagos. Cambiar su propia contraseña. |
| **Jefe Administrativo** | Todo lo del administrativo y del preventor, más: alta de usuarios, reportes globales de ventas por período, rendimiento de preventores, golosinas más vendidas, estadísticas. Descarga de reportes en PDF y Excel. |
| **Minorista(cliente)** | Registrarse e iniciar sesión (correo electrónico o redes sociales). Ver historial de pedidos, estado de sus envíos, descargar facturas históricas, ver el QR de pago si eligió billetera virtual. |

## 5. Requerimientos funcionales por módulo

### 5.1 Productos y almacén
- ABM de productos con imagen, descripción, categoría, presentación (bulto/pack) y stock actual.
- El stock se **descuenta automáticamente al confirmarse la facturación** del pedido.
- Un producto **sin stock deja de mostrarse** automáticamente en el catálogo público y para el preventor.
- Precios mayoristas con **vigencia por fechas**: el precio actual es el que tiene `fechaHasta` nula. Siempre se cobra el precio vigente al momento de cargar el pedido, y el detalle del pedido guarda una copia (`precioUnitario`).

### 5.2 Pedidos y trazabilidad
- El sistema registra el ciclo de vida completo: fecha y hora de carga por el preventor, estado del stock, fecha de facturación, datos del distribuidor y fecha efectiva de entrega.
- **Cada cambio de estado de un pedido escribe una fila en `HistorialEstadoPedido`** (estado, fecha y hora, usuario, observación).

### 5.3 Facturación y medios de pago
- El total se calcula con las cantidades y los precios vigentes, menos el descuento de la promoción si hay.
- Medios de pago: **efectivo** (se paga contra entrega), **transferencia** (se informa el alias, guardado en `FormaDePago.observacion`) y **billetera virtual** (se genera un **QR** que el cliente ve en su panel; el QR es simulado, **no integrar pasarelas de pago reales**).
- Se genera una factura electrónica descargable (PDF) por el cliente desde su panel, y se envía por correo al responsable del comercio.
- `[A CONFIRMAR con el Product Owner]` el enunciado dice a la vez que el pedido se factura automáticamente y que la factura se genera con el pago registrado. Decisión por defecto: se factura al confirmar el stock (`Factura` en estado `PENDIENTE_PAGO`) y pasa a `PAGADA` cuando la suma de los `Pago` registrados cubre el total.

### 5.4 Promociones
- Módulo para registrar promociones y descuentos por volumen o producto destacado, mediante un **código de descuento**.
- El preventor aplica el código al armar el pedido; el subtotal se reduce automáticamente.

### 5.5 Notificaciones automáticas (correo)
- Confirmación de pedido al minorista **al momento de facturarse**.
- **El día 5 de cada mes**: newsletter en HTML con los productos en oferta (`Producto.enOferta`) a todos los clientes (tarea programada).
- Aviso al próximo punto de la ruta de entrega: "en breve se realizará la entrega".
- La configuración SMTP sale de `ConfiguracionCorreoEmpresa`.

### 5.6 Rutas y mapas
- Los puntos de venta tienen geolocalización (`latitud`, `longitud` en `Direccion`).
- **Ruta del preventor** (`RutaPreventor`): puntos de venta ordenados de la forma más óptima posible. Tras tomar un pedido, el sistema propone el siguiente punto más eficiente y muestra el recorrido en un mapa. El preventor también ve todos los puntos del día para identificar su zona.
- **Ruta del distribuidor** (`RutaDistribuidor`): pedidos a entregar, ordenados. Al marcar un pedido como "Entregado", el sistema propone el siguiente punto en el mapa y envía el correo de aviso.
- Ordenamiento: algoritmo del **vecino más cercano** con distancia haversine. **No usar servicios de optimización de rutas externos ni resolver el problema del viajante exacto.**
- Mapas en el frontend con **Leaflet + OpenStreetMap**.

### 5.7 Reportes y descargas (Jefe y administración)
- Pantalla y **PDF**: clientes minoristas, pedidos realizados, estados de envío, montos facturados.
- **Excel**: histórico de ventas discriminado por departamento y preventor en un período.
- Reportes gerenciales (ventas por período, rendimiento de preventores, productos más vendidos) en PDF o Excel.

### 5.8 Estadísticas
- **Gráfico de barras**: por mes, cantidad de pedidos por preventor por día.
- **Gráfico de torta**: cantidad de productos en stock del mayorista.

### 5.9 Sitio institucional
- Catálogo público (fotos, descripción, presentación por bulto/pack, precio mayorista actualizado).
- Información institucional: ubicación de sucursales (mapa) y fotos de sucursales.
- **Cotización actualizada del dólar**: el backend consume una API pública de cotización (cliente HTTP detrás de un Adapter), con caché corta y un valor de respaldo si la API falla.
- **Chatbot con IA** para armar presupuestos según los productos que indique el usuario. **Regla clave:** los precios y totales los calcula el **backend** con los precios vigentes; el modelo de lenguaje solo interpreta el pedido en lenguaje natural. El modelo **nunca inventa precios**.

## 6. Seguridad

- Ingreso de empleados con usuario y clave. Al dar de alta un usuario, la clave por defecto es `dulcemayor` y `claveTemporal = true`.
- Si `claveTemporal` es `true`, el backend **solo permite el endpoint de cambio de clave** hasta que se modifique; cualquier otro endpoint responde 403 con un código de error claro. El frontend redirige a la pantalla de cambio de clave.
- Contraseñas **encriptadas con BCrypt**. Nunca en texto plano, ni en logs, ni en respuestas.
- Autenticación **JWT** sin estado, con expiración. Autorización por rol en cada endpoint (`@PreAuthorize` o equivalente).
- Login de clientes por correo/contraseña o por redes sociales (OAuth2, por ejemplo Google). Se refleja en `Usuario.proveedorAuth`.
- CORS restringido a los orígenes de los frontends. Validación de entradas en el backend con Bean Validation. Respuestas de error que no filtren datos internos.
- Secretos (clave JWT, SMTP, API keys) en variables de entorno, **nunca en el repositorio**.

## 7. Modelo de dominio

El modelo completo (clases, atributos, relaciones, multiplicidades y dirección de cada relación) está en **`01-dominio.md`**. Si hay discrepancia con este archivo, **manda `01-dominio.md`**.

Resumen de las entidades:

- **Geografía y ubicación:** Pais, Provincia, Departamento, Localidad, Direccion (con latitud/longitud).
- **Empresas y contacto:** Mayorista (empresa, con sucursales), ConfiguracionCorreoEmpresa, Contacto (correo electrónico y telefónico).
- **Personas y usuarios:** Persona → Empleado (→ Preventor, Distribuidor), Minorista (cliente), Usuario. El rol está en `Usuario.rol`.
- **Comercial:** PuntoDeVenta, Categoria, SubCategoria, Producto, VigenciaPrecio, Stock, Promocion, Imagen.
- **Operación:** Pedido, DetallePedido, HistorialEstadoPedido, Factura, DetalleFactura, Pago, FormaDePago.
- **Rutas:** Ruta (abstracta: id, fecha, estado) → RutaPreventor, RutaDistribuidor.

### Decisiones de modelado ya tomadas (no las revirtas)
- **No existe la clase `Envio`.** La trazabilidad del despacho y la entrega se resuelve con `HistorialEstadoPedido` y las rutas.
- **Las flechas de dependencia apuntan siempre hacia la clase "referenciada"** para evitar ciclos: `Stock`, `VigenciaPrecio`, `Promocion`, `DetallePedido` y `DetalleFactura` apuntan a `Producto`; `Factura`, `HistorialEstadoPedido` y las rutas apuntan a `Pedido`; `Pago` apunta a `Factura`. **`Pedido` no referencia a `Factura` ni a ninguna ruta**: se consulta desde el otro lado.
- `Producto` **no** depende de `Stock`: el catálogo con stock > 0 se resuelve con una consulta del repositorio.
- El orquestador del proceso "facturar pedido" es un **servicio aparte** que llama en un solo sentido a `StockService`, `FacturaService` y `PedidoService`. **Evitá dependencias circulares entre servicios.**
- `PuntoDeVenta` guarda el preventor asignado hoy; `Pedido` guarda el preventor que tomó ese pedido (dato histórico).

## 8. Patrones de diseño

Obligatorios:
1. Los patrones ya utilizados en el TP Integrador N°1: Patron arquitectonico por capas, Experto en Responsabilidad, Polimorfismo, DAO, DTO, Inyeccion de Dependencias.
2. **Template Method** en clases controladoras y clases de servicio (clases base abstractas con el flujo común de crear, validar, modificar, eliminar y listar; las subclases completan los pasos específicos).
3. **DTO** para el pasaje de información entre controlador y servicio (usar MapStruct para la conversión). **Los controladores nunca exponen entidades JPA.**
4. Uno de cada categoría (creacional, estructural, comportamiento). Propuesta, `[A CONFIRMAR por el equipo]`:
   - **Comportamiento:** *Strategy* para el ordenamiento de rutas, o *Observer* (eventos de Spring) para notificaciones al cambiar el estado de un pedido.
   - **Estructural:** *Adapter* para las APIs externas (cotización del dólar, chatbot).
   - **Creacional:** *Factory Method* o *Builder* (por ejemplo, exportadores PDF/Excel, o armado de Pedido/Factura).

Cada patrón aplicado debe quedar **comentado en el código** (qué patrón es y por qué se usó acá), porque el equipo lo explica en la presentación.

## 9. Stack tecnológico

**Pineá versiones exactas** y no agregues dependencias sin avisar.

- **Backend:** Java 21, Spring Boot , Spring Web, Spring Security + JWT, Spring Data JPA, Bean Validation, Spring Mail, `@Scheduled` para el newsletter, MapStruct, Lombok, Spring AI para chatbot.
- **Base de datos:** `MySQL`. Script de datos semilla incluido.
- **Frontends:** React + TypeScript + Vite. Cliente HTTP generado desde `04-openapi.yaml`.
- **Plantillas web:** `[COMPLETAR: plantilla del dashboard]` y `[COMPLETAR: plantilla del sitio público]`.
- **Mapas:** OpenStreetMap. **PDF:** `iText`. **Excel:** Apache POI. **Gráficos:** `[COMPLETAR: Chart.js o Recharts]`.
- **Tests:** JUnit 5, Mockito, MockMvc; Vitest o Jest para frontend.
- **Infraestructura:** Git, Jenkins (pipeline que compile y corra tests), Docker y Docker Compose para levantar todo.

## 10. Arquitectura y convenciones

### 10.1 Dependencias entre capas y servicios (obligatorio)

1. **Flujo único:** `controller → service → repository`. Ninguna capa se salta a otra.
2. **Un servicio inyecta solo el repositorio de su propia entidad** (`<Entidad>Service` usa
   `<Entidad>Repository`). Para datos de otra entidad inyecta el **servicio** de esa entidad.
   Ejemplo: `SubCategoriaService` usa `SubCategoriaRepository` y llama a `CategoriaService`;
   nunca usa `CategoriaRepository`.
3. **Qué viaja entre capas:** los DTO cruzan solo controlador ↔ servicio. Entre servicios se
   intercambian entidades mediante métodos de acceso interno (`obtenerEntidad(id)`), que los
   controladores no usan. Los controladores nunca ven entidades ni repositorios.
4. **La dirección de las dependencias sigue las flechas del UML** (`01-dominio.md`): si `A → B`,
   `AService` puede inyectar `BService` y nunca al revés. Consecuencia: el servicio referenciado
   no consulta a quienes lo referencian (por ejemplo, `CategoriaService` no revisa subcategorías);
   esa regla vive en el servicio que referencia o en un orquestador.
5. **Orquestadores:** los flujos que necesitan varios servicios (procesar pedido, registrar
   entrega, armar ruta) van en un servicio orquestador (`ProcesamientoPedidoService`, etc.) que
   depende de otros servicios y del que no depende nadie. Define la transacción del flujo.
6. **Servicios de lectura** (reportes, estadísticas, catálogo público): clases `*LecturaService`
   que pueden consultar varios repositorios, **solo para leer**. Es la única excepción a la regla 2.
7. **Inyección por constructor**, sin `@Autowired` sobre campos, y
   `spring.main.allow-circular-references=false`.
8. **Verificación automática:** los tests ArchUnit corren en `mvn test` (y por lo tanto en
   Jenkins). Si alguien rompe una regla, el build falla.

- Capas del backend: `restcontroller → service → repository → entity`. Los DTO viajan entre controlador y servicio. La lógica de negocio vive **solo** en los servicios.
- Paquetes por módulo de dominio (por ejemplo `producto`, `pedido`, `facturacion`), no por tipo técnico.
- API versionada: `/api/v1/...`. Recursos en plural y en minúsculas. Códigos HTTP correctos (201 al crear, 204 al eliminar, 400/404/409/403/401 según corresponda).
- Listados con paginación donde corresponda. Borrado lógico (`eliminado:boolean`) en las entidades del diagrama.
- **Formato único de error** en toda la API: `timestamp`, `status`, `code`, `message`, `path`. Un `@RestControllerAdvice` central.
- Nombres de clases, métodos y atributos **idénticos a los del UML** (en español). Los métodos CRUD del UML se implementan en los servicios, no en las entidades.
- Entidades JPA sin lógica de presentación. Evitá relaciones bidireccionales JPA salvo que sean necesarias, y nunca serialices entidades.

## 11. Reglas de negocio críticas

**Estados del pedido** (`EstadoPedido`) y transiciones permitidas:

| Desde | Hacia | Quién / cuándo |
|---|---|---|
| (nuevo) | `CARGADO` | Preventor carga el pedido |
| `CARGADO` | `FACTURADO` | Al procesar: hay stock para todos los ítems. Descuenta stock, genera `Factura`, envía correo de confirmación |
| `CARGADO` | `SIN_STOCK` | Al procesar: falta stock en algún ítem |
| `SIN_STOCK` | `CARGADO` o `ANULADO` | Tras reposición o por decisión administrativa |
| `FACTURADO` | `EN_REPARTO` | Se incluye en una `RutaDistribuidor` |
| `EN_REPARTO` | `ENTREGADO` | El distribuidor lo marca; avanza la ruta y avisa al siguiente |
| `CARGADO` / `SIN_STOCK` | `ANULADO` | Administrativo o jefe |

- Toda transición escribe `HistorialEstadoPedido`. No se permiten transiciones fuera de esta tabla.
- Un pedido solo se puede **modificar** mientras esté en `CARGADO`.
- La verificación de stock y el descuento ocurren en **una sola transacción**, para no vender stock inexistente.
- Una promoción solo aplica si está vigente (fechas), y si tiene `cantidadMinima` o `producto`, se valida contra el pedido.
- No puede haber más de una ruta por persona por día.

## 12. Cómo trabajar (proceso con el asistente)

**Orden de implementación** (cada paso es un entregable que compila y tiene tests):
1. Esqueleto del backend, Docker Compose, Jenkinsfile, seguridad base y manejo de errores.
2. **Slice de referencia ("golden slice")**: módulo `Categoria` completo (entidad, repositorio, DTO, mapper, servicio y controlador con Template Method, tests). El equipo lo revisa y queda como **molde oficial**.
3. Resto de los módulos, uno por vez, **replicando exactamente la estructura y el estilo del molde**.
4. Contrato `04-openapi.yaml` y cliente tipado para los frontends.
5. Frontends: primero una pantalla de referencia (listado + formulario de `Categoria`), después el resto replicando el patrón.

**Reglas al entregar código:**
- Un archivo por bloque, con su **ruta completa**. Código **completo**: nada de `...` ni "el resto igual".
- Incluí los **tests** del módulo y una lista corta de **supuestos** que hayas hecho.
- No cambies el contrato OpenAPI ni el modelo de dominio sin avisar y justificar.
- Al terminar cada módulo, actualizá `PROGRESO.md` (qué se hizo, decisiones tomadas, pendientes).
- Si no estás seguro de la API de una librería, decilo en vez de adivinar.

**Definición de terminado** (por módulo): compila, los tests pasan, `docker compose up` levanta, respeta el molde, sin dependencias circulares, y está documentado en `PROGRESO.md`.

## 13. Qué NO hacer (evitar sobreingeniería)

Sin microservicios, CQRS, event sourcing, colas de mensajes, Kubernetes ni caché distribuida. Sin integración real con pasarelas de pago ni con servicios de optimización de rutas. Sin clases base ni abstracciones que no estén pedidas en las secciones 8 y 10. Preferí la solución más simple que cumpla el enunciado.

## 14. Pendientes a confirmar con el Product Owner (profesor)

1. Momento de la facturación vs. el registro del pago (ver 5.3).
2. Si el **Distribuidor** es un rol con acceso al sistema y si usa el portal móvil.
3. Si `Ruta` necesita guardar el detalle de cada parada (`ParadaRuta`) o alcanza con `paradaActual`.

## 15. Índice de documentos

| Archivo | Contenido |
|---|---|
| `GENERAL.md` | Este archivo: contexto, restricciones, reglas y proceso |
| `01-dominio.md` | Modelo de clases en texto (derivado del UML) |
| `02-reglas-negocio.md` | Detalle ampliado de reglas y flujos, si hace falta |
| `03-arquitectura.md` | Detalle de capas, paquetes y patrones aplicados |
| `04-openapi.yaml` | Contrato de la API REST |
| `05-seguridad.md` | Detalle de autenticación, autorización y protección de datos |
| `06-tareas.md` | Historias de usuario por sprint |
| `PROGRESO.md` | Avance, decisiones y pendientes (lo actualiza el asistente) |

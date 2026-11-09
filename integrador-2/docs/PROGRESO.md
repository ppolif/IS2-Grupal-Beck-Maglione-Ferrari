# Registro de Progreso y Cambios del Proyecto

**Último commit:** `06c5b52767493bf47b0a9a0262818d4e4ec78167`  
**Rama:** `poli`  
**Autor:** Paulina Ferrari Sánchez  
**Fecha:** 2026-11-06  
**Resumen del Commit:** Implementación del módulo integral de rutas logísticas (preventores y distribuidores), patrones de diseño GoF, asignación inteligente, corrección y consistencia de servicios del dominio, y arquitectura limpia con ArchUnit.

---

## 1. Módulo Integral de Rutas Logísticas

Se desarrolló la arquitectura y lógica transaccional de rutas según `01-dominio.md`, `02-reglas-negocio.md` y `04-openapi.yaml`.

### 1.1 Entidades y Persistencia
- **`Ruta` (Clase Base Abstracta):** Herencia con `@Inheritance(strategy = InheritanceType.JOINED)`. Gestiona identificación, fecha, estado (`PENDIENTE`, `REALIZADA`, `INCOMPLETA`), auditoría y soft delete.
- **`RutaPreventor`:**
  - Responsable asignado: `Preventor`.
  - Historial de paradas ordenadas: `@ElementCollection @OrderColumn List<String> visitadosIds` (almacena ordenadamente los IDs de los puntos de venta ya visitados).
  - Puntero a la parada en curso: `PuntoDeVenta paradaActual`.
- **`RutaDistribuidor`:**
  - Responsable asignado: `Distribuidor`.
  - Colección de pedidos del reparto: `@ManyToMany @OrderColumn List<Pedido> pedidos`.
  - Puntero de entrega actual: `PuntoDeVenta paradaActual`.
- **Enums del Dominio:** `RutaEstado` y `TipoRuta` (`PREVENTA`, `DISTRIBUCION`).
- **Repositorios Spring Data JPA:** `RutaRepository`, `RutaPreventorRepository`, `RutaDistribuidorRepository`.

### 1.2 Patrones de Diseño GoF Aplicados
1. **Patrón Strategy (Comportamiento):**
   - Interfaz `RutaOptimizerStrategy` y su implementación concreta `NearestNeighborStrategy`.
   - Resuelve el problema del viajante (TSP) usando la heurística constructiva del **Vecino Más Cercano** junto con la **Fórmula de Haversine** para calcular distancias ortodrómicas geodésicas en metros.
   - Compatible 100% con las coordenadas geográficas de **OpenStreetMap (WGS84)**.
2. **Patrón Observer (Comportamiento vía Spring Events):**
   - Evento `AvisoProximaEntregaEvent` publicado al confirmar la entrega de un pedido.
   - Listener desacoplado `AvisoProximaEntregaListener` que envía la notificación por correo al minorista de la siguiente parada del recorrido.

### 1.3 Asignación y Automatización Inteligente
- **Origen Automático desde la Sucursal/Sede Central:**
  - Para preventores: se obtienen las coordenadas de la sucursal `Mayorista` a la cual pertenece el preventor.
  - Para distribuidores: se obtienen automáticamente las coordenadas geodésicas de la `Sede Central`.
  - Manejo de borrado lógico en direcciones históricas de mayoristas para tomar únicamente la dirección activa.
- **Asignación Automática de Preventor a Punto de Venta:**
  - Cuando un minorista registra un comercio en la web sin preventor asignado, el sistema busca automáticamente al preventor activo cuya sucursal esté más cercana geográficamente al comercio mediante Haversine.
- **Generación y Balanceo Automático de Repartos (Distribuidores):**
  - Endpoint `POST /rutas-distribuidor`: genera las rutas diarias automáticamente sin necesidad de armado manual.
  - Se obtienen todos los pedidos en estado `FACTURADO` y se ordenan por distancia a la Sede Central.
  - Se asignan pedidos semilla al azar a cada distribuidor disponible.
  - Los pedidos restantes se asignan de forma balanceada y equitativa encadenándolos con el **último punto de venta asignado a cada distribuidor** para formar recorridos continuos y eficientes.
- **Prioridad de Notificación:**
  - El email de aviso de entrega utiliza prioritariamente el correo del usuario (`minorista.getUsuario().getNombreUsuario()`) y respaldo en contactos secundarios.

### 1.4 Controladores y DTOs de Rutas
- `RutaPreventorController` y `RutaDistribuidorController`: endpoints REST documentados en OpenAPI con control de acceso por roles (`JEFE`, `ADMINISTRATIVO`, `PREVENTISTA`, `DISTRIBUIDOR`).
- DTOs inmutables (records): `RutaResponse`, `ParadaResponse`, `AvanceRutaResponse`, `RutaPreventorGenerarRequest`, `RutaDistribuidorRequest`, `CompletarParadaPreventorRequest`, `EntregaRequest`.
- Helper desacoplado: `RutaMapperHelper` para transformar entidades a DTOs enriquecidos con coordenadas e información de comercios.

---

## 2. Refactorización y Correcciones de Servicios del Dominio

- **`PedidoService`:**
  - Agregado método `listarFacturadosPendientes()` para alimentar el algoritmo de distribución logística.
  - Método `marcarEntregado(id, obs)` utilizado para actualizar el estado del pedido de manera transaccional y publicar el evento de auditoría.
- **`EmpleadoService`:**
  - Implementación de métodos para listar preventores y distribuidores activos no eliminados.
  - Algoritmo `buscarPreventorMasCercano(lat, lon)` con cálculo geodésico sobre la sucursal de pertenencia.
- **`PuntoDeVentaService`:**
  - En `antesDePersistir`: asignación automática del preventor más cercano en caso de no venir provisto en la solicitud de alta.
- **`FacturaService` y `PagoService`:**
  - Correcciones de tipado, validaciones transaccionales y consistencia con las reglas de negocio financieras.
- **`MinoristaService` y `PersonaService`:**
  - Corrección en la asociación de usuarios, perfiles de minoristas y manejo seguro de contactos.

---

## 3. Arquitectura y Calidad de Código (ArchUnit & Tests)

- **Cumplimiento estricto de las 6 reglas de arquitectura de ArchUnit (`ArquitecturaTests`):**
  - Desacoplamiento total entre controladores y repositorios/entidades.
  - Uso exclusivo de servicios dedicados para operaciones cruzadas (evitando inyecciones directas de repositorios externos en servicios).
  - Parámetros de controladores desacoplados de enums de entidades (conversión segura mediante tipos primitivos / Strings en la capa de servicio).
- **Resultados de las Pruebas:**
  - Compilación exitosa en Maven (`BUILD SUCCESS`).
  - **117 tests automatizados ejecutados** sin fallos (`Failures: 0, Errors: 0, Skipped: 0`).

---

## 4. Integración de OpenStreetMap y Enrutamiento Callejero Real (OSRM)

Se completó la integración integral de **OpenStreetMap** en el frontend y backend bajo el modelo híbrido estándar de la industria:

### 4.1 Frontend (React 19 + Leaflet)
- **Componente Base Reutilizable `OSMMap`:**
  - Creado en los tres frontends (`frontend-preventor`, `frontend-institucional`, `frontend-admin`).
  - Renderiza capas de OpenStreetMap mediante Leaflet con marcadores vectoriales personalizados (SVG con numeración de paradas y colores corporativos) y popups informativos.
  - Soporte de centrado suave (`flyTo`), ajuste de límites (`fitBounds`) y redimensionamiento responsivo (`invalidateSize`).
  - **Trazado Callejero Real:** Integración asíncrona con el servicio de rutas de OpenStreetMap OSRM (`/route/v1/driving/`). Leaflet dibuja la polilínea continua siguiendo el trazado exacto de las calles y avenidas de Mendoza en lugar de líneas rectas teóricas.
  - **Paleta de Colores Centralizada:** Reemplazo de colores hexadecimales en duro por tokens semánticos corporativos provenientes de `src/theme/colors.js`.
- **Enriquecimiento de Mocks:** Agregadas coordenadas geodésicas en Mendoza (WGS84) a sucursales, visitas comerciales, repartos y agentes en calle.
- **Reemplazo de Placeholders:** Sustitución de `MapPlaceholder` en las vistas de Sucursales (Institucional), Recorrido Preventor/Distribuidor (Móvil) y Rutas/PreventorRutas (Admin).

### 4.2 Backend (Spring Boot)
- **Estrategia de Enrutamiento Vial (`OsrmRoutingStrategy`):**
  - Implementa `RutaOptimizerStrategy` con anotación `@Primary`.
  - Consulta la matriz de distancias por calle en metros a OpenStreetMap OSRM (`/table/v1/driving/`) para calcular la secuencia óptima de paradas por asfalto.
  - **Mecanismo de Resiliencia:** Fallback automático a la fórmula de Haversine (`NearestNeighborStrategy`) ante caídas de red o demoras en la API externa.
  - Configuración externa de URL de servicio en `application.properties` (`app.osm.osrm-url`).
  - Preservación íntegra de los DTOs y del contrato OpenAPI (`04-openapi.yaml`) sin sobrecargar el payload de red.
- **Compilación:** Verificada con `./mvnw test-compile` y `npm run build` en los 3 frontends (100% exitosas).

---

## 5. Módulo Integral de Envío de Correos y Notificaciones (Gmail SMTP)

Se implementó y conectó de extremo a extremo el sistema de notificaciones por correo electrónico según lo estipulado en `01-dominio.md`, `02-reglas-negocio.md` (§6.3, §8), `03-arquitectura.md` (§4.3) y `04-openapi.yaml`.

### 5.1 Infraestructura y Servicio Central de Correo
- **`EmailService`:**
  - Servicio centralizado para despacho de correos en HTML y con archivos adjuntos binarios (`MimeMessageHelper`).
  - **Resolución dinámica de transporte:** Prioriza la entidad de base de datos `ConfiguracionCorreoMayorista`. Si no existe, recurre de forma transparente a las credenciales configuradas en variables de entorno / `application.properties` (`spring.mail.*`).
  - **Resolución de destinatario (`resolverEmailMinorista`):** Aplica la regla de prioridad del negocio:
    1. *Prioridad 1:* Email de la cuenta de usuario del minorista (`minorista.getUsuario().getNombreUsuario()`).
    2. *Prioridad 2 (Respaldo):* Contactos secundarios registrados (`ContactoCorreoElectronico`).
- **`EmailTemplateHelper`:** Generador de plantillas HTML responsivas y profesionales con el diseño visual de Dulce Mayor para facturas, avisos de entrega y catálogo de ofertas.
- **Habilitación global:** Se activaron `@EnableAsync` y `@EnableScheduling` en `BackendApplication`.

### 5.2 Emisión y Reenvío de Facturas con PDF Adjunto
- **`FacturaGeneradaEvent` & `FacturaNotificacionListener`:**
  - Al procesar y facturar un pedido en `FacturaService` (transición de `CARGADO` a `FACTURADO`), se publica un evento de dominio desacoplado.
  - El listener `@Async` genera el comprobante en memoria en formato PDF mediante `generarFacturaPdf` y lo adjunta automáticamente al correo del minorista.
- **Reenvío manual de factura:** Implementación real en `FacturaService.reenviarFacturaPorCorreo(id)` para el endpoint documentado `POST /api/v1/facturas/{id}/reenviar-correo`.

### 5.3 Aviso de Próxima Entrega (Distribuidor)
- **`AvisoProximaEntregaListener`:** Conectado con `EmailService`. Al avanzar una entrega en `POST /rutas-distribuidor/{id}/entregas`, identifica la siguiente parada y despacha el correo con el asunto estricto:
  > *"En breve se realizará la entrega de su pedido Dulce Mayor"*.

### 5.4 Newsletter Mensual de Ofertas y Notificaciones
- **`NewsletterService`:**
  - Tarea programada `@Scheduled(cron = "0 0 8 5 * ?")` (día 5 de cada mes a las 08:00 AM).
  - Consulta productos con `enOferta == true` vía `ProductoRepository.findAllByEnOfertaTrueAndEliminadoFalseOrderByNombreAsc()`, busca sus precios vigentes con `VigenciaPrecioService` y despacha el boletín HTML a todos los minoristas activos con correo.
- **`NotificacionController`:** Expone el endpoint `POST /api/v1/notificaciones/newsletter/enviar` protegido por roles (`JEFE_LOGISTICA`, `JEFE_VENTAS`, `ADMIN`) para permitir el disparo manual.

### 5.5 Integración en Frontend Admin
- Creación de los servicios de API `pedidoService.js`, `facturaService.js` y `notificacionService.js` en `frontend-admin`.
- Conexión real de la pantalla `Pedidos.jsx` y el hook `usePedidos.js` a la API (`POST /api/v1/pedidos/{id}/procesar`) en reemplazo de mocks, incorporando la acción de **Reenviar Factura por Mail**.

### 5.6 Pruebas y Validación
- Pruebas unitarias completas: `EmailServiceTest`, `NewsletterServiceTest` y actualización de `FacturaServiceTest` (100% exitosas).
- Verificación de autenticación SMTP real contra Gmail (`dulcemayor5@gmail.com`) y entrega exitosa en casilla de correo.

---

## Paso 8: Módulo Actores, Personas y Mayorista (Persona, Empleado, Minorista, PuntoDeVenta, VisitaPreventor, Mayorista)

### 1. Qué se hizo
- **Entidades de dominio (`com.DulceMayor.backend.entities.persona`, `com.DulceMayor.backend.entities.mayorista`):**
  - `Persona` (abstracta `JOINED` con `nombre`, `apellido`, `documento`, `contactos` y `usuarios`), `Empleado` (`legajo`, `rol`, `localidadesAsignadas`) y `Minorista` (`puntosDeVenta`).
  - `PuntoDeVenta` (`nombreComercial`, `cuit`, `direccion`, `minorista`), `VisitaPreventor` (`fechaVisita`, `horaVisita`, `observacion`, `preventor`, `puntoDeVenta`) y `Mayorista` (`razonSocial`, `nombreFantasia`, `cuit`, `direcciones`, `contactos`, `imagenes`).
- **Repositorios, DTOs, Mappers, Servicios y Controladores REST:**
  - ABM de `/empleados`, `/minoristas`, `/puntos-de-venta`, `/visitas-preventor`, `/mayoristas` y `/publico/sucursales`, además de la vinculación de `Pedido` con `PuntoDeVenta`, `Empleado` (`preventor` y `distribuidor`) y perfil de `Usuario`.

---

## Paso 9: Módulo Sitio Público (`/publico/**`) y Patrón GoF Estructural Adapter

### 1. Qué se hizo
- **DTOs del módulo público (`com.DulceMayor.backend.dtos.publico`):**
  - Records alineados con `04-openapi.yaml`: `ProductoPublicoResponse` (con `disponible: boolean` y sin `stockActual`), `ProductoPublicoPage`, `CotizacionDolarResponse`, `ChatbotMensaje`, `ChatbotPresupuestoRequest`, `ChatbotItem` y `ChatbotPresupuestoResponse`.
- **Patrón GoF Estructural — Adapter (`com.DulceMayor.backend.services.publico.adapter`):**
  - `CotizacionDolarPort` (Target) y `CotizacionDolarAdapter` (Adapter): adapta la API externa de cotización del dólar (`https://dolarapi.com/v1/dolares`) al DTO interno `CotizacionDolarResponse`, con caché en memoria con TTL configurable (`app.dolar.cache-ttl-minutes`, por defecto 15 min) y fallback resiliente al último valor conocido o valor de respaldo ante caídas de red, garantizando que `GET /publico/cotizacion-dolar` nunca devuelva `5xx`.
  - `ChatbotIntencionPort` (Target), `IntencionItemPedido` y `SpringAiChatbotAdapter` (Adapter): desacopla la extracción de intenciones conversacionales (búsqueda por código, nombre de producto y cantidades solicitadas) de las reglas de negocio.
- **Servicio de lectura y Controlador REST (`com.DulceMayor.backend.services.publico`, `com.DulceMayor.backend.controllers.publico`):**
  - `SitioPublicoLecturaService`: proyecta el catálogo público (`disponible = stockActual > 0` sin exponer cantidades de inventario), consulta la cotización del dólar vía `CotizacionDolarPort` y calcula el presupuesto del chatbot (`POST /publico/chatbot/presupuesto`) exclusivamente en el backend usando `VigenciaPrecio` abierta (`precioUnitario`), `unidadesPorBulto` y `Stock` actual (generando advertencias cuando la cantidad supera el stock o no hay stock disponible).
  - `SitioPublicoController`: expone `GET /publico/catalogo`, `GET /publico/catalogo/{id}`, `GET /publico/cotizacion-dolar` y `POST /publico/chatbot/presupuesto` (y sus variantes `/api/v1/publico/...`), habilitados públicamente en `SecurityConfig`.

---

## Paso 10: Módulo Reportes (`/reportes/**`), Estadísticas (`/estadisticas/**`) y Patrón GoF Creacional Factory Method

### 1. Qué se hizo
- **DTOs de estadísticas y reportes (`com.DulceMayor.backend.dtos.estadistica`, `com.DulceMayor.backend.dtos.reporte`):**
  - Records de series estadísticas: `PuntoDiaCantidad`, `SeriePreventorDia`, `PedidosPorPreventorPorDiaResponse` y `StockPorProductoItem`.
  - Modelo intermedio tabular para exportación: `ColumnaReporte` y `ReporteTabular`.
- **Patrón GoF Creacional — Factory Method (`com.DulceMayor.backend.services.reporte.exporter`):**
  - `ReporteExporter` (Product), `PdfReporteExporter` (ConcreteProduct para `application/pdf` con documento PDF tabular multipágina nativo), `ExcelReporteExporter` (ConcreteProduct para `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` con paquete OpenXML `.xlsx` válido en memoria mediante `ZipOutputStream`) y `ReporteExporterFactory` (Creator que resuelve el exportador según `formato = PDF | EXCEL` o lanza `400 VALIDACION`).
- **Servicios de lectura y Controladores REST (`com.DulceMayor.backend.services.estadistica`, `com.DulceMayor.backend.services.reporte`, `com.DulceMayor.backend.controllers.estadistica`, `com.DulceMayor.backend.controllers.reporte`):**
  - `EstadisticaLecturaService` y `EstadisticaController`: implementan `GET /estadisticas/pedidos-por-preventor-por-dia` (exclusivo `ROLE_JEFE`) y `GET /estadisticas/stock-por-producto` (`ROLE_JEFE` y `ROLE_ADMINISTRATIVO`).
  - `ReporteLecturaService` y `ReporteController`: implementan los 6 endpoints de exportación (`GET /reportes/facturacion`, `GET /reportes/minoristas`, `GET /reportes/pedidos`, `GET /reportes/productos-mas-vendidos`, `GET /reportes/rendimiento-preventores`, `GET /reportes/ventas-historico`) protegidos con `@PreAuthorize("hasRole('JEFE')")`, devolviendo los binarios con headers `Content-Type` y `Content-Disposition: attachment; filename="..."`.
- **Pruebas automatizadas:**
  - `SitioPublicoControllerTest` y `ReporteYEstadisticaControllerTest` verifican catálogo público sin filtración de `stockActual`, cotización del dólar con fallback, presupuesto de chatbot con cálculo backend de subtotales/advertencias, control de acceso por rol en estadísticas y descarga de los 6 reportes en PDF y Excel.



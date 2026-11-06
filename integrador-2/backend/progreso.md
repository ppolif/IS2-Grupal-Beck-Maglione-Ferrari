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

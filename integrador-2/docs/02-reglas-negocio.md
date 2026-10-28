# Reglas de Negocio y Lógica de Dominio — Dulce Mayor

Este documento consolida todas las reglas de negocio, validaciones, cálculos y transiciones de estado que el backend (API REST) debe implementar. Cualquier desarrollador que codifique los `Services` debe guiarse estrictamente por estas especificaciones para evitar inconsistencias.

## 1. Ciclo de Vida del Pedido (Máquina de Estados)

El `Pedido` es la entidad transaccional central del sistema. Su estado (`EstadoPedido`) dicta qué operaciones son válidas.

### 1.1. Transiciones Permitidas y Restricciones

* **\[NUEVO\] -> `CARGADO`**:

  * **Cuándo:** El Preventor (o Jefe) carga un pedido desde el portal.

  * **Reglas:** El pedido debe tener al menos un ítem. El punto de venta asociado debe existir.

  * **Permisos en este estado:** Es el **único** estado donde el pedido puede ser modificado (agregar/quitar ítems, cambiar código de promoción).

* **`CARGADO` -> `FACTURADO`**:

  * **Cuándo:** Un Administrativo ejecuta la acción "Procesar Pedido".

  * **Reglas Críticas (Transaccionalidad):** El sistema debe verificar que **TODOS** los ítems del pedido tengan stock suficiente en ese instante. Si es exitoso, en una única transacción de base de datos (`@Transactional`), se descuenta el stock, se genera la `Factura` en estado `PENDIENTE_PAGO` y se emite el evento para el correo de confirmación.

* **`CARGADO` -> `SIN_STOCK`**:

  * **Cuándo:** Un Administrativo ejecuta "Procesar Pedido" pero **uno o más ítems** no tienen stock suficiente.

  * **Reglas:** El pedido queda retenido. No se descuenta **nada** de stock (ni siquiera de los ítems que sí tenían) y no se genera factura.

* **`SIN_STOCK` -> `FACTURADO`**:

  * **Cuándo:** Tras una reposición de stock, se vuelve a intentar procesar el pedido y esta vez sí hay stock suficiente para todo.

* **`FACTURADO` -> `EN_REPARTO`**:

  * **Cuándo:** El pedido es asignado a una `RutaDistribuidor` para el día.

  * **Reglas:** Un pedido no puede ser asignado a dos rutas a la vez. Al pasar a este estado, se asigna el `Distribuidor` responsable en la entidad `Pedido`.

* **`EN_REPARTO` -> `ENTREGADO`**:

  * **Cuándo:** El Distribuidor marca la parada como completada en su aplicación móvil.

* **Cualquier Estado Previo (Solo `CARGADO` o `SIN_STOCK`) -> `ANULADO`**:

  * **Cuándo:** El Administrativo decide cancelar la operatoria.

  * **Reglas:** Un pedido `FACTURADO`, `EN_REPARTO` o `ENTREGADO` **jamás** puede ser anulado desde el módulo de pedidos. Si se necesita revertir, se debe operar sobre la Factura (Nota de crédito/Anulación) por fuera del flujo normal.

### 1.2. Trazabilidad Obligatoria (`HistorialEstadoPedido`)

Por cada cambio de estado (incluyendo su creación en `CARGADO`), el servicio de Pedidos debe instanciar e insertar un registro en `HistorialEstadoPedido`. Debe capturar:

* `estado`: El nuevo estado.

* `fechaHora`: Timestamp exacto del servidor.

* `usuario`: Nombre del usuario autenticado que disparó la acción.

* `observacion`: Opcional (ej. "Falta stock del producto X", o "Asignado a ruta del distribuidor Y").

## 2. Gestión de Precios (`VigenciaPrecio`) y Cotizaciones

Bajo ninguna circunstancia el frontend (cliente) envía precios en el JSON de creación del Pedido. Esto evita alteraciones maliciosas.

### 2.1. Resolución de Precios al Cargar Pedidos

* Cuando se recibe un `PedidoRequest` con una lista de `DetallePedidoRequest` (solo `productoId` y `cantidad`), el backend debe consultar la **Vigencia de Precio Actual** de cada producto.

* **Vigencia Actual:** Es aquel registro en `VigenciaPrecio` del producto donde `fechaHasta` es `null`.

* Este valor se copia a `precioUnitario` en el `DetallePedido`. Así, si el precio del producto cambia mañana, el histórico del pedido se mantiene inalterado.

### 2.2. Actualización de Precios (ABM Vigencias)

* Cuando un Administrativo registra un *nuevo* precio para un producto desde hoy, el backend debe:

  1. Buscar la vigencia actual (`fechaHasta == null`).

  2. Modificarla, estableciendo su `fechaHasta` al día de ayer (o un milisegundo antes de la nueva).

  3. Insertar el nuevo registro con `fechaDesde` hoy y `fechaHasta` en `null`.

* **Restricción:** No puede haber superposición de fechas (`Overlapping Dates`) para un mismo producto.

## 3. Lógica de Promociones y Descuentos

El descuento se calcula al momento de guardar o actualizar el Pedido.

* **Validación de Vigencia:** La fecha actual del servidor debe estar entre `fechaDesde` y `fechaHasta` de la promoción.

* **Condicionalidad:**

  * Si la promoción tiene un `productoId` definido: El descuento solo aplica si dicho producto está presente en los detalles del pedido.

  * Si la promoción tiene `cantidadMinima` definida: El descuento solo aplica si la suma total de unidades (de todos los ítems o del ítem promocionado) supera ese umbral.

* **Cálculo:** El `subtotal` del pedido es la suma de `(precioUnitario * cantidad)` de sus detalles. El `descuento` se calcula aplicando el `porcentajeDescuento` al subtotal. El `total` es `subtotal - descuento`.

## 4. Gestión de Stock y Catálogo Público

`Stock` opera como un **registro histórico (ledger inmutable) de cambios de inventario**. En el nuevo diseño UML, `Stock` **no contiene atributo directo `producto`**, sino que se relaciona únicamente con `DetalleFactura`, accediendo al producto a través de esta relación (`Stock` $\to$ `DetalleFactura` $\to$ `Producto`). En lugar de mutar un único registro por producto, cada evento que altera la cantidad disponible genera un nuevo registro que almacena la nueva cantidad disponible (`cantidadActual`), la marca temporal precisa (`fechaHora`) y una `observacion` descriptiva que clasifica la operación como un ingreso o egreso.

### 4.1. Visualización y Determinación de Stock Actual

* El stock actual disponible de un producto corresponde siempre al registro de `Stock` más reciente (ordenado por `fechaHora DESC`).
* En el endpoint público del catálogo (`/api/v1/publico/catalogo`) y en el endpoint de productos para el preventor (con `soloConStock=true`), la consulta a la base de datos filtra verificando que el último registro de stock del producto tenga `cantidadActual > 0`.
* Si el stock de un producto llega a 0 en su último movimiento, desaparece automáticamente del catálogo con stock sin intervención humana.

### 4.2. Operaciones sobre el Stock

Cada una de las siguientes operaciones inserta un nuevo registro inmutable en `Stock`:

* **Stock inicial:** Al crearse un `Producto`, se dispara un evento de dominio (`ProductoCreadoEvent` mediante el patrón Observer) que crea el primer registro de stock con la cantidad inicial, `fechaHora = now` y `observacion = "Ingreso - Stock inicial al crear el producto"`.
* **Descuento automático (Egreso):** Ocurre **exclusivamente** durante el paso de `CARGADO` a `FACTURADO`. Se calcula `nuevaCantidad = ultimoStock - DetallePedido.cantidad`, se crea un nuevo registro con `observacion = "Egreso - Descuento por venta"` y se vincula a `DetalleFactura`. Si el stock disponible es insuficiente, la operación es rechazada con HTTP 409 (`STOCK_INSUFICIENTE`).
* **Reposición (`/reposiciones`) (Ingreso):** Suma la cantidad indicada a la última cantidad disponible (`nuevaCantidad = ultimoStock + cantidad`) y persiste un nuevo registro con `observacion = "Ingreso - Reposición"`. Idealmente, el servicio puede retornar o notificar (vía logs/eventos) qué pedidos en `SIN_STOCK` ya podrían procesarse.
* **Ajuste de inventario (`/stock/ajuste`):** Usado tras un conteo físico, mermas o roturas. Persiste un nuevo registro con la cantidad contada físicamente como `cantidadActual`, registrando en `observacion` si resultó un ingreso o egreso por ajuste de inventario.

## 5. Facturación y Pagos

### 5.1. Generación de Factura

* Al pasar a `FACTURADO`, se crea la `Factura` heredando el monto `total` del pedido. Nace en estado `PENDIENTE_PAGO`.

* Se clonan los detalles del pedido hacia `DetalleFactura` (para mantener la integridad si el pedido es archivado a futuro).

### 5.2. Reglas de Pagos

* Un pago (`Pago`) se imputa a una factura.

* **Validación de monto:** La API debe rechazar (HTTP 409) cualquier intento de registrar un pago cuyo monto sea superior al `saldo` de la factura (donde `saldo = total - totalPagado`).

* **Transición:** Al insertar un pago exitosamente, se suma al `totalPagado`. Si `totalPagado == total`, la Factura pasa a estado `PAGADA`.

* **Pagos con Billetera Virtual (QR):** El endpoint de QR genera una imagen al vuelo codificando una URL o cadena con el formato: `DULCEMAYOR|FACTURA:{id}|MONTO:{saldo}`. No interactúa con redes reales de cobro.

## 6. Lógica de Enrutamiento y Mapas (Logística)

El sistema optimiza los recorridos usando el algoritmo del **Vecino Más Cercano (Nearest Neighbor)** basado en la fórmula de **Distancia Haversine** (cálculo de distancia geodésica entre dos puntos de latitud/longitud). *Se utiliza la libreria OpenStreetMap para esto.*

### 6.1. Ruta del Preventor

* **Generación:** Se toman todos los Puntos de Venta que tienen asignado a este preventor (`preventorId`) y se los muestra en un mapa.

* El ordenamiento inicial asume un punto de partida (provisto en el request) y busca el punto de venta más cercano. Luego, desde ese punto, el siguiente más cercano, y así sucesivamente.

* **Avance:** Tras cargar un pedido, el preventor marca el punto como completado. El sistema debe recalcular y devolver el siguiente punto pendiente más cercano a su ubicación actual, omitiendo los ya visitados.

### 6.2. Ruta del Distribuidor

* **Generación:** El Jefe selecciona múltiples pedidos en estado `FACTURADO` y los asigna al distribuidor.

* El backend recupera las direcciones de los Puntos de Venta de cada pedido. Aplica el algoritmo de Vecino Más Cercano para ordenar la lista.

* Al crearla, los pedidos seleccionados cambian automáticamente su estado a `EN_REPARTO`.

### 6.3. Avance y Notificación (Distribuidor)

* Al invocar `/rutas-distribuidor/{id}/entregas`, el pedido pasa a `ENTREGADO`.

* El sistema marca la parada como completada.

* **Regla Crítica:** En el mismo flujo, el backend debe identificar cuál es la *siguiente parada* en el arreglo. Si existe, debe disparar un evento asíncrono para enviar un correo al cliente (`Minorista`) de esa próxima parada con el asunto: "En breve se realizará la entrega de su pedido Dulce Mayor".

* Si no hay más paradas, la ruta pasa a `REALIZADA`.

## 7. Integración de IA (Chatbot Institucional)

* **Alcance:** El modelo de lenguaje provisto por Spring AI tiene un rol estrictamente de **procesamiento de lenguaje natural (NLP)**, no transaccional.

* **Flujo:**

  1. El usuario ingresa "Hola, quiero dos cajas de alfajores triples y 5 chocolates águila".

  2. El backend pasa el texto al LLM con un prompt del sistema instruyéndolo a extraer entidades (nombre del producto y cantidades numéricas) en un formato JSON estructurado.

  3. El backend recibe el JSON, cruza los nombres de productos devueltos por la IA con la base de datos (con un LIKE o similar).

  4. **Cálculos:** El backend asume la responsabilidad matemática. Valida el stock actual, cruza con los precios vigentes y calcula los subtotales y el total.

  5. Si la IA detectó un producto que no existe o no hay stock, el backend lo añade al arreglo de `advertencias` en la respuesta (ej. "No se encontraron alfajores triples en stock").

* **Restricción Estricta:** La IA **nunca** debe responder directamente al cliente con cálculos de dinero o confirmaciones de stock ficticias (alucinaciones). La IA extrae, el backend calcula, y el controlador responde el DTO final estructurado.

## 8. Tareas Programadas y Eventos (Schedulers)

* **Newsletter Mensual:** Se utilizará la anotación `@Scheduled(cron = "0 0 8 5 * ?")` (Día 5 de cada mes a las 08:00 AM) en un servicio de Spring. Este método iterará sobre todos los Minoristas que tengan email cargado y enviará un listado HTML iterando sobre los productos donde `enOferta == true`.

* **Envío de Facturas:** Se utiliza Spring Application Events. Tras generarse la factura (en la transacción de procesamiento de pedido), se emite un `FacturaGeneradaEvent`. Un `@EventListener(classes = FacturaGeneradaEvent.class)` asíncrono toma la factura, genera el PDF en memoria usando iText y la envía como adjunto al correo del Minorista.

## 9. Convenciones de Borrado (Soft Delete)

Ninguna entidad maestra (Producto, Cliente, Empleado, Usuario, Punto de Venta, Categoría) debe ser borrada físicamente de la base de datos mediante la instrucción `DELETE`.

* Todos los repositorios deben sobreescribir el método `deleteById()` para ejecutar un `UPDATE entidad SET eliminado = true WHERE id = ?`.

* Las consultas genéricas (`findAll`) deben incluir una cláusula `@Where(clause = "eliminado = false")` a nivel de entidad de Hibernate para garantizar que no se recuperen datos archivados en la operatoria normal.
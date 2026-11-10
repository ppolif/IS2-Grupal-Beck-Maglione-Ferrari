# Explicación Técnica de la Integración de Pasarela de Pagos (Mercado Pago, Facturación y Logística)

Este documento detalla los fundamentos de diseño, los patrones de software aplicados y el inventario exhaustivo de cambios (clase por clase y método por método) realizados en el proyecto **Dulce Mayor**.

---

## 1. Justificación del Patrón Strategy: ¿Por qué tenemos clases específicas de estrategia?

### 1.1 El Problema de Diseño sin Strategy (Antipadrón)
Si no utilizáramos el patrón Strategy, el sistema caería en un diseño frágil con bifurcaciones condicionales repetitivas (`if-else` o `switch (tipoPago)`):

```java
// Código acoplado y rígido (Antipatrón violatorio de Open/Closed)
if (tipo == BILLETERA_VIRTUAL) {
    // 1. Llamar a API de Mercado Pago (Orders API)
    // 2. Calcular firma criptográfica HMAC
    // 3. Generar imagen QR con ZXing en Base64
    // 4. Configurar expiración en 15 minutos
} else if (tipo == TRANSFERENCIA) {
    // 1. Configurar vencimiento en 24 horas
    // 2. Dejar en estado PENDIENTE_VERIFICACION
    // 3. No generar QR
} else if (tipo == EFECTIVO) {
    // 1. Postergar cobro para la entrega física
    // 2. No llamar a ninguna pasarela
}
```

**Desventajas del enfoque condicional:**
1. **Violación del principio Open/Closed (SOLID)**: Ante un nuevo medio de pago (ej. Modo, Cheque electrónico ECHEQ o Cuenta Corriente con crédito a 30 días), habría que modificar clases ya probadas y productivas, arriesgando la estabilidad del sistema.
2. **Alto Acoplamiento**: El servicio de negocio mezclaría lógica de red de pasarelas, generación de imágenes QR, cálculos de tiempos y conciliaciones manuales.
3. **Dificultad de Testing**: Probar un medio requeriría instanciar y mockear dependencias irrelevantes de los otros medios.

---

### 1.2 La Solución: Patrón Strategy (GoF Comportamental)
El patrón Strategy desacopla el **"qué hacer"** (iniciar un pago y determinar sus reglas de acreditación) del **"cómo se hace"** (las particularidades de cada medio de cobro).

El servicio central (`ProcesamientoPagoService`) delega en la interfaz genérica `PagoStrategy` a través de la fábrica `PagoStrategyFactory`:

```
                             ┌───────────────────────────────┐
                             │       «interface»             │
                             │       PagoStrategy            │
                             ├───────────────────────────────┤
                             │ + iniciarPago(Pedido): Resp   │
                             │ + requiereAprobacionManual()  │
                             │ + notificaPorWebhook()        │
                             └───────────────▲───────────────┘
                                             │
         ┌───────────────────────────────────┼───────────────────────────────────┐
         │                                   │                                   │
┌────────┴───────────────────┐ ┌─────────────┴─────────────────┐ ┌───────────────┴─────────────────┐
│   QrMercadoPagoStrategy    │ │  TransferenciaManualStrategy  │ │ EfectivoContraEntregaStrategy   │
├────────────────────────────┤ ├───────────────────────────────┤ ├─────────────────────────────────┤
│ • Llama a MP Orders API    │ │ • Fija timeout en 24 horas    │ │ • No contacta pasarela externa  │
│ • QR dinámico EMVCo        │ │ • No genera QR                │ │ • No emite cobro inmediato      │
│ • Genera imagen PNG Base64 │ │ • Requiere revisión humana    │ │ • Difiere el pago al reparto    │
│ • Expiración: 15 minutos   │ │ • Aprobación manual: SI       │ │ • Cobro directo por chofer      │
│ • Webhook automático: SI   │ │ • Webhook: NO                 │ │ • Webhook / Aprobación: NO      │
└────────────────────────────┘ └───────────────────────────────┘ └─────────────────────────────────┘
```

---

### 1.3 Comportamiento y Responsabilidad de cada Estrategia

| Medio de Pago | Estrategia Concreta | Aprobación Manual | Webhook | Tiempo de Expiración | Comportamiento Principal |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Billetera Virtual** | `QrMercadoPagoStrategy` | No | Sí | 15 minutos | Llama a la API de Mercado Pago, genera código EMVCo dinámico e imagen PNG Base64. Acreditación automática y en tiempo real. |
| **Transferencia Bancaria** | `TransferenciaManualStrategy` | Sí | No | 24 horas | Fija el estado en verificación, muestra datos CBU/Alias oficiales y espera el reporte del comprobante y cotejo del administrativo. |
| **Efectivo contra entrega** | `EfectivoContraEntregaStrategy` | No | No | Sin vencimiento | Habilita el despacho hacia la ruta de entrega sin pago previo; el cobro y la factura se completan en mano en el local del cliente. |

---

## 2. Inventario Detallado de Cambios (Clase por Clase y Método por Método)

---

### 2.1 Dominio y Persistencia (Backend)

#### `EstadoPago.java` (Nueva Enumeración)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/entities/facturacion/EstadoPago.java`
* `PENDIENTE`: Pago registrado o informado, a la espera de confirmación de webhook o cotejo bancario.
* `APROBADO`: Fondos acreditados fehacientemente (desencadena facturación electrónica y descuento de stock).
* `RECHAZADO`: Pago denegado por Mercado Pago o desestimado por tesorería con motivo justificado.
* `DEVUELTO`: Reembolso o anulación posterior de la operación.

#### `Pago.java` (Entidad Modificada)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/entities/facturacion/Pago.java`
* `estadoPago`: Enumeración `EstadoPago`.
* `mpPaymentId`: ID externo devuelto por Mercado Pago (ej. `1234567890`) para conciliación y trazabilidad.
* `mpStatus`: Estado nativo de la pasarela (`approved`, `rejected`, `in_process`).
* `usuarioAuditor`: Registra quién aprobó o cobró el pago (ej. `"Mercado Pago Webhook"`, transportista o usuario administrativo).
* `pedido`: Relación `@ManyToOne` directa con `Pedido` (permite registrar intentos de pago previos a la emisión de factura).
* `factura`: Se modificó a opcional (`nullable = true`) para admitir pagos pendientes sin factura anticipada.

#### `Factura.java` (Entidad Modificada)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/entities/facturacion/Factura.java`
* `caeSimulado`: Código de Autorización Electrónico simulado de 14 dígitos (normativa AFIP/ARCA).
* `fechaVencimientoCae`: Fecha límite fiscal de validez del CAE (10 días posteriores a la emisión).
* `cuitReceptor`: CUIT o DNI tributario del minorista comprador.
* `condicionIvaReceptor`: Condición impositiva (`RESPONSABLE_INSCRIPTO`, `MONOTRIBUTO`).

#### `Pedido.java` (Entidad Modificada)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/entities/pedido/Pedido.java`
* `formaDePago`: Vinculación con la `FormaDePago` seleccionada.
* `fechaExpiracionPago`: Timestamp de caducidad para el pago actual (15 min para QR, 24 hs para transferencia).

#### `WebhookNotificationLog.java` (Nueva Entidad)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/entities/facturacion/WebhookNotificationLog.java`
* Almacena cada webhook entrante para garantizar **idempotencia** y auditoría: `idTransaccionMp`, `requestId`, `action`, `topic`, `xSignature`, `payloadJson`, `estadoProcesamiento` y `fechaHora`.

#### `PagoRepository.java` (Repositorio Modificado)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/repositories/facturacion/PagoRepository.java`
* `findAllByPedidoIdOrderByFechaPagoDesc(String pedidoId)`: Lista el historial de transacciones de un pedido.
* `findByMpPaymentId(String mpPaymentId)`: Busca pagos por identificador de Mercado Pago (previene duplicaciones).
* `findAllByEstadoPagoOrderByFechaPagoDesc(EstadoPago estado)`: Permite recuperar transferencias `PENDIENTE`.

#### `WebhookNotificationLogRepository.java` (Nuevo Repositorio)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/repositories/facturacion/WebhookNotificationLogRepository.java`
* `existsByIdTransaccionMpAndEstadoProcesamiento(String id, String estado)`: Consulta de alta velocidad para descartar notificaciones duplicadas en menos de 1 ms.

---

### 2.2 DTOs (Data Transfer Objects) y Eventos

* `IniciarPagoQrResponse.java`: Devuelve al cliente el payload EMVCo, la imagen QR en Base64, el vencimiento y los segundos restantes.
* `MercadoPagoWebhookPayload.java`: Mapeo tipado del payload de Mercado Pago (`action`, `type`, `data.id`).
* `InformarTransferenciaRequest.java`: Solicitud enviada por el minorista al informar comprobante y banco emisor.
* `AprobarTransferenciaRequest.java`: Solicitud del administrativo con observaciones de acreditación bancaria.
* `RechazarPagoRequest.java`: Solicitud del administrativo indicando el motivo de rechazo del comprobante.
* `DatosBancariosEmpresaResponse.java`: DTO con la información oficial CBU/Alias/CUIT de la empresa.
* `RegistrarCobroEfectivoRequest.java`: Solicitud del distribuidor para asentar el cobro exacto en mano en destino.
* `PagoAprobadoEvent.java`: Evento de dominio de Spring publicado ante cada pago confirmado.
* `PagoRechazadoEvent.java`: Evento de dominio de Spring publicado ante pagos denegados o cancelados.

---

### 2.3 Adaptador de Mercado Pago y Generador QR (Adapter Pattern)

#### `MercadoPagoPort.java` (Interfaz Target)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/adapter/MercadoPagoPort.java`
* `crearOrdenQrDinamica(...)`: Define el contrato para generar órdenes dinámicas con vencimiento.
* `consultarPago(paymentId)`: Define el contrato para la verificación activa del estado en la API oficial.
* `validarFirmaWebhook(...)`: Define la validación de autenticidad de la cabecera `x-signature`.

#### `MercadoPagoAdapter.java` (Clase Adapter Concreta)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/adapter/MercadoPagoAdapter.java`
* `crearOrdenQrDinamica(externalReference, titulo, monto, minutosExpiracion)`: Invoca la Orders API de Mercado Pago vía HTTP. Si detecta tokens de prueba o entorno local, conmuta a simulación EMVCo sin bloquear la ejecución.
* `consultarPago(paymentId)`: Ejecuta una llamada `GET /v1/payments/{id}` a Mercado Pago para confirmar fehacientemente que el estado sea `approved`.
* `validarFirmaWebhook(xSignature, xRequestId, dataId)`: Parsea `ts` y `v1`, genera el hash HMAC-SHA256 con el `webhookSecret` y descarta peticiones con antigüedad mayor a 5 minutos (mitiga ataques de replay).
* `generarStringEmvcoSimulado(...)`: Genera una cadena estándar interoperable EMVCo compatible con billeteras virtuales para desarrollo local.

#### `QrCodeGeneratorHelper.java` (Helper ZXing)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/QrCodeGeneratorHelper.java`
* `generarQrPngBytes(contenido, ancho, alto)`: Transforma la cadena de datos en una matriz de píxeles y genera un archivo PNG en memoria.
* `generarQrPngBase64(contenido, ancho, alto)`: Transforma los bytes PNG a Base64 estándar para renderizar como `<img src="data:image/png;base64,...">`.

---

### 2.4 Patrón Strategy: Implementación Concreta

#### `PagoStrategy.java` (Interfaz Strategy)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/strategy/PagoStrategy.java`
* `getTipoPago()`: Devuelve el `TipoPago` que implementa.
* `iniciarPago(Pedido pedido)`: Inicializa el flujo y devuelve la respuesta correspondiente.
* `requiereAprobacionManual()`: Indica si el medio demanda revisión administrativa.
* `notificaPorWebhook()`: Indica si se acredita de forma desatendida mediante webhook.

#### `QrMercadoPagoStrategy.java`
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/strategy/QrMercadoPagoStrategy.java`
* `iniciarPago(Pedido pedido)`: Llama a `MercadoPagoPort`, calcula 15 minutos de expiración y genera el Base64 del QR.
* `requiereAprobacionManual()`: Retorna `false`.
* `notificaPorWebhook()`: Retorna `true`.

#### `TransferenciaManualStrategy.java`
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/strategy/TransferenciaManualStrategy.java`
* `iniciarPago(Pedido pedido)`: Fija 24 horas de expiración y asocia la forma de pago `TRANSFERENCIA`.
* `requiereAprobacionManual()`: Retorna `true`.
* `notificaPorWebhook()`: Retorna `false`.

#### `EfectivoContraEntregaStrategy.java`
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/strategy/EfectivoContraEntregaStrategy.java`
* `iniciarPago(Pedido pedido)`: Prepara el pedido para la entrega física posponiendo la cobranza.
* `requiereAprobacionManual()`: Retorna `false`.
* `notificaPorWebhook()`: Retorna `false`.

#### `PagoStrategyFactory.java` (Fábrica Creacional)
Ubicación: `backend/src/main/java/com/DulceMayor/backend/services/facturacion/strategy/PagoStrategyFactory.java`
* `obtenerEstrategia(TipoPago tipoPago)`: Resuelve en tiempo de ejecución la estrategia correspondiente a través de un mapa inyectado por Spring.

---

### 2.5 Servicios del Backend y Orquestación

#### `FormaDePagoService.java`
* `obtenerOCrearPorTipo(TipoPago tipoPago)`: Busca o inicializa dinámicamente la entidad `FormaDePago` en la base de datos si aún no existe.

#### `FacturaService.java`
* `emitirFacturaConPagoAprobado(Pedido pedido)`:
  1. Emite la `Factura` en estado `PAGADA`.
  2. Genera el CAE simulado de 14 dígitos y calcula su vencimiento a 10 días.
  3. Descuenta automáticamente el stock mediante `stockService.descontarPorVenta(...)`.
  4. Genera el comprobante oficial en formato PDF con CAE visible.
  5. Envía automáticamente un correo al minorista con el comprobante adjunto.
* `generarPdfFactura(...)`: Actualizado para incluir código de barras simulado, CAE y datos tributarios del comprador.

#### `WebhookNotificationLogService.java`
* `yaFueProcesado(idTransaccionMp)`: Consulta de idempotencia.
* `registrarRecibido(...)`: Guarda el log en estado `RECIBIDO`.
* `marcarProcesado(log)`: Actualiza a `PROCESADO`.
* `marcarFallido(log, motivo)`: Registra el error en auditoría.

#### `ProcesamientoPagoService.java` (Servicio Orquestador Central)
* `iniciarPagoQr(pedidoId)`: Valida el estado del pedido, delega en la estrategia QR y registra la generación en el historial.
* `consultarEstadoQr(pedidoId)`: Realiza polling del estado y calcula los segundos restantes para la expiración.
* `procesarWebhookMercadoPago(xSignature, xRequestId, payload)`:
  1. Valida criptográficamente la firma del webhook.
  2. Aplica filtro de idempotencia.
  3. Llama a `GET /v1/payments/{id}` en la API de Mercado Pago.
  4. Si el pago es `approved`, llama a `confirmarCobroYFacturar(...)`.
  5. Si es `rejected`, dispara `PagoRechazadoEvent`.
* `confirmarCobroYFacturar(...)`: Método privado atómico (`@Transactional`) que crea el `Pago` aprobado, emite la factura electrónica, descuenta el stock, pasa el pedido a `FACTURADO` y publica el evento de dominio.
* `informarTransferencia(pedidoId, request, usuario)`: Guarda el pago en estado `PENDIENTE` con el comprobante declarado por el cliente.
* `aprobarTransferencia(pagoId, request, usuarioAdmin)`: Permite al administrativo confirmar el ingreso de fondos, emitiendo factura y descontando stock.
* `rechazarPago(pagoId, request, usuarioAdmin)`: Pasa el pago a `RECHAZADO` con motivo justificado sin emitir factura ni alterar el inventario.
* `registrarCobroEfectivoEnEntrega(request, distribuidor)`: Valida el importe exacto cobrado por el chofer, emite la factura, descuenta stock y marca el pedido como `ENTREGADO`.
* `obtenerDatosBancariosEmpresa()`: Retorna los datos CBU/Alias oficiales de la empresa.
* `listarTransferenciasPendientes()`: Lista las transferencias a la espera de conciliación.

---

### 2.6 Controladores REST y Seguridad

#### `PagoOperacionController.java`
* `iniciarPagoQr`: `POST /pedidos/{id}/pagos/qr/iniciar` (`CLIENTE`, `ADMIN`, `JEFE`).
* `consultarEstadoQr`: `GET /pedidos/{id}/pagos/qr/estado` (`CLIENTE`, `ADMIN`, `JEFE`).
* `recibirWebhookMercadoPago`: `POST /pagos/webhook/mercadopago` (Público para servidores de Mercado Pago).
* `obtenerDatosBancarios`: `GET /pagos/datos-bancarios` (Público / Autenticado).
* `informarTransferencia`: `POST /pedidos/{id}/pagos/transferencia/informar` (`MINORISTA`, `ADMINISTRATIVO`, `JEFE`).
* `listarTransferenciasPendientes`: `GET /pagos/transferencias-pendientes` (`ADMINISTRATIVO`, `JEFE`).
* `aprobarTransferencia`: `POST /pagos/{id}/aprobar` (`ADMINISTRATIVO`, `JEFE`).
* `rechazarPago`: `POST /pagos/{id}/rechazar` (`ADMINISTRATIVO`, `JEFE`).
* `registrarCobroEfectivo`: `POST /pagos/efectivo/registrar-cobro` (`DISTRIBUIDOR`, `ADMINISTRATIVO`, `JEFE`). **El rol `PREVENTISTA` queda expresamente excluido de este endpoint**, garantizando que el preventor no pueda recibir dinero en efectivo ni dar pedidos por pagados.

#### `SecurityConfig.java`
* Configuración de `permitAll()` para `/pagos/webhook/**`, `/api/v1/pagos/webhook/**`, `/pagos/datos-bancarios` y `/api/v1/pagos/datos-bancarios`.

---

### 2.7 Frontends

#### `frontend-institucional` (Portal Minorista)
* `pagoService.js`: Cliente HTTP centralizado (`iniciarPagoQr`, `consultarEstadoQr`, `obtenerDatosBancarios`, `informarTransferencia`).
* `Checkout.jsx`:
  * Renderizado del QR dinámico en imagen Base64 con temporizador de 15 minutos.
  * Botón de regeneración de QR al vencer el tiempo sin perder los ítems del carrito.
  * Polling automático cada 4 segundos: al confirmarse el webhook, redirige a `PagoExito.jsx`.
  * Consulta dinámica de CBU/Alias bancario desde la API con botones de copiado rápido.
  * Envío del comprobante de transferencia al backend al confirmar el pedido.

#### `frontend-admin` (Dashboard Administrativo)
* `pagoAdminService.js`: Cliente de administración (`listarTransferenciasPendientes`, `aprobarPago`, `rechazarPago`).
* `usePagos.js`: Hook reactivo que gestiona transferencias pendientes y modales de aprobación/rechazo.
* `Pagos.jsx`: Pestaña dedicada "Transferencias Pendientes de Cotejo Bancario", botón de aprobación con emisión inmediata de factura y modal de rechazo con justificación obligatoria.

#### `frontend-preventor` (App de Preventa y Distribución)
* `recorridoService.js`: Servicio centralizado que calcula en tiempo real los pedidos entregados, pedidos pendientes y el dinero total recaudado en efectivo en la ruta en curso a partir de los remitos cobrados.
* `Modal.jsx`: Componente modal reutilizable para el distribuidor.
* `Carrito.jsx` (Carga y registro de medios de pago por el Preventor):
  * **Mercado Pago QR**: Muestra el código QR dinámico interoperable en la pantalla del celular/tablet para que el comerciante lo escanee in situ con la app de Mercado Pago o su billetera virtual, con temporizador de 15 minutos y botón de regeneración.
  * **Transferencia Bancaria**: Muestra el CBU y Alias oficial de la empresa con botones de copiado rápido, y habilita al preventor a registrar el N° de comprobante exhibido por el cliente para **marcar el pedido como pago** (informado).
  * **Efectivo contra entrega**: Deja constancia expresa de que **el preventor no recibe dinero en efectivo**; asienta el pedido con cobro diferido a cargo del chofer distribuidor en la entrega física.
* `Recorrido.jsx`:
  * Badge superior en tiempo real con el **dinero total recaudado en efectivo** en la ruta actual, además de las entregas realizadas y pendientes.
  * Al marcar como entregado un pedido en efectivo, solicita el número de recibo/remito entregado, confirma el cobro, suma el dinero a la caja del distribuidor, emite la factura, descuenta stock, marca `ENTREGADO`, propone la siguiente parada y notifica por correo al siguiente cliente.
* `Perfil.jsx`:
  * **Vista Distribuidor**: Muestra contador de **Pedidos Entregados**, **Pedidos Pendientes** y un módulo destacado con el **Dinero Recaudado en Efectivo en su Ruta Actual** y el saldo restante por cobrar para la rendición de caja final en tesorería.
  * **Vista Preventista**: Enfocada en visitas comerciales y toma de pedidos, sin facultades de recepción de dinero en efectivo.

---

### 2.8 Pruebas Automatizadas

#### `ProcesamientoPagoServiceTest.java` (7 Tests Unitarios)
1. `iniciarPagoQrExitoso`: Verifica generación del QR EMVCo y registro en historial.
2. `webhookIdempotencia`: Verifica que notificaciones repetidas no generen pagos duplicados.
3. `webhookPagoAprobado`: Verifica flujo completo de aprobación automática, facturación y cambio de estado a `FACTURADO`.
4. `informarTransferenciaExitoso`: Verifica registro de pago `PENDIENTE` con datos del comprobante.
5. `aprobarTransferenciaExitoso`: Verifica aprobación de tesorería, emisión de factura y transición de pedido.
6. `rechazarPagoExitoso`: Verifica pase a `RECHAZADO` sin impacto en stock ni facturación.
7. `registrarCobroEfectivoExitoso`: Verifica cobro en mano del transportista, emisión de factura y pase a `ENTREGADO`.

#### `ArquitecturaTests.java` (6 Tests de ArchUnit)
* Verificación al 100% de aislamiento entre capas, comunicación exclusiva mediante DTOs y restricción de dependencias entre repositorios.

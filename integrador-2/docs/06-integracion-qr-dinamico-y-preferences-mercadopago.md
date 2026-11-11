# Documentación Técnica: Integración de QR Dinámico y Fallback con Preferences en Mercado Pago

**Proyecto:** Dulce Mayor  
**Fecha:** Noviembre 2026  
**Rama:** `tincho`  
**Autores:** Equipo de Desarrollo / Antigravity Agentic Pair  
**Módulo:** Facturación, Pagos y Pasarelas (`com.DulceMayor.backend.services.facturacion`)  

---

## Índice General

1. [Resumen Ejecutivo y Objetivos](#1-resumen-ejecutivo-y-objetivos)
2. [Diagnóstico y Análisis Forense: Causas de Falla Previas](#2-diagnóstico-y-análisis-forense-causas-de-falla-previas)
3. [Decisiones Arquitectónicas y Técnicas (Justificación y Respaldo)](#3-decisiones-arquitectónicas-y-técnicas-justificación-y-respaldo)
4. [Especificación Técnica de APIs de Mercado Pago Integradas](#4-especificación-técnica-de-apis-de-mercado-pago-integradas)
5. [Estándar Matricial ZXing y Formato Interoperable EMVCo](#5-estándar-matricial-zxing-y-formato-interoperable-emvco)
6. [Seguridad Criptográfica y Validación de Webhooks](#6-seguridad-criptográfica-y-validación-de-webhooks)
7. [Ciclo de Vida de Transacciones y Máquina de Estados](#7-ciclo-de-vida-de-transacciones-y-máquina-de-estados)
8. [Diagrama de Secuencia y Arquitectura de Componentes](#8-diagrama-de-secuencia-y-arquitectura-de-componentes)
9. [Guía de Configuración del Proyecto y Entorno Local](#9-guía-de-configuración-del-proyecto-y-entorno-local)
10. [Guía de Configuración en Mercado Pago Developers](#10-guía-de-configuración-en-mercado-pago-developers)
11. [Guía de Pruebas Extremo a Extremo (E2E) y Simulación](#11-guía-de-pruebas-extremo-a-extremo-e2e-y-simulación)
12. [Matriz de Diagnóstico y Resolución de Problemas (Troubleshooting)](#12-matriz-de-diagnóstico-y-resolución-de-problemas-troubleshooting)

---

## 1. Resumen Ejecutivo y Objetivos

El sistema de comercio electrónico y gestión mayorista de **Dulce Mayor** cuenta con un módulo de pagos que implementa el patrón de diseño **Strategy** (GoF) para desacoplar los distintos medios de cobro:
- `TransferenciaManualStrategy` (Transferencia bancaria con cotejo de comprobante administrativo).
- `EfectivoContraEntregaStrategy` (Cobro contra entrega física en ruta por chofer distribuidor).
- `QrMercadoPagoStrategy` (Cobro digital automático e instantáneo vía billetera virtual).

Para este último medio, el presente documento detalla la implementación integral de una **estrategia de cobro híbrida y resiliente de 3 niveles**:

```
                       ┌──────────────────────────────────────────────────┐
                       │           POST /pedidos/{id}/pagos/qr/iniciar    │
                       └─────────────────────────┬────────────────────────┘
                                                 │
                                                 ▼
                             ┌──────────────────────────────────────┐
                             │  NIVEL 1: QR Dinámico In-Store (POS) │
                             │  POST /instore/orders/qr/.../pos/... │
                             └───────────────────┬──────────────────┘
                                                 │
                                 ┌───────────────┴───────────────┐
                     ¿Respuesta HTTP 201?                        │ Falla (HTTP 4xx / 5xx)
                                 │ Sí                            ▼
                                 │               ┌──────────────────────────────────────┐
                                 │               │    NIVEL 2: Checkout Preferences     │
                                 │               │    POST /checkout/preferences        │
                                 │               └───────────────────┬──────────────────┘
                                 │                                   │
                                 │                   ┌───────────────┴───────────────┐
                                 │       ¿Respuesta HTTP 201?                        │ Falla
                                 │                   │ Sí                            ▼
                                 │                   │               ┌───────────────────────────────────┐
                                 │                   │               │     NIVEL 3: Offline EMVCo CRC-16 │
                                 │                   │               │     Generación sintética local    │
                                 │                   │               └─────────────────┬─────────────────┘
                                 │                   │                                 │
                                 ▼                   ▼                                 ▼
                       ┌──────────────────────────────────────────────────────────────────┐
                       │     Generador Matricial ISO/IEC 18004 con ZXing (Base64 PNG)     │
                       └─────────────────────────────────┬────────────────────────────────┘
                                                         │
                                                         ▼
                                       ┌───────────────────────────────────┐
                                       │   Respuesta JSON IniciarPagoQr    │
                                       │   (qrBase64, initPointUrl, ttl)   │
                                       └───────────────────────────────────┘
```

1. **Nivel Primario (QR Dinámico Presencial - In-Store POS API)**: Generación del código QR dinámico oficial de Mercado Pago mediante la API `/instore/orders/qr/seller/collectors/{userId}/pos/{externalPosId}/qrs`. El código generado es estándar EMVCo interoperable y puede ser escaneado directamente desde la app de Mercado Pago o billeteras bancarias habilitadas.
2. **Nivel Secundario (Fallback a Mercado Pago Checkout Preferences)**: Si la cuenta colectora o el entorno de pruebas experimenta restricciones en la venta presencial por QR físico, el sistema conmuta automáticamente a la API `/checkout/preferences`. Esta API devuelve una URL oficial de pago (`init_point` / `sandbox_init_point`), a partir de la cual el backend genera un código QR estándar escaneable y el frontend ofrece un botón directo de redirección web.
3. **Nivel Terciario (Fallback Offline / Mock EMVCo con CRC-16)**: En caso de desconexión total o indisponibilidad temporal de los servidores de Mercado Pago, el sistema genera un payload EMVCo sintético con checksum CRC-16-CCITT válido para mantener el flujo operativo en entornos de prueba locales sin arrojar excepciones 500 al cliente.

---

## 2. Diagnóstico y Análisis Forense: Causas de Falla Previas

Antes de esta refactorización, el escaneo del código QR fallaba sistemáticamente. La auditoría reveló dos causas raíz críticas:

### 2.1 Generación Gráfica Inválida en `QrCodeGeneratorHelper.java`
- **Diagnóstico del código original**: La clase utilitaria dibujaba un mapa de bits de forma manual utilizando primitivas de `java.awt.Graphics2D`. Rellenaba un fondo blanco, pintaba 3 cuadrados esquineros celestes (`new Color(0, 158, 227)`) y luego recorría un arreglo de bytes del payload calculando bits con operadores a nivel de bits (`(bytes[bytePos] >> bitPos) & 1`).
- **Incumplimiento de Estándares**:
  - **Ausencia de Codificación Reed-Solomon**: Un código QR no es un volcado directo de bits; requiere polinomios de corrección de errores de Galois Field $GF(2^8)$.
  - **Falta de Patrones de Sincronización (Timing Patterns)**: Carecía de la alternancia de módulos blancos y negros en la fila 6 y columna 6 que los sensores ópticos usan para medir la densidad matricial.
  - **Falta de Patrones de Alineación y Zonas de Silencio**: Ausencia del *quiet zone* perimetral de al menos 4 módulos.
  - **Incompatibilidad de Color**: Los esquineros coloreados de celeste rompían el ratio de contraste de luminancia requerido por los algoritmos binarizadores de las cámaras móviles.
- **Resultado**: Ningún lector QR (ni la cámara nativa de Android/iOS ni la app de Mercado Pago) podía decodificar el gráfico generado.

### 2.2 Rechazo de la API de Mercado Pago (`HTTP 400 Bad Request`)
- **Diagnóstico de las peticiones HTTP**:
  1. La implementación original llamaba al endpoint `/v1/orders` omitiendo la cabecera obligatoria `X-Idempotency-Key`, produciendo:
     ```json
     {
       "message": "Missing HTTP header: X-Idempotency-Key",
       "error": "bad_request",
       "status": 400
     }
     ```
  2. Al agregar dicha cabecera, la API exigía el atributo `"type": "qr"` y la presencia de un `external_pos_id` activo.
  3. Al consultar la cuenta colectora con el Access Token provisto, se constató que la cuenta poseía **0 sucursales (`stores`) y 0 cajas físicas (`pos`) registradas**.
- **Resultado**: La llamada a la pasarela fallaba de inmediato, forzando la ejecución de fallbacks incompletos.

---

## 3. Decisiones Arquitectónicas y Técnicas (Justificación y Respaldo)

A continuación se detalla cada una de las decisiones técnicas adoptadas en la solución definitiva:

### Decisión 1: Incorporación de ZXing (Zebra Crossing) para la Generación Matricial
* **Ubicación**: [backend/pom.xml](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/pom.xml) y [QrCodeGeneratorHelper.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/services/facturacion/QrCodeGeneratorHelper.java).
* **Justificación**:
  - ZXing es la biblioteca estándar de código abierto más probada y confiable del ecosistema Java para códigos matriciales 2D.
  - Garantiza estricta adhesión a la norma internacional **ISO/IEC 18004:2015**.
  - Se configuró con parámetros óptimos de lectura:
    - `BarcodeFormat.QR_CODE`: Formato bidimensional estandarizado.
    - `EncodeHintType.ERROR_CORRECTION = ErrorCorrectionLevel.M`: Tolerancia a daños u oclusiones de hasta un 15%.
    - `EncodeHintType.CHARACTER_SET = "UTF-8"`: Soporte para caracteres especiales y codificaciones URL complejas.
    - `EncodeHintType.MARGIN = 2`: Zona de silencio perimetral suficiente para evitar interferencias visuales con los bordes de la interfaz de usuario.
    - Tamaño de matriz: $300 \times 300$ píxeles, garantizando nitidez en pantallas de alta densidad (Retina / AMOLED).
  - Generación de imagen PNG en memoria mediante `ByteArrayOutputStream` codificada en Base64 (`data:image/png;base64,...`), evitando almacenamiento temporal en disco y riesgos de contención de E/S.
  - Validado mediante el test unitario [QrCodeGeneratorHelperTest.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/test/java/com/DulceMayor/backend/services/facturacion/QrCodeGeneratorHelperTest.java), que decodifica la imagen generada con `QRCodeReader` verificando la coincidencia exacta del string original.

### Decisión 2: Estrategia Híbrida Resiliente de 3 Niveles
* **Ubicación**: [MercadoPagoAdapter.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/services/facturacion/adapter/MercadoPagoAdapter.java).
* **Justificación**:
  - La API de In-Store QR de Mercado Pago (`/instore/orders/qr/...`) está pensada primordialmente para terminales de punto de venta físico vinculadas a una ubicación geográfica real. En cuentas de Sandbox o integraciones de prueba, Mercado Pago suele aplicar políticas estrictas de validación de perfil fiscal y de comercio.
  - Si el Nivel 1 falla por cualquier razón de infraestructura o restricción de cuenta, el sistema conmuta automáticamente al **Nivel 2 (Checkout Preferences API)**.
  - Checkout Preferences (`/checkout/preferences`) es la pasarela web insignia de Mercado Pago. Al convertir su enlace oficial (`init_point` / `sandbox_init_point`) en un código QR legible y exponer el enlace web simultáneamente, se garantiza que el usuario siempre pueda concretar su pago, ya sea apuntando su teléfono a la pantalla o haciendo clic directamente en el botón de pago desde su computadora.
  - Esta arquitectura desacopla la disponibilidad del negocio de las posibles contingencias del entorno de pruebas de la pasarela.

### Decisión 3: Aprovisionamiento Automático de Sucursal y Caja (Self-Healing / Auto-Provisioning)
* **Ubicación**: [MercadoPagoAdapter.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/services/facturacion/adapter/MercadoPagoAdapter.java) (`obtenerUserIdColector`, `asegurarSucursalYPos`).
* **Justificación**:
  - En lugar de requerir que cada operador o desarrollador configure manualmente tiendas y cajas mediante cURL o mediante el portal web de Mercado Pago, el backend asume un comportamiento **autónomo y autocurativo**:
    1. Consulta dinámicamente `GET /users/me` para obtener el ID numérico del usuario propietario del token.
    2. Consulta `GET /users/{userId}/stores/search`. Si la cuenta no tiene sucursales, efectúa un `POST /users/{userId}/stores` creando la sucursal *"Dulce Mayor Central"*.
    3. Consulta `GET /pos`. Si no existe una caja con identificador externo `DULCEPOS01`, efectúa un `POST /pos` creando la caja física con `fixed_amount: false` vinculada a la sucursal obtenida.
  - Esto garantiza que el repositorio sea **100% portable y reproducible**: cualquier entorno nuevo con credenciales válidas se auto-configura en su primera petición.

### Decisión 4: Flujo de Usuario en 2 Fases en Frontend (Anti-Confusion UX)
* **Ubicación**: [Checkout.jsx](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/frontend-institucional/src/pages/Checkout.jsx).
* **Justificación**:
  - **Antipatrón previo**: La interfaz mostraba un código QR estático antes de que el usuario confirmara el pedido. Si el usuario escaneaba ese código, pagaba una orden inexistente o un monto desactualizado respecto de su carrito.
  - **Patrón implementado**:
    - **Fase 1 (Pre-Confirmación)**: Se presenta una tarjeta informativa con el monto total a pagar y los 3 pasos a seguir una vez generado el código. El botón principal indica claramente: *"Confirmar Pedido y Generar QR"*.
    - **Fase 2 (Post-Confirmación)**: Se crea formalmente el pedido en la base de datos (con estado `PENDIENTE_PAGO`), se contacta a la API de Mercado Pago, se renderiza el QR generado con ZXing, se visualiza el temporizador de cuenta regresiva de 15 minutos, y se activa un sondeo reactivo (*polling*) cada 4 segundos. Si se generó una URL de preferencia, se muestra un botón destacado para abrir el portal web de pago.
    - Se bloquea la duplicación del botón de envío para evitar múltiples órdenes simultáneas.

### Decisión 5: Idempotencia Criptográfica y Base de Datos
* **Ubicación**: [ProcesamientoPagoService.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/services/facturacion/ProcesamientoPagoService.java) y [WebhookNotificationLogRepository.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/repositories/facturacion/WebhookNotificationLogRepository.java).
* **Justificación**:
  - Los servidores de Mercado Pago implementan políticas agresivas de reintento exponencial ante fallos o demoras en la respuesta HTTP del webhook.
  - Para evitar la emisión múltiple de facturas o descuentos repetidos de inventario, se implementó una tabla de auditoría `webhook_notification_log` indexada por `id_transaccion_mp`. Si una notificación ya fue procesada, el backend responde de inmediato `HTTP 200 OK` sin reejecutar lógica de facturación.

---

## 4. Especificación Técnica de APIs de Mercado Pago Integradas

### 4.1 `GET https://api.mercadopago.com/users/me`
Permite identificar al vendedor y obtener su identificador unívoco de colector (`collector_id`).

* **Cabeceras**:
  ```http
  Authorization: Bearer APP_USR-872714652431718-040713-33eb9689e4726e8dbef36e568471c9ba-3693808838
  ```
* **Respuesta Exitosa (HTTP 200)**:
  ```json
  {
    "id": 3693808838,
    "nickname": "TESTUSER5973036538965257855",
    "email": "test_user_3693808838@testuser.com",
    "site_id": "MLA",
    "country_id": "AR"
  }
  ```

---

### 4.2 `POST https://api.mercadopago.com/users/{userId}/stores`
Crea la sucursal comercial asociada a la cuenta del colector si no existe ninguna.

* **Cabeceras**:
  ```http
  Authorization: Bearer <ACCESS_TOKEN>
  Content-Type: application/json
  ```
* **Cuerpo de la Petición (Request Body)**:
  ```json
  {
    "name": "Dulce Mayor Central",
    "location": {
      "address_line": "Av. Corrientes 1234",
      "reference": "Local Comercial Dulce Mayor"
    }
  }
  ```
* **Respuesta Exitosa (HTTP 201)**:
  ```json
  {
    "id": 89853270,
    "name": "Dulce Mayor Central",
    "date_creation": "2026-11-10T13:48:38.000-04:00"
  }
  ```

---

### 4.3 `POST https://api.mercadopago.com/pos`
Crea una caja física de punto de venta (POS) y la asocia a la sucursal creada.

* **Cabeceras**:
  ```http
  Authorization: Bearer <ACCESS_TOKEN>
  Content-Type: application/json
  ```
* **Cuerpo de la Petición (Request Body)**:
  ```json
  {
    "name": "Caja Principal Dulce Mayor",
    "fixed_amount": false,
    "store_id": 89853270,
    "external_id": "DULCEPOS01",
    "category": 621102
  }
  ```
  > **Nota Técnica:** El campo `fixed_amount: false` es obligatorio para el flujo de QR dinámico, ya que permite que cada orden enviada defina su propio importe total y caducidad.
* **Respuesta Exitosa (HTTP 201)**:
  ```json
  {
    "id": 140657129,
    "name": "Caja Principal Dulce Mayor",
    "external_id": "DULCEPOS01",
    "store_id": "89853270"
  }
  ```

---

### 4.4 `POST https://api.mercadopago.com/instore/orders/qr/seller/collectors/{userId}/pos/{externalPosId}/qrs`
Endpoint primario de generación de orden presencial con QR dinámico.

* **Cabeceras**:
  ```http
  Authorization: Bearer <ACCESS_TOKEN>
  Content-Type: application/json
  X-Idempotency-Key: a1b2c3d4-e5f6-7890-abcd-ef1234567890
  ```
* **Cuerpo de la Petición (Request Body)**:
  ```json
  {
    "external_reference": "pedido-1024",
    "title": "Pedido Dulce Mayor #1024",
    "description": "Compra online en Dulce Mayor",
    "notification_url": "https://c7b2-181-44-120-10.ngrok-free.app/pagos/webhook/mercadopago",
    "total_amount": 15450.00,
    "items": [
      {
        "title": "Alfajores de Chocolate x 12",
        "currency_id": "ARS",
        "unit_price": 15450.00,
        "quantity": 1,
        "unit_measure": "unit",
        "total_amount": 15450.00
      }
    ],
    "expiration_date": "2026-11-10T15:05:00.000-03:00"
  }
  ```
* **Respuesta Exitosa (HTTP 201)**:
  ```json
  {
    "in_store_order_id": "99382104-5821-419b-a0d3-199b10294821",
    "qr_data": "00020101021243650016com.mercadolibre02013063636938088380306DULCEPOS01520462115303032540815450.005802AR6304D16A"
  }
  ```

---

### 4.5 `POST https://api.mercadopago.com/checkout/preferences`
Endpoint secundario (Fallback de Alta Disponibilidad).

* **Cabeceras**:
  ```http
  Authorization: Bearer <ACCESS_TOKEN>
  Content-Type: application/json
  ```
* **Cuerpo de la Petición (Request Body)**:
  ```json
  {
    "items": [
      {
        "id": "pedido-1024",
        "title": "Pedido Dulce Mayor #1024",
        "description": "Compra de productos dulces al por mayor",
        "quantity": 1,
        "currency_id": "ARS",
        "unit_price": 15450.00
      }
    ],
    "external_reference": "pedido-1024",
    "notification_url": "https://c7b2-181-44-120-10.ngrok-free.app/pagos/webhook/mercadopago",
    "back_urls": {
      "success": "http://localhost:5173/pago/exito",
      "failure": "http://localhost:5173/checkout",
      "pending": "http://localhost:5173/checkout"
    },
    "expires": true,
    "expiration_date_from": "2026-11-10T14:50:00.000-03:00",
    "expiration_date_to": "2026-11-10T15:05:00.000-03:00"
  }
  ```
* **Respuesta Exitosa (HTTP 201)**:
  ```json
  {
    "id": "3693808838-51f8ba39-2d93-41bb-98a2-23c914bf4821",
    "init_point": "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=3693808838-51f8ba39...",
    "sandbox_init_point": "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=3693808838-51f8ba39..."
  }
  ```

---

### 4.6 `GET https://api.mercadopago.com/v1/payments/{paymentId}`
Invocado por el backend tras recibir la notificación del Webhook para consultar el estado fáctico de la transacción.

* **Cabeceras**:
  ```http
  Authorization: Bearer <ACCESS_TOKEN>
  ```
* **Respuesta Exitosa (HTTP 200)**:
  ```json
  {
    "id": 1318492049,
    "status": "approved",
    "status_detail": "accredited",
    "currency_id": "ARS",
    "transaction_amount": 15450.00,
    "external_reference": "pedido-1024",
    "date_approved": "2026-11-10T14:52:12.000-03:00",
    "payment_method_id": "account_money",
    "payer": {
      "id": "492019482",
      "email": "test_buyer_492019482@testuser.com",
      "identification": {
        "type": "DNI",
        "number": "30111222"
      }
    }
  }
  ```

---

## 5. Estándar Matricial ZXing y Formato Interoperable EMVCo

### 5.1 Especificación EMVCo (Transferencias 3.0 / BCRA)
El valor devuelto en `qr_data` cumple con la arquitectura **Tag-Length-Value (TLV)** reglamentada internacionalmente por EMVCo y localmente por el Banco Central de la República Argentina (BCRA) para pagos con transferencias inmediatas:

| Tag | Longitud | Significado | Ejemplo de Valor |
| :---: | :---: | :--- | :--- |
| `00` | 02 | Versión del Formato del Payload | `01` |
| `01` | 02 | Método de Iniciación (`11` Estático / `12` Dinámico) | `12` |
| `26` | Var | Merchant Account Information (Mercado Pago) | `0016com.mercadolibre...` |
| `52` | 04 | Merchant Category Code (MCC) | `6211` |
| `53` | 03 | Moneda de Transacción (ISO 4217 - ARS = `032`) | `032` |
| `54` | Var | Monto total de la orden | `15450.00` |
| `58` | 02 | Código de País (ISO 3166-1 alpha-2) | `AR` |
| `63` | 04 | Checksum Cíclico CRC-16-CCITT | `D16A` |

#### Cálculo del Polinomio CRC-16-CCITT
El último campo `6304` contiene un checksum calculado mediante el polinomio $X^{16} + X^{12} + X^5 + 1$ (valor hexadecimal `0x1021`) con semilla inicial `0xFFFF`. Si el string leído por la cámara no coincide byte a byte con este hash, la aplicación bancaria descarta el código por seguridad.

### 5.2 Generación con ZXing en `QrCodeGeneratorHelper.java`
```java
public static byte[] generarQrPng(String payload) {
    if (payload == null || payload.isBlank()) return new byte[0];
    try {
        int size = 300;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    } catch (WriterException | IOException e) {
        return new byte[0];
    }
}
```

---

## 6. Seguridad Criptográfica y Validación de Webhooks

Para evitar ataques de falsificación de peticiones (*man-in-the-middle*) y repetición (*replay attacks*), el backend expone el método de validación criptográfica en [MercadoPagoAdapter.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/services/facturacion/adapter/MercadoPagoAdapter.java):

```
Headers de Mercado Pago:
  x-signature: ts=1711234567,v1=9c4a8b7e21a0f5...
  x-request-id: 7b84497e-128c-4a30-84c4-f2a89320e8b2
Payload Body:
  {"data": {"id": "1318492049"}}
```

### Algoritmo de Verificación:
1. **Extracción**: Se divide la cabecera `x-signature` en sus componentes `ts` (marca de tiempo en segundos Epoch) y `v1` (resumen criptográfico esperado).
2. **Defensa contra Replay Attacks**: Se calcula:
   $$\Delta t = |\text{EpochActual} - ts|$$
   Si $\Delta t > 300\text{ s}$ (5 minutos), la notificación se rechaza de inmediato.
3. **Construcción del Manifiesto**: Se concatena exactamente:
   ```
   "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";"
   ```
4. **Cálculo HMAC-SHA256**: Se genera la clave HMAC utilizando la variable `MERCADO_PAGO_WEBHOOK_SECRET` y se calcula el hash en codificación hexadecimal en minúsculas.
5. **Comparación en Tiempo Constante**: Se comparan las firmas utilizando `MessageDigest.isEqual(...)` para impedir vulnerabilidades de canales laterales basados en tiempo (*timing attacks*).
6. **Tolerancia en Desarrollo**: Si no se ha configurado la clave secreta en el archivo `.env` (común en fases iniciales de pruebas con ngrok), el sistema verifica la autenticidad realizando una llamada directa autenticada a `GET /v1/payments/{dataId}` con el token oficial del comercio.

---

## 7. Ciclo de Vida de Transacciones y Máquina de Estados

La integración gestiona las transiciones de estado de forma determinista para garantizar coherencia entre pedidos, pagos, facturas e inventario:

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE_PAGO : Cliente confirma Checkout con Mercado Pago
    PENDIENTE_PAGO --> FACTURADO : Webhook o Polling confirma "approved"
    PENDIENTE_PAGO --> CANCELADO : Expiran 15 min sin pago / "rejected"
    
    state FACTURADO {
        [*] --> EmitirFacturaElectronica
        EmitirFacturaElectronica --> GenerarCaeFiscalSimulado
        GenerarCaeFiscalSimulado --> DescontarStockInventario
        DescontarStockInventario --> EnviarEmailComprobante
        EnviarEmailComprobante --> [*]
    }

    state CANCELADO {
        [*] --> LiberarReserva
        LiberarReserva --> [*]
    }

    FACTURADO --> [*]
    CANCELADO --> [*]
```

---

## 8. Diagrama de Secuencia y Arquitectura de Componentes

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Comprador
    participant UI as Frontend (React / Vite)
    participant Back as Backend (Spring Boot)
    participant MP as Mercado Pago API

    Cliente->>UI: Completa datos de entrega y selecciona "Mercado Pago"
    Cliente->>UI: Clic en "Confirmar Pedido y Generar QR"
    UI->>Back: POST /pedidos (crea pedido en PENDIENTE_PAGO)
    Back-->>UI: Devuelve PedidoDTO (id: 1024)
    
    UI->>Back: POST /pedidos/1024/pagos/qr/iniciar
    Back->>MP: GET /users/me (colector: 3693808838)
    Back->>MP: POST /instore/orders/qr/seller/collectors/3693808838/pos/DULCEPOS01/qrs
    
    alt Éxito Instore Dynamic QR (HTTP 201)
        MP-->>Back: Devuelve qr_data oficial (EMVCo)
    else Falla o Restricción de Cuenta (HTTP 4xx / 5xx)
        Back->>MP: POST /checkout/preferences (Fallback Preferences)
        MP-->>Back: Devuelve init_point / sandbox_init_point
    end

    Back->>Back: Genera matriz gráfica PNG Base64 mediante ZXing
    Back-->>UI: IniciarPagoQrResponse (qrBase64, initPointUrl, ttl: 900)
    UI->>Cliente: Muestra QR en pantalla + Botón "Pagar en Mercado Pago ↗"
    
    par Sondeo Reactivo (Polling cada 4s)
        loop Cada 4 segundos
            UI->>Back: GET /pedidos/1024/pagos/qr/estado
            Back-->>UI: {"estadoPago": "PENDIENTE_PAGO", "estaPagado": false}
        end
    and Pago por el Comprador
        alt Pago Escaneando con App Móvil
            Cliente->>MP: Escanea QR y confirma pago con saldo
        else Pago Web vía Preferences
            Cliente->>MP: Ingresa al init_point y abona con tarjeta de prueba
        end
    end

    MP->>Back: POST /pagos/webhook/mercadopago (action: payment.created, id: 1318492049)
    Back->>MP: GET /v1/payments/1318492049
    MP-->>Back: {"status": "approved", "external_reference": "pedido-1024"}
    
    Back->>Back: Transacción Atómica:
    Back->>Back: 1. Marca Pago como APROBADO
    Back->>Back: 2. Pasa Pedido a FACTURADO
    Back->>Back: 3. Emite Factura con CAE y descuenta Stock
    Back-->>MP: HTTP 200 OK

    UI->>Back: GET /pedidos/1024/pagos/qr/estado
    Back-->>UI: {"estadoPago": "FACTURADO", "estaPagado": true}
    UI->>Cliente: Redirige automáticamente a /pago/exito
```

---

## 9. Guía de Configuración del Proyecto y Entorno Local

### 9.1 Variables de Entorno (`.env`)
En el archivo `.env` ubicado en la raíz del proyecto, verifique o configure:

```env
# Credenciales de Mercado Pago
MERCADO_PAGO_ACCESS_TOKEN=APP_USR-872714652431718-040713-33eb9689e4726e8dbef36e568471c9ba-3693808838

# URL del túnel público ngrok hacia el backend (puerto 8080)
# IMPORTANTE: No incluir barra final '/'
MERCADO_PAGO_WEBHOOK_URL=https://c7b2-181-44-120-10.ngrok-free.app/pagos/webhook/mercadopago

# Firma secreta opcional para validación HMAC (dejar vacía en modo sandbox rápido)
MERCADO_PAGO_WEBHOOK_SECRET=

# URLs de los frontends
FRONTEND_URL=http://localhost:5173
BACKEND_URL=http://localhost:8080
```

### 9.2 Ejecución del Túnel ngrok
Para permitir que los servidores de Mercado Pago notifiquen a su máquina local:
```bash
# Iniciar el túnel en el puerto 8080 de Spring Boot
ngrok http 8080
```
Copie la URL HTTPS generada (ej. `https://xxxx-xx-xx.ngrok-free.app`) y actualice la variable `MERCADO_PAGO_WEBHOOK_URL` en el archivo `.env`.

### 9.3 Reconstrucción y Despliegue con Docker Compose
Dado que se incorporó la librería ZXing en Maven y se reestructuró la compilación de frontend:
```bash
# Detener contenedores existentes
docker compose down

# Recompilar imágenes sin caché y levantar en segundo plano
docker compose up -d --build

# Monitorear logs del backend en tiempo real
docker compose logs -f dulcemayor-backend
```

---

## 10. Guía de Configuración en Mercado Pago Developers

1. Ingrese con sus credenciales al panel de [Mercado Pago Developers](https://www.mercadopago.com.ar/developers).
2. Diríjase a **Tus integraciones** y seleccione su aplicación.
3. **Sección Webhooks**:
   - En el campo **URL de producción o pruebas**, configure:  
     `https://<TU-SUBDOMINIO-NGROK>.ngrok-free.app/pagos/webhook/mercadopago`
   - En **Eventos a escuchar**, active las casillas:
     - `Pagos (payment)`
     - `Órdenes comerciales (merchant_order)`
   - Guarde los cambios y presione el botón *"Probar notificación"*. Deberá visualizar en su terminal de ngrok una petición entrante con estado `200 OK`.
4. **Sección Cuentas de Prueba (Test Users)**:
   - Debe contar con al menos **dos usuarios de prueba**:
     - **Vendedor (Seller / Collector)**: Usuario `3693808838`. Es el creador de la aplicación y propietario del `Access Token`.
     - **Comprador (Buyer / Payer)**: Usuario con saldo de dinero en cuenta ficticio.
   - **Regla Inquebrantable:** Nunca intente abonar o escanear un QR estando logueado con la cuenta del Vendedor. La API deniega cualquier transacción donde el colector y el pagador coinciden.

---

## 11. Guía de Pruebas Extremo a Extremo (E2E) y Simulación

### 11.1 Prueba con Pasarela Web (Checkout Preferences)
1. Abra un navegador en modo incógnito e ingrese a `http://localhost:5173`.
2. Añada productos al carrito y proceda al Checkout.
3. Complete los datos de entrega y seleccione **Mercado Pago**.
4. Haga clic en **Confirmar Pedido y Generar QR**.
5. En pantalla verá el código QR renderizado con ZXing y el botón **Pagar en Mercado Pago ↗**.
6. Haga clic en el botón para abrir la pasarela oficial de Mercado Pago.
7. Utilice una tarjeta de prueba de la siguiente lista oficial de Argentina:

| Entidad / Marca | Número de Tarjeta Ficticio | Fecha Expiración | CVC | Tipo de Aprobación |
| :--- | :---: | :---: | :---: | :--- |
| **Visa Débito / Crédito** | `4242 4242 4242 4242` | `11/28` | `123` | Aprobación Inmediata |
| **Mastercard Crédito** | `5555 5555 5555 5555` | `11/28` | `123` | Aprobación Inmediata |
| **Tarjeta para Rechazo** | `4023 0000 0000 0001` | `11/28` | `123` | Rechazo por Fondos Insuficientes |

8. Al confirmar el pago en la pasarela, observe los logs de Docker:
   ```
   INFO ... MercadoPagoAdapter: Webhook recibido para dataId 1318492049
   INFO ... ProcesamientoPagoService: Pago aprobado para pedido 1024. Emitiendo Factura...
   ```
9. La pantalla de Checkout en su navegador detectará la aprobación en menos de 4 segundos y lo redirigirá a la pantalla de compra exitosa.

### 11.2 Simulación Manual de Webhook por cURL (Sin Conexión a Internet)
Si requiere validar el procesamiento del backend de forma local sin levantar ngrok:
```bash
curl -X POST http://localhost:8080/pagos/webhook/mercadopago \
  -H "Content-Type: application/json" \
  -H "x-request-id: test-req-001" \
  -H "x-signature: ts=1711234567,v1=simulated" \
  -d '{
    "action": "payment.created",
    "type": "payment",
    "data": {
      "id": "1318492049"
    }
  }'
```

---

## 12. Matriz de Diagnóstico y Resolución de Problemas (Troubleshooting)

| Código / Error | Causa Raíz | Solución Implementada en el Proyecto |
| :--- | :--- | :--- |
| `HTTP 400: Missing HTTP header: X-Idempotency-Key` | El endpoint de órdenes presenciales exige una clave única por intento. | Se genera automáticamente un `UUID.randomUUID().toString()` en las cabeceras HTTP de [MercadoPagoAdapter.java](file:///home/mmaglione/Documents/IS2-Grupal-Beck-Maglione-Ferrari/integrador-2/backend/src/main/java/com/DulceMayor/backend/services/facturacion/adapter/MercadoPagoAdapter.java). |
| `HTTP 400: invalid_external_id` | Se intentó crear una caja con guiones bajos o caracteres no alfanuméricos. | Se normalizó el identificador de la caja a `DULCEPOS01` (alfanumérico estricto). |
| `HTTP 400: non_existent_external_store_id` | Se enviaba un ID de tienda no reconocido o un String vacío. | El adaptador primero crea la sucursal y luego pasa el ID numérico (`store_id: 89853270`) directamente al dar de alta la caja. |
| `HTTP 400: auto_return invalid` | Mercado Pago no permite retorno automático cuando la URL de éxito es `localhost`. | Se omitió `auto_return` condicionalmente para entornos de desarrollo local en el payload de preferencias. |
| `HTTP 401: Unauthorized` | El Access Token venció o no tiene permisos de lectura/escritura de órdenes. | Verificar la variable `MERCADO_PAGO_ACCESS_TOKEN` en el archivo `.env`. |
| `HTTP 403: Collector and payer must be different` | El operador intentó pagar con la misma cuenta de Mercado Pago dueña del token. | Iniciar sesión en la app o navegador con la **Cuenta Compradora de Prueba** (*Buyer*). |
| El QR no se lee con la cámara | Generación manual antigua con `Graphics2D` sin estándar ISO ni corrección de errores. | Resuelto al 100% mediante **ZXing** con corrección de errores nivel M y zona de silencio de 2 módulos. |
| El webhook no llega al backend local | La URL de ngrok cambió al reiniciar el proceso o ngrok no está corriendo. | Ejecutar `ngrok http 8080`, copiar la nueva URL y actualizar `MERCADO_PAGO_WEBHOOK_URL` en `.env`. |

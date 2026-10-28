# Arquitectura y Diseño — Dulce Mayor

Este documento describe las decisiones arquitectónicas, la estructura del proyecto y los patrones de diseño aplicados en el desarrollo del ecosistema "Dulce Mayor", cumpliendo con las especificaciones del TP Integrador N°2.

## 1. Visión General (Cliente-Servidor)

El sistema implementa una arquitectura **Cliente-Servidor** comunicada exclusivamente a través de una API RESTful.

* **Servidor (Backend):** Desarrollado en Java 21 con Spring Boot. Centraliza toda la lógica de negocio, reglas de validación, acceso a la base de datos (MySQL) y seguridad.

* **Clientes (Frontends):** Tres aplicaciones independientes desarrolladas en React + TypeScript + Vite. Ningún cliente posee lógica de negocio crítica ni acceso directo a la base de datos:

  * **Dashboard Administrativo:** Para Jefe y Administrativo.

  * **Sitio Institucional / Portal Minorista:** Catálogo público y panel de cliente.

  * **Portal Móvil (PWA/Responsive):** Herramienta de campo para Preventor y Distribuidor.

## 2. Arquitectura de 3 Capas (Backend)

El backend sigue un estricto patrón arquitectónico de capas (Layered Architecture).

1. **Capa de Presentación / Controladores (`restcontroller`):**

   * Recibe peticiones HTTP, valida los datos de entrada (Bean Validation) y maneja las respuestas.

   * **Regla estricta:** Los controladores *nunca* reciben ni devuelven Entidades JPA. Exclusivamente manejan **DTOs**.

2. **Capa de Negocio (`service`):**

   * Contiene el 100% de las reglas de negocio (ej. validación de stock, cálculo de totales, transiciones de estado).

   * Maneja la transaccionalidad (`@Transactional`).

   * Utiliza *MapStruct* para convertir entre DTOs y Entidades.

3. **Capa de Acceso a Datos (`repository`):**

   * Interfaces de Spring Data JPA (Patrón DAO) que abstraen las consultas a MySQL.

## 3. Organización de Paquetes (Domain-Driven)

El código fuente se organiza por **capas MVC** (carpeta controllers, services,dtos,repositories,config, entities).

Ejemplo de estructura:

```
com.dulcemayor
├── config/                # Excepciones globales, utilidades, configs (Seguridad, CORS)
├── services/      # servicios que validan reglas de negocio
├── repositories/              # persisten las clases por JPA
├── entities/         # entidades del modelo de dominio
├── controllers/               # manejo de vistas mediante restcontrollers
└── dtos/                # transporte de datos entre controlador-servicio-repositorio

```

## 4. Patrones de Diseño Aplicados

De acuerdo a las exigencias del enunciado, el equipo implementa los siguientes patrones y documenta su justificación en el código fuente.

### 4.1. Patrones Obligatorios y Base

* **DTO (Data Transfer Object):** Utilizado en todos los endpoints para desacoplar el modelo de base de datos de los contratos de la API.

* **Template Method (Comportamiento):**

  * **Uso:** Clases base abstractas `BaseControllerImpl<E, DTO>` y `BaseServiceImpl<E, ID>`.

  * **Justificación:** Estandariza el flujo del CRUD genérico (crear, validar, modificar, eliminar lógicamente, buscar por ID, listar). Las subclases específicas (ej. `ProductoController`) heredan este comportamiento base y solo sobreescriben o añaden los pasos específicos de su dominio.

* **Patrones TP1 (GRASP y SOLID):** *Experto en Información* (los servicios saben cómo manipular sus entidades), *Polimorfismo* (manejo de jerarquías como `Persona -> Empleado/Minorista`), *DAO* (Spring Data) e *Inyección de Dependencias* (IoC contenedor de Spring).

### 4.2. Patrón Estructural

* **Adapter (Adaptador):**

  * **Uso 1:** Integración con la API externa de cotización del dólar. El `CotizacionDolarAdapter` implementa una interfaz propia del dominio, envolviendo el cliente HTTP externo y proveyendo un valor de respaldo si la API falla.

  * **Uso 2:** Integración con el modelo de lenguaje de Spring AI para el chatbot. El adaptador traduce la intención de la IA a las estructuras de presupuesto del sistema.

  * **Justificación:** Aísla nuestro código de dominio de los cambios en librerías o APIs de terceros.

### 4.3. Patrón de Comportamiento

* **Strategy (Estrategia):**

  * **Uso:** En el ordenamiento de paradas para `RutaPreventor` y `RutaDistribuidor`.

  * **Justificación:** Permite tener una interfaz `RutaOptimizerStrategy` y una implementación concreta `NearestNeighborStrategy`, vecino más cercano usando Haversine (algoritmo utilizado para calcular la **distancia ortodrómica** (o distancia en línea recta sobre la superficie terrestre) entre dos puntos dados sus **coordenadas geográficas**). Si a futuro el Product Owner pide integrar un servicio externo de optimización (Google OR-Tools, etc.), se agrega una nueva estrategia sin tocar la lógica core del servicio.

* **Observer (Observador) - Mediante Spring Events:**

  * **Uso:** Cuando un `Pedido` cambia a `FACTURADO` o a `ENTREGADO`.

  * **Justificación:** El `PedidoService` publica un evento (ej. `PedidoFacturadoEvent`). El `EmailNotificationListener` reacciona enviando el correo. Esto evita que el módulo de pedidos tenga una dependencia fuerte (acoplamiento) con el módulo de notificaciones.

### 4.4. Patrón Creacional

* **Factory Method (Método de Fábrica):**

  * **Uso:** En el motor de reportes (`ReporteExporterFactory`).

  * **Justificación:** El sistema necesita exportar el mismo `ReporteTabular` a PDF o Excel según el parámetro de la petición. La fábrica recibe el tipo (`formato=pdf` o `formato=xlsx`) y devuelve la implementación correcta de la interfaz `ReporteExporter`.

* **Builder (Constructor):**

  * **Uso:** Principalmente generado vía *Lombok* (`@Builder`) en la creación de Entidades complejas (como `Factura` y `Pedido`), donde la inicialización paso a paso de totales, impuestos, estado y fechas hace que los constructores tradicionales sean propensos a errores.

## 5. Prevención de Dependencias Circulares

Un riesgo alto en esta arquitectura es el acoplamiento cíclico entre servicios (ej. `PedidoService` llama a `FacturaService` y viceversa).
**Regla arquitectónica:** La comunicación entre dominios transversales fluye en un solo sentido.
Para el caso complejo de procesar un pedido, se utiliza un servicio orquestador (`ProcesamientoPedidoService`) que coordina `StockService`, `FacturaService` y `PedidoService`, manteniendo a los servicios base ignorantes unos de los otros.

## 6. Manejo de Errores Global

Se implementa el patrón **Controller Advice** (`@RestControllerAdvice`). Captura cualquier excepción lanzada desde cualquier capa (desde un fallo de validación de DTO hasta una entidad no encontrada) y la traduce a la estructura estándar `ErrorResponse` del contrato OpenAPI, asegurando consistencia y previniendo fugas de información del servidor (StackTraces).
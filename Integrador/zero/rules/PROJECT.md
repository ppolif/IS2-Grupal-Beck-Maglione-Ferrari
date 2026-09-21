Arquitectura y Patrones de Diseño

Arquitectura web Cliente-Servidor de al menos 3 capas utilizando obligatoriamente el patrón Modelo-Vista-Controlador (MVC). Implementación de al menos 5 patrones de diseño a elección entre: Capas, Experto en Responsabilidad, Creador, Polimorfismo, Alta Cohesión/Bajo Acoplamiento, MVC, Inyección de Dependencia, DTO (enfocado en reportes) o DAO. 

Stack Tecnológico

Backend y Servidor: Desarrollo en Java utilizando el framework Spring Boot y despliegue sobre Apache Tomcat. Frontend: Vistas públicas en HTML/CSS3 y panel administrativo tipo "Dashboard", complementados con Bootstrap y el motor de plantillas Thymeleaf.   Datos y Versionado: Base de datos MySQL con persistencia vía JDBC o JPA, utilizando GitHub para el versionado de código. 

Especificaciones Core del Sistema

Plataforma E-Commerce estructurada por categorías (Niños, Niñas, Mujeres, Hombres) y subcategorías, integrando carrito de compras, múltiples medios de pago y seguimiento de estados (Pendiente de pago, Pago realizado, Pendiente de entrega/envío, Entregado). Sistema de seguridad que requiere login con correo y contraseña encriptada, validando los nuevos registros mediante un código de activación enviado por email. Ejecución de procesos automáticos para el envío de correos transaccionales con los detalles de compra y la emisión de un newsletter en HTML cada 10 días con productos en oferta.   Control de inventario automatizado: el stock disminuye al confirmarse el pago de un cliente y aumenta al marcar como recibida una orden de compra generada a un proveedor.

Roles y Módulo de Reportes

Usuario Administrador: Acceso a un panel de control para realizar el ABM (Alta, Baja y Modificación) de usuarios, productos (con actualización bimestral de precios por inflación), proveedores y órdenes de compra.   Usuario Cliente: Acceso a la tienda web institucional para modificar sus datos personales, anular pedidos, realizar compras y consultar el historial.   Reportes y Métricas: Generación de resúmenes de ventas filtrados por rango de fechas, evaluación del proveedor más económico y auditoría de stock; esta última incluye alertas de inventario crítico (menor al 20%) con un botón para enviar un mensaje directo por WhatsApp al proveedor.
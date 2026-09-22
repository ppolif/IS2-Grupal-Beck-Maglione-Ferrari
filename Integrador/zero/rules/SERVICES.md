# SERVICIOS DEL SISTEMA

## Servicios principales

- ProductoService: Se encarga de la lógica de negocio relacionada con los productos, incluyendo la gestión de precios y stock.
- CategoriaService: Se encarga de la lógica de negocio relacionada con las categorías de productos.
- SubCategoriaService: Se encarga de la lógica de negocio relacionada con las subcategorías de productos.
- VigenciaPrecioService: Se encarga de la lógica de negocio relacionada con los precios de los productos.
- OrdenCompraService: Se encarga de la gestión de órdenes de compra y del carrito de compras del cliente (OrdenCompra en estado PENDIENTE_COMPLETAR), persistiendo los productos en base de datos para sesiones recurrentes.

# Frontend Preventores y Distribuidores - Dulce Mayor

Aplicación móvil y de escritorio adaptada para el equipo de preventores y repartidores de Dulce Mayor. Desarrollada con React, Vite y Tailwind CSS v4, orientada a decisiones ágiles en ruta y supervisión en computadora.

## Roles Soportados
- **preventor**: Gestión de hoja de ruta, visitas en calle, catálogo de productos, registro de nuevos minoristas y confirmación de pedidos.
- **distribuidor**: Gestión de entregas asignadas del día, cobranzas en efectivo/transferencia y hoja de ruta con tickets de entrega.

## Instrucciones de Ejecución

1. **Instalar dependencias:**
   ```bash
   npm install
   ```

2. **Ejecutar en modo de desarrollo:**
   ```bash
   npm run dev
   ```

3. **Construir para producción:**
   ```bash
   npm run build
   ```

## Cómo Cambiar el Rol de Prueba
En `src/mocks/session.js` puedes alternar el valor de la propiedad `rol`:

```js
export const usuarioMock = {
  id: "usr-prev-01",
  nombre: "Lucía Martínez",
  email: "lucia.martinez@dulcemayor.com",
  avatar: "LM",
  rol: "preventor", // Cambiar por "distribuidor" para ver la interfaz del repartidor
  zona: "Zona Godoy Cruz",
  unidad: "Unidad 08 · Gran Mendoza"
};
```

`RoleRouter.jsx` detecta automáticamente este rol y monta el layout, la barra de navegación y las rutas correspondientes sin menús filtrados.

## Vistas Disponibles por Rol

### Preventor (`rol: "preventor"`)
- `/recorrido` - Mapa de paradas y ficha de visita actual (con OpenStreetMap)
- `/registro-comercio` - Alta rápida de clientes minoristas en ruta
- `/tienda` - Catálogo de productos para carga de pedidos
- `/carrito` - Pedido en curso, control de bultos y descuentos
- `/perfil` - Estadísticas de visitas realizadas y datos de la zona
- `/cambiar-password` - Cambio de credenciales

### Distribuidor (`rol: "distribuidor"`)
- `/pedidos` - Lista de entregas del día ("10 entregas asignadas") y control de pagos
- `/recorrido` - Mapa de reparto y ticket de entrega por bultos (con OpenStreetMap)
- `/perfil` - Estadísticas de entregas completadas y unidad de reparto
- `/cambiar-password` - Cambio de credenciales

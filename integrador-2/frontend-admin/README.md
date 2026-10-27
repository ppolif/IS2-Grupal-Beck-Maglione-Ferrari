# Frontend Panel de Gestión - Dulce Mayor

Aplicación administrativa unificada para el personal administrativo / almaceneros y la jefatura de Dulce Mayor. Desarrollada con React, Vite, Tailwind CSS v4 y Recharts, fusionando las dos pantallas del prototipo en una única arquitectura con layouts específicos y componentes compartidos.

## Roles Soportados
- **administrativo**: Personal Administrativo / Almacenero. Control y verificación física de stock, alta y modificación de productos, recepción y facturación de pedidos, registro de pagos efectuados y cambio de contraseña.
- **jefe**: Jefe Administrativo. Posee todas las capacidades del personal administrativo más las vistas heredadas de preventores, dashboard ejecutivo con gráficos (Recharts), analítica avanzada, alta y gestión de usuarios (restringido a preventores y administrativos) y descarga de reportes en PDF y Excel.

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
  id: "usr-adm-01",
  nombre: "Marcos Silva",
  email: "marcos.silva@dulcemayor.com",
  avatar: "MS",
  rol: "jefe" // Cambiar por "administrativo" para ver la interfaz del almacenero/admin
};
```

`RoleRouter.jsx` evalúa `usuarioMock.rol` y carga la interfaz correspondiente (`AdministrativoLayout` o `JefeLayout`) con sus rutas dedicadas y navegación explícita en JSX.

## Vistas Disponibles por Rol

### Personal Administrativo (`rol: "administrativo"`)
- `/pedidos` - Listado de pedidos recepcionados, detalle y facturación
- `/stock` - Control de inventario físico y ajuste de unidades en depósito
- `/productos` - Catálogo, precios mayoristas y formulario de alta/edición
- `/rutas` - Rutas del día de preventores y distribuidores (con OpenStreetMap)
- `/pagos` - Registro y control de cobros efectuados
- `/cambiar-password` - Cambio de credenciales de acceso

### Jefe Administrativo (`rol: "jefe"`)
- `/dashboard` - KPIs ejecutivos, gráficos de ventas (Recharts), actividad semanal y stock por categoría
- `/estadisticas` - Analítica de facturación semestral y métricas de preventa
- `/reportes` - Informes de ventas por período, rendimiento y golosinas más vendidas con descarga PDF y Excel
- `/rendimiento` - Ranking comparativo de efectividad de preventores
- `/usuarios` - Administración de accesos con alta de roles (`preventor` | `administrativo`)
- `/preventor-rutas` - Supervisión de visitas y recorridos de preventores (con OpenStreetMap)
- Reutilización directa por importación de `/stock`, `/productos`, `/pedidos`, `/pagos`, `/rutas` y `/cambiar-password`.

# Frontend Institucional - Dulce Mayor

Aplicación web institucional y tienda mayorista para clientes y visitantes de Dulce Mayor. Desarrollada con React, Vite y Tailwind CSS v4, fiel al prototipo del sistema de diseño.

## Rol Asociado
- **cliente** (Visitante o comercio cliente logueado)

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

## Configuración de Sesión y Rol de Prueba
En `src/mocks/session.js` puedes modificar el estado de sesión del cliente (logueado / no logueado):
```js
export const usuarioMock = {
  id: "usr-cliente-01",
  nombre: "Juan Pérez",
  email: "juan.perez@comercio.com",
  comercio: "Kiosco San Martín",
  rol: "cliente",
  logueado: true, // Cambia a false para ver el navbar en modo visitante
  avatar: "JP"
};
```

## Vistas Disponibles
- `/` - Inicio (Hero, catálogo preview, beneficios, dólar hoy)
- `/tienda` - Catálogo mayorista completo con buscador y filtros por categoría
- `/carrito` - Carrito de compras y cálculo de descuentos por volumen (Ruta protegida)
- `/sucursales` - Sucursales y depósitos en Mendoza con OpenStreetMap
- `/contacto` - Información de la empresa y formulario de consulta
- `/perfil` - Datos de la cuenta comercial e historial de pedidos (Ruta protegida)
- `/login` - Acceso para comercios registrados
- `/registro` - Formulario de alta para nuevos comercios

# INVENTARIO Y MAPEO DE PANTALLAS - DULCE MAYOR

Este documento resume el análisis completo del prototipo ubicado en `/prototipado`, la extracción del sistema de diseño y la asignación estricta de pantallas a cada uno de los 3 frontends independientes y sus respectivos roles autorizados.

---

## 1. Sistema de Diseño Extraído del Prototipo

El prototipo (`prototipado/src/App.tsx` y `prototipado/src/index.css`) define una identidad visual dulce, moderna y limpia para **Dulce Mayor**, con los siguientes tokens de diseño:

### 1.1. Paleta de Colores (Valores Hex Exactos)
- **Fondo General (`--bg`)**: `#FDFDFD`
- **Fondo Lienzo (`--canvas`)**: `#EDF0F8`
- **Superficie / Cards (`--surface`)**: `#FFFFFF`
- **Bordes (`--border`)**: `#ECECF2`
- **Tinta / Texto Principal (`--ink`)**: `#2B2B3A`
- **Texto Secundario / Apagado (`--muted`)**: `#6B6B80`
- **Acentos Neutros / Textos Tenues**: `#AAA9BA`, `#9896A8`
- **Fondo Oscuro / Barras / Beneficios**: `#292838`

#### Colores de Marca y Acentos:
- **Rosa suave (`--pink`)**: `#FFCEF3`
- **Rosa intenso / Primario (`--pink-strong`)**: `#E86BC4`
- **Celeste suave (`--blue`)**: `#A1EAFB`
- **Celeste intenso / Secundario (`--blue-strong`)**: `#3FB8D9`

#### Estados y Badges / Chips:
- **Éxito / Verde (`--success`)**: `#7ED6A5`
  - Chip/Badge éxito: fondo `#DFF8E9`, texto `#39855B`
- **Advertencia / Cobranza (`--warning`)**: `#FFD98A`
  - Chip/Badge pendiente/efectivo: fondo `#FFF2D3` / `#FFF0D3`, texto `#986B16`
- **Error / Peligro (`--error`)**: `#FF8FA3`
  - Texto peligro / acción crítica: `#C95569`
- **Chip Azul**: fondo `#D9F7FF`, texto `#247F99`
- **Chip Rosa**: fondo `#FFE2F7`, texto `#A34187`

---

### 1.2. Tipografía y Jerarquía
- **Familia principal**: `'Poppins', sans-serif` (Google Fonts: 400 Regular, 500 Medium, 600 Semibold, 700 Bold).
- **Hero H1 (Desktop)**: 54px / line-height 1.08 / font-bold.
- **Hero H1 (Mobile)**: 40px / line-height 1.1 / font-bold.
- **Títulos de sección H2**: 28px - 32px / font-semibold o font-bold.
- **Títulos de tarjetas / módulos H3**: 13px - 15px / font-semibold.
- **Etiquetas y Eyebrows**: 11px, `letter-spacing: 0.14em`, `font-weight: 700`, color `#E86BC4` (uppercase).
- **Cuerpo de texto / lectura**: 12px - 16px / regular / color `#6B6B80`.
- **Microtextos / indicadores**: 8px - 10px / color `#9B99A8` o `#AAA9B8`.

---

### 1.3. Componentes y Estilos UI

- **Logo Dulce Mayor**:
  - Isotipo `candy-mark`: contenedor de 34x24 px con gradiente lineal a 135° de `#E86BC4` a `#3FB8D9`, bordes redondeados (9px), triángulos laterales simulando envoltorio de caramelo (`#FFCEF3` y `#A1EAFB`), y dos detalles circulares blancos.
  - Tipografía: "Dulce Mayor" en Poppins 700 Bold.
- **Botones**:
  - **Primario**: fondo `#E86BC4`, texto blanco, bordes redondeados `13px`, `font-semibold`, sombra `0 8px 20px rgba(232, 107, 196, 0.2)`, padding `13px 20px`.
  - **Secundario**: fondo blanco, texto `#3FB8D9`, borde `1.5px solid #3FB8D9`, bordes redondeados `13px`, `font-semibold`, sin sombra.
  - **Circular Add (`+`)**: tamaño 34x34 px, bordes redondeados `12px`, fondo `#E86BC4`, texto blanco centrado.
- **Campos e Inputs**:
  - Etiqueta: texto `11px`, `font-semibold`, color `#2B2B3A`, margen inferior `4px` - `7px`.
  - Control de entrada / selector: borde `1px solid #ECECF2`, redondeado `12px`, padding `12px 14px`, color `#2B2B3A`, fondo blanco, placeholder `#6B6B80`.
- **Tarjetas y Módulos**:
  - Contenedores con fondo `#FFFFFF`, borde `1px solid #ECECF2`, radio `20px` a `22px`, padding `20px` a `34px`, sombra suave `0 14px 40px rgba(56, 52, 84, 0.09)`.
- **Avatares**:
  - Forma circular con gradiente 135° de `#FFCEF3` a `#A1EAFB`, texto centrado con iniciales en Poppins Bold color `#2B2B3A`.
- **Tablas**:
  - Fila cabecera con textos `8px` uppercase en color `#AAA9B8`.
  - Filas con separador `border-b border-[#F0F0F4]`, textos en `9px`, valores destacados en `font-semibold`.
  - Indicadores de estado tipo badge o punto verde `#308E55`.
- **Modales**:
  - Centrados con fondo blanco, bordes redondeados `20px`, sombra pronunciada `0 22px 80px rgba(32, 30, 50, 0.25)`.
- **Mapas**:
  - Marcador de posición estándar (div con proporciones del prototipo, responsive) con el texto exacto `"usar api de openstreetmap"`.

---

## 2. Roles del Proyecto (Lista Cerrada)

Los identificadores de rol exactos utilizados en mocks y código son:
1. `cliente` (Frontend Institucional)
2. `preventor` (Frontend Preventor)
3. `distribuidor` (Frontend Preventor)
4. `administrativo` (Frontend Admin: Personal Administrativo / Almacenero)
5. `jefe` (Frontend Admin: Jefe Administrativo)

---

## 3. Inventario y Asignación de Pantallas

### Frontend 1: `/frontend-institucional`
*Público / Rol:* `cliente`
*Layout:* `ClienteLayout` (Header sticky con Navbar + Footer institucional + Botón flotante DM)

| # | Pantalla / Vista | Ruta | Dispositivo Original en Prototipo | Adaptación Responsive Requerida |
|---|-------------------|------|-----------------------------------|---------------------------------|
| 1 | **Inicio (Home)** | `/` | Desktop (1440px) | Grilla de productos de 4 a 2 columnas en tablet/mobile; menú hamburguesa móvil; hero colapsa imagen en mobile. |
| 2 | **Tienda (Catálogo)** | `/tienda` | Desktop (1440px) | Filtros de categoría horizontales scrolleables en mobile; tarjetas de producto adaptadas a 1/2 columnas en mobile y 3/4 en desktop. |
| 3 | **Carrito de Compras** | `/carrito` | Mobile / Desktop | Vista protegida (`<RutaProtegida>`). Listado de items con selector de cantidad, resumen de subtotales, descuentos y total, botón de confirmar pedido. |
| 4 | **Sucursales** | `/sucursales` | Desktop (1440px) | Lista de locales en columna izquierda y contenedor de mapa con texto `"usar api de openstreetmap"` a la derecha (apilado en mobile). |
| 5 | **Contacto** | `/contacto` | Desktop (1440px) | Información institucional + formulario de consulta (1 columna en mobile, 2 columnas en desktop). |
| 6 | **Mi Perfil** | `/perfil` | Mobile (390px) | Vista protegida (`<RutaProtegida>`). Tarjeta con avatar, datos del usuario, historial de pedidos con modal de detalle completo, botón de descarga de factura en PDF y visor de código QR de pago para transferencias/billeteras virtuales. |
| 7 | **Iniciar Sesión** | `/login` | Desktop (Modal/Página) | Formulario con email, contraseña y botón primario. |
| 8 | **Registrarse** | `/registro` | Desktop (Modal/Página) | Formulario de registro de usuario/persona física (Nombre, Apellido, Tipo y Número de Documento, Fecha de Nacimiento, Email, Teléfono, Contraseña). |

---

### Frontend 2: `/frontend-preventor`
*Roles:* `preventor` y `distribuidor`
*Layouts:* `PreventorLayout` y `DistribuidorLayout`
*Navegación:* Bottom Navigation bar en mobile (`< md`), Sidebar lateral o Topbar en pantallas grandes (`>= md` y `lg`).

#### Interfaz Preventor (`rol: "preventor"`)
| # | Pantalla / Vista | Ruta | Dispositivo Original en Prototipo | Adaptación Responsive Requerida |
|---|-------------------|------|-----------------------------------|---------------------------------|
| 1 | **Recorrido / Rutero** | `/recorrido` | Mobile (390px) | En mobile muestra mapa de fondo con bottom sheet interactivo (visita actual, chip de estado, tiempo y botones). En tablet/desktop pasa a vista de dos columnas: lista de comercios a visitar a la izquierda y mapa con detalle a la derecha. Mapa con `"usar api de openstreetmap"`. |
| 2 | **Registro de Minorista** | `/registro-comercio` | Mobile (390px) | Formulario de alta rápida de clientes (Nombre, Responsable, CUIT, Teléfono, Email, Dirección). En mobile 1 columna; en tablet/desktop centrado en tarjeta de 2 columnas. |
| 3 | **Tienda / Catálogo Preventor** | `/tienda` | Mobile (390px) | Reutilizada desde `pages/compartidas/` o catálogo preventor. Buscador, categorías y tarjetas de productos con botón rápido de adición. En desktop grilla de 3 o 4 columnas. |
| 4 | **Carrito de Pedido** | `/carrito` | Mobile (390px) | Carrito del pedido en curso para el cliente actual, con control de cantidades, subtotal, descuento, total y botón de confirmación. En desktop centrado con resumen lateral. |
| 5 | **Perfil de Preventor** | `/perfil` | Mobile (390px) | Tarjeta de usuario con avatar, nombre Lucía Martínez, zona asignada, KPIs de visitas y pendientes, botón de cambiar contraseña y cerrar sesión. |
| 6 | **Cambiar Contraseña** | `/cambiar-password` | Mobile (390px) | Formulario de actualización de credenciales. |

#### Interfaz Distribuidor (`rol: "distribuidor"`)
| # | Pantalla / Vista | Ruta | Dispositivo Original en Prototipo | Adaptación Responsive Requerida |
|---|-------------------|------|-----------------------------------|---------------------------------|
| 1 | **Pedidos / Entregas Asignadas** | `/pedidos` | Mobile (390px) | Lista de entregas del día ("10 entregas asignadas") con tarjetas de comercio, distancia y badge de estado de pago ("Cobrar $..." / "Transferencia · pagado"). En tablet/desktop grilla de tarjetas o tabla interactiva. |
| 2 | **Recorrido de Entrega** | `/recorrido` | Mobile (390px) | Mapa con bottom sheet de entrega actual, detalle del ticket (#DM-2084, monto, desglose de bultos), botones "Marcar como entregado" y "Dejar pendiente". En desktop vista dividida con mapa y orden de entrega. Mapa con `"usar api de openstreetmap"`. |
| 3 | **Perfil de Distribuidor** | `/perfil` | Mobile (390px) | Reutilizado desde compartidas/perfil con datos de la unidad de distribución (Unidad 08 · Gran Mendoza) y contadores de entregas realizadas y pendientes. |
| 4 | **Cambiar Contraseña** | `/cambiar-password` | Mobile (390px) | Formulario de cambio de clave. |

---

### Frontend 3: `/frontend-admin`
*Roles:* `administrativo` y `jefe`
*Layouts:* `AdministrativoLayout` y `JefeLayout`
*Estructura Base Compartida:* Sidebar fija en desktop / Drawer móvil hamburguesa en pantallas pequeñas; Header con breadcrumbs, título, buscador, notificaciones y avatar de usuario; tablas responsivas (modo scroll/tarjeta en mobile, grilla en desktop).

#### Interfaz Personal Administrativo / Almacenero (`rol: "administrativo"`)
| # | Pantalla / Vista | Ruta | Dispositivo Original en Prototipo | Adaptación Responsive Requerida |
|---|-------------------|------|-----------------------------------|---------------------------------|
| 1 | **Gestión de Stock / Inventario** | `/stock` | Desktop (1440px) | KPIs operativos de inventario (Stock bajo, Sin stock, etc.), tabla de stock con buscador y botón de alta rápida. En mobile cards apiladas o scroll contenido. |
| 2 | **Catálogo de Productos (Alta y Edición)** | `/productos` | Desktop (1440px) | Formulario modular de alta de productos (datos comerciales, stock inicial, selector de categoría, carga de imagen) y listado/tabla de edición de productos existentes. |
| 3 | **Recepción y Facturación de Pedidos** | `/pedidos` | Desktop (1440px) | Listado de pedidos recepcionados con estados, detalle de productos y botón de acción para "Facturar Pedido" / "Procesar". |
| 4 | **Registro y Control de Pagos** | `/pagos` | Desktop (1440px) | Barra de filtros (período, medio de pago, estado), tabla de pagos efectuados y modal/formulario "Registrar Pago". |
| 5 | **Rutas del Día (Supervisión)** | `/rutas` | Desktop (1440px) | Pestañas de Preventores / Distribuidores con barras de avance de paradas y mapa interactivo con texto `"usar api de openstreetmap"`. En mobile apilado en vertical. |
| 6 | **Cambiar Contraseña** | `/cambiar-password` | Desktop / Mobile | Formulario para que el administrativo actualice su clave de acceso personal. |

#### Interfaz Jefe Administrativo (`rol: "jefe"`)
*Posee todas las vistas del Personal Administrativo y las del Preventor (reutilizadas por importación), más sus vistas exclusivas de supervisión y gestión estratégica:*

| # | Pantalla / Vista | Ruta | Dispositivo Original en Prototipo | Adaptación Responsive Requerida |
|---|-------------------|------|-----------------------------------|---------------------------------|
| 1 | **Dashboard Principal** | `/dashboard` | Desktop (1440px) | KPIs de ventas y clientes, gráficos de línea y barras con `ResponsiveContainer` de `recharts`, tabla de últimos pedidos y gráfico donut de categorías. En mobile KPIs a 1/2 columnas y gráficos fluidos. |
| 2 | **Estadísticas Avanzadas** | `/estadisticas` | Desktop (1440px) | Métricas extendidas de volumen, ticket promedio y tendencias temporales con `ResponsiveContainer`. |
| 3 | **Administración de Usuarios** | `/usuarios` | Desktop (1440px) | Tabla de usuarios del sistema con estados y roles; modal/formulario de "Crear Acceso" con selector de rol restringido estrictamente a `preventor` y `administrativo` (según consigna) y clave por defecto `dulcemayor`. |
| 4 | **Reportes Gerenciales** | `/reportes` | Desktop (1440px) | Filtros por período y zona, botones de "Descargar PDF" y "Descargar Excel", reportes de: ventas por período, rendimiento de preventores y volumen de golosinas más vendidas. |
| 5 | **Rendimiento de Preventores** | `/rendimiento` | Desktop (1440px) | Ranking analítico de preventores (visitas, pedidos concretados, efectividad y montos). |
| 6 | **Vistas Heredadas de Preventor (Rutas, Registro, Catálogo)** | `/preventor/rutas`, `/preventor/registro`, etc. | Mobile / Desktop | Integradas dentro de la plataforma administrativa con diseño de escritorio/tablet. |
| 7 | **Vistas Reutilizadas de Administrativo (Stock, Productos, Pedidos, Pagos)** | `/stock`, `/productos`, `/pedidos`, `/pagos` | Desktop (1440px) | Importadas directamente desde los componentes y páginas compartidas de administración. |
| 8 | **Cambiar Contraseña** | `/cambiar-password` | Desktop / Mobile | Modificación de clave de acceso personal. |

---

## 4. Dudas y Supuestos

1. **Pantalla "Promociones" en el prototipo (`prototipado/src/App.tsx:139`)**:
   - En el menú lateral del prototipo para el Jefe figura un botón "Promociones". Dado que la lista cerrada de requisitos de la consigna para el Jefe no especifica un módulo de promociones sino de Productos, Stock, Pedidos, Pagos, Usuarios, Estadísticas y Reportes, en el menú se priorizan las opciones solicitadas formalmente en la consigna. Se incluye un placeholder visual de promociones vinculado al catálogo si fuera necesario.
2. **Rol "Almacenero" y "Personal Administrativo"**:
   - La consigna indica explícitamente: *"Personal Administrativo / Almacenero (se trata como un único rol: 'administrativo')"*. Por lo tanto, no se creó un rol ni interfaz separada para almacén. La gestión de inventario y pedidos se centraliza en el rol `administrativo`.
3. **Roles para alta de nuevos usuarios**:
   - La consigna establece que el Jefe da de alta a preventores y administrativos. El selector de rol en el modal de alta de usuario contiene únicamente las opciones "Preventor" y "Administrativo".
4. **Mapas de OpenStreetMap**:
   - En todas las pantallas que contienen mapas (Sucursales en Institucional; Recorridos en Preventor y Distribuidor; Rutas en Admin), se implementó un contenedor estilizado con fondo y proporciones acordes al prototipo, con el texto exacto `"usar api de openstreetmap"` y sin librerías de terceros, cumpliendo estrictamente la regla 3.
5. **Autenticación y Rutas Protegidas**:
   - Todo el sistema de autenticación es visual y simulado mediante `src/mocks/session.js`. Las rutas protegidas en el frontend institucional (`/carrito`, `/perfil`) utilizan un wrapper `<RutaProtegida>` que permite el acceso simulado para visualización y pruebas.


# 01-dominio.md — Modelo de dominio de Dulce Mayor

> **Fuente:** UML de diseño `UML-Integrador-2` (versión más reciente del equipo). Este archivo es la **fuente de verdad del dominio**: si otro documento (`GENERAL.md`, `04-openapi.yaml`) se contradice con él, manda este.
> **Para el asistente:** respetá los nombres de clases, atributos y métodos tal como figuran acá. Los nombres están en español y son los del diagrama. Donde el diagrama es ambiguo o incompleto, la sección **"Puntos abiertos"** indica qué interpretación usar hasta que el equipo corrija el UML. No inventes clases ni relaciones que no estén en este archivo.

## Convenciones del modelo

- Todas las entidades tienen `id:String` (se implementa como UUID) y, salvo las marcadas en contrario, `eliminado:boolean` para **borrado lógico**: los listados `...Activo` excluyen los eliminados.
- Los métodos del diagrama (`crear`, `validar`, `buscar`, `modificar`, `eliminar`, `listar`) son la **interfaz de la capa de servicio** de cada clase, no métodos de la entidad JPA. La entidad solo lleva datos y relaciones.
- `Collection<T>`: se implementa como `List<T>` o `Page<T>` según el endpoint.
- Las flechas del diagrama indican **quién guarda la referencia**: `A → B` significa que `A` conoce a `B` y `B` no conoce a `A`. Se mantiene así para evitar dependencias circulares.

## 1. Mapa de relaciones

### 1.1 Herencia

| Clase base | Subclases |
|---|---|
| `Contacto` | `ContactoCorreoElectronico`, `ContactoTelefonico` |
| `Persona` | `Empleado`, `Minorista` |
| `Empleado` | `Preventor`, `Distribuidor` |
| `Ruta` | `RutaPreventor`, `RutaDistribuidor` |

### 1.2 Composición (el todo es dueño de las partes; se crean y eliminan juntos)

| Todo | Parte | Multiplicidad |
|---|---|---|
| `Pedido` | `DetallePedido` | 1 — 1..* |
| `Pedido` | `HistorialEstadoPedido` | 1 — 1..* |
| `Factura` | `DetalleFactura` | 1 — 1..* |

### 1.3 Asociaciones dibujadas en el diagrama

Formato: **origen → destino** = el origen guarda la referencia al destino. Las multiplicidades de esta tabla son las **derivadas del dominio**: el diagrama solo escribe `0..n` junto a la punta de flecha en todas sus asociaciones (ver punto abierto 1).

| Origen | → | Destino | Multiplicidad (origen — destino) | Nota |
|---|---|---|---|---|
| `Provincia` | → | `Pais` | * — 1 |
| `Departamento` | → | `Provincia` | * — 1 |
| `Localidad` | → | `Departamento` | * — 1 |
| `Direccion` | → | `Localidad` | * — 1 |
| `Mayorista` | → | `Direccion` | 1 — 1..* | Sede y sucursales. |
| `Mayorista` | → | `Contacto` | 1 — 0..* |
| `Mayorista` | → | `Imagen` | 1 — 0..* | Fotos de sucursales y logo. |
| `Mayorista` | — | `ConfiguracionCorreoMayorista` | 1 — 1 | Sin flecha en el diagrama. |
| `Persona` | → | `Contacto` | 1 — 0..* |
| `Usuario` | → | `Persona` | 0..1 — 1 | Tal como se dibuja; ver punto abierto 2. |
| `PuntoDeVenta` | → | `Direccion` | * — 1 | Con `latitud` y `longitud`. |
| `PuntoDeVenta` | → | `Minorista` | 1 — 1..* | Tal como se dibuja |
| `PuntoDeVenta` | → | `Distribuidor` | — | La línea termina en `Distribuidor`; |
| `PuntoDeVenta` | → | `Preventor` | * — 0..1 |
| `PuntoDeVenta` | → | `Minorista` | * — 1 |
| `Preventor` | → | `PuntoDeVenta` | — | La línea termina en `PuntoDeVenta`; |
| `Producto` | → | `SubCategoria` | * — 1 |
| `SubCategoria` | → | `Categoria` | * — 1 |
| `Producto` | → | `Imagen` | * — 0..1 | |
| `VigenciaPrecio` | → | `Producto` | * — 1 |
| `Stock` | → | `DetalleFactura` | * — 0..1 | Historial de movimientos de stock. Se asocia a DetalleFactura en caso de egreso por venta/facturación. |
| `Promocion` | → | `Producto` | * — 0..1 | Producto destacado. |
| `DetallePedido` | → | `Producto` | * — 1 |
| `DetalleFactura` | → | `Producto` | * — 1 |
| `Pedido` | → | `PuntoDeVenta` | * — 1 | 
| `Pedido` | → | `Promocion` | * — 0..1 | 
| `Factura` | → | `Pedido` | 1 — 0..1 | Tal como se dibuja; |
| `Factura` | → | `Pago` | 1 — 0..1 | Tal como se dibuja; |
| `Pedido` | → | `Distribuidor` | * — 0..1 | Ver punto abierto 7. |
| `Factura` | → | `Pago` | 1 — 0..* | Tal como se dibuja; |
| `Pago` | → | `FormaDePago` | * — 1 |
| `RutaPreventor` | → | `PuntoDeVenta` | * — 0..* |
| `RutaPreventor` | → | `Preventor` | * — 0..* |
| `RutaDistribuidor` | → | `Distribuidor` | * — 1 |
| `RutaDistribuidor` | → | `Pedido` | * — 1 |

> Al implementar con JPA, cada relación 1—1 o *—1 se resuelve con **una sola clave foránea** (en el lado que figura como origen o en el lado que indica la firma). No se crean referencias bidireccionales.

## 2. Enumeraciones

| Enumeración | Valores |
|---|---|
| `TipoEmpresa` | `SEDE_CENTRAL`, `SUCURSAL` |
| `TipoContacto` | `PERSONAL`, `LABORAL`, `EMPRESA` |
| `TipoTelefono` | `FIJO`, `CELULAR` |
| `TipoDocumento` | `DNI`, `CUIT`, `PASAPORTE` |
| `RolUsuario` *(el diagrama usa `PREVENTISTA` y `MINORISTA`)* | `ADMINISTRATIVO`, `JEFE`, `PREVENTISTA`, `MINORISTA`, `DISTRIBUIDOR` |
| `ProveedorAuth` | `LOCAL`, `GOOGLE`, `FACEBOOK` |
| `TipoImagen` | `PRODUCTO`, `SUCURSAL`, `LOGO_EMPRESA` |
| `EstadoPedido` | `CARGADO`, `SIN_STOCK`, `FACTURADO`, `EN_REPARTO`, `ENTREGADO`, `ANULADO` |
| `EstadoFactura` | `PENDIENTE_PAGO`, `PAGADA`, `ANULADA` |
| `TipoPago` | `EFECTIVO`, `TRANSFERENCIA`, `BILLETERA_VIRTUAL` |
| `RutaEstado` | `REALIZADA`, `PENDIENTE`, `INCOMPLETA` |

### Estados del pedido: transiciones permitidas

| Desde | Hacia | Cuándo |
|---|---|---|
| (nuevo) | `CARGADO` | El preventor carga el pedido |
| `CARGADO` | `FACTURADO` | Se procesa y hay stock para todos los ítems |
| `CARGADO` | `SIN_STOCK` | Se procesa y falta stock en algún ítem |
| `SIN_STOCK` | `CARGADO` / `ANULADO` | Tras reposición, o por decisión administrativa |
| `FACTURADO` | `EN_REPARTO` | Se incluye en una `RutaDistribuidor` |
| `EN_REPARTO` | `ENTREGADO` | El distribuidor confirma la entrega |
| `CARGADO` / `SIN_STOCK` | `ANULADO` | Anulación |

Cada transición se registra con `Pedido.cambiarEstado` → `HistorialEstadoPedido.registrarEstado`.

## 3. Clases

## 3.1 Geografía y ubicación

### Pais
**Atributos:** `id:String`, `nombre:String`, `eliminado:boolean`

**Métodos:**

```text
+crearPais(nombre:String):void
+validar(nombre:String):void
+buscarPais(id:String):Pais
+buscarPaisPorNombre(nombre:String):Pais
+modificarPais(id:String, nombre:String):void
+eliminarPais(id:String):void
+listarPais():Collection<Pais>
+listarPaisActivo():Collection<Pais>
```

### Provincia
**Atributos:** `id:String`, `nombre:String`, `eliminado:boolean`

**Métodos:**

```text
+crearProvincia(nombre:String, idPais:String):void
+validar(nombre:String, idPais:String):void
+buscarProvincia(id:String):Provincia
+buscarProvinciaPorNombre(nombre:String):Provincia
+modificarProvincia(id:String, nombre:String, idPais:String):void
+eliminarProvincia(id:String):void
+listarProvincia(idPais:String):Collection<Provincia>
+listarProvinciaActivo(idPais:String):Collection<Provincia>
```

### Departamento
**Atributos:** `id:String`, `nombre:String`, `eliminado:boolean`

**Métodos:**

```text
+crearDepartamento(nombre:String, idProvincia:String):void
+validar(nombre:String, idProvincia:String):void
+buscarDepartamento(id:String):Departamento
+buscarDepartamentoPorNombre(nombre:String):Departamento
+modificarDepartamento(id:String, nombre:String, idProvincia:String):void
+eliminarDepartamento(id:String):void
+listarDepartamento(idProvincia:String):Collection<Departamento>
+listarDepartamentoActivo(idProvincia:String):Collection<Departamento>
```

### Localidad
**Atributos:** `id:String`, `nombre:String`, `codigoPostal:String`, `eliminado:boolean`

**Métodos:**

```text
+crearLocalidad(nombre:String, codigoPostal:String, idDepartamento:String):void
+validar(nombre:String, codigoPostal:String, idDepartamento:String):void
+buscarLocalidad(id:String):Localidad
+buscarLocalidadPorNombre(nombre:String):Localidad
+buscarLocalidadPorCodigoPostal(codigoPostal:String):Localidad
+modificarLocalidad(id:String, nombre:String, codigoPostal:String, idDepartamento:String):void
+eliminarLocalidad(id:String):void
+listarLocalidad(idDepartamento:String):Collection<Localidad>
+listarLocalidadActivo(idDepartamento:String):Collection<Localidad>
```

### Direccion
**Atributos:** `id:String`, `calle:String`, `numeracion:String`, `barrio:String`, `manzanaPiso:String`, `casaDepartamento:String`, `referencia:String`, `latitud:double`, `longitud:double`, `eliminado:boolean`

**Métodos:**

```text
+crearDireccion(calle:String, numeracion:String, barrio:String, manzanaPiso:String, casaDepartamento:String, referencia:String, latitud:double, longitud:double, idLocalidad:String):Direccion
+validar(calle:String, numeracion:String, barrio:String, manzanaPiso:String, casaDepartamento:String, referencia:String, latitud:double, longitud:double, idLocalidad:String):void
+buscarDireccion(id:String):Direccion
+buscarDireccionPorCalleNumeracion(calle:String, numeracion:String):Direccion
+modificarDireccion(id:String, calle:String, numeracion:String, barrio:String, manzanaPiso:String, casaDepartamento:String, referencia:String, latitud:double, longitud:double, idLocalidad:String):void
+eliminarDireccion(id:String):void
```

## 3.2 Empresa y contacto

### Mayorista
La empresa dueña del sistema (Dulce Mayor) y sus sucursales. `tipoEmpresa` distingue `SEDE_CENTRAL` de `SUCURSAL`.
**Atributos:** `id:String`, `razonSocial:String`, `cuit:String`, `tipoEmpresa:TipoEmpresa`, `eliminado:boolean`

**Métodos:**

```text
+crearMayorista(razonSocial:String, cuit:String, tipoEmpresa:TipoEmpresa):void
+validar(razonSocial:String, cuit:String, tipoEmpresa:TipoEmpresa):void
+buscarMayorista(id:String):Mayorista
+buscarMayoristaPorRazonSocial(razonSocial:String):Mayorista
+modificarMayorista(id:String, razonSocial:String, cuit:String,tipoEmpresa:TipoEmpresa):void
+eliminarMayorista(id:String):void
+listarMayorista():Collection<Mayorista>
+listarMayoristaActivo():Collection<Mayorista>
```

### ConfiguracionCorreoEmpresa
**Atributos:** `id:String`, `correo:String`, `clave:String`, `puerto:String`, `smtp:String`, `tls:boolean`, `eliminado:boolean`

**Métodos:**

```text
+crearConfiguracionCorreoEmpresa(correo:String, clave:String, puerto:String, smtp:String, tls:boolean, idEmpresa:String):void
+validar(correo:String, clave:String, puerto:String, smtp:String, tls:boolean, idEmpresa:String):void
+modificarConfiguracionCorreoEmpresa(id:String, correo:String, clave:String, puerto:String, smtp:String, tls:boolean, idEmpresa:String):void
+buscarConfiguracionCorreoEmpresa(id:String):ConfiguracionCorreoEmpresa
+buscarConfiguracionCorreoEmpresaPorEmpresa(idEmpresa:String):ConfiguracionCorreoEmpresa
+eliminarConfiguracionCorreoEmpresa(id:String):void
```

### Contacto
Clase base de los medios de contacto (correo electrónico y teléfono).
**Atributos:** `id:String`, `tipoContacto:TipoContacto`, `observacion:String`, `eliminado:boolean`

**Métodos:**

```text
+buscarContacto(id:String):Contacto
+eliminarContacto(id:String):void
```

### ContactoCorreoElectronico
*Hereda de* `Contacto`.  
**Atributos:** `email:String`

**Métodos:**

```text
+crearContactoCorreoElectronico(email:String, tipoContacto:TipoContacto, observacion:String):void
+validar(email:String, tipoContacto:TipoContacto, observacion:String):void
+modificarContactoCorreoElectronico(id:String, email:String, tipoContacto:TipoContacto, observacion:String):void
+listarContactoCorreoElectronico():Collection<ContactoCorreoElectronico>
+listarContactoCorreoElectronicoActivo():Collection<ContactoCorreoElectronico>
```

### ContactoTelefonico
*Hereda de* `Contacto`.  
**Atributos:** `telefono:String`, `tipoTelefono:TipoTelefono`

**Métodos:**

```text
+crearContactoTelefonico(telefono:String, tipoTelefono:TipoTelefono, tipoContacto:TipoContacto, observacion:String):void
+validar(telefono:String, tipoTelefono:TipoTelefono, tipoContacto:TipoContacto, observacion:String):void
+modificarContactoTelefonico(id:String, telefono:String, tipoTelefono:TipoTelefono, tipoContacto:TipoContacto, observacion:String):void
+listarContactoTelefonico():Collection<ContactoTelefonico>
+listarContactoTelefonicoActivo():Collection<ContactoTelefonico>
```

### Imagen
**Atributos:** `id:String`, `nombre:String`, `mime:String`, `contenido:byte[]`, `tipoImagen:TipoImagen`, `eliminado:boolean`

**Métodos:**

```text
+crearImagen(nombre:String, contenido:byte[], tipoImagen:TipoImagen):Imagen
+validar(nombre:String, contenido:byte[], tipoImagen:TipoImagen):void
+buscarImagen(id:String):Imagen
+modificarImagen(id:String, nombre:String, contenido:byte[], tipoImagen:TipoImagen):Imagen
+eliminarImagen(id:String):void
```

## 3.3 Personas y acceso

### Persona
Clase base de personas físicas. Se hereda en `Empleado` y `Minorista`.
**Atributos:** `id:String`, `nombre:String`, `apellido:String`, `fechaNacimiento:Date`, `tipoDocumento:TipoDocumento`, `numeroDocumento:String`, `eliminado:boolean`

**Métodos:**

```text
+buscarPersona(id:String):Persona
+eliminarPersona(id:String):void
+asociarUsuarioPersona(id:String, idUsuario:String):Persona
```

### Usuario
Credenciales de acceso. La clave se guarda encriptada. Un usuario nuevo nace con la clave por defecto y `claveTemporal=true`.
**Atributos:** `id:String`, `nombreUsuario:String`, `clave:String (encriptada)`, `rol:RolUsuario`, `claveTemporal:boolean`, `proveedorAuth:ProveedorAuth`, `eliminado:boolean`

**Métodos:**

```text
+crearUsuario(nombreUsuario:String, rol:RolUsuario):void  (asigna clave por defecto y claveTemporal=true)
+modificarUsuario(id:String, nombreUsuario:String, rol:RolUsuario):void
+validar(nombreUsuario:String, clave:String, rol:RolUsuario):void
+eliminarUsuario(id:String):void
+buscarUsuario(id:String):Usuario
+buscarUsuarioPorNombre(nombreUsuario:String):Usuario
+listarUsuario():Collection<Usuario>
+listarUsuarioActivo():Collection<Usuario>
+login(nombreUsuario:String, clave:String):Usuario
+loginExterno(proveedor:ProveedorAuth, tokenExterno:String):Usuario
+requiereCambioClave(id:String):boolean
+modificarClave(id:String, claveActual:String, nuevaClave:String, confirmarClave:String):void
```

### Empleado
*Hereda de* `Persona`.  
Personal de la empresa. Su perfil de acceso lo define `Usuario.rol`. Los empleados `ADMINISTRATIVO` y `JEFE` son `Empleado` directo; los repartidores y vendedores de campo son las subclases `Distribuidor` y `Preventor`.
**Atributos:** `Legajo:String`

**Métodos:**

```text
+crearEmpleado(nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, idUsuario:String, legajo:String):void
+validar(nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, idUsuario:String):void
+modificarEmpleado(id:String, nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String,legajo:String):void
+buscarEmpleado(id:String):Empleado
+listarEmpleado():Collection<Empleado>
+listarEmpleadoActivo():Collection<Empleado>
+listarEmpleadoPorRol(rol:RolUsuario):Collection<Empleado>
+asociarEmpleadoUsuario(empleado:Empleado, usuario:Usuario):void
```

### Preventor
*Hereda de* `Empleado`.  
Vendedor de campo (preventista). Toma los pedidos en los puntos de venta.
**Atributos:** `id:String`

**Métodos:**

```text
+crearPreventor(nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, idUsuario:String, legajo:String):void
+validar(nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, idUsuario:String, legajo:String):void
+modificarPreventor(id:String, nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, legajo:String):void
+buscarPreventor(legajo:String):Preventor
+listarPreventor():Collection<Preventor>
+listarPreventorActivo():Collection<Preventor>
+listarPreventorPorRol(rol:RolUsuario):Collection<Preventor>
+asociarPreventorUsuario(preventor:Preventor, usuario:Usuario):void
```

### Distribuidor
*Hereda de* `Empleado`.  
Equipo de despacho. Entrega los pedidos siguiendo su ruta diaria.
**Atributos:** `id:String`

**Métodos:**

```text
+crearDistribuidor(nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, idUsuario:String, legajo:String):void
+validar(nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, idUsuario:String, legajo:String):void
+modificarDistribuidor(id:String, nombre:String, apellido:String, fechaNacimiento:Date, tipoDocumento:TipoDocumento, numeroDocumento:String, legajo:String):void
+buscarDistribuidor(legajo:String):Distribuidor
+listarDistribuidor():Collection<Distribuidor>
+listarDistribuidorActivo():Collection<Distribuidor>
+listarDistribuidorPorRol(rol:RolUsuario):Collection<Distribuidor>
+asociarDistribuidorUsuario(distribuidor:Distribuidor, usuario:Usuario):void
```

### Minorista
*Hereda de* `Persona`.  
El **cliente**: responsable de uno o más comercios.
**Atributos propios:** ninguno (usa los heredados).

**Métodos:**

```text
+crearMinorista(nombre:String, apellido:String, fechaNacimiento:Date,tipoDocumento:TipoDocumento, numeroDocumento:String):void
+validar(nombre:String, apellido:String, fechaNacimiento:Date,tipoDocumento:TipoDocumento, numeroDocumento:String):void
+modificarMinorista(id:String, nombre:String, apellido:String,fechaNacimiento:Date, tipoDocumento:TipoDocumento,numeroDocumento:String):void
+buscarMinorista(id:String): Minorista
+buscarMinoristaPorDocumento(numeroDocumento:String):Minorista
+listarMinorista():Collection<Minorista>
+listarMinoristaActivo():Collection<Minorista>
+asociarMinoristaUsuario(minorista:Minorista, usuario:Usuario):void
```

### PuntoDeVenta
El comercio físico del minorista (kiosco, almacén, etc.): se visita, se le toman pedidos y se le entrega. Tiene geolocalización a través de su `Direccion`.
**Atributos:** `id:String`, `nombreComercio:String`, `eliminado:boolean`

**Métodos:**

```text
+crearPuntoDeVenta(nombreComercio:String, idCliente:String, idDireccion:String, idPreventista:String):void
+validar(nombreComercio:String, idCliente:String, idDireccion:String, idPreventista:String):void
+buscarPuntoDeVenta(id:String):PuntoDeVenta
+buscarPuntoDeVentaPorNombre(nombreComercio:String):PuntoDeVenta
+modificarPuntoDeVenta(id:String, nombreComercio:String, idCliente:String, idDireccion:String, idPreventista:String):void
+eliminarPuntoDeVenta(id:String):void
+listarPuntoDeVenta():Collection<PuntoDeVenta>
+listarPuntoDeVentaActivo():Collection<PuntoDeVenta>
+listarPuntoDeVentaPorPreventista(idPreventista:String):Collection<PuntoDeVenta>
+listarPuntoDeVentaPorCliente(idCliente:String):Collection<PuntoDeVenta>
```

## 3.4 Catálogo, precios y stock

### Categoria
**Atributos:** `id:String`, `nombre:String`, `eliminado:boolean`

**Métodos:**

```text
+crearCategoria(nombre:String):void
+validar(nombre:String):void
+buscarCategoria(id:String):Categoria
+buscarCategoriaPorNombre(nombre:String):Categoria
+modificarCategoria(id:String, nombre:String):void
+eliminarCategoria(id:String):void
+listarCategoria():Collection<Categoria>
+listarCategoriaActivo():Collection<Categoria>
```

### SubCategoria
**Atributos:** `id:String`, `nombre:String`, `eliminado:boolean`

**Métodos:**

```text
+crearSubCategoria(nombre:String, idCategoria:String):void
+validar(nombre:String, idCategoria:String):void
+buscarSubCategoria(id:String):SubCategoria
+buscarSubCategoriaPorNombre(nombre:String):SubCategoria
+modificarSubCategoria(id:String, nombre:String, idCategoria:String):void
+eliminarSubCategoria(id:String):void
+listarSubCategoria(idCategoria:String):Collection<SubCategoria>
+listarSubCategoriaActivo(idCategoria:String):Collection<SubCategoria>
```

### Producto
**Atributos:** `id:String`, `codigo:String`, `nombre:String`, `descripcion:String`, `presentacion:String (ej. Pack x12)`, `unidadesPorBulto:int`, `enOferta:boolean`, `eliminado:boolean`

**Métodos:**

```text
+crearProducto(codigo:String, nombre:String, descripcion:String, presentacion:String, unidadesPorBulto:int, enOferta:boolean, idImagen:String, idSubCategoria:String):void
+validarProducto(codigo:String, nombre:String, descripcion:String, presentacion:String, unidadesPorBulto:int, enOferta:boolean, idImagen:String, idSubCategoria:String):void
+modificarProducto(id:String, nombre:String, descripcion:String, presentacion:String, unidadesPorBulto:int, enOferta:boolean, idImagen:String, idSubCategoria:String):void
+eliminarProducto(id:String):void
+buscarProducto(id:String):Producto
+buscarProductoPorNombre(nombre:String):Producto
+buscarProductoPorCodigo(codigo:String):Producto
+listarProducto():Collection<Producto>
+listarProductoActivo():Collection<Producto>
+listarProductoCatalogo():Collection<Producto>  (activos con stock > 0)
+listarProductoEnOferta():Collection<Producto>
```

### VigenciaPrecio
**Atributos:** `id:String`, `fechaDesde:Date`, `fechaHasta:Date`, `precio:double`, `eliminado:boolean`

**Métodos:**

```text
+crearVigenciaPrecio(fechaDesde:Date, fechaHasta:Date, precio:double, idProducto:String):void
+validar(fechaDesde:Date, fechaHasta:Date, precio:double, idProducto:String):void
+buscarVigenciaPrecio(id:String):VigenciaPrecio
+modificarVigenciaPrecio(id:String, fechaDesde:Date, fechaHasta:Date, precio:double, idProducto:String):void
+eliminarVigenciaPrecio(id:String):void
+listarVigenciaPrecio():Collection<VigenciaPrecio>
+listarVigenciaPrecioActivo():Collection<VigenciaPrecio>
+buscarVigenciaPrecioVigente(idProducto:String):VigenciaPrecio
```

### Stock
Registro histórico (ledger/auditoría) de movimientos de stock. En el nuevo diseño UML, `Stock` **ya no tiene el atributo `producto`**, sino que se relaciona únicamente con `DetalleFactura`, ya que a través de este se accede al producto (`Stock` $\to$ `DetalleFactura` $\to$ `Producto`).
Cada cambio en el stock genera un nuevo registro inmutable donde se guarda la nueva cantidad disponible, la fecha y hora en la que ocurrió, y el atributo `observacion` que indica si se trata de un ingreso o egreso (stock inicial, reposición, ajuste o descuento por venta).
**Atributos:** `id:String`, `cantidadActual:int`, `fechaHora:LocalDateTime`, `observacion:String`, `eliminado:boolean`, `detalleFactura:DetalleFactura`

**Métodos:**

```text
+crearStock(idProducto:String, cantidadInicial:int):void
+validar(idProducto:String, cantidadInicial:int):void
+buscarStock(id:String):Stock
+buscarStockPorProducto(idProducto:String):Stock
+modificarStock(id:String, cantidadActual:int, observacion:String):void
+hayStock(idProducto:String, cantidad:int):boolean
+descontar(idProducto:String, cantidad:int):void
+reponer(idProducto:String, cantidad:int):void
+eliminarStock(id:String):void
+listarStock():Collection<Stock>
```

### Promocion
Código de descuento. `cantidadMinima` aplica descuento por volumen; el producto asociado representa un producto destacado.
**Atributos:** `id:String`, `codigo:String`, `descripcion:String`, `porcentajeDescuento:double`, `cantidadMinima:int (opcional, descuento por volumen)`, `fechaDesde:Date`, `fechaHasta:Date`, `eliminado:boolean`

**Métodos:**

```text
+crearPromocion(codigo:String, descripcion:String, porcentajeDescuento:double, cantidadMinima:int, fechaDesde:Date, fechaHasta:Date, idProducto:String):void
+validar(codigo:String, descripcion:String, porcentajeDescuento:double, cantidadMinima:int, fechaDesde:Date, fechaHasta:Date, idProducto:String):void
+buscarPromocion(id:String):Promocion
+buscarPromocionVigentePorCodigo(codigo:String):Promocion
+modificarPromocion(id:String, descripcion:String, porcentajeDescuento:double, cantidadMinima:int, fechaDesde:Date, fechaHasta:Date, idProducto:String):void
+eliminarPromocion(id:String):void
+listarPromocion():Collection<Promocion>
+listarPromocionActiva():Collection<Promocion>
```

## 3.5 Pedidos

### Pedido
**Atributos:** `id:String`, `numeroPedido:long`, `fechaHoraCarga:Date`, `subtotal:double`, `descuento:double`, `total:double`, `estado:EstadoPedido`, `eliminado:boolean`

**Métodos:**

```text
+crearPedido(idPuntoDeVenta:String, idPreventista:String, codigoPromocion:String, detalle:Collection<DetallePedido>):void
+validar(idPuntoDeVenta:String, idPreventista:String, codigoPromocion:String, detalle:Collection<DetallePedido>):void
+modificarPedido(id:String, codigoPromocion:String, detalle:Collection<DetallePedido>):void   (solo en estado CARGADO)
+anularPedido(id:String):void
+buscarPedido(id:String):Pedido
+cambiarEstado(id:String, estado:EstadoPedido, idUsuario:String):void
+listarPedido():Collection<Pedido>
+listarPedidoPorEstado(estado:EstadoPedido):Collection<Pedido>
+listarPedidoPorPreventista(idPreventista:String):Collection<Pedido>
+listarPedidoPorCliente(idCliente:String):Collection<Pedido>
+crearDetallePedido(idProducto:String, cantidad:int):DetallePedido
+buscarDetallePedido(id:String):DetallePedido
+modificarDetallePedido(idDetallePedido:String, idProducto:String, cantidad:int):DetallePedido
+eliminarDetallePedido(idDetallePedido:String):void
```

### DetallePedido
Línea del pedido. `precioUnitario` es una copia del precio vigente al cargar el pedido. No tiene métodos propios: los gestiona `Pedido`.
**Atributos:** `id:String`, `cantidad:int`, `precioUnitario:double`, `subtotal:double`, `eliminado:boolean`

### HistorialEstadoPedido
Una fila por cada cambio de estado de un pedido: base de la trazabilidad.
**Atributos:** `id:String`, `estado:EstadoPedido`, `fechaHora:Date`, `observacion:String`

**Métodos:**

```text
+registrarEstado(idPedido:String, estado:EstadoPedido, idUsuario:String, observacion:String):void
+listarHistorialPorPedido(idPedido:String):Collection<HistorialEstadoPedido>
```

## 3.6 Facturación y pagos

### Factura
**Atributos:** `id:String`, `numeroFactura:long`, `fechaFactura:Date`, `total:double`, `estado:EstadoFactura`, `eliminado:boolean`

**Métodos:**

```text
+crearFactura(idPedido:String):void   (genera los DetalleFactura desde el pedido y descuenta stock)
+validar(idPedido:String):void
+buscarFactura(id:String):Factura
+buscarFacturaPorPedido(idPedido:String):Factura
+anularFactura(id:String):void
+eliminarFactura(id:String):void
+listarFactura():Collection<Factura>
+listarFacturaActivo():Collection<Factura>
+listarFacturaPorEstado(estado:EstadoFactura):Collection<Factura>
+listarFacturaPorCliente(idCliente:String):Collection<Factura>
```

### DetalleFactura
Línea de la factura, copiada desde `DetallePedido` al facturar. No tiene métodos propios: los gestiona `Factura`.
**Atributos:** `id:String`, `cantidad:int`, `precioUnitario:double`, `subtotal:double`, `eliminado:boolean`

### Pago
Cobro registrado contra una factura. `referencia` guarda alias, número de operación o referencia del QR.
**Atributos:** `id:String`, `monto:double`, `fechaPago:Date`, `referencia:String (alias / QR)`, `observacion:String`, `eliminado:boolean`

**Métodos:**

```text
+registrarPago(idFactura:String, idFormaDePago:String, monto:double, referencia:String):void
+validar(idFactura:String, idFormaDePago:String, monto:double, referencia:String):void
+buscarPago(id:String):Pago
+eliminarPago(id:String):void
+listarPagoPorFactura(idFactura:String):Collection<Pago>
```

### FormaDePago
Catálogo de medios de pago. `observacion` guarda, por ejemplo, el alias bancario de las transferencias.
**Atributos:** `id:String`, `tipoPago:TipoPago`, `observacion:String (alias bancario)`, `eliminado:boolean`

**Métodos:**

```text
+crearFormaDePago(tipoPago:TipoPago, observacion:String):void
+modificarFormaDePago(id:String, tipoPago:TipoPago, observacion:String):void
+eliminarFormaDePago(id:String):void
+listarFormaDePago():Collection<FormaDePago>
+listarFormaDePagoActivo():Collection<FormaDePago>
```

## 3.7 Rutas

### Ruta
Clase base abstracta de las rutas diarias. `paradaActual` es una referencia al `PuntoDeVenta` en curso. `estado` usa la enumeración `RutaEstado`.
**Atributos:** `id:String`, `fecha:Date`, `estado:RutaEstado`, `paradaActual:PuntoDeVenta`

**Métodos:**

```text
+crearRuta(fecha:Date, idResponsable:String)
+buscarRuta(id:String):Ruta
+cambiarEstado(id:String, estado:RutaEstado):void 
+listarRutaPorFecha(fecha:Date):Collection<Ruta>
+registrarParadaCompletada(idRuta:String):void
```

### RutaPreventor
*Hereda de* `Ruta`.  
Ruta diaria de visitas de un `Preventor`. Sin atributos propios en el diagrama.
**Atributos propios:** ninguno (usa los heredados).

### RutaDistribuidor
*Hereda de* `Ruta`.  
Ruta diaria de entregas de un `Distribuidor`. Sin atributos propios en el diagrama. (En el diagrama figura como `rutaDistribuidor`, en minúscula.)
**Atributos propios:** ninguno (usa los heredados).

## 4. Reglas del dominio que surgen del diagrama

- **Precio vigente:** el precio actual de un producto es el de la `VigenciaPrecio` con `fechaHasta` nula. Al cargar un pedido se copia a `DetallePedido.precioUnitario`; los cambios de precio posteriores no alteran pedidos ya cargados. Ejemplo del diagrama (alfajor caja x24): 1/7/2026 → sin fin: $15.000; 1/4/2026 → 30/6/2026: $13.000; 1/1/2026 → 31/3/2026: $10.000.
- **Catálogo público:** `Producto.listarProductoCatalogo()` devuelve solo productos activos con stock > 0. Un producto sin stock deja de mostrarse automáticamente.
- **Facturación:** `Factura.crearFactura(idPedido)` genera los `DetalleFactura` desde el pedido y descuenta el stock en la misma transacción.
- **Modificación de pedidos:** `Pedido.modificarPedido` solo se permite en estado `CARGADO`.
- **Detalles:** `DetallePedido` y `DetalleFactura` no tienen métodos propios; los gestionan `Pedido` y `Factura`.
- **Usuarios nuevos:** `Usuario.crearUsuario` asigna la clave por defecto y `claveTemporal=true`; `requiereCambioClave` indica si debe cambiarla antes de usar el sistema.
- **Newsletter:** usa `Producto.listarProductoEnOferta()`.
- **Rutas:** una persona no puede tener más de una ruta por día. `Ruta.paradaActual` apunta al `PuntoDeVenta` que se está atendiendo.

## 5. Puntos abiertos del diagrama (interpretación a usar hasta corregir el UML)

| # | Qué muestra el diagrama | Interpretación por defecto para implementar |
|---|---|---|
| 1 | Todas las asociaciones dirigidas solo llevan `0..n` junto a la punta de flecha. | Usar las multiplicidades de la sección 1. |
| 2 | La flecha `Usuario → Persona` indica que `Usuario` guarda a `Persona`, pero las firmas (`asociarUsuarioPersona`, `crearEmpleado(..., idUsuario)`) hacen que `Persona` reciba al `Usuario`. | `Persona` guarda `usuario` (0..1). `Usuario` **no** referencia a `Persona`. |
| 11 | `RolUsuario` usa `PREVENTISTA` y `MINORISTA`; el resto de los documentos puede decir `PREVENTOR` y `CLIENTE`. | Manda el diagrama: `ADMINISTRATIVO`, `JEFE`, `PREVENTISTA`, `MINORISTA`, `DISTRIBUIDOR`. |

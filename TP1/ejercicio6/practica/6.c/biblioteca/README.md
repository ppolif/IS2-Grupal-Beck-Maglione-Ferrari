
--------------------------------------------------------------------------------
DTOs

Los DTOs estan separados en response y request, los request son para recibir datos del exterior de la aplicacion
y permiten asegurar los datos sensibles de la clase, por ejemplo que un usuario quiera inyectar algun valor al atributo rol de la clase usuario.
Los response son los datos de la clase que envia la aplicacion a la vista y se usan para poder enviar lo necesario y no exponer atributos
o datos sensibles de la clase.

----------------------------------------------------------------

Servicios

Los servicios estan divididos en claseServicio y claseServicioImpl donde esta ultima es una interfaz de la primera,
esto se hace asi para cumplir con el Principio de Inversion de Dependencias de SOLID o, en otras palabras, agregar desacoplamiento al codigo,
estas interfaces le dicen al sistema qué puede hacer pero no cómo desacoplando así los métodos del servicio de los controladores (EJ: LibroControlador se guía por lo que le dice que puede hacer el LibroServicioImpl).
Tambien añade flexibilidad y mantenimiento al código si a futuro queres modificar metodos del servicio, ya que si no tuvieras la interfaz de por medio si se cambia algun metodo del servicio habria que revisar el controlador tambien al este implementar directamente los metodos del servicio.

------------------------------------
Controladores

La mayoría de los controladores tienen la anotacion @RestController la cual es una composicion de @Controller y @ResponseBody,
esto lo que hace es transformar en JSON los datos que se mueven a traves de la app y separarlos de los archivos HTML. Esta decision se toma para que la app pueda ser adaptable a distintos dispositivos o vistas ya que los datos consumidos van a ser los mismos al estar en formato JSON.
Los controladores no tienen la anotacion @Autowired porque a partir de la version 4.3 de Spring si una clase tiene un único constructor asume automaticamente que debe inyectar las dependencias por ahi, volviendo implícita la anotacion. El proyecto adopta la Inyección de Dependencias por Constructor en sus API REST en lugar de la inyección por campo (@Autowired). Al aprovechar la inyección implícita de Spring, se permite declarar los servicios como inmutables (final), garantizando la seguridad en tiempo de ejecución, facilitando el mocking en pruebas unitarias y forzando un diseño de software más limpio que respeta la Responsabilidad Única.

--------------------------------------------------------

Seguridad (conceptos de la playlist de youtube)

El uso de BCryptPasswordEncoder en la clase SeguridadWeb permite encriptar las contraseñas de los usuarios para no guardarlas en texto plano en la base de datos.

Las URLs estan securitizadas por roles, en la app existe el rol de USUARIO y el de ADMINISTRADOR, en el método filterChain de la clase mencionada antes
se contempla las URLs que pueden ser visitadas publicamente, las que solo pueden ser visitadas si se está logeado en la app (tanto como USUARIO como ADMINISTRADOR) y despues estan las rutas que solo pueden ser accedidas por
los usuarios admins con el rol ADMINISTRADOR. (para setear un usuario como ADMINISTRADOR se debe hacer desde la base de datos a mano)

Hay métodos con la anotacion @PreAuthorize en los controladores que sirve como una segunda capa de validación en caso de que se logre saltar el filtro anterior.

El .migrateSession() previene ataques de secuestro de sesion(El ataque de secuestro de sesión es una amenaza de ciberseguridad en la que un atacante se apodera de una sesión de usuario activa para suplantar su identidad y obtener acceso no autorizado a cuentas o datos sensibles) porque
hace que Spring genere un nuevo SessionID en el momento en el que el usuario se autentica exitosamente.

Tambien se maneja la vista de errores, mostrando el error 404 cuando se quiera acceder a una URL inexistente, el 403 cuando se quiera acceder a una ruta sin el permiso necesario y el error 500 por si hay algun error en el servidor.


-----------------------------------------------------------------------------------------------------------
Base de Datos

Se usa MySQL con el usuario root y la contraseña 1234, la base de datos se llama biblioteca y el proyecto corre en localhost:8080
-----------------------------------------------------------------------------------------------
Imagenes

Las imagenes pueden ser cargadas como portadas de los libros, para manejar este tipo de archivos hay que añadirle a la clase tipos de datos para representar su MIME ( (Multipurpose Internet Mail Extensions) es un estándar de Internet que identifica la naturaleza y el formato de un archivo o conjunto de datos) y su contenido en un arreglo de bytes, la anotacion @Lob le avisa a la base de datos que se va a almacenar un archivo pesado.

------------------------------------------------------------------------------------------
Permisos 

Los permisos estan programados en el servicio de usuario en el metodo loadByUsername el cual es un metodo de la clase que extiende el servicio la cual se llama UserDetailsService, en ese metodo se crea la lista de permisos y se configura el usuario de sesion con sus atributos http.

------------------------------------------------------------------------------------------------------
Vista

La vista contiene ThymeLeaf en los archivos HTML, no se aplica en el proyecto pero se puede reutilizar codigo HTML utilizando fragmentos en comun que se guardan en una carpeta llamada "fragments" que ThymeLeaf identifica para que sepa que codigo reutilizar en cada archivo HTML que renderice.

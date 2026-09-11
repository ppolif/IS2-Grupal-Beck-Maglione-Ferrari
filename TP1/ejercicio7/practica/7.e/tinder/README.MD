Pruebas Unitarias

Se aplican a los servicios de una API para validar las reglas de negocio
de una manera totalmente independiente, sin depender de la base de datos o una conexion a internet. Se
ejecutan aisladamente por esta razon se usan Mocks para simular que se hacen operaciones de ABM.

Pruebas de Integracion

Se aplican tanto a los repositorios como a las clases encargadas de la seguridad.
Su tarea es checkear la correcta comunicacion entre dos o mas clases o partes de tu API,
en estas prueba se corrobora tanto si la base de datos responde bien como la securitizacion de rutas u otras medidas de seguridad.


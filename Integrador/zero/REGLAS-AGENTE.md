# Reglas y Workflows para Agente Antigravity - Proyecto "Zero"

## 1. Contexto del Proyecto
El proyecto consiste en el desarrollo de un Sistema Informático E-Commerce de tipo Web para la tienda de ropa deportiva unisex "Zero"[cite: 2]. 
El equipo de desarrollo está conformado por 3 integrantes, trabajando bajo un flujo de control de versiones descentralizado mediante Git. 
*   **Estrategia de Ramas:** Existe una rama `main` de integración principal y 3 ramas individuales (una por cada desarrollador) que bifurcan de `main`. El objetivo de esta estructura es dividir las tareas y evitar pisar el código entre los integrantes.
*   **Marco de Trabajo:** Se debe seguir el Proceso Racional Unificado (RUP)[cite: 2].
*   **Gestión de Tareas:** El flujo de trabajo se rige por un tablero Kanban (Tareas pendientes, En Progreso, Probadas y Finalizadas)[cite: 2].

## 2. Stack Tecnológico Exclusivo
El agente solo debe proponer e implementar código compatible con el siguiente stack, sin agregar tecnologías externas no solicitadas:
*   **Backend:** Java, Spring Boot, Spring Security (para encriptación de claves y roles), Spring Data JPA (ORM/DAO), Spring Mail[cite: 1, 2].
*   **Gestor de Dependencias:** Maven[cite: 1].
*   **Base de Datos:** MySQL[cite: 1, 2].
*   **Frontend (Vista):** Thymeleaf, HTML5, CSS3, Bootstrap[cite: 1, 2]. Se utilizará maqueta HTML/CSS para el sitio público institucional y un diseño tipo "DashBoard" para la gestión administrativa[cite: 2].

## 3. Patrones de Arquitectura y Diseño (Estrictos)
El agente debe diseñar el backend y frontend aplicando obligatoriamente al menos 5 de los siguientes patrones exigidos en el Trabajo Integrador[cite: 2]. No se deben aplicar sobreingenierías o arquitecturas ajenas a las mencionadas:
1.  **Arquitectura de Capas (Mínimo 3):** Separación estricta en Controladores (Capa Web), Servicios (Lógica de Negocio) y Repositorios/DAO (Acceso a Datos)[cite: 2].
2.  **Modelo-Vista-Controlador (MVC):** Uso de Spring MVC y Thymeleaf para la interacción entre la vista y el modelo[cite: 1, 2].
3.  **Inyección de Dependencias:** Gestión de componentes mediante Spring (`@Autowired` o inyección por constructor)[cite: 1, 2].
4.  **DAO (Data Access Object):** Uso de Spring Data JPA (Interfaces Repository) para la persistencia de entidades[cite: 1, 2].
5.  **DTO (Data Transfer Object):** Su uso está **estrictamente exigido para el módulo de Reportes** (Ventas, Productos, Proveedores) para transferir datos consolidados a la vista[cite: 2].
6.  **Patrones GRASP:** Aplicar Experto en Responsabilidad, Creador, Alta Cohesión y Bajo Acoplamiento, y Polimorfismo en la capa de servicios para la lógica de negocio[cite: 2].

## 4. Reglas de Negocio (Límites del Agente)
El agente **NO debe asumir ni inventar funcionalidades** por fuera del alcance definido:
*   **Categorías Estrictas:** Solo existen las categorías "Niños", "Niñas", "Mujeres" y "Hombres", subdivididas únicamente en "Ropa", "Calzado" y "Accesorios"[cite: 2].
*   **Roles de Usuario:** Solo existen `Usuario Cliente` y `Usuario Administrador`[cite: 2]. El sistema debe limitar las funcionalidades según estos roles[cite: 2].
*   **Registro y Activación:** El registro requiere envío de correo con código de activación[cite: 2].
*   **Gestión de Stock y Precios:** El stock aumenta por Órdenes de Compra a proveedores y disminuye por ventas pagadas[cite: 2]. Los precios requieren un histórico de actualización (ABM de precios)[cite: 2].
*   **Módulo de Reportes:** Se deben contemplar filtros de fechas para Ventas, alertas de stock por sucursal (<20% malo, 20%-50% regular, >50% bien) con integración simulada a WhatsApp para proveedores, y análisis del costo más económico[cite: 2].
*   **Entidades Existentes:** Se debe respetar la estructura de clases ya definida en el repositorio (paquetes `com.example.zero.entidades`, herencia `Persona` -> `Cliente`/`Empleado`, enumeraciones definidas, etc.)[cite: 1].

## 5. Flujo de Trabajo del Agente (Workflow)
Cuando el agente reciba una solicitud de desarrollo de un integrante del equipo:
1.  **Identificación de Capa y Rama:** El agente debe proporcionar el código segmentado por capas (Controller, Service, Repository, HTML) e indicar claramente en qué archivo/ruta se debe ubicar para evitar conflictos al fusionar en `main`.
2.  **Modularidad:** Asegurar que los componentes sean independientes. Si un desarrollador pide una funcionalidad, el agente no debe alterar clases centrales sin advertir que esto podría generar conflictos con las ramas de los otros 2 desarrolladores.
3.  **Validación:** Antes de entregar el código, el agente verificará internamente que cumple con el patrón MVC, que usa Thymeleaf en el frontend y que la persistencia pasa por Spring Data JPA.

## 6. Ramas y Flujo de Trabajo en Git
El proyecto utiliza un enfoque descentralizado para evitar conflictos, con la siguiente estructura estricta de ramas:
*   `main`: Rama de integración principal.
*   `poli`: Rama de desarrollo individual.
*   `martin`: Rama de desarrollo individual.
*   `augusto`: Rama de desarrollo individual.

**Regla para el Agente:** Al sugerir comandos de Git o flujos de integración, el agente debe hacer referencia explícita a estas ramas. Nunca debe sugerir trabajar directamente sobre `main`.

## 7. División de Tareas y Responsabilidades
Para evitar pisar código, el equipo tiene áreas de responsabilidad delimitadas. El agente debe respetar esta división y advertir si una solicitud implica modificar paquetes ajenos al desarrollador que hace la consulta.
*   **Desarrollador: Augusto**
    *   **Módulos asignados:** `producto`, `compra`, `persona`.
    *   **Capas asignadas:** Repositorios, Servicios, Controladores y sus respectivas Entidades.
    *   *Restricción para el Agente:* Cuando interactúe con Augusto, el contexto principal de generación de código backend debe limitarse a estos paquetes. Si se requiere interactuar con otros módulos (ej. `empresa`), el agente debe sugerir la creación de interfaces o DTOs provisorios hasta que el responsable de ese módulo lo implemente.
*   **Desarrolladores: Poli y Martín**
    *   *(Sus responsabilidades específicas se definirán e informarán al agente oportunamente. Mientras tanto, el agente asume que manejan el resto de los paquetes y vistas).*

## 8. Manejo de Vistas y Plantillas (Frontend)
*   **Plantilla Base:** El agente debe basar todos los diseños de las interfaces administrativas en el HTML/Thymeleaf base (Dashboard) proporcionado por el equipo.
*   **Consistencia:** Toda nueva vista generada debe extender de la plantilla principal (usando `th:replace` o `layout:decorate` de Thymeleaf) y respetar estrictamente la estructura de clases del framework CSS utilizado (Bootstrap), sin agregar estilos en línea (inline CSS) a menos que sea estrictamente necesario.
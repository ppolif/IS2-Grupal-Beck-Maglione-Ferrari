# Spring Boot: JWT Authentication Project

Este proyecto implementa autenticación y autorización stateless mediante **JWT (JSON Web Tokens)** en una aplicación **Spring Boot 3** con **Spring Security 6**.

El proyecto incorpora las mejoras y convenciones del tutorial de YouTube:
* **Video de referencia:** [Java Spring Boot + JWT Authentication | Secure REST APIs Step-by-Step](https://www.youtube.com/watch?v=5990oFzJUa4) (por Liam Bonasuki / Master Labs Systems).
* **Guía base adicional:** [Step-by-step JWT Login Guide](https://youtu.be/nwqQYCM4YT8).

---

## Tecnologías Utilizadas

- **Java:** 17 / 21
- **Spring Boot:** 3.1.0
- **Spring Security:** 6
- **Base de datos:** MariaDB / MySQL (Spring Data JPA)
- **JWT:** JJWT (`io.jsonwebtoken` 0.11.5)
- **Lombok:** 1.18.32 (compatible con JDK 21)

---

## Cambios y Mejoras Aplicadas

1. **DTO de Respuesta Enriquecido (`AuthResponse`):**
   * Retorna tanto `message` como `token` (e.g. `{"message": "Login successful", "token": "ey..."}`).
2. **Expiración de Token JWT:**
   * Actualizada a **1 hora** (`1000 * 60 * 60` ms = 3,600,000 ms), conforme a lo explicado en el video tutorial.
3. **Endpoints y Convenciones REST:**
   * **Login:** `POST /api/auth/login` (y `/auth/login`).
   * **Registro:** `POST /api/auth/register` (y `/auth/register`).
   * **Endpoint Protegido:** `GET /api/demo` (y `/api/v1/demo`), que retorna `"You accessed our protected endpoint"`.
4. **Seguridad Stateless:**
   * Sesión sin estado (`SessionCreationPolicy.STATELESS`), CSRF deshabilitado y filtro `JwtAuthenticationFilter` registrado antes de `UsernamePasswordAuthenticationFilter`.
5. **Compatibilidad:**
   * Soporte y corrección para compilación en OpenJDK 21.

---

## Instalación y Ejecución

### 1. Requisitos previos
* JDK 17 o superior (probado con OpenJDK 21).
* MariaDB o MySQL en ejecución en el puerto 3306 con una base de datos `securitydb`:
  ```sql
  CREATE DATABASE IF NOT EXISTS securitydb;
  ```
  *(Las credenciales configuradas en `application.properties` son `root` / `1234`)*.

### 2. Compilar y Ejecutar Tests
```bash
./mvnw clean test
```

### 3. Ejecutar la Aplicación
```bash
./mvnw spring-boot:run
```
La aplicación iniciará en `http://localhost:8080`.

---

## Guía de Pruebas con cURL / Postman

### 1. Registrar un nuevo usuario
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario_demo",
    "password": "password123",
    "firstname": "Juan",
    "lastname": "Perez",
    "country": "Argentina"
  }'
```
**Respuesta esperada (HTTP 200 OK):**
```json
{
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Iniciar Sesión (Login)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario_demo",
    "password": "password123"
  }'
```
**Respuesta esperada (HTTP 200 OK):**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Acceder al Endpoint Protegido sin Token
```bash
curl -i -X GET http://localhost:8080/api/demo
```
**Respuesta esperada:**
`HTTP/1.1 403 Forbidden`

### 4. Acceder al Endpoint Protegido con Token JWT
```bash
curl -X GET http://localhost:8080/api/demo \
  -H "Authorization: Bearer <TU_TOKEN_AQUI>"
```
**Respuesta esperada (HTTP 200 OK):**
```text
You accessed our protected endpoint
```

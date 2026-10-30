# Etapa 1: Construcción (Build)
# imagen oficial de Maven con Java 21 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiamos primero el pom.xml y descargamos las dependencias
# Esto permite aprovechar el caché de Docker si no hay cambios en las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos el proyecto, omitiendo los tests
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
# Utilizamos una imagen JRE liviana de Java 21 para correr la aplicación
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Exponemos el puerto estándar de Spring Boot
EXPOSE 8080

# Copiamos el .jar generado desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicio de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
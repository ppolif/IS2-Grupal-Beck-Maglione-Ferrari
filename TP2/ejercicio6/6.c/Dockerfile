# ==========================================
# Etapa 1: Compilación (Build Stage)
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Aprovechamiento de la caché de capas de Docker para dependencias Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y empaquetar el JAR (omitiendo tests para agilizar el build del contenedor)
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Imagen Ligera de Producción (Runtime Stage)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Crear usuario y grupo sin privilegios por motivos de seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Parámetros óptimos para ejecución de la JVM en contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom" \
    SPRING_PROFILES_ACTIVE="prod"

# Copiar el JAR generado desde la etapa builder
COPY --from=builder /build/target/zero-*.jar app.jar

# Configurar permisos para el usuario no privilegiado
RUN chown -R appuser:appgroup /app

USER appuser:appgroup

# Exponer el puerto por defecto de la aplicación
EXPOSE 8080

# Ejecutar la aplicación propagando señales del sistema (SIGTERM)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

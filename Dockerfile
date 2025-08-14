# ================================
# Stage 1: Build the application
# ================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

# ================================
# Stage 2: Run the application
# ================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR
COPY --from=builder /app/target/*.jar app.jar

# Copiar carpeta upload (vacía o con contenido inicial)
COPY upload ./upload

# Crear carpeta persistente si no existe
RUN mkdir -p /data && mkdir -p /app/upload && chmod -R 775 /app/upload /data

# Usar /data si existe (Render lo monta automáticamente)
ENV APP_UPLOAD_DIR=/data

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

CMD ["sh", "-c", "ls -R /app && sleep 3600"]


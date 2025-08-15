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

# Usar /data si existe (Render lo monta automáticamente)
ENV APP_UPLOAD_DIR=/data

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


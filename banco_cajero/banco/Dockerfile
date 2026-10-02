# Etapa 1: Compilación con Maven oficial
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copiar código fuente y compilar JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final ligera
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

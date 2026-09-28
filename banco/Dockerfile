# --- Etapa 1: Compilación de Java ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar el archivo de configuración de Maven
COPY pom.xml .

# Descargar las dependencias necesarias en caché
RUN mvn dependency:go-offline -B

# Copiar el código fuente de tu app Java
COPY src ./src

# Compilar y generar el archivo .jar (omitiendo tests para acelerar el pipeline)
RUN mvn package -DskipTests

# --- Etapa 2: Imagen final ligera de ejecución ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de este microservicio (8090)
EXPOSE 8090

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

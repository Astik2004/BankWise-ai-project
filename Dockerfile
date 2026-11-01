# -------------------------------------------------------------
# Stage 1: Build the Spring Boot application using Maven & Java 21
# -------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and POM first for layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Pre-fetch dependencies
RUN ./mvnw dependency:go-offline -B

# Copy application source code and package JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# -------------------------------------------------------------
# Stage 2: Minimal JRE 21 runtime container
# -------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy built artifact from build stage
COPY --from=build /app/target/bankwise-*.jar app.jar

# Render injects PORT environment variable dynamically
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -Dspring.profiles.active=prod -jar app.jar"]

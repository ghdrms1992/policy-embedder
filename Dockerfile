# Stage 1: Build the application with Gradle
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source code
COPY src ./src

# Grant execution rights and build the application
RUN chmod +x ./gradlew && ./gradlew bootJar

# Stage 2: Create the final, smaller runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

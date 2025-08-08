# Use official OpenJDK 17 slim base image
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Expose port 8080 for Spring Boot
EXPOSE 8080

# Copy the built JAR file
COPY build/libs/ride-management.jar app.jar

# Optional: Copy external config (only if needed)
COPY src/main/resources/application.properties /app/application.properties

# Optional: Create a volume
VOLUME /tmp

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]

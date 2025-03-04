# Use OpenJDK as base image
FROM eclipse-temurin:22-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file (After building with Maven)
COPY target/delivery-metrics-kstream-1.0-SNAPSHOT.jar kafka-streams-app.jar

# Expose HTTP port for Prometheus Actuator metrics
EXPOSE 8080

# Command to run the Kafka Streams app
CMD ["java", "-jar", "kafka-streams-app.jar"]

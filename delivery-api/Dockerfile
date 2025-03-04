# Use OpenJDK as base image
FROM eclipse-temurin:22-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file (After building with Maven)
COPY target/delivery-api-1.0-SNAPSHOT.jar delivery-api.jar

# Set environment variables for Kafka connection
ENV SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Expose API port
EXPOSE 8081

# Run the API
CMD ["java", "-jar", "delivery-api.jar"]

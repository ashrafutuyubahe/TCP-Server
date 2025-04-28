# Use OpenJDK 17 as the base image
FROM openjdk:22-jdk-slim

# Set the working directory inside the container
WORKDIR /usr/app

# Copy the JAR file from the local target folder into the container
COPY ./target/cl900-1.0.6.jar app.jar

# Expose the port (only needed if your app runs on a specific port, like Spring Boot)
#EXPOSE 9000

# Run the JAR file using Java
ENTRYPOINT ["java", "-jar", "app.jar"]
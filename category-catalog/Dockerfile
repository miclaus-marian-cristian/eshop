# Use an official Maven image to build the application
FROM maven:3.8.6-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml .
COPY src ./src

# Package the application, skipping tests
RUN mvn clean package -DskipTests

# Use an official OpenJDK runtime image
FROM openjdk:17-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

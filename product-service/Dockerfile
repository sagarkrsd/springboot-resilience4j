# Use an OpenJDK 17 image as base
FROM openjdk:17-alpine

RUN apk update && apk add bash

# Set the working directory in the container
WORKDIR /app

# Copy the Maven executable JAR file and the POM file into the container
COPY target/*.jar /app/app.jar
COPY pom.xml /app/pom.xml

# Expose the port the app runs on
EXPOSE 9191

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]

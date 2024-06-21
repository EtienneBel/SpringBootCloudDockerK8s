# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/*.jar

# Add the application's jar to the container
COPY ${JAR_FILE} configserver.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "/configserver.jar"]
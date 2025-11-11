# Use a lightweight, official Java 21 image as our base
FROM openjdk:21-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files first (for caching)
COPY mvnw .
COPY .mvn .mvn

# Copy the "recipe" (pom.xml)
COPY pom.xml .

# Copy the rest of our source code
COPY src src

# Build the app using the Maven wrapper, skipping tests
RUN ./mvnw package -DskipTests

# Tell Render what port our app will run on
EXPOSE 8080

# The command to run when the container starts
CMD ["java", "-jar", "target/Perfume_e-commerce-0.0.1-SNAPSHOT.jar"]
# Builder stage
FROM gradle:8.4-jdk17-alpine AS builder

WORKDIR /app
COPY . .

# Install bash and curl in the builder stage
RUN apk add --no-cache bash curl

# Note: Skipping tests is generally not recommended except for diagnostic purposes.
RUN gradle build --no-daemon -x test  # Build the application
# RUN gradle build --no-daemon  # Uncomment this line if you want to include tests during the build

# Final stage
FROM openjdk:17-alpine

WORKDIR /app

# Install bash and curl in the final stage
RUN apk add --no-cache bash curl

# Copy the built JAR from the builder stage and rename it for simplicity
COPY --from=builder /app/build/libs/*0.0.1-SNAPSHOT.jar app.jar

# Copy the service.sh script into the container
COPY create-workspace.sh /app/create-workspace.sh

# Make the service.sh script executable
RUN chmod +x /app/create-workspace.sh

EXPOSE 8085

# Keep the existing ENTRYPOINT
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

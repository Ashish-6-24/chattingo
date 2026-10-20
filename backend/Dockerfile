
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app


COPY pom.xml .

# Download dependencies
RUN mvn  dependency:go-offline

# Copy application source code
COPY src ./src

# Build the executable JAR
RUN mvn -B -ntp -DskipTests package


FROM eclipse-temurin:17-jre-alpine AS runtime

# Create a non-root application user
RUN addgroup -S app-group && adduser -S app-user -G app-group

# Copy the JAR from the build stage
COPY --from=builder --chown=appuser:appgroup \
    /app/target/chattingo-0.0.1-SNAPSHOT.jar \
    /app/app.jar

# Run without root privileges
USER app-user

# Spring Boot application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
	

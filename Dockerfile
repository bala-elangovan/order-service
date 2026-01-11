# Multi-stage Dockerfile for production-ready builds
# Supports building different modules via MODULE build arg

# Stage 1: Build
FROM gradle:8.11-jdk21-alpine AS build

# Module to build (orders-app or orders-consumer)
ARG MODULE=orders-app

WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy all module build files
COPY orders/build.gradle.kts ./orders/
COPY orders/orders-domain/build.gradle.kts ./orders/orders-domain/
COPY orders/orders-infra/build.gradle.kts ./orders/orders-infra/
COPY orders/orders-app/build.gradle.kts ./orders/orders-app/
COPY orders/orders-consumer/build.gradle.kts ./orders/orders-consumer/

# Download dependencies (this layer will be cached)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY orders ./orders

# Build application
RUN gradle :orders:${MODULE}:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

ARG MODULE=orders-app

LABEL maintainer="platform@github.io"
LABEL description="Order Management Service - ${MODULE}"
LABEL version="1.0.0"

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage
COPY --from=build /app/orders/${MODULE}/build/libs/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port (8080 for API, 8081 for Consumer)
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

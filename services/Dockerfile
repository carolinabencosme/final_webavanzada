# Build any microservice from the multi-module parent. Usage:
#   docker build --build-arg MODULE=auth-service -f Dockerfile .
ARG MODULE=discovery-service

FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /build
COPY . .
RUN mvn -q -pl "${MODULE}" -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG MODULE
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=build /build/${MODULE}/target/${MODULE}-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

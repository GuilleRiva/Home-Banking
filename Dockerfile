# Imagen base con JDK 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app

ARG JAR_FILE=target/*.jar

COPY --chown=spring:spring ${JAR_FILE} app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
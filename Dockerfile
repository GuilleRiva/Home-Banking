# =========================
# Stage 1: Build
# =========================

FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B


# =========================
# Stage 2: Runtime
# =========================

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring \
    && adduser -S spring -G spring \
    && mkdir -p /app/logs \
    && chown -R spring:spring /app

COPY --from=build /build/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
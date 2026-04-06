# Stage 1: Build
FROM mirror.gcr.io/gradle:8.13-jdk21 AS build

WORKDIR /app

COPY gradle ./gradle
COPY gradlew gradlew.bat ./
COPY build.gradle.kts settings.gradle.kts ./

COPY shared ./shared
COPY apps/backend ./apps/backend

RUN ./gradlew :apps:backend:bootJar --no-daemon -q

# Stage 2: Run
FROM mirror.gcr.io/eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/apps/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

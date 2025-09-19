FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=default
COPY --from=builder /app/build/libs/prompt-lab-backend-0.0.1-SNAPSHOT.jar app.jar
COPY credentials ./credentials
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

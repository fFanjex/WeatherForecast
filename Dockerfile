FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system secureauthx && adduser --system --ingroup secureauthx secureauthx

COPY --from=build /workspace/build/libs/*.jar /app/secureauthx-server.jar

USER secureauthx
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/secureauthx-server.jar"]

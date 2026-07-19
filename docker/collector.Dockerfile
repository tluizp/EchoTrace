FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace
COPY . .
RUN ./gradlew :echotrace-collector:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 echotrace \
    && useradd --uid 10001 --gid echotrace --create-home echotrace

WORKDIR /app
COPY --from=builder --chown=echotrace:echotrace \
    /workspace/echotrace-collector/build/libs/echotrace-collector-1.0.0.jar app.jar

USER echotrace
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

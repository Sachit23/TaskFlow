# syntax=docker/dockerfile:1
# The three service images share one Maven build and dependency cache.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY backend/ .
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && groupadd --gid 10001 taskflow && useradd --uid 10001 --gid taskflow --no-create-home taskflow
WORKDIR /app
ARG SERVICE
COPY --from=build --chown=taskflow:taskflow /build/${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar /app/app.jar
USER taskflow
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java","-jar","/app/app.jar"]

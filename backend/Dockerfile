# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY backend/ .
ARG SERVICE
RUN --mount=type=cache,target=/root/.m2 mvn -B -pl "${SERVICE}" -am -Dmaven.test.skip=true package && cp "${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar" /app.jar
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && groupadd --gid 10001 taskflow && useradd --uid 10001 --gid taskflow --no-create-home taskflow
WORKDIR /app
COPY --from=build --chown=taskflow:taskflow /app.jar /app/app.jar
USER taskflow
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java","-jar","/app/app.jar"]

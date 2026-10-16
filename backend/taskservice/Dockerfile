# Build from repository root: docker build -f backend/taskservice/Dockerfile .
# Compose uses the shared backend/Dockerfile so every build resolves its parent POM.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY backend/ .
RUN mvn -B -pl taskservice -am -Dmaven.test.skip=true package
FROM eclipse-temurin:17-jre-jammy
RUN groupadd --gid 10001 taskflow && useradd --uid 10001 --gid taskflow --no-create-home taskflow
WORKDIR /app
COPY --from=build --chown=taskflow:taskflow /build/taskservice/target/taskservice-1.0.0-SNAPSHOT.jar /app/app.jar
USER taskflow
EXPOSE 8081
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","/app/app.jar"]

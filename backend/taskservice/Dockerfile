FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/taskservice-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

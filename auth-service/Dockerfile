FROM eclipse-temurin:21-jdk

RUN useradd -m -u 1001 appuser

WORKDIR /app

COPY target/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]

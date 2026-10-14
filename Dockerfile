FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 10001 appuser \
    && mkdir -p /app/data/documents \
    && chown -R appuser:appuser /app
COPY --from=build /workspace/target/*.jar /app/app.jar
USER 10001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=300s --retries=3 \
  CMD curl --fail --silent http://127.0.0.1:${PORT:-8080}/actuator/health/liveness >/dev/null || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Djava.io.tmpdir=/tmp","-jar","/app/app.jar"]

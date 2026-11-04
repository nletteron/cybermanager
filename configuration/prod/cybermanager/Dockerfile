# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY backend/ backend/
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -f backend/cybermanager-parent/pom.xml -pl :cm-api -am -Dmaven.test.skip=true package

FROM docker:cli AS docker-cli

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache netcat-openbsd && \
    addgroup -g 10001 application && adduser -D -u 10001 -G application application
WORKDIR /app
COPY --from=docker-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=build --chown=application:application /workspace/backend/cybermanager/cm-api/target/cm-api-*.jar application.jar
ENV CM_DB_MODE=flyway \
    SPRING_CONFIG_LOCATION=file:/etc/cybermanager/cybermanager/ \
    CM_FLYWAY_LOCATIONS=filesystem:/etc/cybermanager/cybermanager/db/migration \
    CM_ERROR_CATALOG_FILE=file:/etc/cybermanager/cybermanager/error.json \
    CM_DATABASE_DUMP_DIR=/var/lib/postgresql/dumps \
    CM_DATABASE_BACKUP_CONTAINER=cybermanager-prod-db
USER application
EXPOSE 8082
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 CMD ["nc", "-z", "127.0.0.1", "8082"]
ENTRYPOINT ["java", "-jar", "/app/application.jar"]

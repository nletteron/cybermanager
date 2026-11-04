# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY backend/ backend/
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -f backend/cybermanager-parent/pom.xml -pl :cm-auth-api -am -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache netcat-openbsd && \
    addgroup -g 10001 application && adduser -D -u 10001 -G application application
WORKDIR /app
COPY --from=build --chown=application:application /workspace/backend/auth/cm-auth-api/target/cm-auth-api-*.jar application.jar
ENV CM_DB_MODE=flyway \
    SPRING_CONFIG_LOCATION=file:/etc/cybermanager/auth/ \
    CM_FLYWAY_LOCATIONS=filesystem:/etc/cybermanager/auth/db/migration
USER application
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 CMD ["nc", "-z", "127.0.0.1", "8081"]
ENTRYPOINT ["java", "-jar", "/app/application.jar"]

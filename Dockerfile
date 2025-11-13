# Build stage
FROM gradle:8.10.1-jdk17-focal AS builder
WORKDIR /build-workspace

COPY build.gradle .
COPY settings.gradle .
COPY src/ src/
COPY expressions-parser/src/ expressions-parser/src/
COPY expressions-parser/build.gradle expressions-parser/
COPY query-language/src/ query-language/src/
COPY query-language/build.gradle query-language/

RUN gradle --no-daemon clean bootJar

# Runtime stage
FROM eclipse-temurin:17-jre-noble AS runtime
WORKDIR /app

RUN adduser -u 1001 --disabled-password --gecos "" appuser && \
    mkdir -p /app/data && \
    chown -R appuser:appuser /app

COPY --from=builder --chown=appuser:appuser /build-workspace/build/libs/ai-dial-admin-backend*.jar ./app.jar

COPY --chown=appuser:appuser docker-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

USER appuser

EXPOSE 8080 9464

HEALTHCHECK --start-period=30s --interval=1m --timeout=3s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["docker-entrypoint.sh"]


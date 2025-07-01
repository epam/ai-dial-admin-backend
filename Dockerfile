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
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN adduser -u 1001 --disabled-password --gecos "" appuser && \
    chown appuser:appuser /app

RUN mkdir -p /app/data && \
    chown -R appuser:appuser /app/data

COPY --from=builder --chown=appuser:appuser /build-workspace/build/libs/ai-dial-admin-backend*.jar ./app.jar

USER appuser

EXPOSE 8080 9464

ENV DEBUG_OPTS=""

HEALTHCHECK --start-period=30s --interval=1m --timeout=3s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${DEBUG_OPTS} -jar app.jar"]

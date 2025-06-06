FROM eclipse-temurin:17-jdk-alpine AS runtime

WORKDIR /app
EXPOSE 8080 9464
# SDK
FROM gradle:8.10.1-jdk17-focal AS sdk
WORKDIR /build-workspace
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
COPY src/ src/
COPY expressions-parser/src/ expressions-parser/src/
COPY expressions-parser/build.gradle expressions-parser/
COPY query-language/src/ query-language/src/
COPY query-language/build.gradle query-language/

# Build.
FROM sdk AS build
WORKDIR /build-workspace
RUN gradle --no-daemon clean bootJar

# Final image.
FROM runtime AS final

RUN adduser -u 1001 --disabled-password --gecos "" appuser
COPY --chown=appuser --from=runtime /app .

COPY --from=build /build-workspace/build/libs/ai-dial-admin-backend*.jar ./app.jar
ENV DEBUG_OPTS=""
USER appuser
ENTRYPOINT ["sh", "-c", "java ${DEBUG_OPTS} -jar app.jar"]

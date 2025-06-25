package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.InvalidVersionException;
import com.epam.aidial.cfg.exception.SchemaValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class VersionedSchemaLoader {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final String SCHEMA_PATH_FORMAT = "core-config-schemas/schema-v%s.json";
    private static final String LATEST_VERSION = "latest";

    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> schemaCache = new ConcurrentHashMap<>();
    private final TreeSet<String> knownVersions = new TreeSet<>(VersionedSchemaLoader::compareVersionsDescending);
    private volatile String cachedLatestVersion;

    @PostConstruct
    public void init() {
        scanAvailableVersions();
        log.info("Preloaded {} schema versions at startup", knownVersions.size());
    }

    public JsonNode loadSchema(String version) {
        validateVersionFormat(version);
        if (LATEST_VERSION.equals(version)) {
            version = findLatestVersion();
        }
        return schemaCache.computeIfAbsent(version, this::loadSchemaFromFile);
    }

    private JsonNode loadSchemaFromFile(String version) {
        try {
            String nearestVersion = findNearestAvailableVersion(version);
            var resource = getSchemaResource(nearestVersion);
            try (var inputStream = resource.getInputStream()) {
                JsonNode schema = objectMapper.readTree(inputStream);
                if (!nearestVersion.equals(version)) {
                    log.info("Schema for version {} not found, using nearest available version: {}", version, nearestVersion);
                } else {
                    log.info("Loaded schema for version: {}", version);
                }
                knownVersions.add(nearestVersion);
                return schema;
            }
        } catch (IOException e) {
            String errorMessage = "Failed to load schema for version: %s".formatted(version);
            log.error(errorMessage, e);
            throw new SchemaValidationException(errorMessage, e);
        }
    }

    protected String findNearestAvailableVersion(String requestedVersion) {
        if (knownVersions.contains(requestedVersion)) {
            return requestedVersion;
        }

        return knownVersions.stream()
            .filter(version -> isCompatibleVersion(version, requestedVersion))
            .findFirst()
            .orElseThrow(() -> new InvalidVersionException("No compatible schema found for version: " + requestedVersion
                + ". Available schemas should be placed in 'core-config-schemas'"));
    }

    private boolean isCompatibleVersion(String candidateVersion, String requestedVersion) {
        String[] candidateParts = candidateVersion.split("\\.");
        String[] requestedParts = requestedVersion.split("\\.");

        int candidateMajor = Integer.parseInt(candidateParts[0]);
        int requestedMajor = Integer.parseInt(requestedParts[0]);

        return candidateMajor <= requestedMajor && candidateVersion.compareTo(requestedVersion) <= 0;
    }

    private String findLatestVersion() {
        if (cachedLatestVersion != null) {
            return cachedLatestVersion;
        }

        cachedLatestVersion = knownVersions.stream()
            .filter(version -> version.matches(VERSION_PATTERN.pattern()))
            .findFirst()
            .orElseThrow(() -> new InvalidVersionException("No schemas available"));

        log.debug("Latest schema version found: {}", cachedLatestVersion);
        return cachedLatestVersion;
    }

    private static int compareVersionsDescending(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);

            if (num1 != num2) {
                return Integer.compare(num2, num1);
            }
        }

        return 0;
    }

    private void scanAvailableVersions() {
        log.debug("Scanning available schema versions");
        cachedLatestVersion = null;

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:core-config-schemas/schema-v*.json");

            for (Resource resource : resources) {
                String filename = resource.getFilename();

                // Extract version from filename (schema-vX.Y.Z.json -> X.Y.Z)
                if (filename.startsWith("schema-v") && filename.endsWith(".json")) {
                    String version = filename.substring(8, filename.length() - 5);
                    if (VERSION_PATTERN.matcher(version).matches()) {
                        knownVersions.add(version);
                        log.trace("Found schema version: {}", version);
                    }
                }
            }

            log.debug("Preloaded {} schema versions", knownVersions.size());
        } catch (IOException e) {
            log.error("Failed to scan available schema versions", e);
            throw new SchemaValidationException("Failed to scan available schema versions", e);
        }
    }

    private static void validateVersionFormat(String version) {
        if (!VERSION_PATTERN.matcher(version).matches() && !LATEST_VERSION.equals(version)) {
            throw new InvalidVersionException("Invalid version format: " + version
                + ". Expected format: X.Y.Z (e.g., '0.23.0', '1.0.1', '2.0.3')) or 'latest'");
        }
    }

    private static ClassPathResource getSchemaResource(String version) {
        final String schemaPath = SCHEMA_PATH_FORMAT.formatted(version);
        return new ClassPathResource(schemaPath);
    }
}
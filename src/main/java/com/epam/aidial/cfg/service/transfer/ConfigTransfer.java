package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.configuration.ConfigExportProperties;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ConfigMapper;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.ExportConfigPreview;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.transfer.exporter.ConfigExporter;
import com.epam.aidial.cfg.service.transfer.importer.AddonImporter;
import com.epam.aidial.cfg.service.transfer.importer.ApplicationImporter;
import com.epam.aidial.cfg.service.transfer.importer.ApplicationTypeSchemaImporter;
import com.epam.aidial.cfg.service.transfer.importer.AssistantImporter;
import com.epam.aidial.cfg.service.transfer.importer.InterceptorImporter;
import com.epam.aidial.cfg.service.transfer.importer.KeyImporter;
import com.epam.aidial.cfg.service.transfer.importer.ModelImporter;
import com.epam.aidial.cfg.service.transfer.importer.RoleImporter;
import com.epam.aidial.cfg.service.transfer.importer.RouteImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class ConfigTransfer {

    private final ConfigExporter configExporter;
    private final ConfigMapper configMapper;
    private final ObjectMapper jsonMapper = JsonMapperConfiguration.createJsonMapper();
    private final ConfigExportProperties properties;

    private final ModelImporter modelImporter;
    private final AddonImporter addonTransfer;
    private final ApplicationImporter applicationImporter;
    private final KeyImporter keyImporter;
    private final RoleImporter roleImporter;
    private final InterceptorImporter interceptorImporter;
    private final ApplicationTypeSchemaImporter applicationTypeSchemaImporter;
    private final RouteImporter routeImporter;
    private final AssistantImporter assistantImporter;

    @Transactional(readOnly = true)
    public StreamingResponseBody exportConfig(ExportRequest request) {
        ExportFormat exportFormat = request.getExportFormat();
        ExportConfig config = configExporter.getConfig(request);
        return switch (exportFormat) {
            case CORE -> exportCoreConfig(config);
            case ADMIN -> exportAdminConfig(config);
        };
    }

    private StreamingResponseBody exportCoreConfig(ExportConfig config) {
        Config coreConfig = configMapper.toCoreConfig(config);
        return outputStream -> {
            try {
                jsonMapper.writeValue(outputStream, coreConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private StreamingResponseBody exportAdminConfig(ExportConfig config) {
        return outputStream -> {
            try (var zos = new ZipOutputStream(outputStream)) {
                zos.putNextEntry(new ZipEntry(properties.getExportConfigFileName()));
                zos.write(jsonMapper.writeValueAsString(config).getBytes());
                zos.closeEntry();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Transactional(readOnly = true)
    public ExportConfigPreview exportPreview(ExportRequest request) {
        return configExporter.preview(request);
    }

    @Transactional(readOnly = true)
    public ImportConfigPreview importPreview(List<MultipartFile> files,
                                             ConflictResolutionPolicy resolutionPolicy) {
        try {
            Config config = readAndMergeConfig(files);
            var roles = roleImporter.preview(config.getRoles(), resolutionPolicy);
            var keys = keyImporter.importKeys(config.getKeys(), resolutionPolicy, true);
            var interceptors = interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy, true);
            var applicationRunners = applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy, true);
            ConfigImportOptions importOptions = createConfigImportOptions(false, resolutionPolicy);
            var models = modelImporter.importModels(config.getModels(), config.getRoles(), importOptions, true);
            var addons = addonTransfer.importAddons(config.getAddons(), config.getRoles(), importOptions, true);
            var applications = applicationImporter.importApplications(config.getApplications(), config.getRoles(), importOptions, true);
            var routes = routeImporter.importRoutes(config.getRoutes(), config.getRoles(), importOptions, true);
            var assistants = assistantImporter.importAssistants(config.getAssistant(), config.getRoles(), importOptions, true);
            return ImportConfigPreview.builder()
                    .roles(roles)
                    .keys(keys)
                    .interceptors(interceptors)
                    .applicationRunners(applicationRunners)
                    .routes(routes)
                    .models(models)
                    .applications(applications)
                    .addons(addons)
                    .assistants(assistants)
                    .build();
        } catch (Exception exception) {
            log.warn("Failed to import config. Conflict resolution policy: {}. Error: {}", resolutionPolicy, exception);
            throw exception;
        }

    }

    @Transactional(readOnly = true)
    public ImportConfigPreview importPreviewZip(MultipartFile zipFile,
                                                ConflictResolutionPolicy resolutionPolicy) {
        try (var zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFile.getBytes()))) {
            ZipEntry zipEntry;
            ImportConfigPreview importConfigPreview = null;
            int validEntryCount = 0;
            var exportConfigFileName = properties.getExportConfigFileName();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(exportConfigFileName)) {
                    validEntryCount++;
                    if (validEntryCount > 1) {
                        throw new IllegalArgumentException("Multiple files {" + exportConfigFileName + "} with data found in the ZIP archive.");
                    }
                    ExportConfig config = jsonMapper.readValue(zipInputStream, ExportConfig.class);
                    importConfigPreview = importPreviewAdminConfig(config, resolutionPolicy);
                } else {
                    log.info("Ignoring file {} in zip archive during import", zipEntry.getName());
                }
                zipInputStream.closeEntry();
                if (validEntryCount == 0) {
                    throw new IllegalArgumentException("No valid export configuration file " + exportConfigFileName + " found in the ZIP archive.");
                }
            }
            return importConfigPreview;
        } catch (Exception ex) {
            log.debug("Config file {} import failed", zipFile.getOriginalFilename(), ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private ImportConfigPreview importPreviewAdminConfig(ExportConfig config, ConflictResolutionPolicy resolutionPolicy) {
        var interceptors = interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy, true);
        var applicationRunners = applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy, true);
        ConfigImportOptions importOptions = createConfigImportOptions(false, resolutionPolicy);
        var routes = routeImporter.importAdminRoutes(config.getRoutes(), importOptions, true);
        var models = modelImporter.importAdminModels(config.getModels(), importOptions, true);
        var applications = applicationImporter.importAdminApplications(config.getApplications(), importOptions, true);
        var roles = roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy, true);
        var keys = keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy, true);

        return ImportConfigPreview.builder()
                .roles(roles)
                .keys(keys)
                .interceptors(interceptors)
                .applicationRunners(applicationRunners)
                .routes(routes)
                .models(models)
                .applications(applications)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void importConfig(List<MultipartFile> files,
                             ConflictResolutionPolicy resolutionPolicy,
                             boolean createRoleIfAbsent) {
        try {
            Config config = readAndMergeConfig(files);
            Set<String> deploymentNamesInConfig = getDeploymentNamesInConfig(config);
            Map<String, CoreRole> importedRoles = roleImporter.importRoles(deploymentNamesInConfig, config.getRoles(), resolutionPolicy);
            keyImporter.importKeys(config.getKeys(), resolutionPolicy, false);
            interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy, false);
            applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy, false);
            ConfigImportOptions importOptions = createConfigImportOptions(createRoleIfAbsent, resolutionPolicy);
            modelImporter.importModels(config.getModels(), config.getRoles(), importOptions, false);
            addonTransfer.importAddons(config.getAddons(), config.getRoles(), importOptions, false);
            applicationImporter.importApplications(config.getApplications(), config.getRoles(), importOptions, false);
            routeImporter.importRoutes(config.getRoutes(), config.getRoles(), importOptions, false);
            assistantImporter.importAssistants(config.getAssistant(), config.getRoles(), importOptions, false);
            roleImporter.importDefaultLimitsForExistingDeployments(importedRoles, deploymentNamesInConfig);
        } catch (Exception exception) {
            log.warn("Failed to import config. Conflict resolution policy: {}. Error: {}", resolutionPolicy, exception);
            throw exception;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void importConfigZip(MultipartFile zipFile,
                                ConflictResolutionPolicy resolutionPolicy,
                                boolean createRoleIfAbsent) {
        try (var zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFile.getBytes()))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(properties.getExportConfigFileName())) {
                    ExportConfig config = jsonMapper.readValue(zipInputStream, ExportConfig.class);
                    importAdminConfig(config, resolutionPolicy, createRoleIfAbsent);
                } else {
                    log.info("Ignoring file {} in zip archive during import", zipEntry.getName());
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception ex) {
            log.debug("Config file {} import failed", zipFile.getOriginalFilename(), ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private void importAdminConfig(ExportConfig config,
                                   ConflictResolutionPolicy resolutionPolicy,
                                   boolean createRoleIfAbsent) {
        interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy, false);
        applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy, false);
        ConfigImportOptions importOptions = createConfigImportOptions(createRoleIfAbsent, resolutionPolicy);
        routeImporter.importAdminRoutes(config.getRoutes(), importOptions, false);
        modelImporter.importAdminModels(config.getModels(), importOptions, false);
        applicationImporter.importAdminApplications(config.getApplications(), importOptions, false);
        roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy, false);
        keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy, false);
    }

    private ConfigImportOptions createConfigImportOptions(boolean createRoleIfAbsent, ConflictResolutionPolicy resolutionPolicy) {
        return new ConfigImportOptions(resolutionPolicy, createRoleIfAbsent);
    }

    private Config readAndMergeConfig(List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            throw new IllegalArgumentException("The files cannot be empty. Please provide at least one configuration file.");
        }
        JsonNode tree = jsonMapper.createObjectNode();

        for (MultipartFile file : files) {
            try (InputStream stream = file.getInputStream()) {
                tree = jsonMapper.readerForUpdating(tree).readTree(stream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + file.getOriginalFilename(), e);
            }
        }

        return jsonMapper.convertValue(tree, Config.class);
    }

    private Set<String> getDeploymentNamesInConfig(Config config) {
        return Stream.of(config.getModels().keySet(), config.getAddons().keySet(),
                        config.getApplications().keySet(), config.getRoutes().keySet(),
                        config.getAssistant().getAssistants().keySet())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

}

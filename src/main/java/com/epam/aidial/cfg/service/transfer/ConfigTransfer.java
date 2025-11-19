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
import com.epam.aidial.cfg.service.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class ConfigTransfer {

    private final ConfigExporter configExporter;
    private final CoreConfigRetriever coreConfigRetriever;
    private final ConfigMapper configMapper;
    private final ObjectMapper jsonMapper = JsonMapperConfiguration.createJsonMapper();
    private final ObjectMapper prettyJsonMapper = JsonMapperConfiguration.createPrettyJsonMapper();
    private final ConfigExportProperties properties;
    private final VersionAwareFieldFilter versionAwareFieldFilter;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public StreamingResponseBody exportConfig(ExportRequest request) {
        ExportFormat exportFormat = request.getExportFormat();
        ExportConfig config = configExporter.getConfig(request);
        return switch (exportFormat) {
            case CORE -> exportCoreConfig(config);
            case ADMIN -> exportAdminConfig(config);
        };
    }

    public StreamingResponseBody exportRawConfig(boolean addSecrets) {
        var rawConfig = coreConfigRetriever.getRawConfig(addSecrets);
        return outputStream -> {
            try (var zos = new ZipOutputStream(outputStream)) {
                for (var config : normalizeZipFileNames(rawConfig.configs()).entrySet()) {
                    zos.putNextEntry(new ZipEntry("config/" + config.getKey()));
                    zos.write(config.getValue().getBytes());
                }
                for (var config : normalizeZipFileNames(rawConfig.secrets()).entrySet()) {
                    zos.putNextEntry(new ZipEntry("secrets/" + config.getKey()));
                    zos.write(config.getValue().getBytes());
                }
                zos.closeEntry();
            } catch (Exception e) {
                log.error("Config file export failed. AddSecrets: {}.", addSecrets, e);
                throw new RuntimeException(e);
            }
        };
    }

    private StreamingResponseBody exportCoreConfig(ExportConfig config) {
        Config fullCoreConfig = configMapper.toCoreConfig(config);
        Config versionedConfig = versionAwareFieldFilter.filterForTargetVersion(fullCoreConfig);
        return outputStream -> {
            try {
                prettyJsonMapper.writeValue(outputStream, versionedConfig);
            } catch (Exception e) {
                throw new RuntimeException("Failed to export core config.", e);
            }
        };
    }

    private StreamingResponseBody exportAdminConfig(ExportConfig config) {
        return outputStream -> {
            try (var zos = new ZipOutputStream(outputStream)) {
                zos.putNextEntry(new ZipEntry(properties.getExportConfigFileName()));
                zos.write(prettyJsonMapper.writeValueAsString(config).getBytes());
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

    public ImportConfigPreview importPreview(List<MultipartFile> files, ConfigImportOptions importOptions) {
        try {
            Config config = readAndMergeConfig(files);
            return configImporter.importPreview(config, importOptions);
        } catch (Exception exception) {
            log.warn("Failed to import config. Config import options: {}. Error: {}", importOptions, exception);
            throw exception;
        }
    }

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
                    importConfigPreview = configImporter.importPreviewAdminConfig(config, resolutionPolicy);
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
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    public void importConfig(List<MultipartFile> files, ConfigImportOptions importOptions) {
        try {
            Config config = readAndMergeConfig(files);
            configImporter.importConfig(config, importOptions);
        } catch (Exception exception) {
            log.warn("Failed to import config. Conflict resolution policy: {}. Error: {}", importOptions.conflictResolutionPolicy(), exception);
            throw exception;
        }
    }

    public void importConfigZip(MultipartFile zipFile,
                                ConfigImportOptions importOptions) {
        try (var zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFile.getBytes()))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(properties.getExportConfigFileName())) {
                    ExportConfig config = jsonMapper.readValue(zipInputStream, ExportConfig.class);
                    configImporter.importAdminConfig(config, importOptions);
                } else {
                    log.info("Ignoring file {} in zip archive during import", zipEntry.getName());
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception ex) {
            log.debug("Config file {} import failed", zipFile.getOriginalFilename(), ex);
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
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

    private Map<String, String> normalizeZipFileNames(Map<String, String> map) {
        var existingNames = new HashSet<String>();

        return map.entrySet().stream()
                .map(entry -> Map.entry(normalizeZipFileName(entry.getKey()), entry.getValue()))
                .collect(Collectors.toMap(entry -> makeNameUnique(entry.getKey(), existingNames), Map.Entry::getValue));
    }

    private String normalizeZipFileName(String fileName) {
        var baseName = FilenameUtils.getBaseName(fileName);
        if (StringUtils.isBlank(baseName)) {
            baseName = UUID.randomUUID().toString();
        }
        var normalizedName = ZipEntryNameNormalizer.normalise(baseName);
        return normalizedName + ".json";
    }

    private String makeNameUnique(String name, Set<String> existingNames) {
        if (!existingNames.contains(name)) {
            existingNames.add(name);
            return name;
        }

        int index = 1;
        while (true) {
            var newName = name + "_v" + index;
            if (!existingNames.contains(newName)) {
                existingNames.add(newName);
                return newName;
            }
            index++;
        }
    }

}

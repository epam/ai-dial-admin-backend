package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.MultiFileImportStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "config.import.auto-import-on-bootstrap")
public class AutoImportOnBootstrapProperties {

    private boolean enabled = false;
    private MultiFileImportStrategy strategy = MultiFileImportStrategy.MERGE_JSON;
    private List<String> filePaths = List.of();
    private ConflictResolutionPolicy conflictResolutionPolicy = ConflictResolutionPolicy.OVERRIDE;
}

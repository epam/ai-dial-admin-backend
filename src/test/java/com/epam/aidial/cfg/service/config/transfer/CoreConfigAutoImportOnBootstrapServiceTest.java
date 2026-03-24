package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.cfg.configuration.AutoImportOnBootstrapProperties;
import com.epam.aidial.cfg.domain.service.DatabaseService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreConfigAutoImportOnBootstrapServiceTest {

    @Mock
    private DatabaseService databaseService;
    @Mock
    private CoreConfigRetriever coreConfigRetriever;
    @Mock
    private ConfigImporter configImporter;
    @Mock
    private CoreConfigAutoImportOnBootstrapLock lock;
    @Mock
    private JsonConfigMerger jsonConfigMerger;
    @Mock
    private AutoImportOnBootstrapProperties properties;
    @InjectMocks
    private CoreConfigAutoImportOnBootstrapService service;

    @Test
    void whenFilePathsEmpty_usesRetriever() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(true);
        when(properties.getFilePaths()).thenReturn(List.of());
        Config config = new Config();
        when(coreConfigRetriever.getConfig(true)).thenReturn(config);

        service.autoImportCoreConfig();

        verify(coreConfigRetriever).getConfig(true);
        verify(configImporter).importConfigWithOverride(config);
        verify(jsonConfigMerger, never()).merge(any());
    }

    @Test
    void whenFilePathsConfigured_mergeJson_usesMerger() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(true);
        List<String> paths = List.of("/data/a.json", "/data/b.json");
        when(properties.getFilePaths()).thenReturn(paths);
        when(properties.getStrategy()).thenReturn(MultiFileImportStrategy.MERGE_JSON);
        Config merged = new Config();
        when(jsonConfigMerger.merge(paths)).thenReturn(merged);

        service.autoImportCoreConfig();

        verify(jsonConfigMerger).merge(paths);
        verify(configImporter).importConfigWithOverride(merged);
        verify(coreConfigRetriever, never()).getConfig(anyBoolean());
    }

    @Test
    void whenFilePathsConfigured_sequential_importsEachFile() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(true);
        List<String> paths = List.of("/data/a.json", "/data/b.json");
        when(properties.getFilePaths()).thenReturn(paths);
        when(properties.getStrategy()).thenReturn(MultiFileImportStrategy.SEQUENTIAL);
        when(properties.getConflictResolutionPolicy()).thenReturn(ConflictResolutionPolicy.OVERRIDE);
        when(jsonConfigMerger.merge(List.of("/data/a.json"))).thenReturn(new Config());
        when(jsonConfigMerger.merge(List.of("/data/b.json"))).thenReturn(new Config());

        service.autoImportCoreConfig();

        verify(jsonConfigMerger).merge(List.of("/data/a.json"));
        verify(jsonConfigMerger).merge(List.of("/data/b.json"));
        verify(configImporter, times(2)).importConfig(any(), any(ConfigImportOptions.class));
        verify(configImporter, never()).importConfigWithOverride(any());
    }

    @Test
    void whenDatabaseNotEmpty_skipsImport() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(false);

        service.autoImportCoreConfig();

        verify(configImporter, never()).importConfigWithOverride(any());
        verify(configImporter, never()).importConfig(any(), any());
    }
}

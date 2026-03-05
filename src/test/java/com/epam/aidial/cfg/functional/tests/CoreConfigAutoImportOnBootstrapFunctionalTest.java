package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see com.epam.aidial.cfg.service.config.transfer.CoreConfigAutoImportOnBootstrapService
 */

@TestPropertySource(properties = {
        "config.import.autoImportOnBootstrap.enabled=true",
})
public abstract class CoreConfigAutoImportOnBootstrapFunctionalTest {

    @Autowired
    private ModelFacade modelFacade;

    @Test
    public void testCoreConfigAutoImportedSuccessfully() {
        Collection<ModelDto> models = modelFacade.getAll();

        assertThat(models).hasSize(1).first().satisfies(modelDto -> {
            assertThat(modelDto.getName()).isEqualTo("testModel");
            assertThat(modelDto.getDisplayName()).isEqualTo("testModel displayName");
        });
    }

    @TestPropertySource(properties = {
            "config.import.autoImportOnBootstrap.enabled=true",
            "config.import.autoImportOnBootstrap.strategy=MERGE_JSON",
            "config.import.autoImportOnBootstrap.filePaths=" +
                    "src/test/resources/import/multifile/config-a.json," +
                    "src/test/resources/import/multifile/config-b.json"
    })
    public abstract static class MergeJsonTests {

        @Autowired
        private ModelFacade modelFacade;

        @Test
        public void testMergeJson_laterFileWins() {
            Collection<ModelDto> models = modelFacade.getAll();
            assertThat(models).extracting(ModelDto::getName)
                    .containsExactlyInAnyOrder("model-a", "model-b", "model-shared");
            assertThat(models).filteredOn(m -> "model-shared".equals(m.getName()))
                    .first().extracting(ModelDto::getDisplayName)
                    .isEqualTo("Shared from B (wins)");
        }
    }

    @TestPropertySource(properties = {
            "config.import.autoImportOnBootstrap.enabled=true",
            "config.import.autoImportOnBootstrap.strategy=SEQUENTIAL",
            "config.import.autoImportOnBootstrap.conflictResolutionPolicy=OVERRIDE",
            "config.import.autoImportOnBootstrap.filePaths=" +
                    "src/test/resources/import/multifile/config-a.json," +
                    "src/test/resources/import/multifile/config-b.json"
    })
    public abstract static class SequentialTests {

        @Autowired
        private ModelFacade modelFacade;

        @Test
        public void testSequential_bothFilesImported() {
            Collection<ModelDto> models = modelFacade.getAll();
            assertThat(models).extracting(ModelDto::getName)
                    .containsExactlyInAnyOrder("model-a", "model-b", "model-shared");
        }
    }
}

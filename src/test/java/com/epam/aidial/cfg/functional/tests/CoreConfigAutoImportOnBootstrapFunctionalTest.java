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
}

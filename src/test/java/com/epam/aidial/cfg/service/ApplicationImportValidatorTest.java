package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationImportValidatorTest {

    private ApplicationImportValidator validator = new ApplicationImportValidator();

    @BeforeEach
    void setUp() {
        validator = new ApplicationImportValidator();
    }

    @Test
    void shouldPassWhenNoDuplicatesInFlatImport() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("2");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(true);

        assertDoesNotThrow(() -> validator.validateApplicationImport(importResources, dto));
    }

    @Test
    void shouldThrowExceptionWhenDuplicatesInFlatImport() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApplicationImport(importResources, dto));

        assertTrue(exception.getMessage().contains("Application uniqueness violation. Conflicts found:Duplicated application name application1 and version  0.0.1"));
    }

    @Test
    void shouldThrowExceptionNonFlatImportEvenIfDuplicatesExist() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApplicationImport(importResources, dto));

        assertTrue(exception.getMessage().contains("Application uniqueness violation. Conflicts found:Duplicated application name application1 and version  0.0.1"));
    }

    @Test
    void shouldPassForNonFlatImportEvenIfDuplicatesExist() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        application2.setFolderId("public/2/");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApplicationImport(importResources, dto));

        assertTrue(exception.getMessage().contains("Application uniqueness violation. Conflicts found:Duplicated application name application1 and version  0.0.1"));
    }

    @Test
    void shouldHandleEmptyApplicationsList() {
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of());
        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(false);

        assertDoesNotThrow(() -> validator.validateApplicationImport(importResources, dto));
    }

    private ApplicationEximDto getApplicationEximDto(String suffix) {
        var application = new ApplicationEximDto();
        application.setName("application" + suffix);
        application.setVersion("0.0." + suffix);
        application.setFolderId("public/" + suffix + "/");
        return application;
    }
}
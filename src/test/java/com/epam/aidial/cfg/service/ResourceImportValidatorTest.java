package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceImportValidatorTest {
    private final ResourceImportValidator validator = new ResourceImportValidator();

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
    void shouldThrowExceptionWhenDuplicatesInFlatImportByNameAndVersionAndPath() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        var application3 = getApplicationEximDto("1");
        application3.setVersion("0.0.2");
        var application4 = getApplicationEximDto("1");
        application4.setVersion("0.0.2");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2, application3, application4));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApplicationImport(importResources, dto));

        assertTrue(exception.getMessage().contains("Application uniqueness violation. Conflicts found:\n"
                + " - Duplicated application name 'application1' and version '0.0.1'\n"
                + " - Duplicated application name 'application1' and version '0.0.2'"));
    }

    @Test
    void shouldThrowExceptionWhenDuplicatesInFlatImportByNameAndVersionNotPath() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        application2.setFolderId("public/2/");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApplicationImport(importResources, dto));

        assertTrue(exception.getMessage().contains("Application uniqueness violation. Conflicts found:\n"
                + " - Duplicated application name 'application1' and version '0.0.1'"));
    }

    @Test
    void shouldThrowExceptionWhenDuplicatesInNonFlatImportByNameAndVersionAndPath() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApplicationImport(importResources, dto));

        assertTrue(exception.getMessage().contains("Application uniqueness violation. Conflicts found:\n"
                + " - Duplicated application name 'application1' and version '0.0.1' and folder 'public/1/'"));
    }

    @Test
    void shouldPassWhenDuplicatesInNonFlatImportByNameAndVersionNotPath() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        application2.setFolderId("public/2/");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(false);

        assertDoesNotThrow(() -> validator.validateApplicationImport(importResources, dto));
    }

    @Test
    void shouldHandleEmptyApplicationsList() {
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of());
        ImportResources importResources = new ImportResources();
        importResources.setFlatImport(false);

        assertDoesNotThrow(() -> validator.validateApplicationImport(importResources, dto));
    }

    @Test
    void shouldThrowExceptionWhenCheckApplicationExistenceWithoutApplicationInside() {
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
        applicationsEximDtos.put("test", new ApplicationsEximDto());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertTrue(exception.getMessage().contains("Application files (e.g., `applications/*.json`) were found in the archive, "
                + "but they do not contain applications. Please verify the content of these files."));
    }

    @Test
    void shouldThrowExceptionWhenCheckApplicationExistenceNotContainApplicationsFileInApplicationsFolder() {
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertTrue(exception.getMessage().contains("No application files (e.g., `applications/*.json`) found or loaded from the archive."
                + " Please ensure application files are placed in a `applications/` directory and have a `.json` extension."));
    }

    @Test
    void shouldThrowExceptionWhenContainConflictingApplicationsWithinFile() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
        applicationsEximDtos.put("test", new ApplicationsEximDto(List.of(application1, application2)));
        var importResource = new ImportResources();
        importResource.setFlatImport(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertTrue(exception.getMessage().equals("""
                Application uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:
                    - File 'test' has duplicate application: name 'application1', version '0.0.1', folder 'public/1/'"""));
    }

    @Test
    void shouldThrowExceptionWhenContainConflictingApplicationsAcrossFiles() {
        var application1 = getApplicationEximDto("1");
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
        applicationsEximDtos.put("test1", new ApplicationsEximDto(List.of(application1)));
        applicationsEximDtos.put("test2", new ApplicationsEximDto(List.of(application1)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertTrue(exception.getMessage().equals("""
                Application uniqueness violation. Conflicts found:
                  Applications shared across different files:
                    - Application with name 'application1', version '0.0.1', folder 'public/1/ found in multiple files: [test2, test1]"""));
    }

    @Test
    void shouldThrowExceptionWhenContainConflictingApplicationsWithinAndAcrossFilesAndFlatImport() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        var application3 = getApplicationEximDto("1");
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
        applicationsEximDtos.put("test1", new ApplicationsEximDto(List.of(application1)));
        applicationsEximDtos.put("test2", new ApplicationsEximDto(List.of(application2, application3)));
        var importResources = new ImportResources();
        importResources.setFlatImport(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertTrue(exception.getMessage().equals("""
                Application uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:  
                    - File 'test2' has duplicate application: name 'application1', version '0.0.1', folder 'public/1/'
                  Applications shared across different files:
                    - Application with name 'application1', version '0.0.1', folder 'public/1/ found in multiple files: [test2, test1]"""));
    }

    @Test
    void shouldThrowExceptionWhenContainConflictingApplicationsWithinAndAcrossFilesAndNonFlatImport() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        var application3 = getApplicationEximDto("1");
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
        applicationsEximDtos.put("test1", new ApplicationsEximDto(List.of(application1)));
        applicationsEximDtos.put("test2", new ApplicationsEximDto(List.of(application2, application3)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertTrue(exception.getMessage().equals("""
                Application uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:  
                    - File 'test2' has duplicate application: name 'application1', version '0.0.1', folder 'public/1/'
                  Applications shared across different files:
                    - Application with name 'application1', version '0.0.1', folder 'public/1/ found in multiple files: [test2, test1]"""));
    }

    private ApplicationEximDto getApplicationEximDto(String suffix) {
        var application = new ApplicationEximDto();
        application.setName("application" + suffix);
        application.setVersion("0.0." + suffix);
        application.setFolderId("public/" + suffix + "/");
        return application;
    }
}
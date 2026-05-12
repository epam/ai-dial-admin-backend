package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.dto.ToolSetEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceImportValidatorTest {
    private final ResourceImportValidator validator = new ResourceImportValidator();

    @Test
    void collectApplicationUniquenessConflicts_shouldBeEmptyWhenNoDuplicatesInFlatImport() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("2");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        assertTrue(validator.collectApplicationUniquenessConflicts(true, dto).isEmpty());
    }

    @Test
    void collectApplicationUniquenessConflicts_flatImport_twoDuplicateVersionGroups() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        var application3 = getApplicationEximDto("1");
        application3.setVersion("0.0.2");
        var application4 = getApplicationEximDto("1");
        application4.setVersion("0.0.2");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2, application3, application4));

        var conflicts = validator.collectApplicationUniquenessConflicts(true, dto);

        assertEquals(2, conflicts.size());
        assertTrue(conflicts.values().stream().anyMatch(m -> m.contains("application1") && m.contains("0.0.1")));
        assertTrue(conflicts.values().stream().anyMatch(m -> m.contains("application1") && m.contains("0.0.2")));
    }

    @Test
    void collectApplicationUniquenessConflicts_flatImport_sameNameAndVersionDifferentFolders() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        application2.setFolderId("public/2/");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        var conflicts = validator.collectApplicationUniquenessConflicts(true, dto);

        var expectedApplicationKey = getResourceNameAndVersionAndPath(application1, true);

        assertEquals(1, conflicts.size());
        assertTrue(conflicts.containsKey(expectedApplicationKey));
        assertEquals("Duplicated application name 'application1' and version '0.0.1' appears multiple times in the import file.",
                conflicts.get(expectedApplicationKey));
    }

    @Test
    void collectApplicationUniquenessConflicts_nonFlatImport_sameFolderDuplicate() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        var conflicts = validator.collectApplicationUniquenessConflicts(false, dto);

        var expectedApplicationKey = getResourceNameAndVersionAndPath(application1, false);

        assertEquals(1, conflicts.size());
        assertTrue(conflicts.containsKey(expectedApplicationKey));
        assertEquals("Duplicated application name 'application1' and version '0.0.1' and folder 'public/1/' appears multiple times in the import file.",
                conflicts.get(expectedApplicationKey));
    }

    @Test
    void collectApplicationUniquenessConflicts_nonFlatImport_sameNameVersionDifferentFolders() {
        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        application2.setFolderId("public/2/");
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of(application1, application2));

        assertTrue(validator.collectApplicationUniquenessConflicts(false, dto).isEmpty());
    }

    @Test
    void collectApplicationUniquenessConflicts_emptyApplicationsList() {
        ApplicationsEximDto dto = new ApplicationsEximDto(List.of());

        assertTrue(validator.collectApplicationUniquenessConflicts(false, dto).isEmpty());
    }

    @Test
    void collectToolSetUniquenessConflicts_flatImport_sameNameAndVersionDifferentFolders() {
        var toolSet1 = getToolSetEximDto("1");
        var toolSet2 = getToolSetEximDto("1");
        toolSet2.setFolderId("public/2/");
        var dto = new ToolSetsEximDto(List.of(toolSet1, toolSet2));

        var conflicts = validator.collectToolSetUniquenessConflicts(true, dto);

        var expectedToolSetKey = getResourceNameAndVersionAndPath(toolSet1, true);

        assertEquals(1, conflicts.size());
        assertTrue(conflicts.containsKey(expectedToolSetKey));
        assertEquals("Duplicated toolset name 'toolSet1' and version '0.0.1' appears multiple times in the import file.",
                conflicts.get(expectedToolSetKey));
    }

    private ToolSetEximDto getToolSetEximDto(String suffix) {
        var toolSet = new ToolSetEximDto();
        toolSet.setName("toolSet" + suffix);
        toolSet.setVersion("0.0." + suffix);
        toolSet.setFolderId("public/" + suffix + "/");
        return toolSet;
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
        assertEquals("""
                Application uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:
                    - File 'test' has duplicate application: name 'application1', version '0.0.1', folder 'public/1/'""", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenContainConflictingApplicationsAcrossFiles() {
        var application1 = getApplicationEximDto("1");
        var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
        applicationsEximDtos.put("test1", new ApplicationsEximDto(List.of(application1)));
        applicationsEximDtos.put("test2", new ApplicationsEximDto(List.of(application1)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.checkApplicationConflicts(new ImportResources(), applicationsEximDtos));
        assertEquals("""
                Application uniqueness violation. Conflicts found:
                  Applications shared across different files:
                    - Application with name 'application1', version '0.0.1', folder 'public/1/ found in multiple files: [test2, test1]""", exception.getMessage());
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
        assertEquals("""
                Application uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:
                    - File 'test2' has duplicate application: name 'application1', version '0.0.1', folder 'public/1/'
                  Applications shared across different files:
                    - Application with name 'application1', version '0.0.1', folder 'public/1/ found in multiple files: [test2, test1]""", exception.getMessage());
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
        assertEquals("""
                Application uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:  
                    - File 'test2' has duplicate application: name 'application1', version '0.0.1', folder 'public/1/'
                  Applications shared across different files:
                    - Application with name 'application1', version '0.0.1', folder 'public/1/ found in multiple files: [test2, test1]""", exception.getMessage());
    }

    @Test
    void shouldPassValidateFileImportInZipWhenNotDuplicates() throws IOException {
        MockMultipartFile mockFile = getMockMultipartZipFile(false);

        assertDoesNotThrow(() -> validator.validateFileImportInZip(new ImportResources(), mockFile));
    }

    @Test
    void shouldPassValidateFileImportInZipWhenDuplicatesInFolderAndNonFlatImport() throws IOException {
        MockMultipartFile mockFile = getMockMultipartZipFile(true);

        assertDoesNotThrow(() -> validator.validateFileImportInZip(new ImportResources(), mockFile));
    }

    @Test
    void shouldThrowExceptionWhenValidateFileImportInZipWhenDuplicatesInFolderAndFlatImport() throws IOException {
        MockMultipartFile mockFile = getMockMultipartZipFile(true);
        var importResources = new ImportResources();
        importResources.setFlatImport(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateFileImportInZip(importResources, mockFile));

        assertEquals("""
                        Files uniqueness violation. Conflicts found:
                         - Duplicated file name 'file1.json' found in folders: folder1/folder2/, folder1/folder2/folder3/""",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenValidateFileImportInZipWhenFileWithWrongFormat() throws IOException {
        MockMultipartFile mockFile = getMockMultipartZipFileWithWrongFormat();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateFileImportInZip(new ImportResources(), mockFile));

        assertEquals("Invalid zip format for file 'test'", exception.getMessage());
    }

    @Test
    void collectMultipartFilesUniquenessConflicts_duplicateOriginalNames() {
        var file1 = new MockMultipartFile("file1", "test.txt", null, "1".getBytes());
        var file2 = new MockMultipartFile("file2", "test.txt", null, "2".getBytes());

        var conflicts = validator.collectMultipartFilesUniquenessConflicts(List.of(file1, file2));

        assertEquals(1, conflicts.size());
        assertTrue(conflicts.containsKey("test.txt"));
    }

    @Test
    void collectMultipartFilesUniquenessConflicts_noDuplicates() {
        var file1 = new MockMultipartFile("file", "file1.txt", null, "1".getBytes());
        var file2 = new MockMultipartFile("file", "file2.txt", null, "2".getBytes());

        var conflicts = validator.collectMultipartFilesUniquenessConflicts(List.of(file1, file2));

        assertTrue(conflicts.isEmpty());
    }

    private MockMultipartFile getMockMultipartZipFile(boolean withDuplicates) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            var fileName1 = "file1.json";
            var fileName2 = withDuplicates ? fileName1 : "file2.json";
            ZipEntry entry1 = new ZipEntry("folder1/folder2/folder3/" + fileName1);
            zos.putNextEntry(entry1);
            zos.write("file1".getBytes());
            zos.closeEntry();
            ZipEntry entry2 = new ZipEntry("folder1/folder2/" + fileName2);
            zos.putNextEntry(entry2);
            zos.write("file1".getBytes());
            zos.closeEntry();

            byte[] zipBytes = baos.toByteArray();
            return new MockMultipartFile("file", "test", "application/zip", zipBytes);
        }
    }

    private MockMultipartFile getMockMultipartZipFileWithWrongFormat() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry1 = new ZipEntry("import/1/");
            zos.putNextEntry(entry1);
            zos.write("file1".getBytes());
            zos.closeEntry();
            zos.closeEntry();

            byte[] zipBytes = baos.toByteArray();
            return new MockMultipartFile("file", "test", "application/zip", zipBytes);
        }
    }

    private ApplicationEximDto getApplicationEximDto(String suffix) {
        var application = new ApplicationEximDto();
        application.setName("application" + suffix);
        application.setVersion("0.0." + suffix);
        application.setFolderId("public/" + suffix + "/");
        return application;
    }

    private ResourceLocation getResourceNameAndVersionAndPath(ApplicationEximDto applicationEximDto,
                                                              boolean isFlatImport) {
        return ResourceLocation.from(
                applicationEximDto.getName(),
                applicationEximDto.getVersion(),
                applicationEximDto.getFolderId(),
                isFlatImport
        );
    }

    private ResourceLocation getResourceNameAndVersionAndPath(ToolSetEximDto toolSetEximDto,
                                                              boolean isFlatImport) {
        return ResourceLocation.from(
                toolSetEximDto.getName(),
                toolSetEximDto.getVersion(),
                toolSetEximDto.getFolderId(),
                isFlatImport
        );
    }
}
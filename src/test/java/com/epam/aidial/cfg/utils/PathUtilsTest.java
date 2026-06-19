package com.epam.aidial.cfg.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.epam.aidial.cfg.utils.PathUtils.parseVersionedPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathUtilsTest {

    @ParameterizedTest
    @MethodSource("validZipEntryPaths")
    void testValidateZipEntryPath_ValidPaths_ShouldNotThrow(String zipEntryPath, String normalizedPath) {
        // given
        // when & then
        assertThat(PathUtils.validateZipEntryPath(zipEntryPath))
                .isEqualTo(normalizedPath);
    }

    @ParameterizedTest
    @MethodSource("invalidZipEntryPaths")
    void testValidateZipEntryPath_InvalidPaths_ShouldThrowIllegalArgumentException(
            String zipEntryPath, String expectedErrorMessage) {
        // given
        // when & then
        assertThatThrownBy(() -> PathUtils.validateZipEntryPath(zipEntryPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedErrorMessage);
    }

    @Test
    void testValidateZipEntryPath_Null_ShouldThrowIllegalArgumentException() {
        // given
        String zipEntryPath = null;
        // when & then
        assertThatThrownBy(() -> PathUtils.validateZipEntryPath(zipEntryPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be blank");
    }

    @Test
    void testValidateZipEntryPath_EmptyString_ShouldThrowIllegalArgumentException() {
        // given
        String zipEntryPath = "";
        // when & then
        assertThatThrownBy(() -> PathUtils.validateZipEntryPath(zipEntryPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be blank");
    }

    @Test
    void testValidateZipEntryPath_WhitespaceOnly_ShouldThrowIllegalArgumentException() {
        // given
        String zipEntryPath = "   ";
        // when & then
        assertThatThrownBy(() -> PathUtils.validateZipEntryPath(zipEntryPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be blank");
    }

    @ParameterizedTest
    @CsvSource({
            "public/folder/, folder/",
            "public/folder1/folder2/, folder2/",
            "public/, public/"
    })
    void testFolderNameWithoutPath_FolderPaths_ReturnsFolders(String folderPath, String expected) {
        assertThat(PathUtils.folderNameWithoutPath(folderPath)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"public/", "public/docs/", "/"})
    void testIsFolderPath_WithTrailingSlash_ReturnsTrue(String path) {
        assertThat(PathUtils.isFolderPath(path)).isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = {"public/file.txt", "public", "''", "null"}, nullValues = "null")
    void testIsFolderPath_WithoutTrailingSlash_ReturnsFalse(String path) {
        assertThat(PathUtils.isFolderPath(path)).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "public/promptname, promptname, null, promptname",
            "public/prompt__1.0, prompt, 1.0, prompt__1.0",
            "public/name__, name, '', name__",
            "public/my__super__model__2.0, my__super__model, 2.0, my__super__model__2.0"}, nullValues = "null")
    void shouldParseVersionedPath(
            String path,
            String expectedName,
            String expectedVersion,
            String expectedVersionedName
    ) {
        var result = parseVersionedPath(path);

        assertThat(result.getName()).isEqualTo(expectedName);
        assertThat(result.getVersion()).isEqualTo(expectedVersion);
        assertThat(result.getVersionedName()).isEqualTo(expectedVersionedName);
    }

    @Test
    void testValidateZipEntryPath_NullByte_ShouldThrowIllegalArgumentException() {
        // given
        String zipEntryPath = "files/test\0.txt";
        // when & then
        assertThatThrownBy(() -> PathUtils.validateZipEntryPath(zipEntryPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Null byte detected");
    }

    @Test
    void testValidateZipEntryPath_ReturnsNormalizedPath() {
        // given
        String zipEntryPath = "files/subdir//file.txt";
        // when
        String result = PathUtils.validateZipEntryPath(zipEntryPath);
        // then
        assertThat(result).isEqualTo("files/subdir/file.txt");
    }

    @Test
    void testValidateZipEntryPath_WindowsSeparator_ShouldNormalize() {
        // given
        String zipEntryPath = "files\\subdir\\file.txt";
        // when
        String result = PathUtils.validateZipEntryPath(zipEntryPath);
        // then
        assertThat(result).isEqualTo("files/subdir/file.txt");
    }

    private static Stream<Arguments> validZipEntryPaths() {
        return Stream.of(
                Arguments.of("files/test.txt", "files/test.txt"),
                Arguments.of("files/subdir/file.txt", "files/subdir/file.txt"),
                Arguments.of("files/subdir/nested/file.txt", "files/subdir/nested/file.txt"),
                Arguments.of("prompts/prompt.json", "prompts/prompt.json"),
                Arguments.of("prompts/public/prompt.json", "prompts/public/prompt.json"),
                Arguments.of("file.txt", "file.txt"),
                Arguments.of("subdir/file.txt", "subdir/file.txt"),
                Arguments.of("files/", "files"),
                Arguments.of("files/test", "files/test"),
                Arguments.of("files/test.json", "files/test.json"),
                Arguments.of("files/test-file.txt", "files/test-file.txt"),
                Arguments.of("files/test_file.txt", "files/test_file.txt"),
                Arguments.of("files/test.file.txt", "files/test.file.txt"),
                Arguments.of("files/123/test.txt", "files/123/test.txt"),
                Arguments.of("files/abc123/test.txt", "files/abc123/test.txt"),
                Arguments.of("files/中文/test.txt", "files/中文/test.txt"),
                Arguments.of("a/b/c/d/e/f/g.txt", "a/b/c/d/e/f/g.txt")
        );
    }

    private static Stream<Arguments> invalidZipEntryPaths() {
        return Stream.of(
                Arguments.of("../test.txt", "Path traversal detected"),
                Arguments.of("../../test.txt", "Path traversal detected"),
                Arguments.of("../../../etc/passwd", "Path traversal detected"),
                Arguments.of("files/../test.txt", "Path traversal detected"),
                Arguments.of("files/../../test.txt", "Path traversal detected"),
                Arguments.of("files/subdir/../../../test.txt", "Path traversal detected"),
                Arguments.of("../files/test.txt", "Path traversal detected"),
                Arguments.of("..", "Path traversal detected"),
                Arguments.of("./test.txt", "Path traversal detected"),
                Arguments.of("files/./test.txt", "Path traversal detected"),
                Arguments.of("files/../other/test.txt", "Path traversal detected"),
                Arguments.of("/etc/passwd", "Path traversal detected"),
                Arguments.of("C:\\Windows\\System32", "Path traversal detected"),
                Arguments.of("/files/test.txt", "Path traversal detected"),
                Arguments.of("files/..", "Path traversal detected"),
                Arguments.of("files/subdir/..", "Path traversal detected"),
                Arguments.of("files/subdir/../", "Path traversal detected"),
                Arguments.of("..\\test.txt", "Path traversal detected"),
                Arguments.of("files\\..\\test.txt", "Path traversal detected"),
                Arguments.of("..\\..\\test.txt", "Path traversal detected")
        );
    }
}
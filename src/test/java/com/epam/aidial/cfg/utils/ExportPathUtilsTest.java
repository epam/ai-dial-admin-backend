package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.NodeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportPathUtilsTest {

    @Test
    void toFolderExportPublicPath() {
        var exportedFolder = "public/folder/folder1/folder2/";
        var item = "public/folder/folder1/folder2/folder3/test.txt";
        assertThat(ExportPathUtils.toFolderExportPublicPath(item, exportedFolder))
                .isEqualTo("public/folder2/folder3/test.txt");
    }

    @Test
    void toSingleFileExportPublicPath_flattensToBasename() {
        assertThat(ExportPathUtils.toSingleFileExportPublicPath("public/folder/folder1/test.txt"))
                .isEqualTo("public/test.txt");
    }

    @Test
    void toExportedFileStoragePath() {
        var folder = "public/folder/folder1/folder2/";
        var item = "public/folder/folder1/folder2/folder3/test.txt";
        assertThat(ExportPathUtils.toExportedFileStoragePath(item, folder))
                .isEqualTo(ExportPathUtils.toFolderExportPublicPath(item, folder));
    }

    @Test
    void toSingleExportPublicPath_flattensToBaseVersionedName() {
        assertThat(ExportPathUtils.toSingleVersionedResourceExportPublicPath("public/folder/PROMPT 1__1.0.0"))
                .isEqualTo("public/PROMPT 1__1.0.0");
    }

    @Test
    void isTechnicalItem_trueForDialFolderMarker() {
        assertThat(ExportPathUtils.isTechnicalItem("public/folder/" + ExportPathUtils.DIAL_FOLDER_FILE))
                .isTrue();
    }

    @Test
    void isTechnicalItem_trueForVersionedDialFolderMarker() {
        assertThat(ExportPathUtils.isTechnicalItem(
                "public/folder/" + ExportPathUtils.DIAL_FOLDER_FILE + "__1.0.0"))
                .isTrue();
    }

    @Test
    void collectExportableLeafPaths_returnPathItems() {
        var folder = FileNodeInfo.builder()
                .nodeType(NodeType.FOLDER)
                .items(List.of(
                        FileNodeInfo.builder()
                                .nodeType(NodeType.ITEM)
                                .path("public/folder/test.txt")
                                .build(),
                        FileNodeInfo.builder()
                                .nodeType(NodeType.ITEM)
                                .path("public/folder/" + ExportPathUtils.DIAL_FOLDER_FILE)
                                .build()
                ))
                .build();

        assertThat(ExportPathUtils.collectExportablePaths(folder))
                .containsExactlyInAnyOrder("public/folder/test.txt");
    }

    @Test
    void collectExportablePaths_excludesVersionedDialFolderMarker() {
        var folder = FileNodeInfo.builder()
                .nodeType(NodeType.FOLDER)
                .items(List.of(
                        FileNodeInfo.builder()
                                .nodeType(NodeType.ITEM)
                                .path("public/folder/a.txt")
                                .build(),
                        FileNodeInfo.builder()
                                .nodeType(NodeType.ITEM)
                                .path("public/folder/" + ExportPathUtils.DIAL_FOLDER_FILE + "__1.0.0")
                                .build()
                ))
                .build();

        assertThat(ExportPathUtils.collectExportablePaths(folder))
                .containsExactlyInAnyOrder("public/folder/a.txt");
    }
}
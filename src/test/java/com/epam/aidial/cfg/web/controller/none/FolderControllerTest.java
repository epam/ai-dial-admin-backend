package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveFolderRequest;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.FolderService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.FolderController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FolderController.class)
@Import({
        JsonMapperConfiguration.class,
        ResourceMapperImpl.class
})
class FolderControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FolderService folderService;

    @Test
    void testGetFolders() throws Exception {
        // given
        String requestDto = ResourceUtils.readResource("/folders/all_folders_request_dto.json");
        String allFoldersRequestJson = ResourceUtils.readResource("/folders/all_folders_request.json");
        ResourceMetadataRequest allFoldersRequest = objectMapper.readValue(allFoldersRequestJson, new TypeReference<>() {
        });
        String folderInfoJson = ResourceUtils.readResource("/folders/folder_infos.json");
        FolderInfo folderInfo = objectMapper.readValue(folderInfoJson, new TypeReference<>() {
        });
        when(folderService.getFolders(any())).thenReturn(folderInfo);
        String dtoJson = ResourceUtils.readResource("/folders/all_folders_response.json");

        // when
        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDto))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(folderService).getFolders(eq(allFoldersRequest));
    }

    @Test
    void testGetRules() throws Exception {
        // given
        String path = "public/test/";
        Rule rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        when(folderService.getRules(path)).thenReturn(Map.of("public/test/", List.of(rule)));
        String response = ResourceUtils.readResource("/folders/rules_response.json");

        // when
        mockMvc.perform(get("/api/v1/folders")
                        .param("path", path))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(response, JsonCompareMode.LENIENT));

        verify(folderService).getRules(path);
    }

    @Test
    void testUpdateRules() throws Exception {
        // given
        var updateRulesRequestDtoJson = ResourceUtils.readResource("/folders/update_rules_request_dto.json");

        var updateRulesRequestJson = ResourceUtils.readResource("/folders/update_rules_request.json");
        var updateRulesRequest = objectMapper.readValue(updateRulesRequestJson, new TypeReference<UpdateRulesRequest>() {
        });

        // when
        mockMvc.perform(post("/api/v1/folders/updateRules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRulesRequestDtoJson))
                // then
                .andExpect(status().isOk());

        verify(folderService).updatesRules(updateRulesRequest);
    }

    @Test
    void testDeleteFolder() throws Exception {
        // given
        String path = "public/test/";
        doNothing().when(folderService).unpublishFolder(path);
        // when
        mockMvc.perform(delete("/api/v1/folders")
                        .param("path", path))
                // then
                .andExpect(status().isOk());

        verify(folderService).unpublishFolder(path);
    }

    @Test
    void testMoveFolder() throws Exception {
        // given
        var moveFolderRequestDtoJson = ResourceUtils.readResource("/folders/move_folder_request_dto.json");

        var moveFolderRequestJson = ResourceUtils.readResource("/folders/move_folder_request.json");
        var moveFolderRequest = objectMapper.readValue(moveFolderRequestJson, new TypeReference<MoveFolderRequest>() {
        });

        doNothing().when(folderService).moveFolder(moveFolderRequest);

        // when
        mockMvc.perform(post("/api/v1/folders/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moveFolderRequestDtoJson))
                // then
                .andExpect(status().isOk());

        verify(folderService).moveFolder(moveFolderRequest);
    }

}
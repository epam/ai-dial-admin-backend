package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.PublicationPathDto;
import com.epam.aidial.cfg.dto.RejectPublicationDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.mapper.FileMapperImpl;
import com.epam.aidial.cfg.mapper.PublicationMapperImpl;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.FilePublication;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.publication.PublicationService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.PublicationController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicationController.class)
@Import({
        JsonMapperConfiguration.class,
        PublicationMapperImpl.class,
        FileMapperImpl.class,
})
class PublicationControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PublicationService publicationService;

    @Test
    void testGetAllPublications() throws Exception {
        var modelJson = ResourceUtils.readResource("/publications/publications.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<PublicationInfos>() {
        });

        when(publicationService.getAllPublications(any())).thenReturn(model);

        var dtoJson = ResourceUtils.readResource("/publications/publications_dto.json");
        mockMvc.perform(get("/api/v1/publications"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(publicationService).getAllPublications(null);
    }

    @Test
    void testGetAllPublications_PromptPublication() throws Exception {
        var modelJson = ResourceUtils.readResource("/publications/publications.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<PublicationInfos>() {
        });

        when(publicationService.getAllPublications(any())).thenReturn(model);

        var dtoJson = ResourceUtils.readResource("/publications/publications_dto.json");
        mockMvc.perform(get("/api/v1/publications")
                        .queryParam("type", "prompt"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(publicationService).getAllPublications(ResourceType.PROMPT);
    }

    @Test
    void testGetPublication_PromptPublication() throws Exception {
        var modelJson = ResourceUtils.readResource("/publications/prompt_publication.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<PromptPublication>() {
        });
        var publicationPath = "bucket/file";

        when(publicationService.getPublication(publicationPath)).thenReturn(model);

        var body = new PublicationPathDto();
        body.setPath(publicationPath);
        var dtoJson = ResourceUtils.readResource("/publications/prompt_publication_dto.json");
        mockMvc.perform(post("/api/v1/publications/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetPublication_FilePublication() throws Exception {
        var modelJson = ResourceUtils.readResource("/publications/file_publication.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<FilePublication>() {
        });
        var publicationPath = "bucket/file";

        when(publicationService.getPublication(publicationPath)).thenReturn(model);

        var body = new PublicationPathDto();
        body.setPath(publicationPath);
        var dtoJson = ResourceUtils.readResource("/publications/file_publication_dto.json");
        mockMvc.perform(post("/api/v1/publications/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetPublication_ApplicationPublication() throws Exception {
        var modelJson = ResourceUtils.readResource("/publications/application_publication.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<ApplicationPublication>() {
        });
        var publicationPath = "bucket/file";

        when(publicationService.getPublication(publicationPath)).thenReturn(model);

        var body = new PublicationPathDto();
        body.setPath(publicationPath);
        var dtoJson = ResourceUtils.readResource("/publications/application_publication_dto.json");
        mockMvc.perform(post("/api/v1/publications/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetPublication_PromptPublication_DifferentActions_ThrowError() throws Exception {
        var modelJson = ResourceUtils.readResource("/publications/prompt_publication_diff_actions.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<PromptPublication>() {
        });
        var publicationPath = "bucket/file";

        when(publicationService.getPublication(publicationPath)).thenReturn(model);

        var body = new PublicationPathDto();
        body.setPath(publicationPath);
        mockMvc.perform(post("/api/v1/publications/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Different actions found inside publication request")));
    }

    @Test
    void testGetPublication_PromptPublication_StatusIsApproved_ReturnNotFound() throws Exception {
        var publicationPath = "bucket/file";

        when(publicationService.getPublication(publicationPath))
                .thenThrow(new EntityNotFoundException("Publication not found"));

        var body = new PublicationPathDto();
        body.setPath(publicationPath);
        mockMvc.perform(post("/api/v1/publications/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testApprovePublication() throws Exception {
        var publicationPath = "bucket/file";
        var body = new PublicationPathDto();
        body.setPath(publicationPath);

        mockMvc.perform(post("/api/v1/publications/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(publicationService).approvePublication(publicationPath);
    }

    @Test
    void testRejectPublication() throws Exception {
        var publicationPath = "bucket/file";
        var comment = "comment";
        var body = new RejectPublicationDto();
        body.setPath(publicationPath);
        body.setComment(comment);

        mockMvc.perform(post("/api/v1/publications/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(publicationService).rejectPublication(publicationPath, comment);
    }

}

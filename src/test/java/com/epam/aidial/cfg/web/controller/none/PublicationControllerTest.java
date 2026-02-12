package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.PublicationPathDto;
import com.epam.aidial.cfg.dto.RejectPublicationDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.mapper.FileMapperImpl;
import com.epam.aidial.cfg.mapper.PublicationMapperImpl;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.FilePublication;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.publication.PublicationService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.PublicationController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicationController.class)
@Import({
        JsonMapperConfiguration.class,
        PublicationMapperImpl.class,
        RouteMapperImpl.class,
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

    @ParameterizedTest
    @MethodSource("testGetPublicationParams")
    void testGetPublication(String publicationFilePath, String publicationDtoFilePath, Class<? extends Publication> publicationClass) throws Exception {
        var modelJson = ResourceUtils.readResource(publicationFilePath);
        var model = objectMapper.readValue(modelJson, publicationClass);
        var publicationPath = "bucket/file";

        when(publicationService.getPublication(publicationPath)).thenReturn(model);

        var body = new PublicationPathDto();
        body.setPath(publicationPath);
        var dtoJson = ResourceUtils.readResource(publicationDtoFilePath);
        mockMvc.perform(post("/api/v1/publications/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @ParameterizedTest
    @MethodSource("testGetPublicationParams")
    void testUpdatePublication(String publicationFilePath, String publicationDtoFilePath, Class<? extends Publication> publicationClass) throws Exception {

        var modelJson = ResourceUtils.readResource(publicationFilePath);
        var model = objectMapper.readValue(modelJson, publicationClass);

        doNothing().when(publicationService).updatePublication(any(), any());

        var dtoJson = ResourceUtils.readResource(publicationDtoFilePath);
        MockMultipartFile publicationFile = new MockMultipartFile("publication", "publication.json", MimeTypeUtils.APPLICATION_JSON_VALUE,
                dtoJson.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/publications/update")
                        .file(mockFile)
                        .file(publicationFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
        verify(publicationService).updatePublication(model, List.of(mockFile));
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
        var comment = "commentcommentcomment";
        var body = new RejectPublicationDto();
        body.setPath(publicationPath);
        body.setComment(comment);

        mockMvc.perform(post("/api/v1/publications/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(publicationService).rejectPublication(publicationPath, comment);
    }

    @Test
    void testRejectPublication_ErrorValidation() throws Exception {
        var publicationPath = "publicationPath";
        var comment = "1236";
        var body = new RejectPublicationDto();
        body.setPath(publicationPath);
        body.setComment(comment);

        mockMvc.perform(post("/api/v1/publications/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                    .value("comment: Comment must be between 15 and 255 characters"));
    }

    private static Stream<Arguments> testGetPublicationParams() {
        return Stream.of(
                Arguments.of(
                        "/publications/prompt_publication.json",
                        "/publications/prompt_publication_dto.json",
                        PromptPublication.class
                ),
                Arguments.of(
                        "/publications/file_publication.json",
                        "/publications/file_publication_dto.json",
                        FilePublication.class
                ),
                Arguments.of(
                        "/publications/application_publication.json",
                        "/publications/application_publication_dto.json",
                        ApplicationPublication.class
                ),
                Arguments.of(
                        "/publications/conversation_publication.json",
                        "/publications/conversation_publication_dto.json",
                        ConversationPublication.class
                )
        );
    }

}
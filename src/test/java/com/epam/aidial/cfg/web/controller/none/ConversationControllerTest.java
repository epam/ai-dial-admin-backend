package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.exception.NotModifiedException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.mapper.ApplicationResourceMapperImpl;
import com.epam.aidial.cfg.mapper.ConversationMapperImpl;
import com.epam.aidial.cfg.mapper.PublicationMapperImpl;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationNodeInfo;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.service.ConversationEximService;
import com.epam.aidial.cfg.service.ConversationService;
import com.epam.aidial.cfg.service.ZipConversationEximService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ConversationController;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConversationController.class)
@Import({
        JsonMapperConfiguration.class,
        ConversationMapperImpl.class,
        PublicationMapperImpl.class,
        RouteMapperImpl.class,
        ResourceMapperImpl.class,
        ApplicationResourceMapperImpl.class
})
class ConversationControllerTest extends AbstractControllerNoneSecureTest {

    private static final String DTO_JSON_BASE_PATH = "/conversation-resources/";
    private static final String CONV_PATH = "rootPath/subFolder/TestConv";
    private static final String BASE_API_PATH = "/api/v1/conversations";
    private static final String GET_API_PATH = BASE_API_PATH + "/get";
    private static final String DELETE_API_PATH = BASE_API_PATH + "/delete";
    private static final String DELETE_BULK_API_PATH = BASE_API_PATH + "/delete/bulk";
    private static final String LIST_API_PATH = BASE_API_PATH + "/list";
    private static final String MOVE_API_PATH = BASE_API_PATH + "/move";
    private static final String TEST_ETAG = "etag123";
    private static final String RETURNED_TEST_ETAG = "\"etag123\"";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private ConversationEximService conversationEximService;

    @MockitoBean
    private ZipConversationEximService zipConversationEximService;

    @Test
    void testGetAllConversations() throws Exception {
        var requestDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "conversation_infos_request_dto.json");

        var requestJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "conversation_infos_request.json");
        var metadataRequest = objectMapper.readValue(requestJson, new TypeReference<ResourceMetadataRequest>() {
        });

        var modelJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "conversation_infos.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<ConversationNodeInfo>() {
        });

        when(conversationService.getConversations(any())).thenReturn(model);

        var dtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "conversation_infos_dto.json");
        mockMvc.perform(post(LIST_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(conversationService).getConversations(eq(metadataRequest));
    }

    @Test
    void testGetConversation() throws Exception {
        var modelJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "conversation_get.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<Conversation>() {
        });

        when(conversationService.getConversation(any(), any()))
                .thenReturn(new DomainModelWithEtag<>(model, TEST_ETAG));

        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);
        var dtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "conversation_get_dto.json");
        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_NONE_MATCH, TEST_ETAG))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(conversationService).getConversation(eq(CONV_PATH), eq(TEST_ETAG));
    }

    @Test
    void testGetConversation_whenResourceNotExist() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);

        doThrow(new ResourceNotFoundException("Not Found")).when(conversationService).getConversation(any(), any());

        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_NONE_MATCH, TEST_ETAG))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Not Found"));

        verify(conversationService).getConversation(eq(CONV_PATH), eq(TEST_ETAG));
    }

    @Test
    void testGetConversation_whenResourceWithSameEtag() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);

        doThrow(new NotModifiedException(Map.of("etag", List.of(TEST_ETAG)))).when(conversationService).getConversation(any(), any());

        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_NONE_MATCH, TEST_ETAG))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HEADER_ETAG, RETURNED_TEST_ETAG));

        verify(conversationService).getConversation(eq(CONV_PATH), eq(TEST_ETAG));
    }

    @Test
    void testGetConversation_whenIfNoneMatchHeaderNotPresent() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);

        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteConversation() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);
        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_MATCH, "*"))
                .andExpect(status().isOk());

        verify(conversationService).delete(CONV_PATH, "*");
    }

    @Test
    void testDeleteConversation_whenResourceNotExist() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);

        doThrow(new ResourceNotFoundException("Not Found")).when(conversationService).delete(any(), any());

        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Not Found"));

        verify(conversationService).delete(CONV_PATH, TEST_ETAG);
    }

    @Test
    void testDeleteConversation_whenWrongEtag() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);

        doThrow(new ResourcePreconditionFailedException("Precondition failed")).when(conversationService).delete(any(), any());

        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message")
                        .value("Precondition failed"));
    }

    @Test
    void testDeleteConversation_whenIfMatchHeaderNotPresent() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(CONV_PATH);

        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteConversationsBulk() throws Exception {
        var path1 = "testPath1/TestName";
        var path2 = "testPath2/TestName";

        var pathDto1 = new ResourcePathDto();
        pathDto1.setPath(path1);

        var pathDto2 = new ResourcePathDto();
        pathDto2.setPath(path2);

        var pathsDto = new ResourcePathsDto();
        pathsDto.setPaths(List.of(pathDto1, pathDto2));

        mockMvc.perform(post(DELETE_BULK_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pathsDto)))
                .andExpect(status().isOk());

        verify(conversationService).deleteConversations(List.of(path1, path2));
    }

    @Test
    void testMoveConversation() throws Exception {
        var moveDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "move_conv_dto.json");

        var moveJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "move_conv.json");
        var moveResource = objectMapper.readValue(moveJson, new TypeReference<MoveResource>() {
        });

        mockMvc.perform(post(MOVE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moveDtoJson))
                .andExpect(status().isOk());

        verify(conversationService).move(moveResource);
    }
}

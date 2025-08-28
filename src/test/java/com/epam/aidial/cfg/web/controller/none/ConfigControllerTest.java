package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.ConfigExportProperties;
import com.epam.aidial.cfg.domain.mapper.ExportConfigMapperImpl;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ExportApplicationTypeSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportConfigPreview;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.ExportKeyInfo;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.dto.FullExportRequestDto;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConfigExportErrorHandler;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.export.CoreConfigService;
import com.epam.aidial.cfg.service.transfer.ConfigTransfer;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ConfigController;
import com.epam.aidial.cfg.web.facade.mapper.AdapterDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.AddonDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ApplicationDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ApplicationTypeSchemaDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.AssistantDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.AttachmentPathDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.FeaturesMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ImportConfigMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.InstantMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.InterceptorDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.InterceptorSourceDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.KeyDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.LimitDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ModelDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ResponseDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.RoleBasedDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.RoleDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.RoleLimitDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.RoleShareResourceLimitDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.RouteDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ShareResourceLimitDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ToolSetDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.UpstreamDtoMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConfigController.class)
@Import({ExportConfigMapperImpl.class, ImportConfigMapperImpl.class, KeyDtoMapperImpl.class, RoleDtoMapperImpl.class,
        InterceptorDtoMapperImpl.class, ModelDtoMapperImpl.class, ApplicationDtoMapperImpl.class, ApplicationTypeSchemaDtoMapperImpl.class,
        AddonDtoMapperImpl.class, AssistantDtoMapperImpl.class, RouteDtoMapperImpl.class, RoleLimitDtoMapperImpl.class,
        LimitDtoMapperImpl.class, UpstreamDtoMapperImpl.class, RoleBasedDtoMapperImpl.class, ResponseDtoMapperImpl.class,
        AdapterDtoMapperImpl.class, ModelEndpointUtils.class, ShareResourceLimitDtoMapperImpl.class,
        RoleShareResourceLimitDtoMapperImpl.class, InterceptorSourceDtoMapperImpl.class, InstantMapperImpl.class, FeaturesMapperImpl.class,
        AttachmentPathDtoMapperImpl.class, ToolSetDtoMapperImpl.class
})
class ConfigControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CoreConfigService coreConfigService;

    @MockitoBean
    private ConfigTransfer configTransfer;

    @MockitoBean
    private ConfigExportProperties properties;

    @MockitoBean
    private ConfigExportErrorHandler configExportErrorHandler;

    @Test
    void reload() throws Exception {
        // given
        doNothing().when(coreConfigService).reloadConfig();
        // when
        mockMvc.perform(get("/api/v1/configs/reload"))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void testImport() throws Exception {
        // given
        String config = getStringJsonContent();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );

        doNothing().when(configTransfer).importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.SKIP, false, true));
        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/configs/import")
                        .file(mockFile)
                        .param("resolutionPolicy", "SKIP")
                        .param("createRoleIfAbsent", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk());
        verify(configTransfer).importConfig(List.of(mockFile), new ConfigImportOptions(ConflictResolutionPolicy.SKIP, false, true));
    }

    @Test
    void testImportZip() throws Exception {
        // given
        String config = getStringJsonContent();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.zip",
                "application/json",
                config.getBytes()
        );

        doNothing().when(configTransfer).importConfigZip(mockFile, new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true));
        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/configs/import/zip")
                        .file(mockFile)
                        .param("resolutionPolicy", "SKIP")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk());
        verify(configTransfer).importConfigZip(mockFile, new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true));
    }

    @Test
    void testImportPreview() throws Exception {
        // given
        String config = getStringJsonContent();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                config.getBytes()
        );
        var model = new Model();
        var deployment = new Deployment("testModel1");
        model.setDeployment(deployment);
        model.setDisplayName("testModel1");
        model.setDisplayVersion("1.0.0");
        model.setInterceptors(List.of("testInterceptor1"));
        model.setAuthor("test-author");
        model.setCreatedAt(Instant.parse("2025-06-01T12:00:00Z").toEpochMilli());
        model.setUpdatedAt(Instant.parse("2025-06-01T15:30:00Z").toEpochMilli());
        model.setDependencies(List.of("dep1", "dep2"));
        model.setFieldsHashingOrder(List.of("prompt", "temperature", "seed", "system"));
        var importConfigPreview = ImportConfigPreview.builder()
                .models(List.of(new ImportComponent<>(CREATE, model)))
                .build();

        var configImportOptions = new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true);
        when(configTransfer.importPreview(List.of(mockFile), configImportOptions)).thenReturn(importConfigPreview);
        String expected = ResourceUtils.readResource("/import/import_preview_model.json");
        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/configs/import/preview")
                        .file(mockFile)
                        .param("resolutionPolicy", "SKIP")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(expected, JsonCompareMode.LENIENT));
        verify(configTransfer).importPreview(List.of(mockFile), configImportOptions);
    }

    @Test
    void testImportPreviewZip() throws Exception {
        // given
        String config = getStringJsonContent();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.zip",
                "application/json",
                config.getBytes()
        );
        var model = new Model();
        var deployment = new Deployment("testModel1");
        model.setDeployment(deployment);
        model.setDisplayName("testModel1");
        model.setDisplayVersion("1.0.0");
        model.setInterceptors(List.of("testInterceptor1"));
        model.setAuthor("test-author");
        model.setCreatedAt(Instant.parse("2025-06-01T12:00:00Z").toEpochMilli());
        model.setUpdatedAt(Instant.parse("2025-06-01T15:30:00Z").toEpochMilli());
        model.setDependencies(List.of("dep1", "dep2"));
        model.setFieldsHashingOrder(List.of("prompt", "temperature", "seed", "system"));
        var importConfigPreview = ImportConfigPreview.builder()
                .models(List.of(new ImportComponent<>(CREATE, model)))
                .build();

        when(configTransfer.importPreviewZip(mockFile, ConflictResolutionPolicy.SKIP)).thenReturn(importConfigPreview);
        String expected = ResourceUtils.readResource("/import/import_preview_model.json");
        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/configs/import/zip/preview")
                        .file(mockFile)
                        .param("resolutionPolicy", "SKIP")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(expected, JsonCompareMode.LENIENT));
        verify(configTransfer).importPreviewZip(mockFile, ConflictResolutionPolicy.SKIP);
    }

    @Test
    void testExport_CoreFormat() throws Exception {
        // given
        FullExportRequestDto request = new FullExportRequestDto();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.KEY));
        request.setAddSecrets(true);
        var data = "exportConfig";
        StreamingResponseBody streamingResponse = outputStream -> outputStream.write(data.getBytes());
        when(configTransfer.exportConfig(any())).thenReturn(streamingResponse);
        when(properties.getExportConfigFileName()).thenReturn("aidial.config.json");
        // when
        mockMvc.perform(post("/api/v1/configs/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aidial.config.json\""));
        verify(configTransfer).exportConfig(any());
    }

    @Test
    void testExport_AdminFormat() throws Exception {
        // given
        FullExportRequestDto request = new FullExportRequestDto();
        request.setAddSecrets(false);
        request.setExportFormat(ExportFormat.ADMIN);
        request.setComponentTypes(Set.of(ExportConfigComponentType.KEY));
        var data = "exportConfig";
        StreamingResponseBody streamingResponse = outputStream -> outputStream.write(data.getBytes());
        when(configTransfer.exportConfig(any())).thenReturn(streamingResponse);
        when(properties.getExportConfigFileZipName()).thenReturn("admin.config.zip");
        // when
        mockMvc.perform(post("/api/v1/configs/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"admin.config.zip\""));
        verify(configTransfer).exportConfig(any());
    }

    @Test
    void testExportPreview_Key_CoreFormat() throws Exception {
        // given
        FullExportRequestDto request = new FullExportRequestDto();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.KEY));
        ExportKeyInfo componentInfo = ExportKeyInfo.builder()
                .type(ExportConfigComponentType.KEY)
                .name("keyName")
                .displayName("displayName")
                .description("key description")
                .roles(List.of("default"))
                .build();

        ExportConfigPreview exportConfigPreview = new ExportConfigPreview();
        exportConfigPreview.setKeys(List.of(componentInfo));
        when(configTransfer.exportPreview(any())).thenReturn(exportConfigPreview);
        String expected = ResourceUtils.readResource("/preview_key.json");
        // when
        mockMvc.perform(post("/api/v1/configs/export/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(expected, JsonCompareMode.LENIENT));
        verify(configTransfer).exportPreview(any());
    }

    @Test
    void testExportPreview_ApplicationTypeSchema_CoreFormat() throws Exception {
        // given
        FullExportRequestDto request = new FullExportRequestDto();
        request.setExportFormat(ExportFormat.CORE);
        request.setComponentTypes(Set.of(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA));
        ExportApplicationTypeSchemaInfo componentInfo = ExportApplicationTypeSchemaInfo.builder()
                .type(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
                .id("id")
                .description("description")
                .displayName("displayName")
                .build();

        ExportConfigPreview exportConfigPreview = new ExportConfigPreview();
        exportConfigPreview.setApplicationRunners(List.of(componentInfo));

        when(configTransfer.exportPreview(any())).thenReturn(exportConfigPreview);
        String expected = ResourceUtils.readResource("/preview_app_type_schema.json");
        // when
        mockMvc.perform(post("/api/v1/configs/export/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(expected, JsonCompareMode.LENIENT));
        verify(configTransfer).exportPreview(any());
    }

    @Test
    void testGetConfigExportStatus_isSuccess() throws Exception {
        // given
        when(configExportErrorHandler.getLastErrorMessage()).thenReturn(null);
        // when
        mockMvc.perform(get("/api/v1/configs/export/status"))
                //then
                .andExpect(status().isOk())
                .andExpect(content().json("{\"errorMessage\":null,\"success\":true}", JsonCompareMode.LENIENT));
    }

    @Test
    void testGetConfigExportStatus_isNotSuccess() throws Exception {
        // given
        when(configExportErrorHandler.getLastErrorMessage()).thenReturn("errorMessage");
        // when
        mockMvc.perform(get("/api/v1/configs/export/status"))
                //then
                .andExpect(status().isOk())
                .andExpect(content().json("{\"errorMessage\":\"errorMessage\",\"success\":false}", JsonCompareMode.LENIENT));
    }

    private String getStringJsonContent() {
        return "{\"models\": {\n"
                + "    \"testModel1\": {\n"
                + "      \"type\": \"embedding\",\n"
                + "      \"name\": \"testModel1\",\n"
                + "      \"displayName\": \"Test Model1\",\n"
                + "      \"displayVersion\": \"2.0.0\",\n"
                + "      \"userRoles\": [\n"
                + "        \"testRole1\"\n"
                + "      ],\n"
                + "      \"interceptors\": [\n"
                + "        \"testInterceptor1\"\n"
                + "      ]\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }


}
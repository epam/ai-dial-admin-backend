package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.dto.ApplicationDataDto;
import com.epam.aidial.cfg.dto.ModelCapabilitiesDataDto;
import com.epam.aidial.cfg.dto.ModelDataDto;
import com.epam.aidial.cfg.dto.ModelLimitsDataDto;
import com.epam.aidial.cfg.dto.ModelPricingDataDto;
import com.epam.aidial.cfg.dto.ToolSetDataDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.mapper.DeploymentMapper;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentType;
import com.epam.aidial.cfg.model.InterfaceType;
import com.epam.aidial.cfg.model.ModelCapabilitiesData;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ModelLimitsData;
import com.epam.aidial.cfg.model.ModelPricingData;
import com.epam.aidial.cfg.model.ToolSetData;
import com.epam.aidial.cfg.service.CoreDeploymentService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.DeploymentController;
import com.epam.aidial.cfg.web.facade.DeploymentFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeploymentController.class)
class DeploymentControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeploymentFacade deploymentFacade;
    @MockitoBean
    private CoreDeploymentService coreDeploymentService;
    @MockitoBean
    private DeploymentMapper deploymentMapper;

    @Test
    void testEnsureExistsShouldReturnEmptyBodyWhenDeploymentExists() throws Exception {
        String deploymentName = "test";

        doNothing().when(deploymentFacade).ensureExists(deploymentName);

        mockMvc.perform(head("/api/v1/deployments/{deploymentName}", deploymentName))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testEnsureExistsShouldReturnNotFoundWhenDeploymentDoesNotExist() throws Exception {
        String deploymentName = "test";
        String notFoundMessage = "Deployment with name " + deploymentName + " doesn't exist";

        doThrow(new EntityNotFoundException(notFoundMessage))
                .when(deploymentFacade).ensureExists(deploymentName);

        mockMvc.perform(head("/api/v1/deployments/{deploymentName}", deploymentName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(notFoundMessage));
    }

    @Test
    void testListDeploymentsShouldReturnAllWhenNoFilter() throws Exception {
        var model = ModelData.builder()
                .id("gpt-4")
                .object("model")
                .displayName("GPT-4")
                .build();
        var deployment = new ModelDataDto();
        deployment.setId("gpt-4");
        deployment.setObject("model");
        deployment.setDisplayName("GPT-4");

        when(coreDeploymentService.listDeployments(null, null)).thenReturn(List.of(model));
        when(deploymentMapper.toDeploymentDataDtoList(List.of(model))).thenReturn(List.of(deployment));

        mockMvc.perform(get("/api/v1/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("gpt-4"))
                .andExpect(jsonPath("$[0].object").value("model"))
                .andExpect(jsonPath("$[0].displayName").value("GPT-4"));

        verify(coreDeploymentService).listDeployments(null, null);
        verify(deploymentMapper).toDeploymentDataDtoList(List.of(model));
    }

    @Test
    void testListDeploymentsShouldReturnMixedDeploymentTypes() throws Exception {
        var model = ModelData.builder()
                .id("gpt-4")
                .object("dial-model")
                .capabilities(ModelCapabilitiesData.builder().chatCompletion(true).build())
                .limits(ModelLimitsData.builder().maxPromptTokens(128000).build())
                .pricing(ModelPricingData.builder().unit("token").build())
                .build();
        var application = ApplicationData.builder()
                .id("my-app")
                .object("dial-application")
                .applicationTypeSchemaId("schema-1")
                .build();
        var toolset = ToolSetData.builder()
                .id("my-toolset")
                .object("dial-toolset")
                .transport("streamable-http")
                .build();

        var modelDto = new ModelDataDto();
        modelDto.setId("gpt-4");
        modelDto.setObject("dial-model");
        modelDto.setCapabilities(new ModelCapabilitiesDataDto());
        modelDto.getCapabilities().setChatCompletion(true);
        modelDto.setLimits(new ModelLimitsDataDto());
        modelDto.getLimits().setMaxPromptTokens(128000);
        modelDto.setPricing(new ModelPricingDataDto());
        modelDto.getPricing().setUnit("token");

        var applicationDto = new ApplicationDataDto();
        applicationDto.setId("my-app");
        applicationDto.setObject("dial-application");
        applicationDto.setApplicationTypeSchemaId("schema-1");

        var toolsetDto = new ToolSetDataDto();
        toolsetDto.setId("my-toolset");
        toolsetDto.setObject("dial-toolset");
        toolsetDto.setTransport("streamable-http");

        var models = List.of(model, application, toolset);
        when(coreDeploymentService.listDeployments(null, null)).thenReturn(models);
        when(deploymentMapper.toDeploymentDataDtoList(models))
                .thenReturn(List.of(modelDto, applicationDto, toolsetDto));

        mockMvc.perform(get("/api/v1/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capabilities.chatCompletion").value(true))
                .andExpect(jsonPath("$[1].applicationTypeSchemaId").value("schema-1"))
                .andExpect(jsonPath("$[2].transport").value("streamable-http"));
    }

    @Test
    void testListDeploymentsShouldPassInterfaceTypeFilters() throws Exception {
        when(coreDeploymentService.listDeployments(List.of(InterfaceType.CHAT, InterfaceType.MCP), null))
                .thenReturn(List.of());
        when(deploymentMapper.toDeploymentDataDtoList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/deployments")
                        .param("interface_types", "CHAT", "MCP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(coreDeploymentService).listDeployments(List.of(InterfaceType.CHAT, InterfaceType.MCP), null);
    }

    @Test
    void testListDeploymentsShouldFilterByDeploymentType() throws Exception {
        var model = ModelData.builder().id("gpt-4").object("dial-model").build();
        var modelDto = new ModelDataDto();
        modelDto.setId("gpt-4");
        modelDto.setObject("dial-model");

        when(coreDeploymentService.listDeployments(null, List.of(DeploymentType.MODEL))).thenReturn(List.of(model));
        when(deploymentMapper.toDeploymentDataDtoList(List.of(model))).thenReturn(List.of(modelDto));

        mockMvc.perform(get("/api/v1/deployments").param("deployment_types", "MODEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("gpt-4"));

        verify(coreDeploymentService).listDeployments(null, List.of(DeploymentType.MODEL));
    }

    @Test
    void testGetConfigurationShouldReturnConfiguration() throws Exception {
        String deploymentName = "chat-google-dlp-anonymizer";

        var dtoJson = ResourceUtils.readResource("/interceptor_config_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<Map<String, Object>>() {
        });

        when(coreDeploymentService.getConfiguration(deploymentName))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/deployments/{deploymentName}/configuration", deploymentName))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson));
    }

    @Test
    void testGetConfigurationShouldReturnNotFoundWhenDeploymentDoesNotExist() throws Exception {
        String deploymentName = "chat-google-dlp-anonymizer";

        doThrow(new ResourceNotFoundException("Not Found"))
                .when(coreDeploymentService).getConfiguration(deploymentName);

        mockMvc.perform(get("/api/v1/deployments/{deploymentName}/configuration", deploymentName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found"));
    }
}
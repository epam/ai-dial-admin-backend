package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
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

import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.controller.DeploymentController;
import com.epam.aidial.cfg.web.facade.DeploymentFacade;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeploymentController.class)
class DeploymentControllerTest extends AbstractControllerNoneSecureTest {

    @MockitoBean
    private DeploymentFacade deploymentFacade;

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
}
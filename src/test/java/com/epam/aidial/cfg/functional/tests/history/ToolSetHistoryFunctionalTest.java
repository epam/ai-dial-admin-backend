package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.AuthenticationTypeDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.source.ToolSetContainerSourceDto;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createToolSetDto;

public abstract class ToolSetHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private ToolSetFacade toolSetFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private DeploymentManagerService deploymentManagerService;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
        roleFacade.createRole(createRoleDto("3"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateToolSet() {
        initRoles();

        // Mock deployment manager
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerUrl = "https://container-url.com";
        String containerName = "Test Container";
        String endpointPath = "/some-path";
        DeploymentInfoDto deploymentInfoDto = new DeploymentInfoDto();
        deploymentInfoDto.setId(UUID.fromString(containerId));
        deploymentInfoDto.setName(containerName);
        deploymentInfoDto.setUrl(containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        // 1. Create ToolSet1
        var containerSourceDto = new ToolSetContainerSourceDto(containerId, containerName, endpointPath);
        var authSettingsDto = new ResourceAuthSettingsDto();
        String clientId = "some-client-id";
        authSettingsDto.setClientId(clientId);
        authSettingsDto.setAuthenticationType(AuthenticationTypeDto.OAUTH);
        authSettingsDto.setScopesSupported(List.of("one", "two"));

        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetDto.setSource(containerSourceDto);
        toolSetDto.setAuthSettings(authSettingsDto);
        toolSetFacade.createToolSet(toolSetDto);

        // 2. Update ToolSet1 description
        ToolSetDto updatedToolSet = createToolSetDto("1");
        updatedToolSet.setDescription("New ToolSet description");
        updatedToolSet.setSource(containerSourceDto);
        updatedToolSet.setAuthSettings(authSettingsDto);
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet);

        // 3. Verify ToolSet1
        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());
        var expected = createToolSetDto("1");
        expected.setDescription("New ToolSet description");
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setEndpoint(containerUrl + endpointPath);
        expected.setSource(containerSourceDto);
        expected.setAuthSettings(authSettingsDto);
        assertToolSet(actual, expected);

        // 4. Add roles to ToolSet1
        updatedToolSet.setSource(containerSourceDto);
        updatedToolSet.setAuthSettings(authSettingsDto);
        updatedToolSet.setEndpoint(containerUrl + endpointPath);
        updatedToolSet.setDefaultRoleLimit(new LimitDto());
        updatedToolSet.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet);
        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        assertToolSet(actual, updatedToolSet);

        // 5. Update ToolSet1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedToolSet.setRoleLimits(Map.of("role3", limitDto));
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet);
        var actualAtOldRevision = toolSetFacade.getAllToolSets();
        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        assertToolSet(actual, updatedToolSet);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 6. Update authSettings
        final long activitiesNum = historyFacade.getActivities().getTotal();

        authSettingsDto.setClientId(clientId + '1');
        updatedToolSet.setAuthSettings(authSettingsDto);
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet);

        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        Assertions.assertEquals(actual.getAuthSettings().getClientId(), clientId + '1');

        // Using activities total instead of rev num because revisions include records from deployment_entity_aud table, while activities filter them
        final long activitiesNumAfterUpdate = historyFacade.getActivities().getTotal();
        Assertions.assertEquals(activitiesNum + 1, activitiesNumAfterUpdate);

        // 7. Delete role3
        roleFacade.deleteRole("role3");
        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());

        // 8. Delete ToolSet1
        toolSetFacade.deleteToolSet(toolSetDto.getName());

        // 9. Create ToolSet2
        ToolSetDto toolSetDto2 = createToolSetDto("2");
        toolSetDto2.setEndpoint("https://test-endpoint3");
        toolSetFacade.createToolSet(toolSetDto2);

        // 10. Create role3
        RoleDto role3 = createRoleDto("3");
        roleFacade.createRole(role3);

        // 11. Create ToolSet3 with assigned role3
        ToolSetDto toolSetDto3 = createToolSetDto("3");
        toolSetDto3.setEndpoint("https://test-endpoint4");
        toolSetFacade.createToolSet(toolSetDto3);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ToolSetDto> toolSetsAfterRollback = toolSetFacade.getAllToolSets();
        Assertions.assertEquals(actualAtOldRevision, toolSetsAfterRollback);
    }

    private void assertToolSet(ToolSetDto actual, ToolSetDto expected) {
        Assertions.assertEquals(expected, actual);
    }
}

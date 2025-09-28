package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.ToolSetDto.TransportDto;
import com.epam.aidial.cfg.dto.source.ToolSetContainerSourceDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;

public abstract class ToolSetFunctionalTest {

    @Autowired
    private ToolSetFacade toolSetFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private McpClientFactory mcpClientFactory;
    @Autowired
    private DeploymentManagerService deploymentManagerService;

    @BeforeEach
    public void beforeEach() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");

        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");

        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetToolSets() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());
        ToolSetDto expected = createDto("1");

        assertToolSet(actual, expected);

        toolSetFacade.createToolSet(createDto("2"));

        Collection<ToolSetDto> actualToolSets = toolSetFacade.getAllToolSets();

        assertToolSets(actualToolSets, List.of(createDto("1"), createDto("2")));
    }

    @Test
    public void shouldSuccessfullyCreateToolSetAndGetDiscoveredTools() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetDto.setTransport(TransportDto.HTTP);
        toolSetFacade.createToolSet(toolSetDto);

        var expectedTools = Mockito.mock(McpSchema.ListToolsResult.class);
        var mcpSyncClient = Mockito.mock(McpSyncClient.class);

        Mockito.when(mcpSyncClient.initialize())
                .thenReturn(null);
        Mockito.when(mcpSyncClient.listTools(null))
                .thenReturn(expectedTools);
        Mockito.when(mcpClientFactory.create(eq(toolSetDto.getEndpoint()), eq(Transport.HTTP)))
                .thenReturn(mcpSyncClient);

        var actualTools = toolSetFacade.getDiscoveredTools(toolSetDto.getName(), null);

        Assertions.assertEquals(expectedTools, actualTools);
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteToolSet() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);
        toolSetFacade.deleteToolSet(toolSetDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> toolSetFacade.getToolSet(toolSetDto.getName()));
        Assertions.assertTrue(toolSetFacade.getAllToolSets().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateToolSet() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto updatedToolSet = createDto("1");
        updatedToolSet.setDescription("New ToolSet description");

        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*");

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());

        var expected = createDto("1");
        expected.setDescription("New ToolSet description");

        assertToolSet(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateToolSetWithCorrectHash() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);
        var hash = toolSetFacade.getToolSetWithHash(toolSetDto.getName()).hash();

        ToolSetDto updatedToolSet = createDto("1");
        updatedToolSet.setDescription("New ToolSet description");

        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, hash);

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());

        var expected = createDto("1");
        expected.setDescription("New ToolSet description");

        assertToolSet(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateToolSetWithIncorrectHash() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto updatedToolSet = createDto("1");
        updatedToolSet.setDescription("New ToolSet description");

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "test"));
    }

    @Test
    public void shouldThrowExceptionWhenRenameToolSet() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);
        ToolSetDto updatedToolSet = createDto("2");
        updatedToolSet.setDescription("New ToolSet description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*")
        );
        Assertions.assertEquals("ToolSet with name: 'ToolSet1' can not be renamed. New name: 'ToolSet2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenToolSetConcurrencyOverwrite() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), toolSetDto, "test")
        );
        Assertions.assertEquals("Optimistic lock conflict on update: toolSetName:'ToolSet1'"
                + ". Reload the data.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        ToolSetDto toolSetDto = createDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), toolSetDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. ToolSet:ToolSet1.",
                exception.getMessage());
    }

    @Test
    public void shouldResolveEndpointsForContainerSource() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerName = "test-container";
        String containerUrl = "https://container-url.com";
        String completionPath = "/api/completion";

        DeploymentInfoDto deploymentInfoDto = new DeploymentInfoDto();
        deploymentInfoDto.setId(UUID.fromString(containerId));
        deploymentInfoDto.setName("Test Container");
        deploymentInfoDto.setUrl(containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ToolSetDto toolSetDto = new ToolSetDto();
        toolSetDto.setName("container-toolset");
        toolSetDto.setDescription("Container toolset");

        ToolSetContainerSourceDto sourceDto = new ToolSetContainerSourceDto(
                containerId,
                containerName,
                completionPath
        );

        toolSetDto.setSource(sourceDto);

        // When
        toolSetFacade.createToolSet(toolSetDto);

        // Then
        ToolSetDto result = toolSetFacade.getToolSet("container-toolset");

        Assertions.assertEquals(containerUrl + completionPath, result.getEndpoint());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(2)).getById(containerId);
    }

    @Test
    public void shouldRefreshEndpointsForContainerSource() {
        // Given
        String deploymentName = "Test Container";
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerName = "test-container";
        String initialUrl = "https://initial-url.com";
        String updatedUrl = "https://updated-url.com";
        String completionPath = "/api/completion";
        String refreshedToolSetName = "refresh-toolset";

        DeploymentInfoDto initialDeploymentInfo = new DeploymentInfoDto();
        initialDeploymentInfo.setId(UUID.fromString(containerId));
        initialDeploymentInfo.setName(deploymentName);
        initialDeploymentInfo.setUrl(initialUrl);

        DeploymentInfoDto updatedDeploymentInfo = new DeploymentInfoDto();
        updatedDeploymentInfo.setId(UUID.fromString(containerId));
        updatedDeploymentInfo.setName(deploymentName);
        updatedDeploymentInfo.setUrl(updatedUrl);

        Mockito.when(deploymentManagerService.getById(containerId))
                .thenReturn(initialDeploymentInfo)
                .thenReturn(initialDeploymentInfo)
                .thenReturn(updatedDeploymentInfo)
                .thenReturn(updatedDeploymentInfo);

        ToolSetDto toolSetDto = new ToolSetDto();
        toolSetDto.setName(refreshedToolSetName);
        toolSetDto.setDescription("Refresh toolset");

        ToolSetContainerSourceDto sourceDto = new ToolSetContainerSourceDto(
                containerId,
                containerName,
                completionPath
        );

        toolSetDto.setSource(sourceDto);
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto initialResult = toolSetFacade.getToolSet(refreshedToolSetName);
        Assertions.assertEquals(initialUrl + completionPath, initialResult.getEndpoint());

        // When
        toolSetFacade.refreshEndpoints();

        // Then
        ToolSetDto refreshedResult = toolSetFacade.getToolSet(refreshedToolSetName);
        Assertions.assertEquals(updatedUrl + completionPath, refreshedResult.getEndpoint());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(2)).getById(containerId);
    }

    private ToolSetDto createDto(String suffix) {
        ToolSetDto toolSetDto = new ToolSetDto();
        toolSetDto.setName("ToolSet" + suffix);
        toolSetDto.setDescription("Description" + suffix);
        toolSetDto.setEndpoint("http://test-endpoint/api");
        toolSetDto.setRoleLimits(Map.of("role" + suffix, new LimitDto()));
        return toolSetDto;
    }

    private void assertToolSet(ToolSetDto actual, ToolSetDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getRoleLimits(), actual.getRoleLimits());
    }

    private void assertToolSets(Collection<ToolSetDto> actual, Collection<ToolSetDto> expected) {
        Map<String, ToolSetDto> actualMap = toMap(actual);
        Map<String, ToolSetDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertToolSet(actualMap.get(name), expectedMap.get(name));
        }
    }

    private Map<String, ToolSetDto> toMap(Collection<ToolSetDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(ToolSetDto::getName, Function.identity()));
    }
}

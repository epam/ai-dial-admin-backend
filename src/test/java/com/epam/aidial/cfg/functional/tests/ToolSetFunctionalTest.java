package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.dto.McpDeploymentInfoDto;
import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.ToolSetDto.TransportDto;
import com.epam.aidial.cfg.dto.source.ToolSetContainerSourceDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import com.epam.aidial.core.config.CoreToolSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createToolSetDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
        "core.client.url= http\\://localhost:3131"
})
public abstract class ToolSetFunctionalTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private ToolSetFacade toolSetFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private McpClientFactory mcpClientFactory;
    @Autowired
    private DeploymentManagerService deploymentManagerService;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;



    @BeforeEach
    public void beforeEach() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetToolSets() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());
        ToolSetDto expected = createToolSetDto("1");

        assertToolSet(actual, expected);

        toolSetFacade.createToolSet(createToolSetDto("2"));

        Collection<ToolSetDto> actualToolSets = toolSetFacade.getAllToolSets();

        assertToolSets(actualToolSets, List.of(createToolSetDto("1"), createToolSetDto("2")));
    }

    @Test
    public void shouldSuccessfullyCreateToolSetAndGetDiscoveredTools() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetDto.setTransport(TransportDto.HTTP);
        toolSetFacade.createToolSet(toolSetDto);

        var expectedTools = Mockito.mock(McpSchema.ListToolsResult.class);
        var mcpSyncClient = Mockito.mock(McpSyncClient.class);

        Mockito.when(mcpSyncClient.initialize())
                .thenReturn(null);
        Mockito.when(mcpSyncClient.listTools(null))
                .thenReturn(expectedTools);
        Mockito.when(AuthorizationTokenHolder.getToken()).thenReturn("Bearer Test");
        Mockito.when(mcpClientFactory.create(argThat(headers -> headers.contains("http://localhost:3131/v1/toolset/ToolSet1/mcp")),
                        eq(Transport.HTTP), any()))
                .thenReturn(mcpSyncClient);

        var actualTools = toolSetFacade.getDiscoveredTools(toolSetDto.getName(), null);

        Assertions.assertEquals(expectedTools, actualTools);
    }

    @Test
    public void shouldSuccessfullyCreateToolSetAndCallTool() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetDto.setTransport(TransportDto.HTTP);
        toolSetFacade.createToolSet(toolSetDto);

        var callToolRequest = Mockito.mock(McpSchema.CallToolRequest.class);
        var expectedCallToolResult = Mockito.mock(McpSchema.CallToolResult.class);
        var mcpSyncClient = Mockito.mock(McpSyncClient.class);
        Mockito.when(mcpClientFactory.create(argThat(mcpEndpoint -> mcpEndpoint.contains("http://localhost:3131/v1/toolset/ToolSet1/mcp")),
                        eq(Transport.HTTP), any()))
                .thenReturn(mcpSyncClient);
        Mockito.when(mcpSyncClient.initialize())
                .thenReturn(null);
        Mockito.when(mcpSyncClient.callTool(callToolRequest))
                .thenReturn(expectedCallToolResult);

        var actualCallToolResult = toolSetFacade.callTool(toolSetDto.getName(), callToolRequest);

        Assertions.assertEquals(expectedCallToolResult, actualCallToolResult);
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteToolSet() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);
        toolSetFacade.deleteToolSet(toolSetDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> toolSetFacade.getToolSet(toolSetDto.getName()));
        Assertions.assertTrue(toolSetFacade.getAllToolSets().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateToolSet() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto updatedToolSet = createToolSetDto("1");
        updatedToolSet.setDescription("New ToolSet description");

        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*");

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());

        var expected = createToolSetDto("1");
        expected.setDescription("New ToolSet description");

        assertToolSet(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateToolSetWithCorrectHash() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);
        var hash = toolSetFacade.getToolSetWithHash(toolSetDto.getName()).hash();

        ToolSetDto updatedToolSet = createToolSetDto("1");
        updatedToolSet.setDescription("New ToolSet description");

        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, hash);

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());

        var expected = createToolSetDto("1");
        expected.setDescription("New ToolSet description");

        assertToolSet(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateToolSetWithIncorrectHash() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto updatedToolSet = createToolSetDto("1");
        updatedToolSet.setDescription("New ToolSet description");

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "test"));
    }

    @Test
    public void shouldThrowExceptionWhenRenameToolSet() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);
        ToolSetDto updatedToolSet = createToolSetDto("2");
        updatedToolSet.setDescription("New ToolSet description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*")
        );
        Assertions.assertEquals("ToolSet with name: 'ToolSet1' can not be renamed. New name: 'ToolSet2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenToolSetConcurrencyOverwrite() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> toolSetFacade.updateToolSet(toolSetDto.getName(), toolSetDto, "test")
        );
        Assertions.assertEquals("Unable to update ToolSet 'ToolSet1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        ToolSetDto toolSetDto = createToolSetDto("1");
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

        McpDeploymentInfoDto deploymentInfoDto = new McpDeploymentInfoDto();
        deploymentInfoDto.setTransport(McpDeploymentInfoDto.McpTransport.HTTP_STREAMING);
        deploymentInfoDto.setId(containerId);
        deploymentInfoDto.setDisplayName("Test Container");
        deploymentInfoDto.setUrl(containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ToolSetDto toolSetDto = createToolSetDto("1");
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

        McpDeploymentInfoDto initialDeploymentInfo = new McpDeploymentInfoDto();
        initialDeploymentInfo.setId(containerId);
        initialDeploymentInfo.setDisplayName(deploymentName);
        initialDeploymentInfo.setUrl(initialUrl);
        initialDeploymentInfo.setTransport(McpDeploymentInfoDto.McpTransport.HTTP_STREAMING);

        McpDeploymentInfoDto updatedDeploymentInfo = new McpDeploymentInfoDto();
        updatedDeploymentInfo.setId(containerId);
        updatedDeploymentInfo.setDisplayName(deploymentName);
        updatedDeploymentInfo.setUrl(updatedUrl);
        updatedDeploymentInfo.setTransport(McpDeploymentInfoDto.McpTransport.HTTP_STREAMING);

        Mockito.when(deploymentManagerService.getById(containerId))
                .thenReturn(initialDeploymentInfo)
                .thenReturn(initialDeploymentInfo)
                .thenReturn(updatedDeploymentInfo)
                .thenReturn(updatedDeploymentInfo);

        ToolSetDto toolSetDto = createToolSetDto("1");
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

    @Test
    public void shouldSaveAndReturnToolSetWithUniqueDescriptionKeywords() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetDto.setDescriptionKeywords(new TreeSet<>(Set.of("topic1", "topic3", "topic2")));
        toolSetFacade.createToolSet(toolSetDto);

        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());

        Assertions.assertEquals(new TreeSet<>(Set.of("topic1", "topic2", "topic3")), actual.getDescriptionKeywords());
    }

    @Test
    public void shouldSuccessfullyGetCoreToolSet() {
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        CoreToolSet expected = new CoreToolSet();
        expected.setName(toolSetDto.getName());
        expected.setAuthSettings(null);
        expected.setEndpoint(toolSetDto.getEndpoint());
        expected.setTransport(CoreToolSet.Transport.HTTP);
        expected.setDisplayName(toolSetDto.getDisplayName());
        expected.setDescription(toolSetDto.getDescription());
        expected.setMaxRetryAttempts(toolSetDto.getMaxRetryAttempts());
        expected.setUserRoles(toolSetDto.getRoleLimits().keySet());

        CoreToolSet actual = toolSetFacade.getCoreToolSetWithHash(toolSetDto.getName()).core();
        actual.setCreatedAt(null);
        actual.setUpdatedAt(null);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldSuccessfullyGetFullySyncedEntitySyncStateWhenToolSetIsEqualToConfigToolSet() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetFacade.createToolSet(toolSetDto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode toolSetState = config.get("toolsets").get("ToolSet1");

        EntitySyncStateDto actualSyncState = toolSetFacade.getSyncState(toolSetDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(toolSetState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(toolSetState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.FULLY_SYNCED);
    }

    @Test
    public void shouldSuccessfullyGetInProgressTooLongEntitySyncStateWhenToolSetIsNotEqualToConfigToolSetAndUpdatedLongAgo() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        ToolSetDto toolSetDto = createToolSetDto("1");
        toolSetDto.setDescription("description OLD");
        toolSetFacade.createToolSet(toolSetDto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 122000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode configToolSetState = config.get("toolsets").get("ToolSet1");
        JsonNode currentToolSetState = configToolSetState.deepCopy();
        ((ObjectNode) currentToolSetState).put("description", "description OLD");

        EntitySyncStateDto actualSyncState = toolSetFacade.getSyncState(toolSetDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(currentToolSetState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(configToolSetState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.IN_PROGRESS_TOO_LONG);
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

    private JsonNode coreConfig() throws JsonProcessingException {
        String config = """
                {
                  "toolsets": {
                    "ToolSet1": {
                      "name": "ToolSet1",
                      "user_roles": [
                        "role1"
                      ],
                      "endpoint": "https://endpoint.test.com/toolset1",
                      "display_name": "ToolSet1",
                      "description": "description1",
                      "forward_auth_token": false,
                      "defaults": {},
                      "interceptors": [],
                      "description_keywords": [],
                      "max_retry_attempts": 1,
                      "created_at": 1000,
                      "updated_at": 1000,
                      "dependencies": [],
                      "forward_per_request_key": false,
                      "auth_settings": {
                        "authentication_type": "NONE"
                      },
                      "transport": "HTTP",
                      "allowed_tools": []
                    }
                  }
                }
                """;
        return OBJECT_MAPPER.readTree(config);
    }
}
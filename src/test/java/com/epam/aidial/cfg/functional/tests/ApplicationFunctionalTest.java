package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.ToolsClient;
import com.epam.aidial.cfg.client.dto.ApplicationDeploymentInfoDto;
import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.McpDto;
import com.epam.aidial.cfg.dto.source.ApplicationContainerSourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationEndpointsSourceDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.core.config.CoreApplication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpointAndLimits;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithMcp;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.defaultCoreFeatures;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public abstract class ApplicationFunctionalTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;
    @Autowired
    private DeploymentManagerService deploymentManagerService;
    @Autowired
    private McpClientFactory mcpClientFactory;
    @Autowired
    private ToolsClient toolsClient;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplication() {
        initRoles();
        ApplicationDto applicationDto = createDtoWithDefaults("1");

        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        ApplicationDto expected = createDtoWithDefaults("1");

        assertApplicationWithDefaults(actual, expected);

        applicationFacade.createApplication(createApplicationDtoWithEndpointAndLimits("2"));

        Collection<ApplicationInfoDto> actualApplications = applicationFacade.getAllApplications();

        assertApp(actualApplications, List.of(createApplicationDtoWithEndpointAndLimits("1"), createApplicationDtoWithEndpointAndLimits("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteApplication() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        applicationFacade.deleteApplication(applicationDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> applicationFacade.getApplication(applicationDto.getName()));
        Assertions.assertTrue(applicationFacade.getAllApplications().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplication() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");
        updatedApplication.setDescription("new application description");

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createApplicationDtoWithEndpointAndLimits("1");
        expected.setDescription("new application description");
        assertApplication(actual, expected);
    }

    @Test
    public void shouldSuccessfullyCreateAndAddInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");

        updatedApplication.setDescription("new model description");
        updatedApplication.setDefaults(Map.of());
        updatedApplication.setInterceptors(List.of("interceptor1"));

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("interceptor1"));
    }

    @Test
    public void shouldThrowExceptionWhenRenameApplication() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("2");
        updatedApplication.setDescription("new application description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*")
        );
        Assertions.assertEquals("Application with name: 'application1' can not be renamed. New name: 'application2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenApplicationConcurrencyOverwrite() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "test")
        );
        Assertions.assertEquals("Unable to update Application 'application1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. Application:application1.",
                exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateApplicationWithCorrectHash() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");
        updatedApplication.setDescription("new application description");

        var hash = applicationFacade.getApplicationWithHash(applicationDto.getName()).hash();

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, hash);

        var actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createApplicationDtoWithEndpointAndLimits("1");
        expected.setDescription("new application description");
        assertApplication(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateApplicationWithIncorrectHash() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "test"));
    }

    @Test
    public void shouldSuccessfullyCreateWithInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setName("int1");
        interceptorDto.setDescription("int1_dsc");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setInterceptors(List.of("int1"));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("int1"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateWithInterceptors() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1", "interceptor2"));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1", "interceptor2"), actualApplication.getInterceptors());

        applicationDto.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "*");

        actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"), actualApplication.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyAddNewInterceptorToTheEndOfTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ApplicationDto applicationDto1 = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto1.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2"));
        applicationFacade.createApplication(applicationDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorDto3.setEntities(List.of("application1", "application2", "application1"));
        interceptorFacade.createInterceptor(interceptorDto3);

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3"), actualApplication1.getInterceptors());

        ApplicationDto actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3"), actualApplication2.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyRemoveDeletedInterceptorFromTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorFacade.createInterceptor(interceptorDto3);

        ApplicationDto applicationDto1 = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto1.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3"));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3"));
        applicationFacade.createApplication(applicationDto2);

        interceptorFacade.deleteInterceptor("interceptor1");

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor3"), actualApplication1.getInterceptors());

        ApplicationDto actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor3"), actualApplication2.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyRemoveUpdatedInterceptorFromTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ApplicationDto applicationDto1 = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto1.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        applicationFacade.createApplication(applicationDto2);

        interceptorDto1.setEntities(List.of("application2"));
        interceptorFacade.updateInterceptor(interceptorDto1.getName(), interceptorDto1, "*");

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(List.of("interceptor2"), actualApplication1.getInterceptors());

        ApplicationDto actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor1", "interceptor2"), actualApplication2.getInterceptors());

        interceptorDto2.setEntities(null);
        interceptorFacade.updateInterceptor(interceptorDto2.getName(), interceptorDto2, "*");

        actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(actualApplication1.getInterceptors(), List.of());

        actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor1"), actualApplication2.getInterceptors());
    }

    @Test
    public void shouldThrowExceptionWhenCreateApplicationWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setDisplayName("display_name");
        applicationDto.setDisplayVersion("1.0");
        applicationFacade.createApplication(applicationDto);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setDisplayName("display_name");
        applicationDto2.setDisplayVersion("1.0");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> applicationFacade.createApplication(applicationDto2)
        );
        Assertions.assertEquals("Application with display name: 'display_name' and display version: '1.0' already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateApplicationWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setDisplayName("display_name");
        applicationFacade.createApplication(applicationDto);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setDisplayName("display_name_2");
        applicationFacade.createApplication(applicationDto2);

        applicationDto.setDisplayName("display_name_2");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "*")
        );
        Assertions.assertEquals("Application with display name: 'display_name_2' and display version: 'null' already exists", exception.getMessage());
    }

    @Test
    public void shouldSaveAndReturnApplicationWithUniqueTopics() {
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setEndpoint("http://my-endpoint");
        applicationDto.setTopics(new TreeSet<>(Set.of("topic1", "topic3", "topic2")));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertEquals(new TreeSet<>(Set.of("topic1", "topic2", "topic3")), actual.getTopics());
    }

    @Test
    public void shouldSuccessfullyGetCoreApplication() {
        initRoles();

        ApplicationDto applicationDto = createDtoWithDefaults("1");
        applicationFacade.createApplication(applicationDto);

        CoreApplication expected = new CoreApplication();
        expected.setName(applicationDto.getName());
        expected.setDisplayName(applicationDto.getDisplayName());
        expected.setDescription(applicationDto.getDescription());
        expected.setEndpoint(applicationDto.getEndpoint());
        expected.setDefaults(applicationDto.getDefaults());
        expected.setApplicationProperties(applicationDto.getApplicationProperties());
        expected.setFeatures(defaultCoreFeatures());
        expected.setUserRoles(applicationDto.getRoleLimits().keySet());
        expected.setRoutes(null);
        expected.setForwardAuthToken(applicationDto.getForwardAuthToken());

        CoreApplication actual = applicationFacade.getCoreApplicationWithHash(applicationDto.getName()).core();
        actual.setCreatedAt(null);
        actual.setUpdatedAt(null);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldSuccessfullyGetApplicationWithMcp() {
        ApplicationDto applicationDto = createApplicationDtoWithMcp("1");
        applicationFacade.createApplication(applicationDto);
        var actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createApplicationDtoWithMcp("1");
        expected.setRoleLimits(Map.of());
        assertApplication(actual, expected);
    }

    @Test
    public void shouldSuccessfullyGetFullySyncedEntitySyncStateWhenApplicationIsEqualToConfigApplication() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode applicationState = config.get("applications").get("application1");

        EntitySyncStateDto actualSyncState = applicationFacade.getSyncState(applicationDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(applicationState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(applicationState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.FULLY_SYNCED);
    }

    @Test
    public void shouldSuccessfullyGetInProgressTooLongEntitySyncStateWhenApplicationIsNotEqualToConfigApplicationAndUpdatedLongAgo() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationDto.setDescription("description OLD");
        applicationFacade.createApplication(applicationDto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 122000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode configApplicationState = config.get("applications").get("application1");
        JsonNode currentApplicationState = configApplicationState.deepCopy();
        ((ObjectNode) currentApplicationState).put("description", "description OLD");

        EntitySyncStateDto actualSyncState = applicationFacade.getSyncState(applicationDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(currentApplicationState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(configApplicationState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.IN_PROGRESS_TOO_LONG);
    }

    @Test
    public void shouldSuccessfullyCreateApplicationAndGetDiscoveredTools() {
        var mcp = new McpDto();
        mcp.setEndpoint("https://endpoint.test.com/application1");
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationDto.setMcp(mcp);
        applicationFacade.createApplication(applicationDto);

        var expectedTools = Mockito.mock(McpSchema.ListToolsResult.class);
        when(toolsClient.getTools(eq(applicationDto.getName()), isNull()))
                .thenReturn(expectedTools);

        var actualTools = applicationFacade.getDiscoveredTools(applicationDto.getName(), null);

        Assertions.assertEquals(expectedTools, actualTools);

    }

    @Test
    public void shouldSuccessfullyCreateApplicationAndCallTool() {
        var mcp = new McpDto();
        mcp.setEndpoint("https://endpoint.test.com/application1");
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationDto.setMcp(mcp);
        applicationFacade.createApplication(applicationDto);

        var callToolRequest = Mockito.mock(McpSchema.CallToolRequest.class);
        var expectedCallToolResult = Mockito.mock(McpSchema.CallToolResult.class);
        var mcpSyncClient = Mockito.mock(McpSyncClient.class);
        Mockito.when(mcpSyncClient.initialize())
                .thenReturn(null);
        Mockito.when(mcpClientFactory.create(eq("http://localhost:8081/v1/deployments/application1/mcp"),
                eq(ToolSet.Transport.HTTP), isNull())).thenReturn(mcpSyncClient);
        Mockito.when(mcpSyncClient.callTool(callToolRequest))
                .thenReturn(expectedCallToolResult);

        var actualCallToolResult = applicationFacade.callTool(applicationDto.getName(), callToolRequest);

        Assertions.assertEquals(expectedCallToolResult, actualCallToolResult);
    }

    @Test
    public void shouldResolveEndpointsForContainerSource() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerName = "test-container";
        String containerUrl = "https://container-url.com";
        String completionPath = "/api/completion";
        String mcpCompletionPath = "/mcp/";

        ApplicationDeploymentInfoDto deploymentInfoDto =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setSource(new ApplicationContainerSourceDto(
                containerId,
                containerName,
                completionPath,
                mcpCompletionPath
        ));

        // When
        applicationFacade.createApplication(applicationDto);

        // Then
        ApplicationDto result = applicationFacade.getApplication("application1");

        Assertions.assertEquals(containerUrl + completionPath, result.getEndpoint());
        Assertions.assertEquals(containerUrl + mcpCompletionPath, result.getMcp().getEndpoint());
        Assertions.assertInstanceOf(ApplicationContainerSourceDto.class, result.getSource());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(1)).getById(containerId);
    }

    @Test
    public void shouldRefreshEndpointsForContainerSource() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440000";
        String containerName = "test-container";
        String initialUrl = "https://initial-url.com";
        String updatedUrl = "https://updated-url.com";
        String completionPath = "/api/completion";
        String mcpCompletionPath = "/mcp/";

        ApplicationDeploymentInfoDto initialDeploymentInfo =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", initialUrl);

        ApplicationDeploymentInfoDto updatedDeploymentInfo =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", updatedUrl);

        Mockito.when(deploymentManagerService.getById(containerId))
                .thenReturn(initialDeploymentInfo)
                .thenReturn(updatedDeploymentInfo);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setSource(new ApplicationContainerSourceDto(
                containerId,
                containerName,
                completionPath,
                mcpCompletionPath
        ));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto initialResult = applicationFacade.getApplication("application1");
        Assertions.assertEquals(initialUrl + completionPath, initialResult.getEndpoint());
        Assertions.assertEquals(initialUrl + mcpCompletionPath, initialResult.getMcp().getEndpoint());

        // When
        applicationFacade.refreshEndpoints();

        // Then
        ApplicationDto refreshedResult = applicationFacade.getApplication("application1");
        Assertions.assertEquals(updatedUrl + completionPath, refreshedResult.getEndpoint());
        Assertions.assertEquals(updatedUrl + mcpCompletionPath, refreshedResult.getMcp().getEndpoint());

        Mockito.verify(deploymentManagerService, Mockito.atLeast(1)).getById(containerId);
    }

    @Test
    public void shouldResetEndpointsSourceWhenChangingApplicationSourceFromEndpointsToContainer() {
        // Create an Application with endpoints source
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        // Verify the Application has endpoints source
        ApplicationDto actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertInstanceOf(ApplicationEndpointsSourceDto.class, actualApplication.getSource());
        Assertions.assertEquals("endpoint1", actualApplication.getEndpoint());

        // Update the Application to Container source
        String containerId = "container-123";
        String containerUrl = "https://container-url.com";
        String completionPath = "/api/completion";
        String mcpCompletionPath = "/mcp/";

        ApplicationDeploymentInfoDto deploymentInfoDto =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", containerUrl);

        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ApplicationDto updatedApplication = createBaseApplicationDto("1");
        updatedApplication.setSource(new ApplicationContainerSourceDto(
                containerId,
                "test-container",
                completionPath,
                mcpCompletionPath
        ));
        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");

        // Verify the Application now has Container source
        actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertInstanceOf(ApplicationContainerSourceDto.class, actualApplication.getSource());
        ApplicationContainerSourceDto containerSource =
                (ApplicationContainerSourceDto) actualApplication.getSource();
        Assertions.assertEquals(containerId, containerSource.containerId());
        Assertions.assertEquals(containerUrl + completionPath, actualApplication.getEndpoint());
        Assertions.assertEquals(containerUrl + mcpCompletionPath, actualApplication.getMcp().getEndpoint());

        // Update the Application back to endpoints source
        ApplicationDto revertedApplication = createApplicationDtoWithEndpoint("1");
        revertedApplication.setEndpoint("https://new-endpoint.com");
        applicationFacade.updateApplication(applicationDto.getName(), revertedApplication, "*");

        // Verify the Application now has endpoints source again
        actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertInstanceOf(ApplicationEndpointsSourceDto.class, actualApplication.getSource());
        Assertions.assertEquals("https://new-endpoint.com", actualApplication.getEndpoint());
    }

    @Test
    public void shouldNotCreateMcpForContainerSourceWhenMcpAndPathAreAbsent() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440001";
        String containerUrl = "https://container-no-mcp.com";
        String completionPath = "/api/completion";

        ApplicationDeploymentInfoDto deploymentInfoDto =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", containerUrl);
        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setSource(new ApplicationContainerSourceDto(
                containerId,
                "test-container",
                completionPath,
                null
        ));

        // When
        applicationFacade.createApplication(applicationDto);

        // Then
        ApplicationDto result = applicationFacade.getApplication("application1");
        Assertions.assertEquals(containerUrl + completionPath, result.getEndpoint());
        Assertions.assertNull(result.getMcp());
    }

    @Test
    public void shouldCreateMcpForContainerSourceWhenOnlyMcpPathIsProvided() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440002";
        String containerUrl = "https://container-mcp-path.com";
        String completionPath = "/api/completion";
        String mcpCompletionPath = "/mcp/";

        ApplicationDeploymentInfoDto deploymentInfoDto =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", containerUrl);
        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setSource(new ApplicationContainerSourceDto(
                containerId,
                "test-container",
                completionPath,
                mcpCompletionPath
        ));

        // When
        applicationFacade.createApplication(applicationDto);

        // Then
        ApplicationDto result = applicationFacade.getApplication("application1");
        Assertions.assertNotNull(result.getMcp());
        Assertions.assertEquals(containerUrl + mcpCompletionPath, result.getMcp().getEndpoint());
    }

    @Test
    public void shouldSetMcpEndpointToContainerUrlWhenMcpIsProvidedWithoutPath() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440003";
        String containerUrl = "https://container-mcp-only.com";
        String completionPath = "/api/completion";

        ApplicationDeploymentInfoDto deploymentInfoDto =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", containerUrl);
        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setSource(new ApplicationContainerSourceDto(
                containerId,
                "test-container",
                completionPath,
                null
        ));
        McpDto mcp = new McpDto();
        mcp.setEndpoint("stale-endpoint");
        applicationDto.setMcp(mcp);

        // When
        applicationFacade.createApplication(applicationDto);

        // Then
        ApplicationDto result = applicationFacade.getApplication("application1");
        Assertions.assertNotNull(result.getMcp());
        Assertions.assertEquals(containerUrl, result.getMcp().getEndpoint());
    }

    @Test
    public void shouldSetMcpEndpointToContainerUrlPlusPathWhenBothProvided() {
        // Given
        String containerId = "550e8400-e29b-41d4-a716-446655440004";
        String containerUrl = "https://container-both.com";
        String completionPath = "/api/completion";
        String mcpCompletionPath = "/mcp/";

        ApplicationDeploymentInfoDto deploymentInfoDto =
                new ApplicationDeploymentInfoDto(containerId, "Test Container", containerUrl);
        Mockito.when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfoDto);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setSource(new ApplicationContainerSourceDto(
                containerId,
                "test-container",
                completionPath,
                mcpCompletionPath
        ));
        McpDto mcp = new McpDto();
        mcp.setEndpoint("stale-endpoint");
        applicationDto.setMcp(mcp);

        // When
        applicationFacade.createApplication(applicationDto);

        // Then
        ApplicationDto result = applicationFacade.getApplication("application1");
        Assertions.assertNotNull(result.getMcp());
        Assertions.assertEquals(containerUrl + mcpCompletionPath, result.getMcp().getEndpoint());
    }

    private ApplicationDto createDtoWithDefaults(String suffix) {
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits(suffix);
        applicationDto.setDefaults(Map.of("max_tokens", 8000));
        return applicationDto;
    }

    private void assertApplication(ApplicationDto actual, ApplicationDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getRoleLimits(), actual.getRoleLimits());
        Assertions.assertEquals(expected.getMcp(), actual.getMcp());
    }

    private void assertApplicationWithDefaults(ApplicationDto actual, ApplicationDto expected) {
        assertApplication(actual, expected);
        Assertions.assertEquals(expected.getDefaults(), actual.getDefaults());
    }

    private void assertApp(Collection<ApplicationInfoDto> actual, Collection<ApplicationDto> expected) {
        Map<String, ApplicationInfoDto> actualMap = toMap(actual, ApplicationInfoDto::getName);
        Map<String, ApplicationDto> expectedMap = toMap(expected, ApplicationDto::getName);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertApp(actualMap.get(name), expectedMap.get(name));
        }
    }

    private void assertApp(ApplicationInfoDto actual, ApplicationDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getSource(), actual.getSource());
    }

    private <T> Map<String, T> toMap(Collection<T> dtos, Function<T, String> getName) {
        return dtos.stream()
                .collect(Collectors.toMap(getName, Function.identity()));
    }

    private JsonNode coreConfig() throws JsonProcessingException {
        String config = """
                {
                  "applications": {
                    "application1": {
                      "name": "application1",
                      "user_roles": [],
                      "endpoint": "endpoint1",
                      "display_name": "application1",
                      "description": "description1",
                      "forward_auth_token": false,
                      "features": {
                        "system_prompt_supported": true,
                        "tools_supported": false,
                        "seed_supported": false,
                        "url_attachments_supported": false,
                        "folder_attachments_supported": false,
                        "allow_resume": true,
                        "accessible_by_per_request_key": true,
                        "content_parts_supported": false,
                        "temperature_supported": true,
                        "parallel_tool_calls_supported": true,
                        "assistant_attachments_in_request_supported": false,
                        "max_tokens_supported": true,
                        "custom_temperature_supported": true
                      },
                      "defaults": {},
                      "responses_defaults": {},
                      "interceptors": [],
                      "description_keywords": [],
                      "max_retry_attempts": 1,
                      "created_at": 1000,
                      "updated_at": 1000,
                      "dependencies": [],
                      "application_properties": {},
                      "routes": {}
                    }
                  }
                }
                """;
        return OBJECT_MAPPER.readTree(config);
    }
}
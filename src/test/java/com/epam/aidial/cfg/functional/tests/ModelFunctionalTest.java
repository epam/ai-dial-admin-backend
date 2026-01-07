package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.InferenceDeploymentInfoDto;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.source.AdapterSourceDto;
import com.epam.aidial.cfg.dto.source.ModelContainerSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.core.config.CoreModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAdapterDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithLimitsAndEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.defaultCoreFeatures;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public abstract class ModelFunctionalTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private DeploymentManagerService deploymentManagerService;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
        roleFacade.createRole(createRoleDto("3"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetModels() {
        initRoles();

        ModelDto modelDto = createDtoWithDefaults("1");

        modelFacade.createModel(modelDto);

        ModelDto actual = modelFacade.getModel(modelDto.getName());

        assertModel(actual, expectedDto1WithDefaults());

        modelFacade.createModel(createModelDtoWithLimitsAndEndpoint("2"));

        Collection<ModelDto> actualModels = modelFacade.getAll();

        assertModels(actualModels, expectedDtos());
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteModel() {
        initRoles();

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelFacade.createModel(modelDto);

        modelFacade.deleteModel(modelDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> modelFacade.getModel(modelDto.getName()));
        Assertions.assertTrue(modelFacade.getAll().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateModel() {
        initRoles();
        adapterFacade.createAdapter(createAdapterDto("1"));
        adapterFacade.createAdapter(createAdapterDto("2"));

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setSource(new AdapterSourceDto("adapter1", "/chat/completions"));
        modelFacade.createModel(modelDto);

        ModelDto updatedModel = createModelDtoWithLimitsAndEndpoint("1");
        updatedModel.setSource(new AdapterSourceDto("adapter2", "/newEndpointDeploymentName/chat/completions"));
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");

        ModelDto actual = modelFacade.getModel(modelDto.getName());
        var expected = createModelDtoWithLimitsAndEndpoint("1");
        expected.setDescription("new model description");
        expected.setDefaults(Map.of());
        expected.setMaxRetryAttempts(1);
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setSource(new AdapterSourceDto("adapter2", "/newEndpointDeploymentName/chat/completions"));
        expected.setEndpoint(null);
        updatedModel.setDefaults(Map.of());
        updatedModel.setDefaultRoleLimit(new LimitDto());
        assertModel(actual, expected);

        updatedModel.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");
        actual = modelFacade.getModel(modelDto.getName());
        expected.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        assertModel(actual, expected);

        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedModel.setRoleLimits(Map.of("role3", limitDto));
        updatedModel.setSource(new AdapterSourceDto("adapter2", "/chat/completions"));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");
        actual = modelFacade.getModel(modelDto.getName());
        expected.setRoleLimits(Map.of("role3", limitDto));
        expected.setSource(new AdapterSourceDto("adapter2", "/chat/completions"));
        assertModel(actual, expected);

        roleFacade.deleteRole("role3");

        actual = modelFacade.getModel(modelDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenRenameModel() {
        initRoles();

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelFacade.createModel(modelDto);
        ModelDto updatedModel = createModelDtoWithLimitsAndEndpoint("2");
        updatedModel.setDescription("new model description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> modelFacade.updateModel(modelDto.getName(), updatedModel, "*")
        );
        Assertions.assertEquals("Model with name: 'model1' can not be renamed. New name: 'model2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyCreateAndAddInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelFacade.createModel(modelDto);
        ModelDto updatedModel = createModelDtoWithLimitsAndEndpoint("1");
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        updatedModel.setInterceptors(List.of("interceptor1"));

        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");

        ModelDto actual = modelFacade.getModel(modelDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("interceptor1"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateWithInterceptors() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1", "interceptor2"));
        modelFacade.createModel(modelDto);

        ModelDto actualModel = modelFacade.getModel(modelDto.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1", "interceptor2"),
                actualModel.getInterceptors());

        modelDto.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        actualModel = modelFacade.getModel(modelDto.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"),
                actualModel.getInterceptors());
    }

    @Test
    public void shouldThrowExceptionWhenModelConcurrencyOverwrite() {
        initRoles();

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelFacade.createModel(modelDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> modelFacade.updateModel(modelDto.getName(), modelDto, "test")
        );
        Assertions.assertEquals("Unable to update Model 'model1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        initRoles();

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelFacade.createModel(modelDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> modelFacade.updateModel(modelDto.getName(), modelDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check.",
                exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyAddNewInterceptorToTheEndOfTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ModelDto modelDto1 = createModelDtoWithLimitsAndEndpoint("1");
        modelDto1.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        modelFacade.createModel(modelDto1);

        ModelDto modelDto2 = createModelDtoWithLimitsAndEndpoint("2");
        modelDto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2"));
        modelFacade.createModel(modelDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorDto3.setEntities(List.of("model1", "model2", "model1"));
        interceptorFacade.createInterceptor(interceptorDto3);

        ModelDto actualModel1 = modelFacade.getModel(modelDto1.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3"),
                actualModel1.getInterceptors());

        ModelDto actualModel2 = modelFacade.getModel(modelDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3"),
                actualModel2.getInterceptors());
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

        ModelDto modelDto1 = createModelDtoWithLimitsAndEndpoint("1");
        modelDto1.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3"));
        modelFacade.createModel(modelDto1);

        ModelDto modelDto2 = createModelDtoWithLimitsAndEndpoint("2");
        modelDto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3"));
        modelFacade.createModel(modelDto2);

        interceptorFacade.deleteInterceptor("interceptor1");

        ModelDto actualModel1 = modelFacade.getModel(modelDto1.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor3"), actualModel1.getInterceptors());

        ModelDto actualModel2 = modelFacade.getModel(modelDto2.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor3"), actualModel2.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyRemoveUpdatedInterceptorFromTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ModelDto modelDto1 = createModelDtoWithLimitsAndEndpoint("1");
        modelDto1.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        modelFacade.createModel(modelDto1);

        ModelDto modelDto2 = createModelDtoWithLimitsAndEndpoint("2");
        modelDto2.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        modelFacade.createModel(modelDto2);

        interceptorDto1.setEntities(List.of("model2"));
        interceptorFacade.updateInterceptor(interceptorDto1.getName(), interceptorDto1, "*");

        ModelDto actualModel1 = modelFacade.getModel(modelDto1.getName());
        Assertions.assertEquals(List.of("interceptor2"), actualModel1.getInterceptors());

        ModelDto actualModel2 = modelFacade.getModel(modelDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor1", "interceptor2"), actualModel2.getInterceptors());

        interceptorDto2.setEntities(null);
        interceptorFacade.updateInterceptor(interceptorDto2.getName(), interceptorDto2, "*");

        actualModel1 = modelFacade.getModel(modelDto1.getName());
        Assertions.assertNull(actualModel1.getInterceptors());

        actualModel2 = modelFacade.getModel(modelDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor1"), actualModel2.getInterceptors());
    }

    @Test
    public void shouldThrowExceptionWhenCreateModelWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setDisplayName("display_name");
        modelDto.setDisplayVersion("1.0");
        modelFacade.createModel(modelDto);

        ModelDto modelDto2 = createModelDtoWithLimitsAndEndpoint("2");
        modelDto2.setDisplayName("display_name");
        modelDto2.setDisplayVersion("1.0");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> modelFacade.createModel(modelDto2)
        );
        Assertions.assertEquals("Model with display name: 'display_name' and display version: '1.0' already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateModelWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setDisplayName("display_name");
        modelFacade.createModel(modelDto);

        ModelDto modelDto2 = createModelDtoWithLimitsAndEndpoint("2");
        modelDto2.setDisplayName("display_name_2");
        modelFacade.createModel(modelDto2);

        modelDto.setDisplayName("display_name_2");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> modelFacade.updateModel(modelDto.getName(), modelDto, "*")
        );
        Assertions.assertEquals("Model with display name: 'display_name_2' and display version: 'null' already exists", exception.getMessage());
    }

    @Test
    public void shouldSaveAndReturnModelWithUniqueTopics() {
        ModelDto modelDto = createModelDto("1");
        modelDto.setTopics(List.of("topic1", "topic2", "topic1", "topic3", "topic2"));
        modelFacade.createModel(modelDto);

        ModelDto actual = modelFacade.getModel(modelDto.getName());

        Assertions.assertEquals(List.of("topic1", "topic2", "topic3"), actual.getTopics());
    }

    @Test
    public void shouldSuccessfullyGetCoreModel() {
        initRoles();

        ModelDto modelDto = createDtoWithDefaults("1");
        modelFacade.createModel(modelDto);

        CoreModel expected = new CoreModel();
        expected.setTokenizerModel(modelDto.getTokenizerModel());
        expected.setName(modelDto.getName());
        expected.setDisplayName(modelDto.getDisplayName());
        expected.setDescription(modelDto.getDescription());
        expected.setEndpoint(modelDto.getEndpoint());
        expected.setDefaults(modelDto.getDefaults());
        expected.setFeatures(defaultCoreFeatures());
        expected.setMaxRetryAttempts(modelDto.getMaxRetryAttempts());
        expected.setUserRoles(modelDto.getRoleLimits().keySet());

        CoreModel actual = modelFacade.getCoreModelWithHash(modelDto.getName()).core();
        actual.setCreatedAt(null);
        actual.setUpdatedAt(null);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldResetAdapterToNullWhenChangingModelSourceFromAdapterToContainer() {
        initRoles();

        // Create an adapter
        adapterFacade.createAdapter(createAdapterDto("1"));

        // Create a model with adapter source
        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setSource(new AdapterSourceDto("adapter1", "/chat/completions"));
        modelFacade.createModel(modelDto);

        // Verify the model has adapter source
        ModelDto actualModel = modelFacade.getModel(modelDto.getName());
        Assertions.assertNotNull(actualModel.getSource());
        Assertions.assertInstanceOf(AdapterSourceDto.class, actualModel.getSource());
        AdapterSourceDto adapterSource = (AdapterSourceDto) actualModel.getSource();
        Assertions.assertEquals("adapter1", adapterSource.adapterName());

        // Verify the adapter has the model in its models list
        var adapter = adapterFacade.getAdapter("adapter1");
        Assertions.assertTrue(adapter.getModels().contains("model1"));

        // Update the model to container source
        final String containerId = "container-123";
        DeploymentInfoDto deploymentInfo = new InferenceDeploymentInfoDto();
        deploymentInfo.setUrl("http://dial-test-host-name.ooops/yes/no/true/false");
        when(deploymentManagerService.getById(containerId)).thenReturn(deploymentInfo);

        ModelDto updatedModel = createModelDtoWithLimitsAndEndpoint("1");
        updatedModel.setSource(new ModelContainerSourceDto(containerId, "test-container", "/chat/completions"));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");

        // Verify the model now has container source (not adapter source)
        actualModel = modelFacade.getModel(modelDto.getName());
        Assertions.assertNotNull(actualModel.getSource());
        Assertions.assertInstanceOf(ModelContainerSourceDto.class, actualModel.getSource());
        ModelContainerSourceDto containerSource = (ModelContainerSourceDto) actualModel.getSource();
        Assertions.assertEquals("container-123", containerSource.containerId());

        // Verify the adapter no longer has the model in its models list
        adapter = adapterFacade.getAdapter("adapter1");
        Assertions.assertFalse(adapter.getModels().contains("model1"),
                "Adapter should not contain the model after switching to container source");

        // Add updatedModel (with Container) to Adapter and save adapter. model source should be switched to adapter back
        // Simulate adding the model (now container source) back to the adapter
        adapter.setModels(List.of(actualModel.getName()));
        adapterFacade.updateAdapter(adapter.getName(), adapter, "*");

        // Verify the model now has adapter source again
        actualModel = modelFacade.getModel(modelDto.getName());
        Assertions.assertNotNull(actualModel.getSource());
        Assertions.assertInstanceOf(AdapterSourceDto.class, actualModel.getSource());
        AdapterSourceDto adapterSourceAgain = (AdapterSourceDto) actualModel.getSource();
        Assertions.assertEquals("adapter1", adapterSourceAgain.adapterName());
        // Verify the adapter has the model again in its models list
        adapter = adapterFacade.getAdapter("adapter1");
        Assertions.assertTrue(adapter.getModels().contains("model1"),
                "Adapter should contain the model after switching back to adapter source");
    }

    @Test
    public void shouldSuccessfullyGetFullySyncedEntitySyncStateWhenModelIsEqualToConfigModel() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDto("1");
        modelFacade.createModel(modelDto);

        ObjectNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode modelState = coreModel();

        EntitySyncStateDto actualSyncState = modelFacade.getSyncState(modelDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(modelState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(modelState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.FULLY_SYNCED);
    }

    @Test
    public void shouldSuccessfullyGetInProgressTooLongEntitySyncStateWhenModelIsNotEqualToConfigModelAndUpdatedLongAgo() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDto("1");
        modelDto.setDescription("description OLD");
        modelFacade.createModel(modelDto);

        ObjectNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 122000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode configModelState = coreModel();
        JsonNode currentModelState = ((ObjectNode) coreModel()).put("description", "description OLD");

        EntitySyncStateDto actualSyncState = modelFacade.getSyncState(modelDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(currentModelState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(configModelState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.IN_PROGRESS_TOO_LONG);
    }

    private void assertModels(Collection<ModelDto> actual, Collection<ModelDto> expected) {
        Map<String, ModelDto> actualMap = toMap(actual);
        Map<String, ModelDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertModel(actualMap.get(name), expectedMap.get(name));
        }
    }

    private Map<String, ModelDto> toMap(Collection<ModelDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(ModelDto::getName, Function.identity()));
    }

    private void assertModel(ModelDto actual, ModelDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private ModelDto expectedDto1() {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model1");
        modelDto.setDisplayName("model1");
        modelDto.setDescription("description1");
        modelDto.setRoleLimits(Map.of(
                "role1", new LimitDto()
        ));
        modelDto.setDefaults(Map.of());
        modelDto.setDefaultRoleLimit(new LimitDto());
        modelDto.setSource(new ModelEndpointsSourceDto());
        modelDto.setEndpoint("https://endpoint1/chat/completions");
        return modelDto;
    }

    private ModelDto expectedDto1WithDefaults() {
        ModelDto modelDto = expectedDto1();
        modelDto.setDefaults(Map.of("max_tokens", 8000));
        modelDto.setMaxRetryAttempts(1);
        return modelDto;
    }

    private Collection<ModelDto> expectedDtos() {
        ModelDto modelDto1 = createModelDto("1");
        modelDto1.setRoleLimits(Map.of(
                "role1", new LimitDto()
        ));
        modelDto1.setDefaultRoleLimit(new LimitDto());
        modelDto1.setDefaults(Map.of("max_tokens", 8000));
        modelDto1.setEndpoint("https://endpoint1/chat/completions");
        modelDto1.setSource(new ModelEndpointsSourceDto());
        modelDto1.setMaxRetryAttempts(1);

        ModelDto modelDto2 = createModelDto("2");
        modelDto2.setRoleLimits(Map.of(
                "role2", new LimitDto()
        ));
        modelDto2.setDefaultRoleLimit(new LimitDto());
        modelDto2.setDefaults(Map.of());
        modelDto2.setEndpoint("https://endpoint1/chat/completions");
        modelDto2.setSource(new ModelEndpointsSourceDto());
        modelDto2.setMaxRetryAttempts(1);

        return List.of(modelDto1, modelDto2);
    }

    private ModelDto createDtoWithDefaults(String suffix) {
        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint(suffix);
        modelDto.setDefaults(Map.of("max_tokens", 8000));
        return modelDto;
    }

    private ObjectNode coreConfig() throws JsonProcessingException {
        ObjectNode coreModels = JsonNodeFactory.instance.objectNode();
        coreModels.set("model1", coreModel());

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.set("models", coreModels);

        return config;
    }

    private JsonNode coreModel() throws JsonProcessingException {
        String model = """
                {
                   "name": "model1",
                   "userRoles": [],
                   "displayName": "model1",
                   "description": "description1",
                   "forwardAuthToken": false,
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
                     "assistant_attachments_in_request_supported": false
                   },
                   "defaults": {},
                   "interceptors": [],
                   "descriptionKeywords": [],
                   "maxRetryAttempts": 1,
                   "createdAt": 1000,
                   "updatedAt": 1000,
                   "upstreams": []
                 }
                """;
        return OBJECT_MAPPER.readTree(model);
    }
}

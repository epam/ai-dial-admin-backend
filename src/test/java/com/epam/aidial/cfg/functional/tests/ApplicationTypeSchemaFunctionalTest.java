package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.epam.aidial.cfg.domain.service.ExternalSchemaLoader;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.invalidState;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.validState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public abstract class ApplicationTypeSchemaFunctionalTest {

    @Autowired
    private ApplicationTypeSchemaFacade typeSchemaFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;
    @Autowired
    private ExternalSchemaLoader externalSchemaLoader;

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private ApplicationTypeSchemaDto dto;
    private ApplicationTypeSchemaDto dto2;

    @BeforeEach
    public void beforeEach() throws JsonProcessingException {
        clearInvocations(externalSchemaLoader);
        var dtosJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        dto = objectMapper.readValue(dtosJson, new TypeReference<>() {
        });
        dto2 = objectMapper.readValue(dtosJson, new TypeReference<>() {
        });
        dto2.setId(dto.getId() + 2);

    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplicationTypeSchema() {
        // when
        typeSchemaFacade.create(dto);
        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        dto.setApplications(List.of());
        dto.setInterceptors(List.of());
        dto.setApplicationTypeAssistantAttachmentsInRequestSupported(false);
        dto.setApplicationTypeSchemaEndpoint("https://test.com/endpoint");
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplicationTypeSchemaWithApplication() {
        // given
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application1"));
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());
        dto.setApplicationTypeAssistantAttachmentsInRequestSupported(true);

        // when
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        typeSchemaFacade.create(dto);

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual).isEqualTo(dto);

        ApplicationDto updatedApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(updatedApplication.getEndpoint()).isNull();
        Assertions.assertThat(updatedApplication.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(220));
        Assertions.assertThat(updatedApplication.getValidityState())
                .isEqualTo(invalidState("$: required property 'temperature' not found, "
                        + "$: required property 'instructions' not found, $: required property 'model' not found, $: required property 'web_api_toolset' not found"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplicationTypeSchemaWithApplication() {
        // given
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of(applicationDto.getName()));
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());
        dto.setApplicationTypeAssistantAttachmentsInRequestSupported(true);

        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        typeSchemaFacade.create(dto);

        // when
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        dto.setApplications(List.of(applicationDto.getName()));
        dto.setRequired(List.of("instructions"));
        typeSchemaFacade.update(dto.getId(), dto, "*");

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual).isEqualTo(dto);

        ApplicationDto updatedApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(updatedApplication.getEndpoint()).isNull();
        Assertions.assertThat(updatedApplication.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(320));
        Assertions.assertThat(updatedApplication.getValidityState())
                .isEqualTo(invalidState("$: required property 'instructions' not found"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplicationTypeSchemaWithInterceptors() {
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        dto.setApplications(List.of());
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1"));

        // when
        typeSchemaFacade.create(dto);

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1")).isEqualTo(actual.getInterceptors());

        dto.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor2"));
        typeSchemaFacade.update(dto.getId(), dto, "*");

        actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor2")).isEqualTo(actual.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyAddNewInterceptorToTheEndOfTheInterceptorsList() {
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        dto.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        typeSchemaFacade.create(dto);

        dto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2"));
        typeSchemaFacade.create(dto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorDto3.setApplicationTypeSchemas(List.of("https://test-schema.example", "https://test-schema.example2", "https://test-schema.example"));
        interceptorFacade.createInterceptor(interceptorDto3);

        ApplicationTypeSchemaDto actualDto1 = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3")).isEqualTo(actualDto1.getInterceptors());

        ApplicationTypeSchemaDto actualDto2 = typeSchemaFacade.get(dto2.getId());
        Assertions.assertThat(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3")).isEqualTo(actualDto2.getInterceptors());

        InterceptorDto actualInterceptorDto = interceptorFacade.getInterceptor("interceptor3");
        Assertions.assertThat(List.of("https://test-schema.example", "https://test-schema.example2")).isEqualTo(actualInterceptorDto.getApplicationTypeSchemas());
    }

    @Test
    public void shouldSuccessfullyRemoveDeletedInterceptorFromTheInterceptorsList() throws JsonProcessingException {
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorFacade.createInterceptor(interceptorDto3);

        dto.setInterceptors(List.of("interceptor2", "interceptor1", "interceptor2", "interceptor3", "interceptor3"));
        typeSchemaFacade.create(dto);

        dto2.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        typeSchemaFacade.create(dto2);

        interceptorFacade.deleteInterceptor("interceptor1");

        ApplicationTypeSchemaDto actualDto1 = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of("interceptor2", "interceptor2", "interceptor3", "interceptor3")).isEqualTo(actualDto1.getInterceptors());

        ApplicationTypeSchemaDto actualDto2 = typeSchemaFacade.get(dto2.getId());
        Assertions.assertThat(List.of("interceptor2")).isEqualTo(actualDto2.getInterceptors());

        Assertions.assertThat(List.of("https://test-schema.example", "https://test-schema.example2"))
                .isEqualTo(interceptorFacade.getInterceptor("interceptor2").getApplicationTypeSchemas());
        Assertions.assertThat(List.of("https://test-schema.example"))
                .isEqualTo(interceptorFacade.getInterceptor("interceptor3").getApplicationTypeSchemas());
    }

    @Test
    public void shouldSuccessfullyRemoveUpdatedInterceptorFromTheInterceptorsList() throws JsonProcessingException {
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        dto.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        typeSchemaFacade.create(dto);

        dto2.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        typeSchemaFacade.create(dto2);

        interceptorDto1.setApplicationTypeSchemas(List.of("https://test-schema.example2"));
        interceptorFacade.updateInterceptor(interceptorDto1.getName(), interceptorDto1, "*");

        ApplicationTypeSchemaDto actualDto1 = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of("interceptor2")).isEqualTo(actualDto1.getInterceptors());

        ApplicationTypeSchemaDto actualDto2 = typeSchemaFacade.get(dto2.getId());
        Assertions.assertThat(List.of("interceptor1", "interceptor1", "interceptor2")).isEqualTo(actualDto2.getInterceptors());

        interceptorDto2.setEntities(null);
        interceptorFacade.updateInterceptor(interceptorDto2.getName(), interceptorDto2, "*");

        actualDto1 = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of()).isEqualTo(actualDto1.getInterceptors());

        actualDto2 = typeSchemaFacade.get(dto2.getId());
        Assertions.assertThat(List.of("interceptor1", "interceptor1")).isEqualTo(actualDto2.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteApplicationTypeSchema() {
        // given
        typeSchemaFacade.create(dto);

        // when
        typeSchemaFacade.delete(dto.getId(), true);

        // then
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.get(dto.getId()))
                .isInstanceOf(EntityNotFoundException.class);
        Assertions.assertThat(typeSchemaFacade.getAll()).isEmpty();
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteApplicationTypeSchemaAndApplication() throws URISyntaxException {
        // given
        typeSchemaFacade.create(dto);

        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application");
        applicationDto.setDisplayName("application");
        applicationDto.setCustomAppSchemaId(new URI("https://test-schema.example"));
        applicationFacade.createApplication(applicationDto);
        // when
        typeSchemaFacade.delete(dto.getId(), true);
        // then
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.get(dto.getId()))
                .isInstanceOf(EntityNotFoundException.class);
        Collection<ApplicationTypeSchemaDto> all = typeSchemaFacade.getAll();
        Assertions.assertThat(all).isEmpty();
        Collection<ApplicationInfoDto> allApplications = applicationFacade.getAllApplications();
        Assertions.assertThat(allApplications).isEmpty();
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteApplicationTypeSchemaAndUpdateApplication() throws URISyntaxException {
        // given
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        typeSchemaFacade.create(dto);

        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI("https://test-schema.example"));
        applicationFacade.createApplication(applicationDto);
        // when
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        typeSchemaFacade.delete(dto.getId(), false);
        // then
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.get(dto.getId()))
                .isInstanceOf(EntityNotFoundException.class);
        Collection<ApplicationTypeSchemaDto> all = typeSchemaFacade.getAll();
        Assertions.assertThat(all).isEmpty();

        ApplicationDto updatedApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(updatedApplication.getName()).isEqualTo("application1");
        Assertions.assertThat(updatedApplication.getCustomAppSchemaId()).isNull();
        Assertions.assertThat(updatedApplication.getEndpoint()).isEqualTo("https://test-schema.example");
        Assertions.assertThat(updatedApplication.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(320));
        Assertions.assertThat(updatedApplication.getValidityState()).isEqualTo(validState());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplicationTypeSchema() {
        // given
        dto.setTopics(new TreeSet<>(Set.of("test", "example")));
        dto.setApplicationTypeAssistantAttachmentsInRequestSupported(true);
        typeSchemaFacade.create(dto);
        dto.setApplications(List.of());
        dto.setApplicationTypeSchemaEndpoint("https://test.com/endpoint");
        dto.setInterceptors(List.of());
        ApplicationTypeSchemaDto schemaDto = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(schemaDto).isEqualTo(dto);
        dto.setDescription("newDescription");
        dto.setTopics(new TreeSet<>(Set.of("newTopic")));
        // when
        typeSchemaFacade.update(dto.getId(), dto, "*");
        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldNotUpdateSchemaApplicationsWhenTheyMissingInRequest() {
        // given
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application1"));
        typeSchemaFacade.create(dto);

        dto.setApplications(null);
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());
        dto.setApplicationTypeAssistantAttachmentsInRequestSupported(true);

        // when
        typeSchemaFacade.update(dto.getId(), dto, "*");

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        dto.setApplications(List.of("application1"));
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldThrowExceptionWhenUpdateSchemaIdOfApplicationTypeSchema() {
        // given
        typeSchemaFacade.create(dto);
        String oldId = dto.getId();
        dto.setId("https://newId.example");
        // then
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.update(oldId, dto, "*"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schema id can not be updated for application type schema with schema id: 'https://test-schema.example'. New schema id: 'https://newId.example'");
    }

    @Test
    public void shouldThrowExceptionWhenCreateApplicationTypeSchemaWithExistingSchemaId() {
        typeSchemaFacade.create(dto);

        Assertions.assertThatThrownBy(() -> typeSchemaFacade.create(dto))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Application type schema with schema id https://test-schema.example already exists");
    }

    @Test
    public void shouldThrowExceptionWhenUpdateApplicationTypeSchemaWithExistingSchemaId() {
        typeSchemaFacade.create(dto);

        dto.setId("https://newId.example");
        typeSchemaFacade.create(dto);

        Assertions.assertThatThrownBy(() -> typeSchemaFacade.update("https://test-schema.example", dto, "*"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schema id can not be updated for application type schema with schema id: 'https://test-schema.example'. New schema id: 'https://newId.example'");
    }

    @Test
    public void shouldThrowExceptionWhenCreatingWithNonExistentApplications() {
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application1", "application2", "application3"));

        Assertions.assertThatThrownBy(() -> typeSchemaFacade.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Unable to find applications: [application2, application3]");
    }

    @Test
    public void shouldThrowExceptionWhenApplicationTypeSchemaConcurrencyOverwrite() {
        typeSchemaFacade.create(dto);
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.update(dto.getId(), dto, "test"))
                .isInstanceOf(OptimisticLockConflictException.class)
                .hasMessage("Unable to update ApplicationTypeSchema 'https://test-schema.example'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.");
    }

    @Test
    public void shouldThrowExceptionWhenUpdateApplicationTypeSchemaAndHashIsNull() {
        typeSchemaFacade.create(dto);

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> typeSchemaFacade.update(dto.getId(), dto, null)
        );
        Assertions.assertThat(exception.getMessage()).isEqualTo("Hash must not be null. Use \"*\" to skip optimistic check. Schema:https://test-schema.example.");
    }

    @Test
    public void shouldSuccessfullyUpdateSchemaWithCorrectHash() {
        typeSchemaFacade.create(dto);
        var updatedApplicationTypeSchema = new ApplicationTypeSchemaDto(dto);
        updatedApplicationTypeSchema.setDescription("new schema description");

        var hash = typeSchemaFacade.getSchemaWithHash(dto.getId()).hash();

        typeSchemaFacade.update(dto.getId(), updatedApplicationTypeSchema, hash);

        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual.getDescription()).isEqualTo("new schema description");
    }

    @Test
    public void shouldSuccessfullyGetCoreApplicationTypeSchema() {
        typeSchemaFacade.create(dto);

        var mcp = new CoreApplicationTypeSchema.ApplicationTypeMcp();
        mcp.setEndpoint("http://localhost:9876/mcp");
        mcp.setAllowedTools(List.of("classify_text"));
        mcp.setConfigDelivery(CoreApplicationTypeSchema.McpConfigDelivery.META);

        CoreApplicationTypeSchema expected = new CoreApplicationTypeSchema();
        expected.setSchema(dto.getSchema());
        expected.setId(dto.getId());
        expected.setType(CoreApplicationTypeSchema.CoreType.OBJECT);
        expected.setTitle(dto.getTitle());
        expected.setDescription(dto.getDescription());
        expected.setApplicationTypeEditorUrl(dto.getApplicationTypeEditorUrl());
        expected.setApplicationTypeViewerUrl(dto.getApplicationTypeViewerUrl());
        expected.setApplicationTypeDisplayName(dto.getApplicationTypeDisplayName());
        expected.setApplicationTypeCompletionEndpoint(dto.getApplicationTypeCompletionEndpoint());
        expected.setApplicationTypeConfigurationEndpoint(dto.getApplicationTypeConfigurationEndpoint());
        expected.setApplicationTypeRateEndpoint(dto.getApplicationTypeRateEndpoint());
        expected.setApplicationTypeTokenizeEndpoint(dto.getApplicationTypeTokenizeEndpoint());
        expected.setApplicationTypeTruncatePromptEndpoint(dto.getApplicationTypeTruncatePromptEndpoint());
        expected.setAppendApplicationPropertiesHeader(dto.getAppendApplicationPropertiesHeader());
        expected.setApplicationTypeIconUrl(dto.getApplicationTypeIconUrl());
        expected.setApplicationTypePlaybackSupport(dto.getApplicationTypePlaybackSupport());
        expected.setApplicationTypeBucketCopy(CoreApplicationTypeSchema.CopyAppBucketOptions.ENABLED);
        expected.setApplicationTypeInterceptors(List.of());
        expected.setDefs(dto.getDefs());
        expected.setProperties(dto.getProperties());
        expected.setRequired(dto.getRequired());
        expected.setApplicationTypeMcp(mcp);
        expected.setApplicationTypeAssistantAttachmentsInRequestSupported(false);
        expected.setApplicationTypeSchemaEndpoint("https://test.com/endpoint");

        CoreApplicationTypeSchema actual = typeSchemaFacade.getCoreSchemaWithHash(dto.getId()).core();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSuccessfullyGetFullySyncedEntitySyncStateWhenSchemaIsEqualToConfigSchema() throws JsonProcessingException {
        typeSchemaFacade.create(dto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode schemaState = config.get("applicationTypeSchemas").get(0);

        EntitySyncStateDto actualSyncState = typeSchemaFacade.getSyncState(dto.getId(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(schemaState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(schemaState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.FULLY_SYNCED);
    }

    @Test
    public void shouldSuccessfullyGetInProgressTooLongEntitySyncStateWhenSchemaIsNotEqualToConfigSchemaAndUpdatedLongAgo() throws JsonProcessingException {
        doReturn(1000L).when(transactionTimestampContext).getTimestamp();
        dto.setTitle("Sample Schema NEW");
        typeSchemaFacade.create(dto);

        JsonNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 122000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode configSchemaState = config.get("applicationTypeSchemas").get(0);
        JsonNode currentSchemaState = configSchemaState.deepCopy();
        ((ObjectNode) currentSchemaState).put("title", "Sample Schema NEW");

        EntitySyncStateDto actualSyncState = typeSchemaFacade.getSyncState(dto.getId(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(currentSchemaState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(configSchemaState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.IN_PROGRESS_TOO_LONG);
    }

    @Test
    public void shouldReturnResolvedTypeSchemaWhenApplicationTypeSchemaEndpointIsSet() {
        String endpointUrl = "https://test.com/external-schema";
        dto.setRequired(null);
        dto.setApplicationTypeSchemaEndpoint(endpointUrl);
        typeSchemaFacade.create(dto);

        var externalSchema = new ExternalSchema();
        externalSchema.setRequired(List.of("externalField"));

        when(externalSchemaLoader.fetchExternalSchema(endpointUrl)).thenReturn(externalSchema);

        var result = typeSchemaFacade.getResolvedTypeSchema(dto.getId());

        verify(externalSchemaLoader).fetchExternalSchema(endpointUrl);
        assertThat(result.schema().getId()).isEqualTo(dto.getId());
        assertThat(result.schema().getRequired()).isEqualTo(List.of("externalField"));
        assertThat(result.isReadOnly()).isTrue();
    }

    @Test
    public void shouldReturnResolvedTypeSchemaWithoutFetchingWhenApplicationTypeSchemaEndpointIsNull() {
        dto.setApplicationTypeSchemaEndpoint(null);
        dto.setApplications(List.of());
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());
        dto.setRequired(null);
        typeSchemaFacade.create(dto);

        var result = typeSchemaFacade.getResolvedTypeSchema(dto.getId());

        assertThat(result.schema().getId()).isEqualTo(dto.getId());
        assertThat(result.isReadOnly()).isFalse();
        assertThat(result.message()).isNull();
        assertThat(result.schema().getRequired()).isNull();
        verifyNoInteractions(externalSchemaLoader);
    }

    @Test
    public void shouldThrowExceptionWhenGetResolvedTypeSchemaForAbsentSchema() {
        String absentId = "https://absent-schema.example";

        Assertions.assertThatThrownBy(() -> typeSchemaFacade.getResolvedTypeSchema(absentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Application type schema with schema id " + absentId + " does not exist");
    }

    private JsonNode coreConfig() throws JsonProcessingException {
        String config = """
                {
                  "applicationTypeSchemas": [
                    {
                      "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                      "$id": "https://test-schema.example",
                      "dial:applicationTypeEditorUrl": "https://test.com/billings",
                      "dial:applicationTypeViewerUrl": "https://test.com/claims",
                      "dial:applicationTypeDisplayName": "Claims Use case",
                      "dial:applicationTypeCompletionEndpoint": "https://test.io/openai/deployments/mindmap/chat/completions",
                      "dial:applicationTypeConfigurationEndpoint": "https://test.io/openai/configuration",
                      "dial:applicationTypeRateEndpoint": "https://test.io/openai/rate",
                      "dial:applicationTypeTokenizeEndpoint": "https://test.io/openai/tokenize",
                      "dial:applicationTypeTruncatePromptEndpoint": "https://test.io/openai/truncate",
                      "dial:appendApplicationPropertiesHeader": true,
                      "dial:applicationTypeAssistantAttachmentsInRequestSupported": false,
                      "dial:applicationTypeInterceptors": [],
                      "dial:applicationTypeSchemaEndpoint": "https://test.com/endpoint",
                      "dial:applicationTypeBucketCopy": "ENABLED",
                      "dial:applicationTypeMcp": {
                        "dial:endpoint": "http://localhost:9876/mcp",
                        "dial:transport": "HTTP",
                        "dial:allowedTools": ["classify_text"],
                        "dial:mcpConfigDelivery": "META",
                        "dial:forwardPerRequestKey": true
                      },
                      "$defs": {
                        "ToolEndpointInfo": {
                          "properties": {
                            "name": {
                              "title": "Name",
                              "type": "string"
                            },
                            "method_url": {
                              "title": "Method Url",
                              "type": "string"
                            },
                            "method_type": {
                              "$ref": "#/$defs/ToolEndpointInfoMethodType"
                            },
                            "description": {
                              "title": "Description",
                              "type": "string"
                            },
                            "parameters": {
                              "items": {
                                "$ref": "#/$defs/ToolEndpointParameterInfo"
                              },
                              "title": "Parameters",
                              "type": "array"
                            }
                          },
                          "required": [
                            "name",
                            "method_url",
                            "method_type",
                            "description",
                            "parameters"
                          ],
                          "title": "ToolEndpointInfo",
                          "type": "object"
                        }
                      },
                      "properties": {
                        "temperature": {
                          "title": "Temperature",
                          "type": "number",
                          "dial:meta": {
                            "dial:propertyKind": "server",
                            "dial:propertyOrder": 1
                          }
                        }
                      },
                      "required": [
                        "temperature",
                        "instructions",
                        "model",
                        "web_api_toolset"
                      ],
                      "description": "testDescription",
                      "type": "object",
                      "title": "Sample Schema"
                    }
                  ]
                }
                """;
        return objectMapper.readTree(config);
    }
}
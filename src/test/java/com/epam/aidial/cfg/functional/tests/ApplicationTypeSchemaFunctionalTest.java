package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;

public abstract class ApplicationTypeSchemaFunctionalTest {

    @Autowired
    private ApplicationTypeSchemaFacade typeSchemaFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private ApplicationTypeSchemaDto dto;
    private ApplicationTypeSchemaDto dto2;

    @BeforeEach
    public void beforeEach() throws JsonProcessingException {
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
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplicationTypeSchemaWithApplication() {
        // given
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application1"));
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());

        // when
        typeSchemaFacade.create(dto);

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual).isEqualTo(dto);

        ApplicationDto updatedApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(updatedApplication.getEndpoint()).isNull();
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplicationTypeSchemaWithInterceptors() {

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);
        ;

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
    }

    @Test
    public void shouldSuccessfullyRemoveDeletedInterceptorFromTheInterceptorsList() throws JsonProcessingException {
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorFacade.createInterceptor(interceptorDto3);

        dto.setInterceptors(List.of("interceptor2", "interceptor1", "interceptor2", "interceptor3"));
        typeSchemaFacade.create(dto);

        dto2.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        typeSchemaFacade.create(dto2);

        interceptorFacade.deleteInterceptor("interceptor1");

        ApplicationTypeSchemaDto actualDto1 = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(List.of("interceptor2", "interceptor2", "interceptor3")).isEqualTo(actualDto1.getInterceptors());

        ApplicationTypeSchemaDto actualDto2 = typeSchemaFacade.get(dto2.getId());
        Assertions.assertThat(List.of("interceptor2")).isEqualTo(actualDto2.getInterceptors());

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
        typeSchemaFacade.create(dto);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI("https://test-schema.example"));
        applicationFacade.createApplication(applicationDto);
        // when
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
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplicationTypeSchema() {
        // given
        dto.setTopics(Set.of("test", "example"));
        typeSchemaFacade.create(dto);
        dto.setApplications(List.of());
        dto.setApplicationTypeRoutes(List.of());
        dto.setInterceptors(List.of());
        ApplicationTypeSchemaDto schemaDto = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(schemaDto).isEqualTo(dto);
        dto.setDescription("newDescription");
        dto.setTopics(Set.of("newTopic"));
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
                .hasMessage("unable to find applications: [application2, application3]");
    }

    @Test
    public void shouldThrowExceptionWhenApplicationTypeSchemaConcurrencyOverwrite() {
        typeSchemaFacade.create(dto);
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.update(dto.getId(), dto, "test"))
                .isInstanceOf(OptimisticLockConflictException.class)
                .hasMessage("Optimistic lock conflict on update: schemaId:'https://test-schema.example'. Reload the data.");
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

        CoreApplicationTypeSchema actual = typeSchemaFacade.getCoreSchemaWithHash(dto.getId()).core();

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
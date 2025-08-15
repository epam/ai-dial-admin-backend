package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
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

public abstract class ApplicationTypeSchemaFunctionalTest {

    @Autowired
    private ApplicationTypeSchemaFacade typeSchemaFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private ApplicationTypeSchemaDto dto;

    @BeforeEach
    public void beforeEach() throws JsonProcessingException {
        var dtosJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        dto = objectMapper.readValue(dtosJson, new TypeReference<>() {
        });
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplicationTypeSchema() {
        // when
        typeSchemaFacade.create(dto);
        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        dto.setApplications(List.of());
        dto.setApplicationTypeRoutes(List.of());
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplicationTypeSchemaWithApplication() {
        // given
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application");
        applicationDto.setEndpoint("endpoint");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application"));
        dto.setApplicationTypeRoutes(List.of());

        // when
        typeSchemaFacade.create(dto);

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual).isEqualTo(dto);

        ApplicationDto updatedApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(updatedApplication.getEndpoint()).isNull();
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

        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application");
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
        Assertions.assertThat(updatedApplication.getName()).isEqualTo("application");
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
        ApplicationTypeSchemaDto schemaDto = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(schemaDto).isEqualTo(dto);
        dto.setDescription("newDescription");
        dto.setTopics(Set.of("newTopic"));
        // when
        typeSchemaFacade.update(dto.getId(), dto);
        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldNotUpdateSchemaApplicationsWhenTheyMissingInRequest() {
        // given
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application");
        applicationDto.setEndpoint("endpoint");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application"));
        typeSchemaFacade.create(dto);

        dto.setApplications(null);
        dto.setApplicationTypeRoutes(List.of());

        // when
        typeSchemaFacade.update(dto.getId(), dto);

        // then
        ApplicationTypeSchemaDto actual = typeSchemaFacade.get(dto.getId());
        dto.setApplications(List.of("application"));
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldThrowExceptionWhenUpdateSchemaIdOfApplicationTypeSchema() {
        // given
        typeSchemaFacade.create(dto);
        String oldId = dto.getId();
        dto.setId("https://newId.example");
        // then
        Assertions.assertThatThrownBy(() -> typeSchemaFacade.update(oldId, dto))
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

        Assertions.assertThatThrownBy(() -> typeSchemaFacade.update("https://test-schema.example", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schema id can not be updated for application type schema with schema id: 'https://test-schema.example'. New schema id: 'https://newId.example'");
    }

    @Test
    public void shouldThrowExceptionWhenCreatingWithNonExistentApplications() {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application");
        applicationDto.setEndpoint("endpoint");
        applicationFacade.createApplication(applicationDto);

        dto.setApplications(List.of("application", "application2", "application3"));

        Assertions.assertThatThrownBy(() -> typeSchemaFacade.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("unable to find applications: [application2, application3]");
    }

}

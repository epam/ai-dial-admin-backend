package com.epam.aidial.cfg.service.validitystate;

import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaRouteCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.DeploymentCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.ResourceAuthSettingsCoreMapperImpl;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.model.ValidityStateResource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ApplicationTypeSchemaCoreMapperImpl.class,
        ApplicationTypeSchemaRouteCoreMapperImpl.class,
        DeploymentCoreMapperImpl.class,
        ResourceAuthSettingsCoreMapperImpl.class
})
class ApplicationResourceValidityStateOnGetResolverTest {

    @MockitoBean
    private ApplicationTypeSchemaService applicationTypeSchemaService;
    @Autowired
    private ApplicationTypeSchemaCoreMapper applicationTypeSchemaCoreMapper;

    private ApplicationResourceValidityStateOnGetResolver applicationResourceValidityStateOnGetResolver;

    @BeforeEach
    void setUp() {
        applicationResourceValidityStateOnGetResolver = new ApplicationResourceValidityStateOnGetResolver(
                applicationTypeSchemaService,
                applicationTypeSchemaCoreMapper
        );
    }

    @Test
    void resolveValidityState_shouldReturnValidStateWhenApplicationResourceDoesNotHaveSchema() {
        // given
        ApplicationResourceDto applicationResourceDto = new ApplicationResourceDto();

        ValidityStateResource expected = new ValidityStateResource();
        expected.setValid(true);

        // when
        ValidityStateResource actual = applicationResourceValidityStateOnGetResolver.resolveValidityState(applicationResourceDto);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnInvalidStateWhenApplicationResourceHasNotExistingSchema() {
        // given
        ApplicationResourceDto applicationResourceDto = new ApplicationResourceDto();
        applicationResourceDto.setApplicationTypeSchemaId("https://test-schema.example");

        ValidityStateResource expected = new ValidityStateResource();
        expected.setMessage("Schema not found");
        expected.setValid(false);

        when(applicationTypeSchemaService.tryGet("https://test-schema.example"))
                .thenReturn(Optional.empty());

        // when
        ValidityStateResource actual = applicationResourceValidityStateOnGetResolver.resolveValidityState(applicationResourceDto);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnValidStateWhenApplicationConformToItsSchema() {
        // given
        ApplicationTypeSchema applicationTypeSchema = new ApplicationTypeSchema();
        applicationTypeSchema.setSchemaId("https://test-schema.example");
        applicationTypeSchema.setSchema("https://dial.epam.com/application_type_schemas/schema#");
        applicationTypeSchema.setRequired(List.of("requiredProp"));

        ApplicationResourceDto applicationResourceDto = new ApplicationResourceDto();
        applicationResourceDto.setApplicationTypeSchemaId("https://test-schema.example");
        applicationResourceDto.setApplicationProperties(Map.of("requiredProp", "some-value"));

        ValidityStateResource expected = new ValidityStateResource();
        expected.setValid(true);

        when(applicationTypeSchemaService.tryGet("https://test-schema.example"))
                .thenReturn(Optional.of(applicationTypeSchema));

        // when
        ValidityStateResource actual = applicationResourceValidityStateOnGetResolver.resolveValidityState(applicationResourceDto);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnInvalidStateWhenApplicationDoesNotConformToItsSchema() {
        // given
        ApplicationTypeSchema applicationTypeSchema = new ApplicationTypeSchema();
        applicationTypeSchema.setSchemaId("https://test-schema.example");
        applicationTypeSchema.setSchema("https://dial.epam.com/application_type_schemas/schema#");
        applicationTypeSchema.setRequired(List.of("requiredProp"));

        ApplicationResourceDto applicationResourceDto = new ApplicationResourceDto();
        applicationResourceDto.setApplicationTypeSchemaId("https://test-schema.example");
        applicationResourceDto.setApplicationProperties(Map.of());

        ValidityStateResource expected = new ValidityStateResource();
        expected.setMessage("$: required property 'requiredProp' not found");
        expected.setValid(false);

        when(applicationTypeSchemaService.tryGet("https://test-schema.example"))
                .thenReturn(Optional.of(applicationTypeSchema));

        // when
        ValidityStateResource actual = applicationResourceValidityStateOnGetResolver.resolveValidityState(applicationResourceDto);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
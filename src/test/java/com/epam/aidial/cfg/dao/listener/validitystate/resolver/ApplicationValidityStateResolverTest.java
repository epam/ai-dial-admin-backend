package com.epam.aidial.cfg.dao.listener.validitystate.resolver;

import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapper;
import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapper;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.DependentRouteEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.DeploymentEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.FeaturesEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.LimitEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.MapPropertiesMapperImpl;
import com.epam.aidial.cfg.dao.mapper.PropertiesEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.ResourceAuthSettingsEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.RoleLimitEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.ValidityStateEntityMapperImpl;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaRouteCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.DeploymentCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.ResourceAuthSettingsCoreMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ApplicationEntityMapperImpl.class,
        RoleLimitEntityMapperImpl.class,
        LimitEntityMapperImpl.class,
        ResourceAuthSettingsEntityMapperImpl.class,
        DeploymentEntityMapperImpl.class,
        MapPropertiesMapperImpl.class,
        ObjectMapper.class,
        DependentRouteEntityMapperImpl.class,
        FeaturesEntityMapperImpl.class,
        ValidityStateEntityMapperImpl.class,
        DeploymentCoreMapperImpl.class,
        ResourceAuthSettingsCoreMapperImpl.class,
        ApplicationTypeSchemaEntityMapperImpl.class,
        PropertiesEntityMapperImpl.class,
        ApplicationTypeSchemaCoreMapperImpl.class,
        ApplicationTypeSchemaRouteCoreMapperImpl.class
})
class ApplicationValidityStateResolverTest {

    @Autowired
    private ApplicationEntityMapper applicationEntityMapper;
    @Autowired
    private ApplicationTypeSchemaEntityMapper applicationTypeSchemaEntityMapper;
    @Autowired
    private ApplicationTypeSchemaCoreMapper applicationTypeSchemaCoreMapper;

    private ApplicationValidityStateResolver applicationValidityStateResolver;

    @BeforeEach
    void setUp() {
        applicationValidityStateResolver = new ApplicationValidityStateResolver(
                applicationEntityMapper,
                applicationTypeSchemaEntityMapper,
                applicationTypeSchemaCoreMapper
        );
    }

    @Test
    void resolveValidityState_shouldReturnValidStateWhenApplicationDoesNotHaveSchema() {
        // given
        ApplicationEntity applicationEntity = new ApplicationEntity();

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setValid(true);

        // when
        ValidityStateEntity actual = applicationValidityStateResolver.resolveValidityState(applicationEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnValidStateWhenApplicationConformToItsSchema() {
        // given
        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = new ApplicationTypeSchemaEntity();
        applicationTypeSchemaEntity.setSchemaId("https://test-schema.example");
        applicationTypeSchemaEntity.setSchema("https://dial.epam.com/application_type_schemas/schema#");
        applicationTypeSchemaEntity.setRequired(List.of("requiredProp"));

        DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setIsPublic(true);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationTypeSchema(applicationTypeSchemaEntity);
        applicationEntity.setDeployment(deploymentEntity);
        applicationEntity.setApplicationProperties("{\"requiredProp\":\"some-value\"}");

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setValid(true);

        // when
        ValidityStateEntity actual = applicationValidityStateResolver.resolveValidityState(applicationEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnInvalidStateWhenApplicationDoesNotConformToItsSchema() {
        // given
        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = new ApplicationTypeSchemaEntity();
        applicationTypeSchemaEntity.setSchemaId("https://test-schema.example");
        applicationTypeSchemaEntity.setSchema("https://dial.epam.com/application_type_schemas/schema#");
        applicationTypeSchemaEntity.setRequired(List.of("requiredProp"));

        DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setIsPublic(true);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationTypeSchema(applicationTypeSchemaEntity);
        applicationEntity.setDeployment(deploymentEntity);

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setMessage("$: required property 'requiredProp' not found");
        expected.setValid(false);

        // when
        ValidityStateEntity actual = applicationValidityStateResolver.resolveValidityState(applicationEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
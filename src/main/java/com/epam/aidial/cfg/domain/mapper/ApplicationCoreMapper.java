package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSource;
import com.epam.aidial.core.config.CoreApplication;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.net.URI;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, FeatureCoreMapper.class, RouteCoreMapper.class
        }
)
public abstract class ApplicationCoreMapper {

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    @Mapping(target = "applicationTypeSchemaId", source = "model", qualifiedByName = "extractSchemaId")
    public abstract CoreApplication mapApplication(Application model);

    @Mapping(target = "deployment", source = "coreApplication", qualifiedByName = "toDeployment")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validityState", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "applicationTypeSchemaId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Application mapApplication(CoreApplication coreApplication,
                                               @Context List<RoleLimit> roleLimits,
                                               @MappingTarget Application application);

    public abstract Application copy(Application application);

    @Named("extractSchemaId")
    protected URI extractSchemaId(Application application) {
        ApplicationSource source = application.getSource();
        if (source instanceof ApplicationSchemaSource schemaSource) {
            return schemaSource.getApplicationTypeSchemaId();
        }
        return null;
    }

    @AfterMapping
    protected void mapSourceFromCore(CoreApplication coreApplication, @MappingTarget Application application) {
        if (coreApplication.getApplicationTypeSchemaId() != null) {
            application.setSource(new ApplicationSchemaSource(coreApplication.getApplicationTypeSchemaId()));
        } else if (application.getSource() == null) {
            application.setSource(new ApplicationEndpointsSource());
        }
    }

    @AfterMapping
    protected void copySource(Application source, @MappingTarget Application target) {
        if (source.getSource() != null) {
            target.setSource(source.getSource());
        }
    }

}

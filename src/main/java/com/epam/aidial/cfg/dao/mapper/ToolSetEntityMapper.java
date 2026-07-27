package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.CatalogSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.dao.model.ToolSetMcpRegistryEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, MapPropertiesMapper.class
})
public abstract class ToolSetEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;
    @Autowired
    private ToolSetContainerEntityMapper toolSetContainerEntityMapper;
    @Autowired
    private ToolSetMcpRegistryEntityMapper toolSetMcpRegistryEntityMapper;

    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    @Mapping(target = "catalogSchemaId", source = "catalogSchema.schemaId")
    public abstract ToolSet toDomain(ToolSetEntity entity);

    @Named("mapSource")
    protected ToolSetSource mapSource(ToolSetEntity entity) {
        ToolSetContainerEntity containerEntity = entity.getToolSetContainer();
        if (containerEntity != null) {
            return toolSetContainerEntityMapper.toDomain(containerEntity);
        }
        ToolSetMcpRegistryEntity mcpRegistryEntity = entity.getToolSetMcpRegistry();
        if (mcpRegistryEntity != null) {
            return toolSetMcpRegistryEntityMapper.toDomain(mcpRegistryEntity);
        }
        return new ToolSetEndpointsSource();
    }

    public ToolSetEntity toEntity(ToolSet domain,
                                  ToolSetEntity entity,
                                  ToolSetContainerEntity toolSetContainer,
                                  ToolSetMcpRegistryEntity toolSetMcpRegistry,
                                  CatalogSchemaEntity catalogSchema,
                                  List<RoleLimit> roleLimits,
                                  List<RoleEntity> rolesForLimits) {
        ToolSetEntity updatedEntity = update(domain, entity);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.TOOL_SET);
        updatedEntity.getDeployment().setOwner(updatedEntity);

        updatedEntity.setToolSetContainer(toolSetContainer);
        updatedEntity.setToolSetMcpRegistry(toolSetMcpRegistry);

        CatalogSchemaEntity currentCatalogSchema = updatedEntity.getCatalogSchema();
        if (currentCatalogSchema != null && !currentCatalogSchema.equals(catalogSchema)) {
            currentCatalogSchema.getToolSets().remove(updatedEntity);
        }
        if (catalogSchema != null && !catalogSchema.equals(currentCatalogSchema)) {
            catalogSchema.getToolSets().add(updatedEntity);
        }
        updatedEntity.setCatalogSchema(catalogSchema);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "toolSetContainer", ignore = true)
    @Mapping(target = "toolSetMcpRegistry", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "catalogSchema", ignore = true)
    public abstract ToolSetEntity update(ToolSet domain, @MappingTarget ToolSetEntity entity);

    protected URI mapStringToUri(String uriString) {
        try {
            return uriString == null ? null : new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid application applicationTypeSchemaId: " + uriString);
        }
    }
}

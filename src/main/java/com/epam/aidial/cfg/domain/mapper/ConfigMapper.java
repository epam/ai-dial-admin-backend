package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        uses = {
                RouteCoreMapper.class,
                ModelCoreMapper.class,
                ApplicationCoreMapper.class,
                KeyCoreMapper.class,
                InterceptorCoreMapper.class,
                ApplicationTypeSchemaCoreMapper.class,
                ToolSetCoreMapper.class,
        }
)
public abstract class ConfigMapper {

    @Autowired
    private RoleCoreMapper roleCoreMapper;

    @Mapping(target = "assistant", ignore = true)
    @Mapping(target = "retriableErrorCodes", ignore = true)
    @Mapping(target = "addons", ignore = true)
    @Mapping(target = "applicationTypeSchemas", source = "config.applicationRunners")
    @Mapping(target = "roles", ignore = true)
    public abstract Config toCoreConfig(ExportConfig config);

    @AfterMapping
    public void mapRoles(@MappingTarget Config config, ExportConfig exportConfig) {
        Map<String, Role> roles = exportConfig.getRoles();
        if (roles == null) {
            return;
        }

        Map<String, CoreRole> coreRoles = new HashMap<>();
        Collection<Deployment> deployments = exportConfig.collectDeployment();
        for (Map.Entry<String, Role> entry : roles.entrySet()) {
            String key = entry.getKey();
            CoreRole value = roleCoreMapper.mapRole(entry.getValue(), deployments);
            coreRoles.put(key, value);
        }

        config.setRoles(coreRoles);
    }

}

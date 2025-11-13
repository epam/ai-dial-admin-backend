package com.epam.aidial.cfg.dao.audit.mapper;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.AssistantEntity;
import com.epam.aidial.cfg.dao.model.AssistantsPropertyEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.dao.model.SecuredResourceEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Component;

@Component
public class AuditActivityMapper {

    public ActivityResourceType mapResourceType(Class entityClass) {
        if (entityClass == AdapterEntity.class) {
            return ActivityResourceType.Adapter;
        } else if (entityClass == AddonEntity.class) {
            return ActivityResourceType.Addon;
        } else if (entityClass == ApplicationEntity.class) {
            return ActivityResourceType.Application;
        } else if (entityClass == ApplicationTypeSchemaEntity.class) {
            return ActivityResourceType.ApplicationTypeSchema;
        } else if (entityClass == AssistantEntity.class) {
            return ActivityResourceType.Assistant;
        } else if (entityClass == AssistantsPropertyEntity.class) {
            return ActivityResourceType.AssistantsProperty;
        } else if (entityClass == DeploymentEntity.class) {
            return ActivityResourceType.Deployment;
        } else if (entityClass == SecuredResourceEntity.class) {
            return ActivityResourceType.SecuredResource;
        } else if (entityClass == InterceptorEntity.class) {
            return ActivityResourceType.Interceptor;
        } else if (entityClass == InterceptorRunnerEntity.class) {
            return ActivityResourceType.InterceptorRunner;
        } else if (entityClass == KeyEntity.class) {
            return ActivityResourceType.Key;
        } else if (entityClass == ModelEntity.class) {
            return ActivityResourceType.Model;
        } else if (entityClass == RoleEntity.class) {
            return ActivityResourceType.Role;
        } else if (entityClass == RoleLimitEntity.class) {
            return ActivityResourceType.RoleLimit;
        } else if (entityClass == RouteEntity.class) {
            return ActivityResourceType.Route;
        } else if (entityClass == ToolSetEntity.class) {
            return ActivityResourceType.ToolSet;
        } else {
            throw new IllegalArgumentException("Unable to find resource type for class " + entityClass);
        }
    }

    public ActivityType mapActivityType(RevisionType revisionType) {
        switch (revisionType) {
            case ADD -> {
                return ActivityType.Create;
            }
            case MOD -> {
                return ActivityType.Update;
            }
            case DEL -> {
                return ActivityType.Delete;
            }
            default -> throw new IllegalArgumentException("Unknown revision type " + revisionType);
        }
    }

    public ActivityResourceType mapDeploymentTypeToActivityResourceType(DeploymentTypeEntity deploymentEntityType) {
        return switch (deploymentEntityType) {
            case ADDON -> ActivityResourceType.Addon;
            case APPLICATION -> ActivityResourceType.Application;
            case ASSISTANT -> ActivityResourceType.Assistant;
            case MODEL -> ActivityResourceType.Model;
            case ROUTE -> ActivityResourceType.Route;
            case TOOL_SET -> ActivityResourceType.ToolSet;
        };
    }
}

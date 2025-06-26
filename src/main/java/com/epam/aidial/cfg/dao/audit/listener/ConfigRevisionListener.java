package com.epam.aidial.cfg.dao.audit.listener;

import com.epam.aidial.cfg.dao.audit.mapper.AuditActivityMapper;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.dao.audit.model.ConfigRevisionEntity;
import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitId;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitId;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class ConfigRevisionListener implements EntityTrackingRevisionListener, ApplicationContextAware {
    private final ThreadLocal<Map<AuditActivityEntityId, AuditActivityEntity>> changeListHolder = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Set<UUID>> issuedUpdateActivitiesHolder = ThreadLocal.withInitial(HashSet::new);
    private ApplicationContext applicationContext;
    private TransactionTimestampContext transactionTimestampContext;
    private DeploymentJpaRepository deploymentJpaRepository;
    private AuditActivityMapper auditActivityMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.transactionTimestampContext = applicationContext.getBean(TransactionTimestampContext.class);
        this.auditActivityMapper = applicationContext.getBean(AuditActivityMapper.class);
    }

    @Override
    public void newRevision(Object revisionEntity) {
        ConfigRevisionEntity revEntity = (ConfigRevisionEntity) revisionEntity;
        revEntity.setAuthor(SecurityClaimsExtractor.getAuthor());
        revEntity.setEmail(SecurityClaimsExtractor.getEmail());
        revEntity.setTimestamp(transactionTimestampContext.getTimestamp());
        reset();
        log.debug("Revision entity: {}", revisionEntity);
    }

    @Override
    public void entityChanged(Class entityClass, String entityName, Object entityId, RevisionType revisionType, Object revisionEntity) {
        ConfigRevisionEntity revEntity = (ConfigRevisionEntity) revisionEntity;
        AuditActivityEntity auditActivity = buildAuditActivity(entityClass, entityId, revisionType, revEntity);
        List<AuditActivityEntity> modifyDeploymentActivities = issueUpdateActivities(revisionType, entityId, entityClass, revEntity);
        Set<UUID> uuids = issuedUpdateActivitiesHolder.get();
        addAuditActivity(revEntity, auditActivity);
        for (AuditActivityEntity auditActivityEntity : modifyDeploymentActivities) {
            uuids.add(auditActivityEntity.getActivityId());
            addAuditActivity(revEntity, auditActivityEntity);
        }
    }

    private List<AuditActivityEntity> issueUpdateActivities(RevisionType revisionType, Object entityId, Class<?> entityClass, ConfigRevisionEntity revEntity) {
        if (RoleLimitEntity.class == entityClass) {
            RoleLimitId roleLimitId = (RoleLimitId) entityId;
            return issueDeploymentAuditActivity(roleLimitId.getDeploymentName(), revEntity, roleLimitId.getRoleName());
        }
        if (RoleShareResourceLimitEntity.class == entityClass) {
            RoleShareResourceLimitId roleShareResourceLimitId = (RoleShareResourceLimitId) entityId;
            return issueDeploymentAuditActivity(roleShareResourceLimitId.getDeploymentName(), revEntity, roleShareResourceLimitId.getRoleName());
        }
        if (revisionType == RevisionType.MOD && DeploymentEntity.class == entityClass) {
            String deploymentName = (String) entityId;
            DeploymentEntity deploymentEntity = findDeployment(deploymentName);
            return List.of(buildDeploymentActivity(revEntity, deploymentEntity));
        }
        return List.of();
    }

    private List<AuditActivityEntity> issueDeploymentAuditActivity(String deploymentName, ConfigRevisionEntity revEntity, String roleName) {
        DeploymentEntity deploymentEntity = findDeployment(deploymentName);
        AuditActivityEntity deploymentAuditActivity = buildDeploymentActivity(revEntity, deploymentEntity);
        AuditActivityEntity roleActivity = buildAuditActivity(revEntity, ActivityType.Update, ActivityResourceType.Role, roleName);
        if (deploymentAuditActivity == null) {
            return List.of(roleActivity);
        }
        return List.of(deploymentAuditActivity, roleActivity);
    }

    private AuditActivityEntity buildDeploymentActivity(ConfigRevisionEntity revEntity, DeploymentEntity deploymentEntity) {
        if (deploymentEntity == null) {
            return null;
        }
        ActivityResourceType resourceType = auditActivityMapper.mapDeploymentTypeToActivityResourceType(deploymentEntity.getType());
        String resourceId = deploymentEntity.getName();

        return buildAuditActivity(revEntity, ActivityType.Update, resourceType, resourceId);
    }

    private AuditActivityEntity buildAuditActivity(Class<?> entityClass, Object entityId, RevisionType revisionType, ConfigRevisionEntity revEntity) {
        ActivityType activityType = auditActivityMapper.mapActivityType(revisionType);
        ActivityResourceType resourceType = auditActivityMapper.mapResourceType(entityClass);
        String resourceId = Objects.toString(entityId);

        return buildAuditActivity(revEntity, activityType, resourceType, resourceId);
    }

    @NotNull
    private AuditActivityEntity buildAuditActivity(ConfigRevisionEntity revEntity, ActivityType activityType, ActivityResourceType resourceType, String resourceId) {
        AuditActivityEntity auditActivity = new AuditActivityEntity();
        auditActivity.setActivityId(generateUuid());
        auditActivity.setActivityType(activityType);
        auditActivity.setResourceType(resourceType);
        auditActivity.setResourceId(resourceId);
        auditActivity.setEpochTimestampMs(revEntity.getTimestamp());
        auditActivity.setInitiatedAuthor(revEntity.getAuthor());
        auditActivity.setInitiatedEmail(revEntity.getEmail());
        auditActivity.setRevision(revEntity.getId());
        return auditActivity;
    }

    private UUID generateUuid() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    private DeploymentEntity findDeployment(String deploymentName) {
        return getDeploymentJpaRepository().findById(deploymentName).orElse(null);
    }

    private void addAuditActivity(ConfigRevisionEntity revEntity, AuditActivityEntity source) {
        var changeList = changeListHolder.get();
        AuditActivityEntityId auditActivityId = AuditActivityEntityId.of(source);

        AuditActivityEntity target = changeList.get(auditActivityId);
        if (target == null) {
            revEntity.getActivities().add(source);
            changeList.put(auditActivityId, source);
            return;
        }

        if (!issuedUpdateActivitiesHolder.get().contains(source.getActivityId())) {
            revEntity.getActivities().remove(target);
            revEntity.getActivities().add(source);
            changeList.put(auditActivityId, source);
        }
    }

    private DeploymentJpaRepository getDeploymentJpaRepository() {
        if (deploymentJpaRepository == null) {
            deploymentJpaRepository = applicationContext.getBean(DeploymentJpaRepository.class);
        }
        return deploymentJpaRepository;
    }

    record AuditActivityEntityId(ActivityResourceType resourceType, String resourceId) {

        static AuditActivityEntityId of(AuditActivityEntity entity) {
            return new AuditActivityEntityId(entity.getResourceType(), entity.getResourceId());
        }
    }

    private void reset() {
        changeListHolder.get().clear();
        issuedUpdateActivitiesHolder.get().clear();
    }
}

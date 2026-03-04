package com.epam.aidial.cfg.dao.hibernate.integrator;

import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.AssistantEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.dao.model.TimeTrackableEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;

import java.util.Objects;

/**
 * @see <a href="https://hibernate.zulipchat.com/#narrow/stream/132096-hibernate-user/topic/Modification.20of.20collection.20notification">
 * Modification of collection notification</a>
 */

@RequiredArgsConstructor
public class CollectionOwnerUpdatedAtModifier implements PreCollectionUpdateEventListener {

    private final TransactionTimestampContext transactionTimestampContext;

    @Override
    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        Object owner = event.getAffectedOwnerOrNull();

        if (owner instanceof TimeTrackableEntity<?> timeTrackableEntity) {
            setUpdatedAtIfNeeded(event, timeTrackableEntity);
        } else if (owner instanceof DeploymentEntity deploymentEntity) {
            Class<?> entityType = resolveEntityType(deploymentEntity);
            Object deployment = event.getSession().get(entityType, deploymentEntity.getId());
            if (deployment instanceof TimeTrackableEntity<?> timeTrackableEntity) {
                setUpdatedAtIfNeeded(event, timeTrackableEntity);
            }
        }
    }

    private void setUpdatedAtIfNeeded(PreCollectionUpdateEvent event, TimeTrackableEntity<?> timeTrackableEntity) {
        Object storedSnapshot = event.getCollection().getStoredSnapshot();
        Object currentValue = event.getCollection().getValue();
        if (!Objects.equals(storedSnapshot, currentValue)) {
            timeTrackableEntity.setUpdatedAt(transactionTimestampContext.getTimestamp());
        }
    }

    private Class<?> resolveEntityType(DeploymentEntity deployment) {
        return switch (deployment.getType()) {
            case ADDON -> AddonEntity.class;
            case APPLICATION -> ApplicationEntity.class;
            case ASSISTANT -> AssistantEntity.class;
            case MODEL -> ModelEntity.class;
            case ROUTE -> RouteEntity.class;
            case TOOL_SET -> ToolSetEntity.class;
        };
    }
}

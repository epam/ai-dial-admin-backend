package com.epam.aidial.cfg.dao.hibernate.integrator;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.TimeTrackableEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;

/**
 * @see <a href="https://hibernate.zulipchat.com/#narrow/stream/132096-hibernate-user/topic/Modification.20of.20collection.20notification">
 *     Modification of collection notification</a>
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
            TimeTrackableEntity<String> deploymentOwner = deploymentEntity.getOwner();
            setUpdatedAtIfNeeded(event, deploymentOwner);
        }
    }

    private void setUpdatedAtIfNeeded(PreCollectionUpdateEvent event, TimeTrackableEntity<?> timeTrackableEntity) {
        PersistentCollection<?> pc = event.getCollection();
        CollectionEntry entry = event.getSession().getPersistenceContext().getCollectionEntry(pc);
        boolean dirty = pc.getStoredSnapshot() == null || !pc.equalsSnapshot(entry.getLoadedPersister());

        if (dirty) {
            timeTrackableEntity.setUpdatedAt(transactionTimestampContext.getTimestamp());
        }
    }
}

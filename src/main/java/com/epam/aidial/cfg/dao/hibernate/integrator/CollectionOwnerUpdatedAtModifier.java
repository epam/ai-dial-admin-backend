package com.epam.aidial.cfg.dao.hibernate.integrator;

import com.epam.aidial.cfg.dao.model.TimeTrackableEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;

import java.util.Objects;

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
            Object storedSnapshot = event.getCollection().getStoredSnapshot();
            Object currentValue = event.getCollection().getValue();
            if (!Objects.equals(storedSnapshot, currentValue)) {
                timeTrackableEntity.setUpdatedAt(transactionTimestampContext.getTimestamp());
            }
        }
    }
}

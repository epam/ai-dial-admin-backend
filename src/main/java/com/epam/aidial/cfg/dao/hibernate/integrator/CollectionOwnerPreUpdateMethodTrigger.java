package com.epam.aidial.cfg.dao.hibernate.integrator;

import com.epam.aidial.cfg.dao.model.TimeTrackableEntity;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;

public class CollectionOwnerPreUpdateMethodTrigger implements PreCollectionUpdateEventListener {

    @Override
    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        Object owner = event.getAffectedOwnerOrNull();

        if (owner instanceof TimeTrackableEntity<?> timeTrackableEntity) {
            // hack to trigger @PreUpdate method for the collection owner entity
            // where valid value will be set into updatedAt field
            timeTrackableEntity.setUpdatedAt(0);
        }
    }
}

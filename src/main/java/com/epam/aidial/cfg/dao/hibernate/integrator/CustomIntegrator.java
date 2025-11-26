package com.epam.aidial.cfg.dao.hibernate.integrator;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jetbrains.annotations.NotNull;

public class CustomIntegrator implements Integrator {

    @Override
    public void integrate(@NotNull Metadata metadata,
                          @NonNull BootstrapContext bootstrapContext,
                          @NonNull SessionFactoryImplementor sessionFactory) {
        ServiceRegistry serviceRegistry = sessionFactory.getServiceRegistry();
        EventListenerRegistry registry = serviceRegistry.requireService(EventListenerRegistry.class);

        var preCollectionUpdateEventListenerGroup = registry.getEventListenerGroup(EventType.PRE_COLLECTION_UPDATE);
        preCollectionUpdateEventListenerGroup.appendListener(new CollectionOwnerPreUpdateMethodTrigger());
    }

    @Override
    public void disintegrate(@NotNull SessionFactoryImplementor sessionFactory,
                             @NotNull SessionFactoryServiceRegistry serviceRegistry) {
        // no-op
    }
}

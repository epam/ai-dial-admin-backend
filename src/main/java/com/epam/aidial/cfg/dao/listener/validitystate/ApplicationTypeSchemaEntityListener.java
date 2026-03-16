package com.epam.aidial.cfg.dao.listener.validitystate;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import jakarta.persistence.PreUpdate;

@LogExecution
public class ApplicationTypeSchemaEntityListener {

    @PreUpdate
    public void preUpdate(ApplicationTypeSchemaEntity entity) {
        handleEntity(entity);
    }

    private void handleEntity(ApplicationTypeSchemaEntity entity) {
        // will trigger preUpdate for application where actual validity state will be calculated and set
        entity.getApplications().forEach(app -> app.setValidityState(null));
    }
}

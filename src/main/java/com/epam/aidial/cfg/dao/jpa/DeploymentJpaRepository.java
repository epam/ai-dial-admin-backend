package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import org.springframework.data.repository.CrudRepository;

public interface DeploymentJpaRepository extends CrudRepository<DeploymentEntity, String> {
}

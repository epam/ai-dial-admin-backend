package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface DeploymentJpaRepository extends CrudRepository<DeploymentEntity, String> {

    @Query("SELECT d.name FROM DeploymentEntity d")
    Set<String> findAllNames();

    @Query("SELECT d.name FROM DeploymentEntity d WHERE d.name IN :names")
    Set<String> findAllByNames(Set<String> names);
}

package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ModelJpaRepository extends JpaRepository<ModelEntity, String> {

    boolean existsByDisplayNameAndDisplayVersion(String displayName, String displayVersion);

    List<ModelEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT m FROM ModelEntity m WHERE m.modelContainer IS NOT NULL")
    List<ModelEntity> findByContainerIdIsNotNull();

}

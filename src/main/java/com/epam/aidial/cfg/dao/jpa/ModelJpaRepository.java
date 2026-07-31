package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ModelJpaRepository extends JpaRepository<ModelEntity, String> {

    @Query("SELECT m FROM ModelEntity m WHERE m.modelContainer IS NOT NULL")
    List<ModelEntity> findByContainerIdIsNotNull();

    List<ModelEntity> findByIdNotIn(Collection<String> ids);

    List<ModelEntity> findByDisplayVersion(String displayVersion);

    List<ModelEntity> findByIdIn(Collection<String> names);
}

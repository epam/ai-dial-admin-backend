package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelJpaRepository extends JpaRepository<ModelEntity, String> {

    boolean existsByDisplayNameAndDisplayVersion(String displayName, String displayVersion);

    @Query("DELETE FROM ModelEntity m WHERE m.id NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);

    @Query("SELECT m FROM ModelEntity m WHERE m.modelContainer IS NOT NULL")
    List<ModelEntity> findByContainerIdIsNotNull();

}

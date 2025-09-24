package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ToolSetJpaRepository extends JpaRepository<ToolSetEntity, String> {

    @Modifying
    @Query("DELETE FROM ToolSetEntity t WHERE t.deploymentName NOT IN :ids")
    void deleteAllExcept(@Param("ids") List<String> ids);

    @Query("SELECT i FROM ToolSetEntity i WHERE i.toolSetContainer IS NOT NULL")
    List<ToolSetEntity> findByContainerIdIsNotNull();
}

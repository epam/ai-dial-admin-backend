package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ToolSetJpaRepository extends CrudRepository<ToolSetEntity, String> {

    @Modifying
    @Query("DELETE FROM ToolSetEntity t WHERE t.deploymentName NOT IN :ids")
    void deleteAllExcept(@Param("ids") List<String> ids);
}

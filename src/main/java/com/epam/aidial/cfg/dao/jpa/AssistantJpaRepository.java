package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AssistantEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssistantJpaRepository extends CrudRepository<AssistantEntity, String> {

    @Query("DELETE FROM AssistantEntity a WHERE a.deploymentName NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);
}

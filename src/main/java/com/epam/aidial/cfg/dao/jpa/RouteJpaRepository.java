package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.RouteEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteJpaRepository extends CrudRepository<RouteEntity, String> {

    @Query("DELETE FROM RouteEntity r WHERE r.deploymentName NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);
}

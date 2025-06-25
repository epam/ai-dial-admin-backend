package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterceptorRunnerJpaRepository extends CrudRepository<InterceptorRunnerEntity, String> {

    @Query("DELETE FROM InterceptorRunnerEntity ir WHERE ir.name NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);
}
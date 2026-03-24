package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface InterceptorRunnerJpaRepository extends CrudRepository<InterceptorRunnerEntity, String> {

    List<InterceptorRunnerEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT i.name FROM InterceptorRunnerEntity i")
    Set<String> findAllNames();

    List<InterceptorRunnerEntity> findAllByOrderByDisplayNameAscIdAsc();

    List<InterceptorRunnerEntity> findByIdInOrderByDisplayNameAscIdAsc(Collection<String> ids);
}
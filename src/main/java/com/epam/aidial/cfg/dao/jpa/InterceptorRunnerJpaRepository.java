package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface InterceptorRunnerJpaRepository extends CrudRepository<InterceptorRunnerEntity, String> {

    List<InterceptorRunnerEntity> findByIdNotIn(Collection<String> ids);
}
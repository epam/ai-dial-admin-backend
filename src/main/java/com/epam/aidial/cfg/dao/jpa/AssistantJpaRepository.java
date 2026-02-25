package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AssistantEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface AssistantJpaRepository extends CrudRepository<AssistantEntity, String> {

    List<AssistantEntity> findByIdNotIn(Collection<String> ids);
}

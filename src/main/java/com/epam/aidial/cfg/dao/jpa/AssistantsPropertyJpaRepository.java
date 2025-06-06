package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AssistantsPropertyEntity;
import org.springframework.data.repository.CrudRepository;

public interface AssistantsPropertyJpaRepository extends CrudRepository<AssistantsPropertyEntity, Long> {
}

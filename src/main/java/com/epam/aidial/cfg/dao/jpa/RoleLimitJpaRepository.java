package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitId;
import org.springframework.data.repository.CrudRepository;

public interface RoleLimitJpaRepository extends CrudRepository<RoleLimitEntity, RoleLimitId> {
}

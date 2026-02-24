package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.RoleEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface RoleJpaRepository extends CrudRepository<RoleEntity, String> {

    List<RoleEntity> findByIdNotIn(Collection<String> ids);
}

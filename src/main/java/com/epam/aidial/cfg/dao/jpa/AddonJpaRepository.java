package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AddonEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface AddonJpaRepository extends CrudRepository<AddonEntity, String> {

    List<AddonEntity> findByIdNotIn(Collection<String> ids);
}

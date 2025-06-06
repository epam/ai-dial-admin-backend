package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdapterJpaRepository extends CrudRepository<AdapterEntity, String> {

    Optional<AdapterEntity> findByBaseEndpoint(String endpoint);
}

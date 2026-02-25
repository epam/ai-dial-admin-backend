package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface KeyJpaRepository extends CrudRepository<KeyEntity, String> {

    boolean existsByKey(String key);

    Optional<KeyEntity> findByKey(String key);

    List<KeyEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT k.name FROM KeyEntity k")
    Set<String> findAllKeys();

    List<KeyEntity> findAllByValidityStateIsValidTrue();
}

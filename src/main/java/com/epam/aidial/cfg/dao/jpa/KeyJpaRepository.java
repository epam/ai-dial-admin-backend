package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface KeyJpaRepository extends CrudRepository<KeyEntity, String> {

    boolean existsByKey(String key);

    Optional<KeyEntity> findByKey(String key);

    @Query("DELETE FROM KeyEntity k WHERE k.name NOT IN :names")
    @Modifying
    void deleteAllExcept(@Param("names") List<String> names);

    @Query("SELECT k.name FROM KeyEntity k")
    Set<String> findAllKeys();

    List<KeyEntity> findAllByValidityStateIsValidTrue();
}

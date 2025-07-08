package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdapterJpaRepository extends CrudRepository<AdapterEntity, String> {

    Optional<AdapterEntity> findByBaseEndpoint(String endpoint);

    @Query("DELETE FROM AdapterEntity a WHERE a.name NOT IN :names")
    @Modifying
    void deleteAllExcept(@Param("names") List<String> names);
}

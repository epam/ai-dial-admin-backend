package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ModelEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModelJpaRepository extends CrudRepository<ModelEntity, String> {

    boolean existsByDisplayNameAndDisplayVersion(String displayName, String displayVersion);

    @Query("DELETE FROM ModelEntity m WHERE m.id NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM ModelEntity m WHERE m.id = :id")
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")
    })
    Optional<ModelEntity> findAndLockById(@Param("id") String id);
}

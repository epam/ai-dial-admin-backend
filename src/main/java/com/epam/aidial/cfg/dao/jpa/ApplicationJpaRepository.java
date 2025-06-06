package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, String> {

    boolean existsByDisplayNameAndDisplayVersion(String displayName, String displayVersion);

    @Query("DELETE FROM ApplicationEntity a WHERE a.deploymentName NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);
}

package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.RoleEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleJpaRepository extends CrudRepository<RoleEntity, String> {

    @Query("DELETE FROM RoleEntity r WHERE r.name NOT IN :roles")
    @Modifying
    void deleteAllExcept(@Param("roles") List<String> roles);
}

package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.GlobalInterceptorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GlobalInterceptorJpaRepository extends JpaRepository<GlobalInterceptorEntity, String> {
    @Query("DELETE FROM GlobalInterceptorEntity i WHERE i.name NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);
}
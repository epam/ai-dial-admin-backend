package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterceptorJpaRepository extends JpaRepository<InterceptorEntity, String> {

    @Query("DELETE FROM InterceptorEntity i WHERE i.name NOT IN :ids")
    @Modifying
    void deleteAllExcept(@Param("ids") List<String> ids);

    List<InterceptorEntity> findByContainerIdIsNotNull();
}

package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface InterceptorJpaRepository extends JpaRepository<InterceptorEntity, String> {

    List<InterceptorEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT i FROM InterceptorEntity i WHERE i.interceptorContainer IS NOT NULL")
    List<InterceptorEntity> findByContainerIdIsNotNull();

    @Query("SELECT i.name FROM InterceptorEntity i")
    Set<String> findAllNames();

    List<InterceptorEntity> findAllByOrderByDisplayNameAscIdAsc();

    List<InterceptorEntity> findByIdInOrderByDisplayNameAscIdAsc(Collection<String> ids);
}

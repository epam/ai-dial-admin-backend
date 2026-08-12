package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ToolSetJpaRepository extends JpaRepository<ToolSetEntity, String> {

    List<ToolSetEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT i FROM ToolSetEntity i WHERE i.toolSetContainer IS NOT NULL")
    List<ToolSetEntity> findByContainerIdIsNotNull();

    List<ToolSetEntity> findAllByOrderByDisplayNameAscIdAsc();

    List<ToolSetEntity> findByIdInOrderByDisplayNameAscIdAsc(Collection<String> ids);
}

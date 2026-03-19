package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.RouteEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface RouteJpaRepository extends CrudRepository<RouteEntity, String> {

    List<RouteEntity> findByIdNotIn(Collection<String> ids);

    List<RouteEntity> findAllByOrderByDisplayNameAscIdAsc();

    List<RouteEntity> findByIdInOrderByDisplayNameAscIdAsc(Collection<String> ids);
}

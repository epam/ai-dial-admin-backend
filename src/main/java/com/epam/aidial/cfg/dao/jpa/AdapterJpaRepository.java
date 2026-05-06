package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AdapterJpaRepository extends CrudRepository<AdapterEntity, String> {

    Iterable<AdapterEntity> findByBaseEndpointAndResponsesEndpointNullOrderByNameAsc(String endpoint);

    Iterable<AdapterEntity> findByResponsesEndpointAndBaseEndpointNullOrderByNameAsc(String responsesEndpoint);

    Iterable<AdapterEntity> findByBaseEndpointAndResponsesEndpointOrderByNameAsc(String endpoint, String responsesEndpoint);

    @Query("SELECT a FROM AdapterEntity a WHERE a.adapterContainer IS NOT NULL")
    List<AdapterEntity> findByAdapterContainerIsNotNull();

    List<AdapterEntity> findByIdNotIn(Collection<String> ids);

    @Query("SELECT a.name FROM AdapterEntity a")
    Set<String> findAllNames();

    List<AdapterEntity> findAllByOrderByDisplayNameAscIdAsc();

    List<AdapterEntity> findByIdInOrderByDisplayNameAscIdAsc(Collection<String> ids);
}

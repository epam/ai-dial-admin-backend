package com.epam.aidial.cfg.dao.audit.jpa;

import com.epam.aidial.cfg.dao.audit.model.ConfigRevisionEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ConfigRevisionJpaRepository extends CrudRepository<ConfigRevisionEntity, Integer>,
        PagingAndSortingRepository<ConfigRevisionEntity, Integer>,
        JpaSpecificationExecutor<ConfigRevisionEntity> {

    ConfigRevisionEntity findFirstByTimestampLessThanEqualOrderByTimestampDesc(@Param("timestamp") Long timestamp);
}

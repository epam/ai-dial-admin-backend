package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, String> {
    List<ApplicationEntity> findByIdNotIn(Collection<String> ids);

    List<ApplicationEntity> findAllByOrderByDisplayNameAscDisplayVersionAscIdAsc();

    List<ApplicationEntity> findByValidityStateIsValidTrueOrderByDisplayNameAscDisplayVersionAscIdAsc();

    List<ApplicationEntity> findByIdInOrderByDisplayNameAscDisplayVersionAscIdAsc(Collection<String> ids);

    List<ApplicationEntity> findByApplicationContainerIsNotNull();

    List<ApplicationEntity> findByDisplayVersion(String displayVersion);
}

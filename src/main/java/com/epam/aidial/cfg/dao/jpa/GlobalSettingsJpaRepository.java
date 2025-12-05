package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.GlobalSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalSettingsJpaRepository extends JpaRepository<GlobalSettingsEntity, Integer> {
}
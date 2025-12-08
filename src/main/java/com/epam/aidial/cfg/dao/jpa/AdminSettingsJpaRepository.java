package com.epam.aidial.cfg.dao.jpa;

import com.epam.aidial.cfg.dao.model.AdminSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSettingsJpaRepository extends JpaRepository<AdminSettingsEntity, Integer> {
}

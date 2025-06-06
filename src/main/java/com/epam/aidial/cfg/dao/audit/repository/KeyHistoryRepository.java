package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.KeyJpaRepository;
import com.epam.aidial.cfg.dao.mapper.KeyEntityMapper;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class KeyHistoryRepository extends RevisionRepository {

    private final KeyJpaRepository keyJpaRepository;
    private final KeyEntityMapper keyEntityMapper;


    public void rollbackKeys(Number revision, AuditReader auditReader) {
        List<KeyEntity> keys = getEntitiesAtRevision(revision, auditReader, KeyEntity.class);
        keyJpaRepository.deleteAllExcept(keys.stream().map(KeyEntity::getId).collect(Collectors.toList()));
        for (KeyEntity key : keys) {
            Key domain = keyEntityMapper.toDomain(key);
            KeyEntity entity = keyJpaRepository.findById(domain.getName()).orElseGet(KeyEntity::new);
            KeyEntity keyEntity = keyEntityMapper.toEntity(domain, key.getKeyGeneratedAt(), entity);
            keyJpaRepository.save(keyEntity);
        }
    }
}

package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.jpa.KeyJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RoleEntityMapper;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class RoleHistoryRepository extends RevisionRepository {
    private final RoleJpaRepository roleJpaRepository;
    private final RoleEntityMapper roleEntityMapper;
    private final DeploymentJpaRepository deploymentJpaRepository;
    private final KeyJpaRepository keyJpaRepository;


    public void rollbackRoles(Number revision, AuditReader auditReader) {
        List<RoleEntity> roles = getEntitiesAtRevision(revision, auditReader, RoleEntity.class);
        List<String> roleNames = roles.stream().map(RoleEntity::getId).collect(Collectors.toList());
        roleJpaRepository.deleteAllExcept(roleNames);
        Set<String> allDeploymentNames = deploymentJpaRepository.findAllNames();
        Set<String> allKeys = keyJpaRepository.findAllKeys();
        for (RoleEntity role : roles) {
            Role domain = roleEntityMapper.toDomain(role);
            RoleEntity entity = roleJpaRepository.findById(domain.getName()).orElseGet(RoleEntity::new);
            domain.getLimits().removeIf(roleLimit -> !allDeploymentNames.contains(roleLimit.getDeploymentName()));
            domain.getKeys().removeIf(key -> !allKeys.contains(key));
            RoleEntity roleEntity = roleEntityMapper.toEntity(domain, entity);
            roleJpaRepository.save(roleEntity);
        }
    }
}

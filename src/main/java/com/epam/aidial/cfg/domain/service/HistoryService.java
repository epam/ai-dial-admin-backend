package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.ConfigRevisionJpaRepository;
import com.epam.aidial.cfg.dao.audit.model.ConfigRevisionEntity;
import com.epam.aidial.cfg.dao.audit.repository.AdapterHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.AddonHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.ApplicationHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.ApplicationTypeSchemaHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.AssistantHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.AssistantPropertiesHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.InterceptorHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.InterceptorRunnerHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.KeyHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.ModelHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.RoleHistoryRepository;
import com.epam.aidial.cfg.dao.audit.repository.RouteHistoryRepository;
import com.epam.aidial.cfg.dao.mapper.ConfigRevisionEntityMapper;
import com.epam.aidial.cfg.dao.mapper.PageEntityMapper;
import com.epam.aidial.cfg.domain.model.ConfigRevision;
import com.epam.aidial.cfg.domain.model.page.PageRequestModel;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HistoryService {

    private static final Set<String> configRevisionCaseInSensitiveColumns = Set.of(
            "email",
            "author"
    );

    @PersistenceContext
    private final EntityManager entityManager;
    private final ConfigRevisionJpaRepository configRevisionJpaRepository;
    private final RoleHistoryRepository roleHistoryRepository;
    private final ModelHistoryRepository modelHistoryRepository;
    private final AddonHistoryRepository addonHistoryRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final ApplicationTypeSchemaHistoryRepository applicationTypeSchemaHistoryRepository;
    private final AssistantHistoryRepository assistantHistoryRepository;
    private final AssistantPropertiesHistoryRepository assistantPropertiesHistoryRepository;
    private final RouteHistoryRepository routeHistoryRepository;
    private final InterceptorHistoryRepository interceptorHistoryRepository;
    private final InterceptorRunnerHistoryRepository interceptorRunnerHistoryRepository;
    private final KeyHistoryRepository keyHistoryRepository;
    private final AdapterHistoryRepository adapterHistoryRepository;
    private final ConfigRevisionEntityMapper configRevisionEntityMapper;
    private final PageEntityMapper pageEntityMapper;

    @Transactional(readOnly = true)
    public List<ConfigRevision> getRevisionsList(PageRequestModel pageRequestModel) {
        PageRequest pageRequest = pageEntityMapper.toPageRequest(pageRequestModel);
        List<Specification<ConfigRevisionEntity>> filters = pageEntityMapper.toSpecifications(pageRequestModel,
                new PageEntityMapper.SpecificationContext(configRevisionCaseInSensitiveColumns), ConfigRevisionEntity.class);
        var specification = Specification.allOf(filters);
        return configRevisionJpaRepository.findAll(specification, pageRequest)
                .stream()
                .map(configRevisionEntityMapper::map)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackToRevision(Number revision) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        roleHistoryRepository.rollbackRoles(revision, auditReader);
        keyHistoryRepository.rollbackKeys(revision, auditReader);
        modelHistoryRepository.rollbackModels(revision, auditReader);
        addonHistoryRepository.rollbackAddons(revision, auditReader);
        applicationTypeSchemaHistoryRepository.rollbackApplicationTypeSchemas(revision, auditReader);
        applicationHistoryRepository.rollbackApplications(revision, auditReader);
        assistantHistoryRepository.rollbackAssistants(revision, auditReader);
        assistantPropertiesHistoryRepository.rollbackAssistantsProperties(revision, auditReader);
        routeHistoryRepository.rollbackRoutes(revision, auditReader);
        interceptorHistoryRepository.rollbackInterceptors(revision, auditReader);
        interceptorRunnerHistoryRepository.rollbackInterceptorRunners(revision, auditReader);
        adapterHistoryRepository.rollbackAdapters(revision, auditReader);
    }

    @Transactional(readOnly = true)
    public <T, I> T entitySnapshotAtRevision(Integer revision, I id, Class<T> clazz) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        return Optional.ofNullable(auditReader.find(clazz, id, revision))
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + clazz.getSimpleName() + " with id " + id + " at revision " + revision));
    }

    @Transactional(readOnly = true)
    public <T> List<T> getEntitiesAtRevision(Number revision, Class<T> entityClass) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.createQuery()
                .forEntitiesAtRevision(entityClass, revision)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public ConfigRevision getRevision(Integer id) {
        return configRevisionJpaRepository.findById(id)
                .map(configRevisionEntityMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find config revision with id " + id));
    }

    @Transactional(readOnly = true)
    public ConfigRevision getRevision(Long timestamp) {
        return Optional.ofNullable(configRevisionJpaRepository.findFirstByTimestampLessThanEqualOrderByTimestampDesc(timestamp))
                .map(configRevisionEntityMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find config revision at timestamp " + timestamp));
    }
}

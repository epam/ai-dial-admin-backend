package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.dao.mapper.AuditActivityEntityMapper;
import com.epam.aidial.cfg.dao.mapper.PageEntityMapper;
import com.epam.aidial.cfg.domain.model.AuditActivity;
import com.epam.aidial.cfg.domain.model.Page;
import com.epam.aidial.cfg.domain.model.page.PageRequestModel;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuditActivityService {

    private static final Set<String> auditActivityCaseInSensitiveColumns = Set.of(
            "activityId",
            "activityType",
            "resourceType",
            "resourceId",
            "initiatedAuthor",
            "initiatedEmail"
    );

    private final AuditActivityEntityMapper auditActivityEntityMapper;
    private final PageEntityMapper pageEntityMapper;
    private final AuditActivityJpaRepository auditActivityJpaRepository;

    @Transactional(readOnly = true)
    public Page<AuditActivity> getActivitiesList(PageRequestModel pageRequest) {
        var page = pageEntityMapper.toPageRequest(pageRequest);
        var filters = pageEntityMapper.toSpecifications(pageRequest,
                new PageEntityMapper.SpecificationContext(auditActivityCaseInSensitiveColumns), AuditActivityEntity.class);
        var specification = Specification.allOf(Specification.allOf(filters), defaultFilters());
        var resultPage = auditActivityJpaRepository.findAll(specification, page);

        var activities = resultPage
                .stream()
                .map(auditActivityEntityMapper::map)
                .collect(Collectors.toList());

        return Page.<AuditActivity>builder()
                .data(activities)
                .total(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public AuditActivity getActivity(UUID activityId) {
        return auditActivityJpaRepository.findById(activityId)
                .map(auditActivityEntityMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find activity with id " + activityId));
    }

    private static Specification<AuditActivityEntity> defaultFilters() {
        return Specification.allOf(
                (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("resourceType"), "RoleLimit"),
                (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("resourceType"), "Deployment")
        );
    }
}

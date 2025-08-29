package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.domain.model.page.SortDirection;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.PageDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.dto.page.SortDto;
import com.epam.aidial.cfg.web.facade.AuditActivityFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ActivityAuditFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private AuditActivityFacade auditActivityFacade;

    private void initRoles() {
        RoleDto role1 = createRoleDto("ActivityAudit1");
        RoleDto role2 = createRoleDto("ActivityAudit2");
        RoleDto role3 = createRoleDto("ActivityAudit3");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
        roleFacade.createRole(role3);
    }

    @Test
    public void shouldSuccessfullyLogAuditActivities() {
        long prevTotal = auditActivityFacade.getAuditActivities(new PageRequestDto()).getTotal();

        initRoles();

        // assert roles creation
        prevTotal = assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Create", "Role", "roleActivityAudit3"),
                new AuditActivityEntityId("Create", "Role", "roleActivityAudit2"),
                new AuditActivityEntityId("Create", "Role", "roleActivityAudit1")));

        // create model1
        ModelDto modelDto = createDto("ActivityAudit1");
        modelFacade.createModel(modelDto);

        // assert model creation
        prevTotal = assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Update", "Role", "roleActivityAudit1"),
                new AuditActivityEntityId("Create", "Model", "modelActivityAudit1")));

        // update model1 description
        ModelDto updatedModel = createDto("ActivityAudit1");
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        modelFacade.updateModel(modelDto.getName(), updatedModel, null);

        // assert model creation
        prevTotal = assertAudit(prevTotal, List.of(new AuditActivityEntityId("Update", "Model", "modelActivityAudit1")));

        // add roles to model1
        updatedModel.setDefaults(Map.of());
        updatedModel.setDefaultRoleLimit(new LimitDto());
        updatedModel.setRoleLimits(Map.of("roleActivityAudit2", new LimitDto(), "roleActivityAudit3", new LimitDto()));
        modelFacade.updateModel(modelDto.getName(), updatedModel, null);

        prevTotal = assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Update", "Role", "roleActivityAudit3"),
                new AuditActivityEntityId("Update", "Role", "roleActivityAudit2"),
                new AuditActivityEntityId("Update", "Role", "roleActivityAudit1"),
                new AuditActivityEntityId("Update", "Model", "modelActivityAudit1")));

        // update model1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        updatedModel.setRoleLimits(Map.of("roleActivityAudit3", limitDto));
        modelFacade.updateModel(modelDto.getName(), updatedModel, null);

        prevTotal = assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Update", "Role", "roleActivityAudit3"),
                new AuditActivityEntityId("Update", "Role", "roleActivityAudit2"),
                new AuditActivityEntityId("Update", "Model", "modelActivityAudit1")));

        LimitDto limitDto2 = new LimitDto();
        limitDto2.setDay(20L);
        updatedModel.setDefaultRoleLimit(limitDto2);
        modelFacade.updateModel(modelDto.getName(), updatedModel, null);

        prevTotal = assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Update", "Model", "modelActivityAudit1")));

        roleFacade.deleteRole("roleActivityAudit3");

        prevTotal = assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Update", "Model", "modelActivityAudit1"),
                new AuditActivityEntityId("Delete", "Role", "roleActivityAudit3")));

        // delete model 1
        modelFacade.deleteModel(modelDto.getName());

        assertAudit(prevTotal, List.of(
                new AuditActivityEntityId("Delete", "Model", "modelActivityAudit1")));
    }

    private long assertAudit(long prevTotal, List<AuditActivityEntityId> expected) {
        PageDto<AuditActivityDto> auditActivities = auditActivityFacade.getAuditActivities(queryLastActivities(expected.size()));
        Assertions.assertEquals(prevTotal + expected.size(), auditActivities.getTotal());
        List<AuditActivityEntityId> actual = mapActual(auditActivities);
        Assertions.assertEquals(expected, actual);
        return auditActivities.getTotal();
    }

    private List<AuditActivityEntityId> mapActual(PageDto<AuditActivityDto> auditActivities) {
        return auditActivities.getData().stream()
                .map(AuditActivityEntityId::of)
                .collect(Collectors.toList());
    }

    private PageRequestDto queryLastActivities(int size) {
        PageRequestDto pageRequestDto = new PageRequestDto(0, size, List.of(
                new SortDto("epochTimestampMs", SortDirection.DESC),
                new SortDto("activityType", SortDirection.DESC),
                new SortDto("resourceType", SortDirection.DESC),
                new SortDto("resourceId", SortDirection.DESC)
        ), List.of());
        return pageRequestDto;
    }

    private ModelDto createDto(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        modelDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return modelDto;
    }

    private RoleDto createRoleDto(String suffix) {
        RoleDto role1 = new RoleDto();
        role1.setName("role" + suffix);
        role1.setDescription("role" + suffix);
        return role1;
    }

    record AuditActivityEntityId(String activityType, String resourceType, String resourceId) {
        static AuditActivityEntityId of(AuditActivityDto entity) {
            return new AuditActivityEntityId(entity.getActivityType(), entity.getResourceType(), entity.getResourceId());
        }
    }
}

package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.EntityRevisionDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.ConfigTransfer;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAuditActivityDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class GeneralHistoryFunctionalTest {

    @Autowired
    private ConfigTransfer configTransfer;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @PersistenceContext
    EntityManager em;

    @Test
    public void shouldTrackOnlyKeyDeletionDuringSystemRollback() {
        // load initial data
        loadData();

        // remember rev number
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // create key
        KeyDto keyDto = createKeyDto("1");
        keyFacade.createKey(keyDto);

        // rollback
        historyFacade.rollbackToRevision(revNumberToRollback);

        // verify
        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        Collection<EntityRevisionDto<RoleDto>> roleEntityRevisions = roleFacade.getEntityRevisionsAt(latestRevision);
        assertThat(roleEntityRevisions).isEmpty();
        Collection<EntityRevisionDto<AdapterDto>> adapterEntityRevisions = adapterFacade.getEntityRevisionsAt(latestRevision);
        assertThat(adapterEntityRevisions).isEmpty();

        EntityRevisionDto<KeyDto> expectedKeyRevision = new EntityRevisionDto<>(null, latestRevision, EntityRevisionDto.RevisionTypeDto.DEL);

        Collection<EntityRevisionDto<KeyDto>> keyEntityRevisions = keyFacade.getEntityRevisionsAt(latestRevision);
        assertThat(keyEntityRevisions)
                .usingRecursiveFieldByFieldElementComparatorOnFields("configRevisionId", "revisionType")
                .containsExactlyInAnyOrder(expectedKeyRevision);

        AuditActivityDto expectedKeyActivity = createAuditActivityDto("Delete", "Key", keyDto.getName());

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId")
                .containsExactlyInAnyOrder(expectedKeyActivity);
    }

    @Test
    public void shouldTrackOnlyRoleAndModelDeletionDuringSystemRollback() {
        // load initial data
        loadData();

        // remember rev number
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // create model
        ModelDto modelDto = createModelDto("1");
        modelFacade.createModel(modelDto);

        // create role
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        RoleDto roleDto = createRoleDto("1");
        roleDto.setLimits(Map.of(modelDto.getName(), limitDto));
        roleFacade.createRole(roleDto);

        // rollback
        historyFacade.rollbackToRevision(revNumberToRollback);

        // verify
        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        Collection<EntityRevisionDto<KeyDto>> keyEntityRevisions = keyFacade.getEntityRevisionsAt(latestRevision);
        assertThat(keyEntityRevisions).isEmpty();
        Collection<EntityRevisionDto<AdapterDto>> adapterEntityRevisions = adapterFacade.getEntityRevisionsAt(latestRevision);
        assertThat(adapterEntityRevisions).isEmpty();

        EntityRevisionDto<RoleDto> expectedRoleRevision = new EntityRevisionDto<>(null, latestRevision, EntityRevisionDto.RevisionTypeDto.DEL);

        Collection<EntityRevisionDto<RoleDto>> roleEntityRevisions = roleFacade.getEntityRevisionsAt(latestRevision);
        assertThat(roleEntityRevisions)
                .usingRecursiveFieldByFieldElementComparatorOnFields("configRevisionId", "revisionType")
                .containsExactlyInAnyOrder(expectedRoleRevision);

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Delete", "Role", roleDto.getName());
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Delete", "Model", modelDto.getName());

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId")
                .containsExactlyInAnyOrder(expectedRoleActivity, expectedModelActivity);
    }

    private void loadData() {
        MultipartFile multipartFile = initialConfig();
        configTransfer.importConfigZip(multipartFile, new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, true));
    }

    @SneakyThrows
    private MultipartFile initialConfig() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            String config = ResourceUtils.readResource("/config_in_admin_format.json");

            ZipEntry entry = new ZipEntry("aidial.config.json");
            zos.putNextEntry(entry);
            zos.write(config.getBytes(StandardCharsets.UTF_8));

            zos.closeEntry();
            zos.finish();

            return new MockMultipartFile("file", baos.toByteArray());
        }
    }
}

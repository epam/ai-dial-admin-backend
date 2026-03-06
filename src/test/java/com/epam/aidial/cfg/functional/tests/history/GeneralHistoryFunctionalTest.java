package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.EntityRevisionDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.ConfigTransfer;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDto;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class GeneralHistoryFunctionalTest {

    @Autowired
    ConfigTransfer configTransfer;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldTrackOnlyKeyDeletionDuringSystemRollback() {
        // load initial data
        loadData();

        // remember rev number
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // create key
        keyFacade.createKey(createKeyDto("1"));

        // rollback
        historyFacade.rollbackToRevision(revNumberToRollback);

        // verify
        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        Collection<EntityRevisionDto<RoleDto>> roleEntityRevisions = roleFacade.getEntityRevisionsAt(latestRevision);
        assertThat(roleEntityRevisions).isEmpty();
        Collection<EntityRevisionDto<AdapterDto>> adapterEntityRevisions = adapterFacade.getEntityRevisionsAt(latestRevision);
        assertThat(adapterEntityRevisions).isEmpty();

        Collection<EntityRevisionDto<KeyDto>> keyEntityRevisions = keyFacade.getEntityRevisionsAt(latestRevision);
        assertThat(keyEntityRevisions).hasSize(1).first().satisfies(
                revision -> {
                    assertThat(revision.getRevisionType()).isEqualTo(EntityRevisionDto.RevisionTypeDto.DEL);
                    assertThat(revision.getConfigRevisionId()).isEqualTo(latestRevision);
                }
        );

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities).hasSize(1).first().satisfies(
                auditActivity -> {
                    assertThat(auditActivity.getResourceType()).isEqualTo("Key");
                    assertThat(auditActivity.getResourceId()).isEqualTo("key1");
                    assertThat(auditActivity.getActivityType()).isEqualTo("Delete");
                }
        );
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

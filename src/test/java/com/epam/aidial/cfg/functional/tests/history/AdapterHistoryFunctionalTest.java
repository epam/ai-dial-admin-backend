package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AdapterHistoryFunctionalTest {

    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateAdapter() {

        // create adapter1
        AdapterDto adapterDto = createDto("1");
        adapterFacade.createAdapter(adapterDto);

        // update adapter1 description
        AdapterDto updatedAdapter = createDto("1");
        updatedAdapter.setDescription("new adapter description");
        adapterFacade.updateAdapter(adapterDto.getName(), updatedAdapter);

        // verify adapter1
        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        var expected = createDto("1");
        expected.setDescription("new adapter description");
        assertAdapter(actual, expected);

        var actualAtRevision = actual;
        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        updatedAdapter.setDescription("new new adapter description");
        adapterFacade.updateAdapter(adapterDto.getName(), updatedAdapter);

        // delete adapter 1
        adapterFacade.deleteAdapter(adapterDto.getName());

        // create adapter 2
        adapterFacade.createAdapter(createDto("2"));

        // create adapter3
        adapterFacade.createAdapter(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<AdapterDto> adaptersAfterRollbackToRevision = adapterFacade.getAllAdapters();
        Assertions.assertEquals(List.of(actualAtRevision), adaptersAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackAdaptersLinkedWithModels() {
        initRoles();

        // create model1
        ModelDto model1 = createModel("1");
        modelFacade.createModel(model1);
        ModelDto model2 = createModel("2");
        modelFacade.createModel(model2);

        // create adapter1
        AdapterDto adapter1 = createDto("1");
        adapter1.setModels(List.of(model1.getName()));
        adapterFacade.createAdapter(adapter1);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = adapterFacade.getAllAdapters();

        // update adapter
        adapter1.setModels(List.of(model2.getName()));
        adapterFacade.updateAdapter(adapter1.getName(), adapter1);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        var adaptersAfterRollbackToRevision = adapterFacade.getAllAdapters();
        Assertions.assertEquals(actualAtRevision, adaptersAfterRollbackToRevision);
    }

    private ModelDto createModel(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        modelDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return modelDto;
    }

    private ApplicationDto createApplication(String suffix) {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application" + suffix);
        applicationDto.setDescription("description" + suffix);
        applicationDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return applicationDto;
    }

    private void initRoles() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");
        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
    }

    private void assertAdapter(AdapterDto actual, AdapterDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private AdapterDto createDto(String suffix) {
        AdapterDto adapterDto = new AdapterDto();
        adapterDto.setName("adapter" + suffix);
        adapterDto.setBaseEndpoint("https://endpoint.test.com/adapter" + suffix);
        adapterDto.setDescription("description" + suffix);
        adapterDto.setModels(List.of());
        return adapterDto;
    }
}
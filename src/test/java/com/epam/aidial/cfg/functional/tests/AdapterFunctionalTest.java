package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AdapterFunctionalTest {

    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private ModelFacade modelFacade;

    @Test
    public void shouldSuccessfullyCreateAndGetAdapters() {
        AdapterDto adapterDto = createDto("1");

        adapterFacade.createAdapter(adapterDto);

        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        AdapterDto expected = createDto("1");

        Assertions.assertEquals(expected, actual);

        adapterFacade.createAdapter(createDto("2"));

        Collection<AdapterDto> actualAdapters = adapterFacade.getAllAdapters();

        assertAdapters(actualAdapters, List.of(createDto("1"), createDto("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteAdapter() {
        AdapterDto adapterDto = createDto("1");
        adapterFacade.createAdapter(adapterDto);

        adapterFacade.deleteAdapter(adapterDto.getName(), false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> adapterFacade.getAdapter(adapterDto.getName()));
        Assertions.assertTrue(adapterFacade.getAllAdapters().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateAdapter() {
        AdapterDto adapterDto = createDto("1");
        adapterFacade.createAdapter(adapterDto);
        AdapterDto updatedAdapter = createDto("1");
        updatedAdapter.setBaseEndpoint("new adapter endpoint");

        adapterFacade.updateAdapter(adapterDto.getName(), updatedAdapter);

        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        var expected = createDto("1");
        expected.setBaseEndpoint("new adapter endpoint");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowExceptionWhenRenameAdapter() {
        AdapterDto adapterDto = createDto("1");
        adapterFacade.createAdapter(adapterDto);
        AdapterDto updatedAdapter = createDto("2");
        updatedAdapter.setBaseEndpoint("new adapter endpoint");

        Assertions.assertThrows(IllegalArgumentException.class, () -> adapterFacade.updateAdapter(adapterDto.getName(), updatedAdapter));
    }

    @Test
    public void shouldCreateAndAddAdapterToModel() {
        AdapterDto adapterDto = createDto("1");

        adapterFacade.createAdapter(adapterDto);

        ModelDto model1 = createModel("1");
        model1.setAdapter("adapter1");
        modelFacade.createModel(model1);

        ModelDto model2 = createModel("2");
        model2.setAdapter("adapter1");
        modelFacade.createModel(model2);

        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        AdapterDto expected = createDto("1");
        expected.setModels(List.of("model1", "model2"));

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldChangeModelAdapter() {
        AdapterDto adapterDto1 = createDto("1");
        adapterFacade.createAdapter(adapterDto1);
        AdapterDto adapterDto2 = createDto("2");
        adapterFacade.createAdapter(adapterDto2);

        ModelDto model1 = createModel("1");
        model1.setAdapter("adapter1");
        modelFacade.createModel(model1);
        model1.setAdapter("adapter2");
        model1.setEndpointDeploymentName("model1");
        modelFacade.updateModel(model1.getName(), model1);

        AdapterDto actualAdapter1 = adapterFacade.getAdapter(adapterDto1.getName());
        AdapterDto actualAdapter2 = adapterFacade.getAdapter(adapterDto2.getName());
        ModelDto actualModel1 = modelFacade.getModel(model1.getName());

        AdapterDto expectedAdapter1 = createDto("1");
        expectedAdapter1.setModels(List.of());
        Assertions.assertEquals(expectedAdapter1, actualAdapter1);

        AdapterDto expectedAdapter2 = createDto("2");
        expectedAdapter2.setModels(List.of("model1"));
        Assertions.assertEquals(expectedAdapter2, actualAdapter2);

        ModelDto expectedModel1 = createModel("1");
        expectedModel1.setAdapter("adapter2");
        expectedModel1.setEndpoint("endpoint2/model1/chat/completions");
        expectedModel1.setDefaults(Map.of());
        expectedModel1.setRoleLimits(Map.of());
        expectedModel1.setRoleShareResourceLimits(Map.of());
        expectedModel1.setDefaultRoleLimit(new LimitDto());
        expectedModel1.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        expectedModel1.setEndpointDeploymentName("model1");
        Assertions.assertEquals(expectedModel1, actualModel1);
    }

    @Test
    public void shouldChangeModelsInAdapter() {
        AdapterDto adapterDto1 = createDto("1");
        adapterFacade.createAdapter(adapterDto1);

        ModelDto model1 = createModel("1");
        modelFacade.createModel(model1);

        // add model to adapter
        adapterDto1.setModels(List.of("model1"));

        adapterFacade.updateAdapter(adapterDto1.getName(), adapterDto1);
        AdapterDto actualAdapter1 = adapterFacade.getAdapter(adapterDto1.getName());
        ModelDto actualModel1 = modelFacade.getModel(model1.getName());

        // verify adapter and model
        AdapterDto expectedAdapter1 = createDto("1");
        expectedAdapter1.setModels(List.of("model1"));
        Assertions.assertEquals(expectedAdapter1, actualAdapter1);

        ModelDto expectedModel1 = createModel("1");
        expectedModel1.setAdapter("adapter1");
        expectedModel1.setEndpoint("endpoint1/chat/completions");
        expectedModel1.setDefaults(Map.of());
        expectedModel1.setRoleLimits(Map.of());
        expectedModel1.setRoleShareResourceLimits(Map.of());
        expectedModel1.setDefaultRoleLimit(new LimitDto());
        expectedModel1.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        Assertions.assertEquals(expectedModel1, actualModel1);

        // remove model from adapter
        adapterDto1.setModels(List.of());

        adapterFacade.updateAdapter(adapterDto1.getName(), adapterDto1);
        AdapterDto actualAdapter2 = adapterFacade.getAdapter(adapterDto1.getName());
        ModelDto actualModel2 = modelFacade.getModel(model1.getName());

        AdapterDto expectedAdapter2 = createDto("1");
        expectedAdapter2.setModels(List.of());
        Assertions.assertEquals(expectedAdapter2, actualAdapter2);

        ModelDto expectedModel2 = createModel("1");
        expectedModel2.setDefaults(Map.of());
        expectedModel2.setRoleLimits(Map.of());
        expectedModel2.setRoleShareResourceLimits(Map.of());
        expectedModel2.setDefaultRoleLimit(new LimitDto());
        expectedModel2.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        Assertions.assertEquals(expectedModel2, actualModel2);
    }

    private AdapterDto createDto(String suffix) {
        AdapterDto adapterDto = new AdapterDto();
        adapterDto.setName("adapter" + suffix);
        adapterDto.setBaseEndpoint("endpoint" + suffix);
        return adapterDto;
    }

    private ModelDto createModel(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        return modelDto;
    }

    private void assertAdapters(Collection<AdapterDto> actual, Collection<AdapterDto> expected) {
        Map<String, AdapterDto> actualMap = toMap(actual);
        Map<String, AdapterDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            AdapterDto actual1 = actualMap.get(name);
            Assertions.assertEquals(expectedMap.get(name), actual1);
        }
    }

    private Map<String, AdapterDto> toMap(Collection<AdapterDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(AdapterDto::getName, Function.identity()));
    }
}

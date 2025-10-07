package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.source.AdapterSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;
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

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAdapterDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;

public abstract class AdapterFunctionalTest {

    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private ModelFacade modelFacade;

    @Test
    public void shouldSuccessfullyCreateAndGetAdapters() {
        AdapterDto adapterDto = createAdapterDto("1");

        adapterFacade.createAdapter(adapterDto);

        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        AdapterDto expected = createAdapterDto("1");

        Assertions.assertEquals(expected, actual);

        adapterFacade.createAdapter(createAdapterDto("2"));

        Collection<AdapterDto> actualAdapters = adapterFacade.getAllAdapters();

        assertAdapters(actualAdapters, List.of(createAdapterDto("1"), createAdapterDto("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteAdapter() {
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);

        adapterFacade.deleteAdapter(adapterDto.getName(), false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> adapterFacade.getAdapter(adapterDto.getName()));
        Assertions.assertTrue(adapterFacade.getAllAdapters().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateAdapter() {
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);
        AdapterDto updatedAdapter = createAdapterDto("1");
        updatedAdapter.setBaseEndpoint("new adapter endpoint");

        adapterFacade.updateAdapter(adapterDto.getName(), updatedAdapter);

        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        var expected = createAdapterDto("1");
        expected.setBaseEndpoint("new adapter endpoint");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowExceptionWhenRenameAdapter() {
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);
        AdapterDto updatedAdapter = createAdapterDto("2");
        updatedAdapter.setBaseEndpoint("new adapter endpoint");

        Assertions.assertThrows(IllegalArgumentException.class, () -> adapterFacade.updateAdapter(adapterDto.getName(), updatedAdapter));
    }

    @Test
    public void shouldCreateAndAddAdapterToModel() {
        AdapterDto adapterDto = createAdapterDto("1");

        adapterFacade.createAdapter(adapterDto);

        AdapterSourceDto sourceDto = new AdapterSourceDto("adapter1", "/some-path/chat/completions");

        ModelDto model1 = createModelDto("1");
        model1.setSource(sourceDto);
        modelFacade.createModel(model1);

        ModelDto model2 = createModelDto("2");
        model2.setSource(sourceDto);
        modelFacade.createModel(model2);

        AdapterDto actual = adapterFacade.getAdapter(adapterDto.getName());
        AdapterDto expected = createAdapterDto("1");
        expected.setModels(List.of("model1", "model2"));

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldChangeModelAdapter() {
        AdapterDto adapterDto1 = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto1);
        AdapterDto adapterDto2 = createAdapterDto("2");
        adapterFacade.createAdapter(adapterDto2);

        ModelDto model1 = createModelDto("1");
        AdapterSourceDto source1 = new AdapterSourceDto("adapter1", "/some-path/chat/completions");
        model1.setSource(source1);
        modelFacade.createModel(model1);
        AdapterSourceDto source2 = new AdapterSourceDto("adapter2", "/model1/chat/completions");
        model1.setSource(source2);
        modelFacade.updateModel(model1.getName(), model1, "*");

        AdapterDto actualAdapter1 = adapterFacade.getAdapter(adapterDto1.getName());
        AdapterDto actualAdapter2 = adapterFacade.getAdapter(adapterDto2.getName());
        ModelDto actualModel1 = modelFacade.getModel(model1.getName());

        AdapterDto expectedAdapter1 = createAdapterDto("1");
        expectedAdapter1.setModels(List.of());
        Assertions.assertEquals(expectedAdapter1, actualAdapter1);

        AdapterDto expectedAdapter2 = createAdapterDto("2");
        expectedAdapter2.setModels(List.of("model1"));
        Assertions.assertEquals(expectedAdapter2, actualAdapter2);

        ModelDto expectedModel1 = createModelDto("1");
        expectedModel1.setSource(source2);
        expectedModel1.setDefaults(Map.of());
        expectedModel1.setRoleLimits(Map.of());
        expectedModel1.setRoleShareResourceLimits(Map.of());
        expectedModel1.setDefaultRoleLimit(new LimitDto());
        expectedModel1.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        Assertions.assertEquals(expectedModel1, actualModel1);
    }

    @Test
    public void shouldChangeModelsInAdapter() {
        AdapterDto adapterDto1 = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto1);

        ModelDto model1 = createModelDto("1");
        modelFacade.createModel(model1);

        model1.setSource(new AdapterSourceDto(adapterDto1.getName(), "/chat/completions"));
        modelFacade.updateModel(model1.getName(), model1, "*");

        AdapterDto actualAdapter1 = adapterFacade.getAdapter(adapterDto1.getName());
        ModelDto actualModel1 = modelFacade.getModel(model1.getName());

        // verify adapter and model
        AdapterDto expectedAdapter1 = createAdapterDto("1");
        expectedAdapter1.setModels(List.of("model1"));
        Assertions.assertEquals(expectedAdapter1, actualAdapter1);

        ModelDto expectedModel1 = createModelDto("1");
        expectedModel1.setSource(new AdapterSourceDto("adapter1", "/chat/completions"));
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

        AdapterDto expectedAdapter2 = createAdapterDto("1");
        expectedAdapter2.setModels(List.of());
        Assertions.assertEquals(expectedAdapter2, actualAdapter2);

        ModelDto expectedModel2 = createModelDto("1");
        expectedModel2.setDefaults(Map.of());
        expectedModel2.setRoleLimits(Map.of());
        expectedModel2.setRoleShareResourceLimits(Map.of());
        expectedModel2.setDefaultRoleLimit(new LimitDto());
        expectedModel2.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        expectedModel2.setSource(new ModelEndpointsSourceDto());
        expectedModel2.setEndpoint("https://endpoint.test.com/adapter1/chat/completions");
        Assertions.assertEquals(expectedModel2, actualModel2);
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

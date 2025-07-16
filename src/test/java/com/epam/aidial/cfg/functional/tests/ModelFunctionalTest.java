package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ModelFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private AdapterFacade adapterFacade;

    private void initRoles() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");
        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
        roleFacade.createRole(role3);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetModels() {
        initRoles();

        ModelDto modelDto = createDtoWithDefaults("1");

        modelFacade.createModel(modelDto);

        ModelDto actual = modelFacade.getModel(modelDto.getName());

        assertModel(actual, expectedDto1WithDefaults());

        modelFacade.createModel(createDto("2"));

        Collection<ModelDto> actualModels = modelFacade.getAll();

        assertModels(actualModels, expectedDtos());
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteModel() {
        initRoles();

        ModelDto modelDto = createDto("1");
        modelFacade.createModel(modelDto);

        modelFacade.deleteModel(modelDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> modelFacade.getModel(modelDto.getName()));
        Assertions.assertTrue(modelFacade.getAll().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateModel() {
        initRoles();
        createAdapter(1);
        createAdapter(2);

        ModelDto modelDto = createDto("1");
        modelDto.setAdapter("adapter1");
        modelFacade.createModel(modelDto);

        ModelDto updatedModel = createDto("1");
        updatedModel.setAdapter("adapter2");
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        updatedModel.setAlias("newModelAlias");
        modelFacade.updateModel(modelDto.getName(), updatedModel);

        ModelDto actual = modelFacade.getModel(modelDto.getName());
        var expected = createDto("1");
        expected.setDescription("new model description");
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setDefaults(Map.of());
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setAdapter("adapter2");
        expected.setEndpoint("http://adapter.endpoint2/newModelAlias/chat/completions");
        expected.setAlias("newModelAlias");
        updatedModel.setDefaults(Map.of());
        updatedModel.setDefaultRoleLimit(new LimitDto());
        assertModel(actual, expected);

        updatedModel.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        modelFacade.updateModel(modelDto.getName(), updatedModel);
        actual = modelFacade.getModel(modelDto.getName());
        expected.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        assertModel(actual, expected);

        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        updatedModel.setRoleLimits(Map.of("role3", limitDto));
        updatedModel.setAlias(null);
        modelFacade.updateModel(modelDto.getName(), updatedModel);
        actual = modelFacade.getModel(modelDto.getName());
        expected.setRoleLimits(Map.of("role3", limitDto));
        expected.setEndpoint("http://adapter.endpoint2/chat/completions");
        expected.setAlias(null);
        assertModel(actual, expected);

        roleFacade.deleteRole("role3");

        actual = modelFacade.getModel(modelDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenRenameModel() {
        initRoles();

        ModelDto modelDto = createDto("1");
        modelFacade.createModel(modelDto);
        ModelDto updatedModel = createDto("2");
        updatedModel.setDescription("new model description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> modelFacade.updateModel(modelDto.getName(), updatedModel)
        );
        Assertions.assertEquals("Model with name: 'model1' can not be renamed. New name: 'model2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyCreateAndAddInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("int1");
        interceptorDto.setDescription("int1_dsc");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ModelDto modelDto = createDto("1");
        modelFacade.createModel(modelDto);
        ModelDto updatedModel = createDto("1");
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        updatedModel.setInterceptors(List.of("int1"));

        modelFacade.updateModel(modelDto.getName(), updatedModel);

        ModelDto actual = modelFacade.getModel(modelDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("int1"));
    }

    @Test
    public void shouldSuccessfullyCreateWithInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("int1");
        interceptorDto.setDescription("int1_dsc");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ModelDto modelDto = createDto("1");
        modelDto.setInterceptors(List.of("int1"));
        modelFacade.createModel(modelDto);

        ModelDto actual = modelFacade.getModel(modelDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("int1"));
    }

    @Test
    public void shouldThrowExceptionWhenCreateModelWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ModelDto modelDto = createDto("1");
        modelDto.setDisplayName("display_name");
        modelDto.setDisplayVersion("1.0");
        modelFacade.createModel(modelDto);

        ModelDto modelDto2 = createDto("2");
        modelDto2.setDisplayName("display_name");
        modelDto2.setDisplayVersion("1.0");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> modelFacade.createModel(modelDto2)
        );
        Assertions.assertEquals("Model with display name: 'display_name' and display version: '1.0' already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateModelWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ModelDto modelDto = createDto("1");
        modelDto.setDisplayName("display_name");
        modelFacade.createModel(modelDto);

        ModelDto modelDto2 = createDto("2");
        modelDto2.setDisplayName("display_name_2");
        modelFacade.createModel(modelDto2);

        modelDto.setDisplayName("display_name_2");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> modelFacade.updateModel(modelDto.getName(), modelDto)
        );
        Assertions.assertEquals("Model with display name: 'display_name_2' and display version: 'null' already exists", exception.getMessage());
    }

    private void assertModels(Collection<ModelDto> actual, Collection<ModelDto> expected) {
        Map<String, ModelDto> actualMap = toMap(actual);
        Map<String, ModelDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertModel(actualMap.get(name), expectedMap.get(name));
        }
    }

    private Map<String, ModelDto> toMap(Collection<ModelDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(ModelDto::getName, Function.identity()));
    }

    private void assertModel(ModelDto actual, ModelDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private ModelDto expectedDto1() {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model1");
        modelDto.setDescription("description1");
        modelDto.setRoleLimits(Map.of(
                "role1", new LimitDto()
        ));
        modelDto.setDefaults(Map.of());
        modelDto.setDefaultRoleLimit(new LimitDto());
        return modelDto;
    }

    private ModelDto expectedDto1WithDefaults() {
        ModelDto modelDto = expectedDto1();
        modelDto.setDefaults(Map.of("max_tokens", 8000));
        return modelDto;
    }

    private Collection<ModelDto> expectedDtos() {
        ModelDto modelDto1 = new ModelDto();
        modelDto1.setName("model1");
        modelDto1.setDescription("description1");
        modelDto1.setRoleLimits(Map.of(
                "role1", new LimitDto()
        ));
        modelDto1.setDefaultRoleLimit(new LimitDto());
        modelDto1.setDefaults(Map.of("max_tokens", 8000));

        ModelDto modelDto2 = new ModelDto();
        modelDto2.setName("model2");
        modelDto2.setDescription("description2");
        modelDto2.setRoleLimits(Map.of(
                "role2", new LimitDto()
        ));
        modelDto2.setDefaultRoleLimit(new LimitDto());
        modelDto2.setDefaults(Map.of());

        return List.of(modelDto1, modelDto2);
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

    private void createAdapter(int i) {
        AdapterDto adapterDto = new AdapterDto();
        adapterDto.setName("adapter" + i);
        adapterDto.setBaseEndpoint("http://adapter.endpoint" + i);
        adapterFacade.createAdapter(adapterDto);
    }

    private ModelDto createDtoWithDefaults(String suffix) {
        ModelDto modelDto = createDto(suffix);
        modelDto.setDefaults(Map.of("max_tokens", 8000));
        return modelDto;
    }
}

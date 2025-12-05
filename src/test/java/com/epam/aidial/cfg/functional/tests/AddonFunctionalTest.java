package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAddonWithRoleLimitsDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public abstract class AddonFunctionalTest {

    @Autowired
    private AddonFacade addonFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetAddons() {
        initRoles();
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");

        addonFacade.createAddon(addonDto);

        AddonDto actual = addonFacade.getAddon(addonDto.getName());
        AddonDto expected = createAddonWithRoleLimitsDto("1");

        assertAddon(actual, expected);

        addonFacade.createAddon(createAddonWithRoleLimitsDto("2"));

        Collection<AddonDto> actualAddons = addonFacade.getAllAddons();

        assertAddons(actualAddons, List.of(createAddonWithRoleLimitsDto("1"),
                createAddonWithRoleLimitsDto("2")));
    }

    @Test
    void testCreate_UnsupportedException() {
        // given
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        doThrow(new UnsupportedOperationException("Feature flag 'addonsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> addonFacade.createAddon(addonDto))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'addonsSupported' is disabled.");
    }

    @Test
    void testDeleteAddon_UnsupportedException() {
        // given
        doThrow(new UnsupportedOperationException("Feature flag 'addonsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> addonFacade.deleteAddon("addonName"))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'addonsSupported' is disabled.");
    }

    @Test
    void testUpdateAddon_UnsupportedException() {
        // given
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        doThrow(new UnsupportedOperationException("Feature flag 'addonsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> addonFacade.updateAddon(addonDto.getName(), addonDto, "*"))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'addonsSupported' is disabled.");

    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteAddon() {
        initRoles();
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);

        addonFacade.deleteAddon(addonDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> addonFacade.getAddon(addonDto.getName()));
        Assertions.assertTrue(addonFacade.getAllAddons().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateAddon() {
        initRoles();
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);
        AddonDto updatedAddon = createAddonWithRoleLimitsDto("1");
        updatedAddon.setDescription("new addon description");

        addonFacade.updateAddon(addonDto.getName(), updatedAddon, "*");

        AddonDto actual = addonFacade.getAddon(addonDto.getName());
        var expected = createAddonWithRoleLimitsDto("1");
        expected.setDescription("new addon description");
        assertAddon(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateAddon() {
        initRoles();
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);
        AddonDto updatedAddon = createAddonWithRoleLimitsDto("1");
        updatedAddon.setDescription("new addon description");

        addonFacade.updateAddon(addonDto.getName(), updatedAddon, "*");

        AddonDto actual = addonFacade.getAddon(updatedAddon.getName());

        var expected = createAddonWithRoleLimitsDto("1");
        expected.setDescription("new addon description");
        assertAddon(actual, expected);
    }

    @Test
    public void shouldThrowExceptionWhenRenameAddon() {
        initRoles();

        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);

        AddonDto updatedAddon = createAddonWithRoleLimitsDto("2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> addonFacade.updateAddon(addonDto.getName(), updatedAddon, "*")
        );
        Assertions.assertEquals("Addon with name: 'addon1' can not be renamed. New name: 'addon2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateAddonWithCorrectHash() {
        initRoles();
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);
        AddonDto updatedAddon = createAddonWithRoleLimitsDto("1");
        updatedAddon.setDescription("new addon description");

        var hash = addonFacade.getAddonWithHash(addonDto.getName()).hash();

        addonFacade.updateAddon(addonDto.getName(), updatedAddon, hash);

        AddonDto actual = addonFacade.getAddon(updatedAddon.getName());

        var expected = createAddonWithRoleLimitsDto("1");
        expected.setDescription("new addon description");
        assertAddon(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateAddonWithIncorrectHash() {
        initRoles();
        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);
        AddonDto updatedAddon = createAddonWithRoleLimitsDto("1");
        updatedAddon.setDescription("new addon description");

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> addonFacade.updateAddon(addonDto.getName(), updatedAddon, "test"));
    }

    @Test
    public void shouldThrowExceptionWhenAddonConcurrencyOverwrite() {
        initRoles();

        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> addonFacade.updateAddon(addonDto.getName(), addonDto, "test")
        );
        Assertions.assertEquals("Unable to update Addon 'addon1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        initRoles();

        AddonDto addonDto = createAddonWithRoleLimitsDto("1");
        addonFacade.createAddon(addonDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> addonFacade.updateAddon(addonDto.getName(), addonDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. Addon:addon1.",
                exception.getMessage());
    }

    private void assertAddon(AddonDto actual, AddonDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getRoleLimits(), actual.getRoleLimits());
    }

    private Map<String, AddonDto> toMap(Collection<AddonDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(AddonDto::getName, Function.identity()));
    }

    private void assertAddons(Collection<AddonDto> actual, Collection<AddonDto> expected) {
        Map<String, AddonDto> actualMap = toMap(actual);
        Map<String, AddonDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertAddon(actualMap.get(name), expectedMap.get(name));
        }
    }

}

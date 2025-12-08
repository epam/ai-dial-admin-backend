package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AdminSettingsDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.facade.AdminSettingsFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.adminSettingsDto;

public abstract class AdminSettingsFunctionalTest {

    @Autowired
    private AdminSettingsFacade adminSettingsFacade;

    @Test
    public void shouldSuccessfullyGetAdminSettings() {
        AdminSettingsDto actual = adminSettingsFacade.getAdminSettingsWithHash().dto();
        AdminSettingsDto expected = adminSettingsDto(null);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldSuccessfullyUpdateAdminSettingsCoreConfigVersion() {
        String coreConfigVersion = "0.40.0";
        adminSettingsFacade.updateCoreConfigVersion(coreConfigVersion, "*");

        AdminSettingsDto actual = adminSettingsFacade.getAdminSettingsWithHash().dto();
        AdminSettingsDto expected = adminSettingsDto(coreConfigVersion);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldSuccessfullyUpdateAdminSettingsCoreConfigVersionWithCorrectHash() {
        String coreConfigVersion = "0.40.0";

        String hash = adminSettingsFacade.getAdminSettingsWithHash().hash();
        adminSettingsFacade.updateCoreConfigVersion(coreConfigVersion, hash);

        AdminSettingsDto actual = adminSettingsFacade.getAdminSettingsWithHash().dto();
        AdminSettingsDto expected = adminSettingsDto(coreConfigVersion);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowWhenUpdateAdminSettingsCoreConfigVersionWithIncorrectHash() {
        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> adminSettingsFacade.updateCoreConfigVersion("0.40.0", "test")
        );

        Assertions.assertEquals("Unable to update AdminSettings. The data may have been modified by another user. Please reload the data and try again.",
                exception.getMessage());
    }
}

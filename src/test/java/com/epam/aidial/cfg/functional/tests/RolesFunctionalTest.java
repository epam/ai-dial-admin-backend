package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ResourceTypeDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.ValidityStateDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAddonDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

public abstract class RolesFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private AddonFacade addonFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;

    @BeforeEach
    public void beforeEach() {
        initKeys();
        initAddons();
    }

    private void initKeys() {
        keyFacade.createKey(createKeyDto("1"));
        keyFacade.createKey(createKeyDto("2"));
        keyFacade.createKey(createKeyDto("3"));
    }

    private void initAddons() {
        addonFacade.createAddon(createAddonDto("1"));
        addonFacade.createAddon(createAddonDto("2"));
        addonFacade.createAddon(createAddonDto("3"));
    }

    @Test
    public void shouldSuccessfullyGetDefaultRole() {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("default");

        RoleDto actualRoleDto = roleFacade.getRole("default");

        assertRole(actualRoleDto, roleDto);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetRoles() {
        RoleDto roleDto = createRoleDto("1");

        roleFacade.createRole(roleDto);

        RoleDto actual = roleFacade.getRole(roleDto.getName());

        assertRole(actual, expectedDto1());

        roleFacade.createRole(createRoleDto("2"));

        Collection<RoleDto> actualRoles = roleFacade.getAllRoles();

        assertRoles(actualRoles, expectedDtos());
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteRole() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        roleFacade.deleteRole(roleDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> roleFacade.getRole(roleDto.getName()));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateRole() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);
        RoleDto updatedRole = createDto("1");
        updatedRole.setDescription("new role description");

        roleFacade.updateRole(roleDto.getName(), updatedRole, "*");

        RoleDto actual = roleFacade.getRole(roleDto.getName());
        var expected = createDto("1");
        expected.setDescription("new role description");
        assertRole(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateRoleWithCorrectHash() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);
        RoleDto updatedRole = createDto("1");
        updatedRole.setDescription("new role description");

        var hash = roleFacade.getRoleWithHash(roleDto.getName()).hash();

        roleFacade.updateRole(roleDto.getName(), updatedRole, hash);

        RoleDto actual = roleFacade.getRole(roleDto.getName());
        var expected = createDto("1");
        expected.setDescription("new role description");
        assertRole(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateRoleWithIncorrectHash() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);
        RoleDto updatedRole = createDto("1");
        updatedRole.setDescription("new role description");

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> roleFacade.updateRole(roleDto.getName(), updatedRole, "test"));
    }

    @Test
    public void shouldThrowExceptionWhenRoleConcurrencyOverwrite() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> roleFacade.updateRole(roleDto.getName(), roleDto, "test")
        );
        Assertions.assertEquals("Optimistic lock conflict on update: roleName:'role1'"
                + ". Reload the data.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> roleFacade.updateRole(roleDto.getName(), roleDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. Role:role1.",
                exception.getMessage());
    }


    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    public void shouldThrowExceptionWhenEmptyName(String name) {
        RoleDto roleDto = createDto("1");
        roleDto.setName(name);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> roleFacade.createRole(roleDto)
        );
        Assertions.assertEquals("Role name must not be empty", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenRenameRole() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);
        RoleDto updatedRole = createDto("2");
        updatedRole.setDescription("new role description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> roleFacade.updateRole(roleDto.getName(), updatedRole, "*")
        );
        Assertions.assertEquals("Role with name: 'role1' can not be renamed. New role name: 'role2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateKeys() {
        // create role1: keys=[key1, key2]; role2: keys=[key1, key3]
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createDtoWithKeys("1", List.of("key1", "key2"));
        roleFacade.createRole(roleDto);
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto2 = createDtoWithKeys("2", List.of("key1", "key3"));
        roleFacade.createRole(roleDto2);

        // check key1: roles=[role1, role2]; key2: roles=[role1]; key3: roles=[role2]
        KeyDto key1 = keyFacade.getKey("key1");
        Assertions.assertEquals(List.of("role1", "role2"), key1.getRoles());
        Assertions.assertEquals(keyValidState(), key1.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key1.getUpdatedAt());
        KeyDto key2 = keyFacade.getKey("key2");
        Assertions.assertEquals(List.of("role1"), key2.getRoles());
        Assertions.assertEquals(keyValidState(), key2.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(120), key2.getUpdatedAt());
        KeyDto key3 = keyFacade.getKey("key3");
        Assertions.assertEquals(List.of("role2"), key3.getRoles());
        Assertions.assertEquals(keyValidState(), key3.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key3.getUpdatedAt());

        // update role1: keys=[key2, key3]
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        RoleDto updatedRoleDto = createDtoWithKeys("1", List.of("key2", "key3"));
        roleFacade.updateRole(updatedRoleDto.getName(), updatedRoleDto, "*");

        // check role1: keys=[key2, key3]
        RoleDto actual = roleFacade.getRole(updatedRoleDto.getName());
        updatedRoleDto.setLimits(Map.of());
        Assertions.assertEquals(actual, updatedRoleDto);

        // check key1: roles=[role2]; key2: roles=[role1]; key3: roles=[role2, role1]
        key1 = keyFacade.getKey("key1");
        Assertions.assertEquals(List.of("role2"), key1.getRoles());
        Assertions.assertEquals(keyValidState(), key1.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(320), key1.getUpdatedAt());
        key2 = keyFacade.getKey("key2");
        Assertions.assertEquals(List.of("role1"), key2.getRoles());
        Assertions.assertEquals(keyValidState(), key2.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(120), key2.getUpdatedAt());
        key3 = keyFacade.getKey("key3");
        Assertions.assertEquals(List.of("role2", "role1"), key3.getRoles());
        Assertions.assertEquals(keyValidState(), key3.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(320), key3.getUpdatedAt());
    }

    @Test
    public void shouldSuccessfullyUpdateRolesWhenDeleteKey() {
        // create role1: keys=[key1, key2]; role2: keys=[key1, key3]
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createDtoWithKeys("1", List.of("key1", "key2"));
        roleFacade.createRole(roleDto);
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto2 = createDtoWithKeys("2", List.of("key1", "key3"));
        roleFacade.createRole(roleDto2);

        // check key1: roles=[role1, role2]; key2: roles=[role1]; key3: roles=[role2]
        KeyDto key1 = keyFacade.getKey("key1");
        Assertions.assertEquals(List.of("role1", "role2"), key1.getRoles());
        Assertions.assertEquals(keyValidState(), key1.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key1.getUpdatedAt());
        KeyDto key2 = keyFacade.getKey("key2");
        Assertions.assertEquals(List.of("role1"), key2.getRoles());
        Assertions.assertEquals(keyValidState(), key2.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(120), key2.getUpdatedAt());
        KeyDto key3 = keyFacade.getKey("key3");
        Assertions.assertEquals(List.of("role2"), key3.getRoles());
        Assertions.assertEquals(keyValidState(), key3.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key3.getUpdatedAt());

        // delete key1
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        keyFacade.deleteKey("key1");

        // check role1: keys=[key2]; role2: keys=[key3]
        RoleDto expectedRoleDto1 = createDtoWithKeys("1", List.of("key2"));
        expectedRoleDto1.setLimits(Map.of());
        RoleDto expectedRoleDto2 = createDtoWithKeys("2", List.of("key3"));
        expectedRoleDto2.setLimits(Map.of());
        RoleDto actualRole1 = roleFacade.getRole("role1");
        RoleDto actualRole2 = roleFacade.getRole("role2");
        Assertions.assertEquals(expectedRoleDto1, actualRole1);
        Assertions.assertEquals(Instant.ofEpochMilli(320), actualRole1.getUpdatedAt());
        Assertions.assertEquals(expectedRoleDto2, actualRole2);
        Assertions.assertEquals(Instant.ofEpochMilli(320), actualRole2.getUpdatedAt());
    }

    @Test
    public void shouldSuccessfullyUpdateKeysWhenDeleteRole() {
        // create role1: keys=[key1, key2]; role2: keys=[key1, key3]
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createDtoWithKeys("1", List.of("key1", "key2"));
        roleFacade.createRole(roleDto);
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto2 = createDtoWithKeys("2", List.of("key1", "key3"));
        roleFacade.createRole(roleDto2);

        // check key1: roles=[role1, role2]; key2: roles=[role1]; key3: roles=[role2]
        KeyDto key1 = keyFacade.getKey("key1");
        Assertions.assertEquals(List.of("role1", "role2"), key1.getRoles());
        Assertions.assertEquals(keyValidState(), key1.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key1.getUpdatedAt());
        KeyDto key2 = keyFacade.getKey("key2");
        Assertions.assertEquals(List.of("role1"), key2.getRoles());
        Assertions.assertEquals(keyValidState(), key2.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(120), key2.getUpdatedAt());
        KeyDto key3 = keyFacade.getKey("key3");
        Assertions.assertEquals(List.of("role2"), key3.getRoles());
        Assertions.assertEquals(keyValidState(), key3.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key3.getUpdatedAt());

        // delete role1
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        roleFacade.deleteRole("role1");

        // check key1: roles=[role2]; key2: roles=[]; key3: keys=[role2]
        key1 = keyFacade.getKey("key1");
        Assertions.assertEquals(List.of("role2"), key1.getRoles());
        Assertions.assertEquals(keyValidState(), key1.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(320), key1.getUpdatedAt());
        key2 = keyFacade.getKey("key2");
        Assertions.assertEquals(List.of(), key2.getRoles());
        Assertions.assertEquals(keyInvalidState(), key2.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(320), key2.getUpdatedAt());
        key3 = keyFacade.getKey("key3");
        Assertions.assertEquals(List.of("role2"), key3.getRoles());
        Assertions.assertEquals(keyValidState(), key3.getValidityState());
        Assertions.assertEquals(Instant.ofEpochMilli(220), key3.getUpdatedAt());
    }

    @Test
    public void shouldSuccessfullyUpdateLimits() {
        // create role1: limits=[addon1:day=10, addon2:day=10]; role2: limits=[addon1:day=10, addon3:day=10]
        LimitDto dayLimit = new LimitDto();
        dayLimit.setDay(10L);
        RoleDto roleDto = createDtoWithLimits("1", Map.of("addon1", dayLimit, "addon2", dayLimit));
        roleFacade.createRole(roleDto);
        RoleDto roleDto2 = createDtoWithLimits("2", Map.of("addon1", dayLimit, "addon3", dayLimit));
        roleFacade.createRole(roleDto2);

        // check addon1: limits=[role1:day=10, role2:day=10]; addon2: limits=[role1:day=10]; addon3: limits=[role2:day=10]
        AddonDto addon1 = addonFacade.getAddon("addon1");
        Assertions.assertEquals(Map.of("role1", dayLimit, "role2", dayLimit), addon1.getRoleLimits());
        AddonDto addon2 = addonFacade.getAddon("addon2");
        Assertions.assertEquals(Map.of("role1", dayLimit), addon2.getRoleLimits());
        AddonDto addon3 = addonFacade.getAddon("addon3");
        Assertions.assertEquals(Map.of("role2", dayLimit), addon3.getRoleLimits());

        // update role1: limits=[addon2:week=20, addon3:week=20]
        LimitDto weekLimit = new LimitDto();
        weekLimit.setWeek(20L);
        RoleDto updatedRoleDto = createDtoWithLimits("1", Map.of("addon2", weekLimit, "addon3", weekLimit));
        roleFacade.updateRole(updatedRoleDto.getName(), updatedRoleDto, "*");

        // check role1: limits=[addon2:week=20, addon3:week=20]
        RoleDto actual = roleFacade.getRole(updatedRoleDto.getName());
        updatedRoleDto.setGrantedKeys(List.of());
        Assertions.assertEquals(actual, updatedRoleDto);

        // check addon1: limits=[role2:day=10]; addon2: limits=[role1:week=20]; addon3: limits=[role2:day=10, role1:week=20]
        addon1 = addonFacade.getAddon("addon1");
        Assertions.assertEquals(Map.of("role2", dayLimit), addon1.getRoleLimits());
        addon2 = addonFacade.getAddon("addon2");
        Assertions.assertEquals(Map.of("role1", weekLimit), addon2.getRoleLimits());
        addon3 = addonFacade.getAddon("addon3");
        Assertions.assertEquals(Map.of("role1", weekLimit, "role2", dayLimit), addon3.getRoleLimits());
    }

    @Test
    public void shouldSuccessfullyUpdateRolesWhenDeleteAddon() {
        // create role1: limits=[addon1:day=10, addon2:day=10]; role2: limits=[addon1:day=10, addon3:day=10]
        LimitDto dayLimit = new LimitDto();
        dayLimit.setDay(10L);
        RoleDto roleDto = createDtoWithLimits("1", Map.of("addon1", dayLimit, "addon2", dayLimit));
        roleFacade.createRole(roleDto);
        RoleDto roleDto2 = createDtoWithLimits("2", Map.of("addon1", dayLimit, "addon3", dayLimit));
        roleFacade.createRole(roleDto2);

        // check addon1: limits=[role1:day=10, role2:day=10]; addon2: limits=[role1:day=10]; addon3: limits=[role2:day=10]
        AddonDto addon1 = addonFacade.getAddon("addon1");
        Assertions.assertEquals(Map.of("role1", dayLimit, "role2", dayLimit), addon1.getRoleLimits());
        AddonDto addon2 = addonFacade.getAddon("addon2");
        Assertions.assertEquals(Map.of("role1", dayLimit), addon2.getRoleLimits());
        AddonDto addon3 = addonFacade.getAddon("addon3");
        Assertions.assertEquals(Map.of("role2", dayLimit), addon3.getRoleLimits());

        // delete addon1
        addonFacade.deleteAddon("addon1");

        // check role1: limits=[addon2:day=10]; role2: limits=[addon3:day=10]
        RoleDto expectedRoleDto1 = createDtoWithLimits("1", Map.of("addon2", dayLimit));
        expectedRoleDto1.setGrantedKeys(List.of());
        RoleDto expectedRoleDto2 = createDtoWithLimits("2", Map.of("addon3", dayLimit));
        expectedRoleDto2.setGrantedKeys(List.of());
        RoleDto actualRole1 = roleFacade.getRole("role1");
        RoleDto actualRole2 = roleFacade.getRole("role2");
        Assertions.assertEquals(actualRole1, expectedRoleDto1);
        Assertions.assertEquals(actualRole2, expectedRoleDto2);
    }

    @Test
    public void shouldSuccessfullyUpdateAddonsWhenDeleteRole() {
        // create role1: limits=[addon1:day=10, addon2:day=10]; role2: limits=[addon1:day=10, addon3:day=10]
        LimitDto dayLimit = new LimitDto();
        dayLimit.setDay(10L);
        RoleDto roleDto = createDtoWithLimits("1", Map.of("addon1", dayLimit, "addon2", dayLimit));
        roleFacade.createRole(roleDto);
        RoleDto roleDto2 = createDtoWithLimits("2", Map.of("addon1", dayLimit, "addon3", dayLimit));
        roleFacade.createRole(roleDto2);

        // check addon1: limits=[role1:day=10, role2:day=10]; addon2: limits=[role1:day=10]; addon3: limits=[role2:day=10]
        AddonDto addon1 = addonFacade.getAddon("addon1");
        Assertions.assertEquals(Map.of("role1", dayLimit, "role2", dayLimit), addon1.getRoleLimits());
        AddonDto addon2 = addonFacade.getAddon("addon2");
        Assertions.assertEquals(Map.of("role1", dayLimit), addon2.getRoleLimits());
        AddonDto addon3 = addonFacade.getAddon("addon3");
        Assertions.assertEquals(Map.of("role2", dayLimit), addon3.getRoleLimits());

        // delete role1
        roleFacade.deleteRole("role1");

        // check addon1: limits=[role2:day=10]; addon2: limits=[]; addon3: limits=[role2:day=10]
        addon1 = addonFacade.getAddon("addon1");
        Assertions.assertEquals(Map.of("role2", dayLimit), addon1.getRoleLimits());
        addon2 = addonFacade.getAddon("addon2");
        Assertions.assertEquals(Map.of(), addon2.getRoleLimits());
        addon3 = addonFacade.getAddon("addon3");
        Assertions.assertEquals(Map.of("role2", dayLimit), addon3.getRoleLimits());
    }

    @Test
    public void shouldSuccessfullyCreateWithKeysAndLimits() {
        LimitDto dayLimit = new LimitDto();
        dayLimit.setDay(10L);

        ShareResourceLimitDto shareResourceLimit = new ShareResourceLimitDto();
        shareResourceLimit.setMaxAcceptedUsers(20);

        RoleDto roleDto = createDtoWithKeysAndLimits(
                "1",
                List.of("key1", "key2"),
                Map.of("addon1", dayLimit, "addon2", dayLimit),
                Map.of(ResourceTypeDto.APPLICATION, shareResourceLimit, ResourceTypeDto.FILE, shareResourceLimit)
        );
        roleFacade.createRole(roleDto);

        RoleDto actual = roleFacade.getRole(roleDto.getName());
        Assertions.assertEquals(roleDto, actual);
    }

    @Test
    public void shouldThrowExceptionWhenCreateRoleWithExistingName() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> roleFacade.createRole(createDto("1"))
        );

        Assertions.assertEquals("Role with name role1 already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateRoleWithExistingName() {
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        RoleDto roleDto2 = createDto("2");
        roleFacade.createRole(roleDto2);

        roleDto.setName("role2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> roleFacade.updateRole("role1", roleDto, "*")
        );

        Assertions.assertEquals("Role with name: 'role1' can not be renamed. New role name: 'role2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyGetCoreRole() {
        LimitDto dayLimit = new LimitDto();
        dayLimit.setDay(10L);

        ShareResourceLimitDto shareResourceLimit = new ShareResourceLimitDto();
        shareResourceLimit.setMaxAcceptedUsers(30);
        shareResourceLimit.setInvitationTtl(72000000L);

        RoleDto roleDto = createDtoWithKeysAndLimits(
                "1",
                List.of("key1", "key2"),
                Map.of("addon1", dayLimit, "addon2", dayLimit),
                Map.of(ResourceTypeDto.APPLICATION, shareResourceLimit, ResourceTypeDto.FILE, shareResourceLimit)
        );
        roleFacade.createRole(roleDto);

        CoreLimit coreLimit1 = CoreLimit.empty();
        coreLimit1.setDay(10L);
        CoreLimit coreLimit2 = CoreLimit.empty();
        coreLimit2.setDay(10L);

        Map<String, CoreLimit> expectedLimits = Map.of("addon1", coreLimit1, "addon2", coreLimit2);

        CoreShareResourceLimit coreShareResourceLimit1 = new CoreShareResourceLimit();
        coreShareResourceLimit1.setMaxAcceptedUsers(30);
        coreShareResourceLimit1.setInvitationTtl(20L);
        CoreShareResourceLimit coreShareResourceLimit2 = new CoreShareResourceLimit();
        coreShareResourceLimit2.setMaxAcceptedUsers(30);
        coreShareResourceLimit2.setInvitationTtl(20L);

        Map<String, CoreShareResourceLimit> expectedShare = Map.of(
                "APPLICATION", coreShareResourceLimit1,
                "FILE", coreShareResourceLimit2
        );

        CoreRole expected = new CoreRole();
        expected.setName(roleDto.getName());
        expected.setLimits(expectedLimits);
        expected.setShare(expectedShare);

        CoreRole actual = roleFacade.getCoreRoleWithHash(roleDto.getName()).core();

        Assertions.assertEquals(expected, actual);
    }

    private void assertRoles(Collection<RoleDto> actual, Collection<RoleDto> expected) {
        Map<String, RoleDto> actualMap = toMap(actual);
        Map<String, RoleDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertRole(actualMap.get(name), expectedMap.get(name));
        }
    }

    private Map<String, RoleDto> toMap(Collection<RoleDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(RoleDto::getName, Function.identity()));
    }

    private void assertRole(RoleDto actual, RoleDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
    }

    private RoleDto expectedDto1() {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("role1");
        roleDto.setDescription("role1");
        return roleDto;
    }

    private Collection<RoleDto> expectedDtos() {
        RoleDto roleDto1 = new RoleDto();
        roleDto1.setName("role1");
        roleDto1.setDescription("role1");
        RoleDto roleDto2 = new RoleDto();
        roleDto2.setName("role2");
        roleDto2.setDescription("description2");
        roleDto2.setGrantedKeys(List.of("key1", "key2"));
        RoleDto defaultRole = new RoleDto();
        defaultRole.setName("default");
        return List.of(roleDto1, roleDto2, defaultRole);
    }

    private RoleDto createDto(String suffix) {
        return createDtoWithKeysAndLimits(suffix, null, null);
    }

    private RoleDto createDtoWithKeys(String suffix, List<String> keys) {
        return createDtoWithKeysAndLimits(suffix, keys, null);
    }

    private RoleDto createDtoWithLimits(String suffix, Map<String, LimitDto> limits) {
        return createDtoWithKeysAndLimits(suffix, null, limits);
    }

    private RoleDto createDtoWithKeysAndLimits(String suffix, List<String> keys, Map<String, LimitDto> limits) {
        return createDtoWithKeysAndLimits(suffix, keys, limits, Map.of());
    }

    private RoleDto createDtoWithKeysAndLimits(String suffix,
                                               List<String> keys,
                                               Map<String, LimitDto> limits,
                                               Map<ResourceTypeDto, ShareResourceLimitDto> shareResourceLimits) {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("role" + suffix);
        roleDto.setDisplayName("displayName" + suffix);
        roleDto.setDescription("description" + suffix);
        roleDto.setGrantedKeys(keys);
        roleDto.setLimits(limits);
        roleDto.setShare(shareResourceLimits);
        return roleDto;
    }

    private ValidityStateDto keyValidState() {
        ValidityStateDto validityStateDto = new ValidityStateDto();
        validityStateDto.setValid(true);
        return validityStateDto;
    }

    private ValidityStateDto keyInvalidState() {
        ValidityStateDto validityStateDto = new ValidityStateDto();
        validityStateDto.setMessage("No roles assigned");
        validityStateDto.setValid(false);
        return validityStateDto;
    }
}

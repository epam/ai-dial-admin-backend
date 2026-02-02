package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.core.config.CoreKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDtoWithRole;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.invalidState;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.validState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

public abstract class KeyFunctionalTest {

    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;

    @BeforeEach
    public void beforeEach() {
        initRoles();
    }

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
        roleFacade.createRole(createRoleDto("3"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetKeys() {
        doReturn(1L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        KeyDto actual = keyFacade.getKey(keyDto.getName());

        keyDto.setKeyGeneratedAt(Instant.ofEpochMilli(1L));
        keyDto.setValidityState(validState());
        assertKey(actual, keyDto);
        RoleDto role1 = roleFacade.getRole("role1");
        assertEquals(role1.getUpdatedAt(), Instant.ofEpochMilli(1L));

        doReturn(2L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createKeyDtoWithRole("2");
        keyFacade.createKey(keyDto2);

        RoleDto role2 = roleFacade.getRole("role2");
        assertEquals(role2.getUpdatedAt(), Instant.ofEpochMilli(2L));

        Collection<KeyDto> actualKeys = keyFacade.getAllKeys();

        keyDto2.setKeyGeneratedAt(Instant.ofEpochMilli(2L));
        keyDto2.setValidityState(validState());
        assertKeys(actualKeys, List.of(keyDto, keyDto2), false);

        List<KeyDto> actualKeysList = actualKeys.stream().toList();
        KeyDto actualKeyDto1 = actualKeysList.get(0);
        KeyDto actualKeyDto2 = actualKeysList.get(1);

        assertEquals(actualKeyDto1.getCreatedAt(), Instant.ofEpochMilli(1L));
        assertEquals(actualKeyDto1.getUpdatedAt(), Instant.ofEpochMilli(1L));
        assertEquals(actualKeyDto2.getCreatedAt(), Instant.ofEpochMilli(2L));
        assertEquals(actualKeyDto2.getUpdatedAt(), Instant.ofEpochMilli(2L));
    }

    @Test
    public void shouldSuccessfullyCreateWithEmptyRolesAndGetKeys() {
        KeyDto keyDto = createKeyDto("1");
        keyDto.setAllowedIpAddressRanges(null);
        keyFacade.createKey(keyDto);

        KeyDto actual = keyFacade.getKey(keyDto.getName());
        keyDto.setRoles(List.of());
        keyDto.setValidityState(invalidState("No roles assigned"));
        assertKeyExcludingGeneratedFields(actual, keyDto);

        KeyDto keyDto2 = createKeyDto("2");
        keyFacade.createKey(keyDto2);

        Collection<KeyDto> actualKeys = keyFacade.getAllKeys();
        keyDto2.setValidityState(invalidState("No roles assigned"));
        Collection<KeyDto> expectedDtos = List.of(keyDto, keyDto2);
        expectedDtos.forEach(dto -> dto.setRoles(List.of()));
        assertKeys(actualKeys, expectedDtos, true);
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteKey() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        keyFacade.deleteKey(keyDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> keyFacade.getKey(keyDto.getName()));
        Assertions.assertTrue(keyFacade.getAllKeys().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateKey() {
        doReturn(1L).when(transactionTimestampContext).getTimestamp();

        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        KeyDto createdKey = keyFacade.getKey(keyDto.getName());
        assertEquals(createdKey.getCreatedAt(), Instant.ofEpochMilli(1L));
        assertEquals(createdKey.getUpdatedAt(), Instant.ofEpochMilli(1L));

        doReturn(2L).when(transactionTimestampContext).getTimestamp();

        KeyDto updatedKey = createKeyDtoWithRole("1");
        updatedKey.setDescription("new key description");
        keyFacade.updateKey(keyDto.getName(), updatedKey, "*");

        KeyDto actual = keyFacade.getKey(keyDto.getName());
        var expected = createKeyDtoWithRole("1");
        expected.setDescription("new key description");
        expected.setValidityState(validState());
        assertKeyExcludingGeneratedFields(actual, expected);

        assertEquals(actual.getCreatedAt(), Instant.ofEpochMilli(1L));
        assertEquals(actual.getUpdatedAt(), Instant.ofEpochMilli(2L));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetKeyWithExpiresAt() {
        doReturn(1L).when(transactionTimestampContext).getTimestamp();

        KeyDto keyDto = createKeyDto("1");
        keyDto.setExpiresAt(Instant.ofEpochMilli(3));
        keyFacade.createKey(keyDto);

        doReturn(5L).when(transactionTimestampContext).getTimestamp();

        KeyDto actual = keyFacade.getKey(keyDto.getName());

        KeyDto expected = createKeyDto("1");
        expected.setRoles(List.of());
        expected.setExpiresAt(Instant.ofEpochMilli(3));
        expected.setValidityState(invalidState("No roles assigned, Key is expired"));

        assertKeyExcludingGeneratedFields(actual, expected);
    }

    @Test
    public void shouldSuccessfullyCreateTwoKeysWithNullKeyValue() {
        KeyDto keyDto = createKeyDto("1");
        keyDto.setKey(null);
        keyFacade.createKey(keyDto);

        KeyDto keyDto2 = createKeyDto("2");
        keyDto2.setKey(null);
        keyFacade.createKey(keyDto2);

        Collection<KeyDto> actualKeys = keyFacade.getAllKeys();
        Collection<KeyDto> expectedDtos = List.of(keyDto, keyDto2);
        expectedDtos.forEach(dto -> {
            dto.setRoles(List.of());
            dto.setValidityState(invalidState("No roles assigned, Key value is missing"));
        });
        assertKeys(actualKeys, expectedDtos, true);
    }

    @Test
    public void shouldThrowExceptionWhenRenameKey() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);
        KeyDto updatedKey = createKeyDtoWithRole("2");
        updatedKey.setDescription("new key description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> keyFacade.updateKey(keyDto.getName(), updatedKey, "*")
        );
        assertEquals("Key with name: 'key1' can not be renamed. New key name: 'key2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenCreateKeyWithExistingValue() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);
        KeyDto keyDto2 = createKeyDtoWithRole("2");
        keyDto2.setKey("keyValue1");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> keyFacade.createKey(keyDto2)
        );
        assertEquals("Key with value keyValue1 already exists", exception.getMessage());
    }

    @Test
    public void shouldUpdateKeyGeneratedAtFieldWhenUpdateKeyValue() {
        doReturn(1L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        doReturn(2L).when(transactionTimestampContext).getTimestamp();
        keyDto.setKey("new keyValue");
        keyFacade.updateKey(keyDto.getName(), keyDto, "*");

        KeyDto actual = keyFacade.getKey(keyDto.getName());

        var expected = createKeyDtoWithRole("1");
        expected.setKey("new keyValue");
        expected.setCreatedAt(Instant.ofEpochMilli(1L));
        expected.setKeyGeneratedAt(Instant.ofEpochMilli(2L));
        expected.setValidityState(validState());
        expected.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));

        assertKey(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateRoles() {
        // create key1: roles=[role1, role2]; key2: roles=[role1, role3]
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createDto("2", List.of("role1", "role3"));
        keyFacade.createKey(keyDto2);

        // check role1: keys=[key1, key2]; role2: keys=[key1]; role3: keys=[key2]
        RoleDto role1 = roleFacade.getRole("role1");
        assertEquals(List.of("key1", "key2"), role1.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role1.getUpdatedAt());
        RoleDto role2 = roleFacade.getRole("role2");
        assertEquals(List.of("key1"), role2.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(120), role2.getUpdatedAt());
        RoleDto role3 = roleFacade.getRole("role3");
        assertEquals(List.of("key2"), role3.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role3.getUpdatedAt());

        // update key1: roles=[role2, role3]
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        KeyDto updatedKeyDto = createDto("1", List.of("role2", "role3"));
        keyFacade.updateKey(updatedKeyDto.getName(), updatedKeyDto, "*");

        // check key1: roles=[role2, role3]
        KeyDto actual = keyFacade.getKey(updatedKeyDto.getName());
        updatedKeyDto.setValidityState(validState());
        assertKeyExcludingGeneratedFields(actual, updatedKeyDto);

        // check role1: keys=[key2]; role2: keys=[key1]; role3: keys=[key2, key1]
        role1 = roleFacade.getRole("role1");
        assertEquals(List.of("key2"), role1.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(320), role1.getUpdatedAt());
        role2 = roleFacade.getRole("role2");
        assertEquals(List.of("key1"), role2.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(120), role2.getUpdatedAt());
        role3 = roleFacade.getRole("role3");
        org.assertj.core.api.Assertions.assertThat(role3.getGrantedKeys())
                .containsExactlyInAnyOrderElementsOf(List.of("key1", "key2"));
        Assertions.assertEquals(Instant.ofEpochMilli(320), role3.getUpdatedAt());
    }

    @Test
    public void shouldThrowExceptionWhenCreatingWithNonExistentRoles() {
        KeyDto keyDto = createKeyDto("1");
        keyDto.setRoles(List.of("role1", "role4", "role5"));
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> keyFacade.createKey(keyDto)
        );

        assertEquals("unable to find roles: [role4, role5]", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingWithNonExistentRoles() {
        KeyDto keyDto = createKeyDto("1");
        keyDto.setRoles(List.of("role1", "role2"));
        keyFacade.createKey(keyDto);

        KeyDto updatedKeyDto = createKeyDto("1");
        updatedKeyDto.setRoles(List.of("role1", "role4", "role5"));
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> keyFacade.updateKey(updatedKeyDto.getName(), updatedKeyDto, "*")
        );

        assertEquals("unable to find roles: [role4, role5]", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateKeysWhenDeleteRole() {
        // create key1: roles=[role1, role2]; key2: roles=[role1, role3]
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createDto("2", List.of("role1", "role3"));
        keyFacade.createKey(keyDto2);

        // check role1: keys=[key1, key2]; role2: keys=[key1]; role3: keys=[key2]
        RoleDto role1 = roleFacade.getRole("role1");
        assertEquals(List.of("key1", "key2"), role1.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role1.getUpdatedAt());
        RoleDto role2 = roleFacade.getRole("role2");
        assertEquals(List.of("key1"), role2.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(120), role2.getUpdatedAt());
        RoleDto role3 = roleFacade.getRole("role3");
        assertEquals(List.of("key2"), role3.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role3.getUpdatedAt());

        // delete role1
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        roleFacade.deleteRole("role1");

        // check key1: roles=[role2]; key2: roles=[role3]
        KeyDto expectedKeyDto1 = createDto("1", List.of("role2"));
        KeyDto expectedKeyDto2 = createDto("2", List.of("role3"));
        expectedKeyDto1.setValidityState(validState());
        expectedKeyDto2.setValidityState(validState());
        KeyDto actualKey1 = keyFacade.getKey("key1");
        KeyDto actualKey2 = keyFacade.getKey("key2");
        assertKeyExcludingGeneratedFields(actualKey1, expectedKeyDto1);
        Assertions.assertEquals(Instant.ofEpochMilli(320), actualKey1.getUpdatedAt());
        assertKeyExcludingGeneratedFields(actualKey2, expectedKeyDto2);
        Assertions.assertEquals(Instant.ofEpochMilli(320), actualKey2.getUpdatedAt());
    }

    @Test
    public void shouldSuccessfullyUpdateRolesWhenDeleteKey() {
        // create key1: roles=[role1, role2]; key2: roles=[role1, role3]
        doReturn(120L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);
        doReturn(220L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createDto("2", List.of("role1", "role3"));
        keyFacade.createKey(keyDto2);

        // check role1: keys=[key1, key2]; role2: keys=[key1]; role3: keys=[key2]
        RoleDto role1 = roleFacade.getRole("role1");
        assertEquals(List.of("key1", "key2"), role1.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role1.getUpdatedAt());
        RoleDto role2 = roleFacade.getRole("role2");
        assertEquals(List.of("key1"), role2.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(120), role2.getUpdatedAt());
        RoleDto role3 = roleFacade.getRole("role3");
        assertEquals(List.of("key2"), role3.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role3.getUpdatedAt());

        // delete key1
        doReturn(320L).when(transactionTimestampContext).getTimestamp();
        keyFacade.deleteKey("key1");

        // check role1: keys=[key1]; role2: keys=[]; role3: keys=[key2]
        role1 = roleFacade.getRole("role1");
        assertEquals(List.of("key2"), role1.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(320), role1.getUpdatedAt());
        role2 = roleFacade.getRole("role2");
        assertEquals(List.of(), role2.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(320), role2.getUpdatedAt());
        role3 = roleFacade.getRole("role3");
        assertEquals(List.of("key2"), role3.getGrantedKeys());
        Assertions.assertEquals(Instant.ofEpochMilli(220), role3.getUpdatedAt());
    }

    @Test
    public void shouldThrowExceptionWhenCreateKeyWithExistingKeyName() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> keyFacade.createKey(createKeyDtoWithRole("1"))
        );

        assertEquals("Key with name key1 already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateKeyWithExistingKeyName() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        KeyDto keyDto2 = createKeyDtoWithRole("2");
        keyFacade.createKey(keyDto2);

        keyDto.setName("key2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> keyFacade.updateKey("key1", keyDto, "*")
        );

        assertEquals("Key with name: 'key1' can not be renamed. New key name: 'key2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateKeyWithCorrectHash() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        KeyDto updatedKeyDto = createKeyDtoWithRole("1");
        updatedKeyDto.setDisplayName("updated DisplayName");

        var hash = keyFacade.getKeyWithHash(keyDto.getName()).hash();

        keyFacade.updateKey(keyDto.getName(), updatedKeyDto, hash);

        KeyDto actual = keyFacade.getKey(keyDto.getName());
        var expected = createKeyDtoWithRole("1");
        expected.setDisplayName("updated DisplayName");
        expected.setValidityState(validState());
        assertKeyExcludingGeneratedFields(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateKeyWithIncorrectHash() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        KeyDto updatedKeyDto = createKeyDtoWithRole("1");
        updatedKeyDto.setDisplayName("updatedDisplayName");

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> keyFacade.updateKey(keyDto.getName(), updatedKeyDto, "test")
        );
        Assertions.assertEquals("Unable to update Key 'key1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyGetCoreKey() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        CoreKey expected = new CoreKey();
        expected.setProject(keyDto.getProject());
        expected.setSecured(keyDto.isSecured());
        expected.setRoles(keyDto.getRoles());
        expected.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));

        CoreKey actual = keyFacade.getCoreKeyWithHash(keyDto.getName()).core();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSuccessfullyUpdateKeyByCoreKey() {
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        CoreKey coreKey = new CoreKey();
        coreKey.setProject("newKeyProject");
        coreKey.setSecured(true);
        coreKey.setRoles(List.of("role2", "role3"));
        coreKey.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));

        KeyDto expected = new KeyDto();
        expected.setName(keyDto.getName());
        expected.setKey(keyDto.getKey());
        expected.setDisplayName(keyDto.getDisplayName());
        expected.setProjectContactPoint(keyDto.getProjectContactPoint());
        expected.setDescription(keyDto.getDescription());
        expected.setExpiresAt(keyDto.getExpiresAt());
        expected.setProject("newKeyProject");
        expected.setSecured(true);
        expected.setRoles(List.of("role2", "role3"));
        expected.setTopics(new TreeSet<>(Set.of("topic1")));
        expected.setValidityState(validState());
        expected.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));

        keyFacade.updateKey(keyDto.getName(), coreKey, "*");

        KeyDto actual = keyFacade.getKey(keyDto.getName());

        assertKeyExcludingGeneratedFields(actual, expected);
    }

    private void assertKeys(Collection<KeyDto> actual, Collection<KeyDto> expected, boolean excludeGeneratedFields) {
        expected.forEach(keyDto -> keyDto.setKey(null));
        Map<String, KeyDto> actualMap = toMap(actual);
        Map<String, KeyDto> expectedMap = toMap(expected);
        assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            if (excludeGeneratedFields) {
                assertKeyExcludingGeneratedFields(actualMap.get(name), expectedMap.get(name));
            } else {
                assertKey(actualMap.get(name), expectedMap.get(name));
            }
        }
    }

    private Map<String, KeyDto> toMap(Collection<KeyDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(KeyDto::getName, Function.identity()));
    }

    private void assertKeyExcludingGeneratedFields(KeyDto actual, KeyDto expected) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt", "keyGeneratedAt")
                .isEqualTo(expected);
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getKeyGeneratedAt()).isNotNull();
    }

    private void assertKey(KeyDto actual, KeyDto expected) {
        assertThat(actual).isEqualTo(expected);
    }
}